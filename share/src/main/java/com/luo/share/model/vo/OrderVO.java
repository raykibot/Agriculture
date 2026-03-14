package com.luo.share.model.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class OrderVO {
    private String trackingNo;      // 订单流水号
    private Integer status;         // 订单状态
    private String machineName;     // 农机名称
    private String machineImage;    // 农机首图
    private Date leaseStartTime;    // 起租时间
    private Integer rentDays;       // 租期
    private BigDecimal totalAmount; // 订单总金额

    private String buyerName;       // 租户姓名
    private String ownerName;       // 机主姓名
}