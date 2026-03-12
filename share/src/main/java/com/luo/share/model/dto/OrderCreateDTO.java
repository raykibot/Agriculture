package com.luo.share.model.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class OrderCreateDTO {
    private Long buyerId;
    private Long machineryId;
    private Long ownerId;       // 机器所属人ID
    private Long addressId;     // 收货地址ID
    private Integer rentDays;
    private Date leaseStartTime;
    private Date leaseEndTime;
    private BigDecimal totalAmount;
    private BigDecimal depositAmount;
    private String remark;      // 新增的留言字段
}