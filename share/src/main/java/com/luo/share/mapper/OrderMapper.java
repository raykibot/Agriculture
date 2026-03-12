package com.luo.share.mapper;

import com.luo.share.model.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderMapper {

    int insertOrder(Order order);

    Order selectByTrackingNo(@Param("trackingNo") String trackingNo);

    int updateOrderStatus(@Param("trackingNo") String trackingNo, @Param("status") Integer status);

}
