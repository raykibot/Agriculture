package com.luo.share.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class MachineryVO {

    private Long id;
    private String name;
    private String brand;
    private String model;
    private String categoryId;
    private BigDecimal price;
    private BigDecimal deposit;
    private String description;
    private Map<String, Object> parameters; // JSON 解析为 Map
    private String images;   // 字符串转为数组
    private List<String> tagList;  // 关联 tags 表查出的名称列表
    private Integer status;
    private Double avgRating;

}
