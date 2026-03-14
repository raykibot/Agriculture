package com.luo.share.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luo.share.config.RabbitMQConfig;
import com.luo.share.mapper.MachineryMapper;
import com.luo.share.mapper.OrderLogisticsMapper;
import com.luo.share.mapper.OrderMapper;
import com.luo.share.model.dto.OrderCreateDTO;
import com.luo.share.model.entity.Machinery;
import com.luo.share.model.entity.Order;
import com.luo.share.model.entity.OrderLogistics;
import com.luo.share.model.vo.OrderVO;
import com.luo.share.service.IOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class OrderServiceImpl implements IOrderService {

    // SpringBoot 内置的字符串专用的 Redis 模板，完美支持 increment/decrement 纯数字操作
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private MachineryMapper machineryMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private OrderLogisticsMapper orderLogisticsMapper;

    // 定义 Redis 库存 Key 的前缀
    private static final String STOCK_CACHE_PREFIX = "machinery:stock:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(OrderCreateDTO dto) {
        String stockKey = STOCK_CACHE_PREFIX + dto.getMachineryId();

        // ================= 1. Redis 预扣减库存 (抗击高并发第一线) =================
        // 先检查 Redis 是否有库存缓存
        Boolean hasKey = stringRedisTemplate.hasKey(stockKey);
        log.info("redis库存key:{}",stockKey);
        if (Boolean.FALSE.equals(hasKey)) {
            // 【完全手写SQL调用】查询机器真实库存并预热到 Redis
            Machinery machinery = machineryMapper.selectMachineryById(dto.getMachineryId());
            if (machinery == null || machinery.getStock() == null || machinery.getStock() <= 0) {
                throw new IllegalStateException("该设备已下架或暂无可用库存");
            }
            stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(machinery.getStock()));
        }

        // Redis 单线程原子递减，绝对安全
        Long remainStock = stringRedisTemplate.opsForValue().decrement(stockKey);

        if (remainStock == null || remainStock < 0) {
            // 扣减后小于0，说明被秒杀了。为了保证数据一致性，把刚扣成负数的库存补回去 (+1)
            stringRedisTemplate.opsForValue().increment(stockKey);
            throw new IllegalStateException("手慢了，该设备刚刚被别人抢先租走啦！");
        }

        try {
            // ================= 2. 锁定数据库库存 (乐观锁防超卖) =================
            // 【完全手写SQL调用】执行带有 AND stock > 0 条件的 UPDATE 语句
            int updateCount = machineryMapper.lockMachineryStock(dto.getMachineryId());

            if (updateCount == 0) {
                // 如果影响行数为 0，说明数据库底层库存已经没了，触发异常让外层 catch 回滚 Redis
                throw new IllegalStateException("系统繁忙，库存锁定失败");
            }

            // ================= 3. 组装订单并插入数据库 =================
            Order order = new Order();
            BeanUtils.copyProperties(dto, order);

            // 组装业务规则字段
            String orderNo = "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
            order.setTrackingNo(orderNo);
            order.setStatus(0); // 0-待支付
            order.setCreateTime(new Date());

            // 【完全手写SQL调用】插入订单
            int insertCount = orderMapper.insertOrder(order);
            if (insertCount == 0) {
                throw new RuntimeException("订单创建失败，写入数据库异常");
            }

            // ================= 4. 发送延迟消息到 RabbitMQ =================
            // 将生成的订单号发送到"延迟交换机"，利用我们配置的死信机制，它会在队列里停留设定的时间（如15分钟，测试期为60秒）
            // 时间一到，它就会掉入死信队列，被 OrderTimeoutListener 抓取并执行取消和回滚操作。
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.DELAY_EXCHANGE,
                    RabbitMQConfig.DELAY_ROUTING_KEY,
                    orderNo
            );

            return orderNo;

        } catch (Exception e) {
            // 【异常处理与强一致性补偿】
            // 无论由于什么原因（如网络抖动、数据库宕机、乐观锁冲突），导致业务抛出异常，
            // 必须把 Redis 预先扣除的这 1 个库存加回来，并让 Spring 触发数据库回滚。
            stringRedisTemplate.opsForValue().increment(stockKey);

            // 再次抛出异常，让 Controller 去捕获
            if (e instanceof IllegalStateException) {
                throw e; // 抛出已知业务异常
            }
            throw new RuntimeException("订单创建失败：" + e.getMessage());
        }
    }

    @Override
    public List<OrderVO> getUserOrders(Long userId, String role,String statusGroup) {
        return orderMapper.selectUserOrders(userId, role,statusGroup);
    }

    @Override
    public void shipOrder(String orderNo) {
        // 1. 修改订单状态为 2 (租赁中/已发货)
        int updateCount = orderMapper.updateOrderStatus(orderNo, 2);
        if (updateCount == 0) {
            throw new RuntimeException("发货失败，订单可能不存在或状态已改变");
        }

        // 2. 自动生成发货的物流轨迹
        OrderLogistics logistics = new OrderLogistics();
        logistics.setOrderNo(orderNo);
        logistics.setDescription("机主已发货，设备正由【重型平板拖车】运往您的目的地。");
        logistics.setCreateTime(new Date());
        orderLogisticsMapper.insertLogistics(logistics);
    }
}