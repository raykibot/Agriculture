package com.luo.share.listener;

import com.luo.share.config.RabbitMQConfig;
import com.luo.share.mapper.MachineryMapper;
import com.luo.share.mapper.OrderMapper;
import com.luo.share.model.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class OrderTimeoutListener {


    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private MachineryMapper machineryMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // 必须和下单时的前缀保持绝对一致
    private static final String STOCK_CACHE_PREFIX = "machinery:stock:";

    @RabbitListener(queues = RabbitMQConfig.DEAD_QUEUE)
    @Transactional(rollbackFor = Exception.class)
    public void processTimeoutOrder(String orderNo) {
        log.info(">>> [RabbitMQ] 收到死信队列消息，检查订单超时状态：" + orderNo);

        // 1. 从数据库查出最新订单状态
        Order order = orderMapper.selectByTrackingNo(orderNo);
        if (order == null) {
            log.info("--- 订单不存在，直接忽略 ---");
            return;
        }

        // 2. 幂等性判断：如果状态不是 0 (待支付)，说明用户已付款，或被管理员手动取消了，直接放行
        if (order.getStatus() != 0) {
            log.info("--- 订单 [" + orderNo + "] 当前状态为: " + order.getStatus() + "，无需回滚 ---");
            return;
        }

        log.info("--- 订单 [" + orderNo + "] 超时未支付，开始执行数据回滚！ ---");

        // 3. 改状态：将订单状态修改为 4 (已取消)
        orderMapper.updateOrderStatus(orderNo, 4);

        // 4. DB 恢复：真实库存 stock + 1，锁定库存 locked_stock - 1
        machineryMapper.releaseMachineryStock(order.getMachineryId());

        // 5. Redis 恢复：把预扣减的库存加回去，让其他用户可以继续抢
        String redisKey = STOCK_CACHE_PREFIX + order.getMachineryId();
        stringRedisTemplate.opsForValue().increment(redisKey);

        log.info(">>> [RabbitMQ] 订单 [" + orderNo + "] 超时取消及库存回滚处理完成！");
    }
}