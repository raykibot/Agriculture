package com.luo.share.mapper;

import com.luo.share.model.entity.Order;
import com.luo.share.model.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {

    int insertOrder(Order order);

    Order selectByTrackingNo(@Param("trackingNo") String trackingNo);

    int updateOrderStatus(@Param("trackingNo") String trackingNo, @Param("status") Integer status);

    List<OrderVO> selectUserOrders(@Param("userId") Long userId, @Param("role") String role,@Param("statusGroup") String statusGroup);
}
