package com.train.trainticketserver.mapper;

import com.train.trainticketserver.entity.TicketOrder;
import org.apache.ibatis.annotations.*;

@Mapper
public interface TicketOrderMapper {

    @Insert("""
        INSERT INTO ticket_order
        (
            user_id,
            total_price,
            status
        )
        VALUES
        (
            #{userId},
            #{totalPrice},
            #{status}
        )
        """)
    @Options(
            useGeneratedKeys = true,
            keyProperty = "orderId"
    )
    int insert(TicketOrder ticketOrder);

    @Select("""
        SELECT *
        FROM ticket_order
        WHERE order_id = #{orderId}
        """)
    TicketOrder findById(
            @Param("orderId") Long orderId
    );

    @Update("""
        UPDATE ticket_order
        SET status = '已取消'
        WHERE order_id = #{orderId}
       """)
    int cancelOrder(@Param("orderId") Long orderId);

    @Update("""
        UPDATE ticket_order
        SET status = '已改签'
        WHERE order_id = #{orderId}
       """)
    int changeOrder(@Param("orderId") Long orderId);

    @Update("""
        UPDATE ticket_order
        SET status = '已退票'
        WHERE order_id = #{orderId}
       """)
    int refundOrder(@Param("orderId") Long orderId);

    @Update("""
        UPDATE ticket_order
        SET status = '改签票'
        WHERE order_id = #{orderId}
       """)
    int payChangeOrder(@Param("orderId") Long orderId);

    @Update("""
        UPDATE ticket_order
        SET status = '已支付'
        WHERE order_id = #{orderId}
    """)
    void payOrder(@Param("orderId") Long orderId);

}