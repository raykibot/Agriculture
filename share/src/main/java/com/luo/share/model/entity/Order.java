package com.luo.share.model.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class Order {


    private Long id;
    private Long buyerId;
    private Long machineryId;
    BigDecimal totalAmount;
    private Integer status;
    private String trackingNo;
    private Long addressId;
    private Date payTime;
    private Date createTime;
    private Long ownerId;
    private Integer rentDays;
    private Date leaseStartTime;
    private Date leaseEndTime;
    private BigDecimal depositAmount;
    private String returnTrackingNo;
    private String remark;
}
