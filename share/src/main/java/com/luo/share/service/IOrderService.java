package com.luo.share.service;

import com.luo.share.model.dto.OrderCreateDTO;

public interface IOrderService {

    /**
     * 创建订单（Redis预扣减库存方案）
     * @return 返回生成的全局唯一订单号
     */
    String createOrder(OrderCreateDTO dto);


}
