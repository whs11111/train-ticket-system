package com.train.trainticketserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.train.trainticketserver.entity.Payment;
import com.train.trainticketserver.entity.Seat;
import com.train.trainticketserver.entity.Train;
import org.apache.ibatis.annotations.*;

@Mapper
public interface SeatMapper extends BaseMapper<Seat> {

    @Select("""
        SELECT s.*
        FROM seat s
        JOIN coach c
        ON s.coach_id = c.coach_id
        WHERE c.train_id = (
            SELECT train_id
            FROM train_schedule
            WHERE schedule_id = #{scheduleId}
        )
        AND s.seat_type_id = #{seatTypeId}
        AND s.seat_no LIKE CONCAT('%', #{seatColumn})
        AND NOT EXISTS (
            SELECT 1
            FROM seat_occupancy so
            WHERE so.seat_id = s.seat_id
            AND so.schedule_id = #{scheduleId}
            AND so.start_order < #{endOrder}
            AND so.end_order > #{startOrder}
        )
        ORDER BY s.seat_id
        LIMIT 1
        """)
    Seat findSeatByColumn(
            @Param("scheduleId") Long scheduleId,
            @Param("seatTypeId") Long seatTypeId,
            @Param("seatColumn") String seatColumn,
            @Param("startOrder") Integer startOrder,
            @Param("endOrder") Integer endOrder
    );

    @Select("""
        SELECT s.*
        FROM seat s
        JOIN coach c
        ON s.coach_id = c.coach_id
        WHERE c.train_id = (
            SELECT train_id
            FROM train_schedule
            WHERE schedule_id = #{scheduleId}
        )
        AND s.seat_type_id = #{seatTypeId}
        AND NOT EXISTS (
            SELECT 1
            FROM seat_occupancy so
            WHERE so.seat_id = s.seat_id
            AND so.schedule_id = #{scheduleId}
            AND so.start_order < #{endOrder}
            AND so.end_order > #{startOrder}
        )
        ORDER BY s.seat_id
        LIMIT 1
        """)
    Seat findAutoSeat(
            @Param("scheduleId") Long scheduleId,
            @Param("seatTypeId") Long seatTypeId,
            @Param("startOrder") Integer startOrder,
            @Param("endOrder") Integer endOrder
    );

    @Select("""
        SELECT *
        FROM seat
        WHERE seat_id = #{seatId}
    """)
    Seat findById(
            @Param("seatId") Long seatId
    );

    @Insert("""
        INSERT INTO seat (
            coach_id,
            seat_no,
            seat_type_id
        )
        VALUES (
            #{coachId},
            #{seatNo},
            #{seatTypeId}
        )
        """)
    @Options(
            useGeneratedKeys = true,
            keyProperty = "seatId",
            keyColumn = "seat_id"
    )
    int insert(Seat seat);


}