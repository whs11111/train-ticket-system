package com.train.trainticketserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.train.trainticketserver.entity.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {
    @Select("""
        SELECT *
        FROM payment
        WHERE order_id = #{orderId}
    """)
    Payment findByOrderId(
            @Param("orderId") Long orderId
    );

}