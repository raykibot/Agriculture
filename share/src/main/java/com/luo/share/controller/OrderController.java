package com.luo.share.controller;


import com.alibaba.fastjson2.JSONObject;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.luo.share.common.api.Result;
import com.luo.share.config.AlipayConfig;
import com.luo.share.mapper.OrderLogisticsMapper;
import com.luo.share.mapper.OrderMapper;
import com.luo.share.model.dto.OrderCreateDTO;
import com.luo.share.model.entity.Order;
import com.luo.share.model.entity.OrderLogistics;
import com.luo.share.model.vo.OrderVO;
import com.luo.share.model.vo.UserProfileVO;
import com.luo.share.service.IOrderService;
import com.luo.share.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/order")
@Slf4j
public class OrderController {

    @Autowired
    private IOrderService orderService;

    @Autowired
    private AlipayClient alipayClient;

    @Autowired
    private AlipayConfig alipayConfig;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderLogisticsMapper orderLogisticsMapper;

    @Autowired
    private IUserService userService;

    // 在 OrderController.java 中
    @PostMapping("/create")
    public Result<String> createOrder(@RequestBody OrderCreateDTO dto) {
        log.info("创建订单");
        log.info("传入订单信息：{}",dto);
        try {

            UserProfileVO userProfile = userService.getUserProfile(dto.getBuyerId());

            // 如果你暂时不想改上面的逻辑，至少要做个判空防呆：
            if (dto.getOwnerId() == null) {
                return Result.failed("未能获取到机主信息，请返回重试");
            }
            if (Objects.equals(userProfile.getId(), dto.getOwnerId())){
                return Result.failed("不能租赁自己的设备");
            }

            // 调用业务逻辑，获取订单流水号
            String trackingNo = orderService.createOrder(dto);
            // 使用你封装的 success 方法
            return Result.success(trackingNo, "订单创建成功，请前往支付");

        } catch (IllegalStateException e) {
            // 捕获业务逻辑异常（如机器被抢租），使用你封装的 failed 方法优雅返回
            return Result.failed(e.getMessage());
        } catch (Exception e) {
            // 捕获不可预知的系统异常
            e.printStackTrace();
            return Result.failed("系统繁忙，创建订单失败，请稍后再试");
        }
    }


    /**
     * 发起支付宝支付请求
     * @param orderNo 刚才生成的订单号
     * @return 返回一段由支付宝生成的 HTML 页面代码
     */
    @GetMapping("/pay")
    public Result<String> pay(@RequestParam("orderNo") String orderNo) {
        try {
            // 1. 从数据库查询该订单的真实金额（防篡改）
            Order order = orderMapper.selectByTrackingNo(orderNo);
            if (order == null || order.getStatus() != 0) {
                return Result.failed("订单不存在或已失效");
            }

            // 2. 实例化具体API对应的request类 (电脑网页支付)
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setReturnUrl(alipayConfig.getReturnUrl());
            request.setNotifyUrl(alipayConfig.getNotifyUrl()); // 暂未生效，后续讲

            // 3. 组装业务参数
            JSONObject bizContent = new JSONObject();
            bizContent.put("out_trade_no", orderNo); // 我们的订单号
            bizContent.put("total_amount", order.getTotalAmount().toString()); // 实付总额
            bizContent.put("subject", "农机租赁订单-" + orderNo); // 订单标题
            bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY"); // 网页支付的固定产品码

            request.setBizContent(bizContent.toString());

            // 4. 调用支付宝SDK执行请求，拿到 HTML 表单
            String formHtml = alipayClient.pageExecute(request).getBody();

            // 5. 直接把这串 HTML 代码返回给前端
            return Result.success(formHtml);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.failed("拉起支付失败：" + e.getMessage());
        }
    }

    @PostMapping("/notify")
    public String payNotify(HttpServletRequest request) {
        log.info("收到支付宝异步回调通知！");

        // 1. 获取支付宝 POST 过来反馈信息，转换成 Map
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }

        try {
            // 2. 【核心安全机制】调用支付宝 SDK 进行验签，防止黑客伪造支付成功请求！
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayConfig.getAlipayPublicKey(),
                    alipayConfig.getCharset(),
                    alipayConfig.getSignType()
            );

            if (signVerified) {
                // 3. 验签通过，检查交易状态
                String tradeStatus = params.get("trade_status");
                String outTradeNo = params.get("out_trade_no"); // 当时传给支付宝的订单号
                String alipayTradeNo = params.get("trade_no"); // 支付宝内部的流水号

                if ("TRADE_SUCCESS".equals(tradeStatus)) {
                    log.info("验签成功！订单号: {} 支付成功，支付宝流水号: {}", outTradeNo, alipayTradeNo);

                    // 4. 去数据库把订单状态改为 1 (待发货)
                    orderMapper.updateOrderStatus(outTradeNo, 1);

                    OrderLogistics logistics = new OrderLogistics();
                    logistics.setOrderNo(outTradeNo);
                    logistics.setDescription("支付成功，平台已收到您的订单，正在为您联系机主交接设备。");
                    logistics.setCreateTime(new Date());
                    orderLogisticsMapper.insertLogistics(logistics);
                }

                // 5. 【关键】必须给支付宝返回 "success" 字符串，否则支付宝会认为你没收到，在 24 小时内不断重发通知！
                return "success";
            } else {
                log.error("支付宝回调验签失败！可能有黑客在伪造请求！");
                return "failure";
            }
        } catch (Exception e) {
            log.error("处理支付宝回调出现异常", e);
            return "failure";
        }
    }


    /**
     * 获取订单的物流调度轨迹
     */
    @GetMapping("/logistics")
    public Result<List<OrderLogistics>> getLogistics(@RequestParam("orderNo") String orderNo) {
        log.info("查询物流信息");
        List<OrderLogistics> list = orderLogisticsMapper.selectByOrderNo(orderNo);
        return Result.success(list);
    }


    /**
     * 获取用户订单列表 (支持按角色和状态组复合查询)
     */
    @GetMapping("/list")
    public Result<List<OrderVO>> getUserOrders(
            @RequestParam("userId") Long userId,
            @RequestParam("role") String role,
            @RequestParam(value = "statusGroup", required = false) String statusGroup) {

        List<OrderVO> list = orderService.getUserOrders(userId, role, statusGroup);
        return Result.success(list);
    }

    /**
     * 机主确认发货
     */
    @PostMapping("/ship")
    public Result<String> shipOrder(@RequestParam("orderNo") String orderNo) {
        try {
            orderService.shipOrder(orderNo);
            return Result.success("发货成功");
        } catch (Exception e) {
            return Result.failed("发货失败：" + e.getMessage());
        }
    }


}
