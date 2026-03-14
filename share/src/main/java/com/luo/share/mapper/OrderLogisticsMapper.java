package com.luo.share.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.luo.share.model.entity.OrderLogistics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface OrderLogisticsMapper{
    // 插入一条轨迹
    int insertLogistics(OrderLogistics logistics);
    // 查询某个订单的所有轨迹（按时间倒序排，最新的在最上面）
    List<OrderLogistics> selectByOrderNo(@Param("orderNo") String orderNo);
}