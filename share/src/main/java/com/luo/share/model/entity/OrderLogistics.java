package com.luo.share.model.entity;

import lombok.Data;
import java.util.Date;

@Data
public class OrderLogistics {
    private Long id;
    private String orderNo;
    private String description;
    private Date createTime;
}