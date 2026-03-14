package com.luo.share.model.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MachineryAddDTO {
    private String name;
    private String brand;
    private String model;
    private Long categoryId;
    private BigDecimal price;
    private BigDecimal deposit;
    private String description;
    private String parameters; // 前端传来的 JSON 字符串
    private String images;     // 前端传来的逗号分隔的图片URL
    private Long ownerId;      // 机主ID
}