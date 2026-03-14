package com.luo.share.model.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class Machinery {

    private Long id;
    private Long ownerId;
    private Long categoryId;
    private String name;
    private String brand;
    private String model;
    private BigDecimal price;
    private BigDecimal deposit;
    private String description;
    //private Map<String, Object> parameters; // JSON 解析为 Map
    private String parameters;
    private String images;   // 字符串转为数组
    private List<String> tagList;  // 关联 tags 表查出的名称列表
    private Integer status;
    private Double avgRating;
    private Integer stock;
    private Integer lockedStock;
    private Date createTime;



}
