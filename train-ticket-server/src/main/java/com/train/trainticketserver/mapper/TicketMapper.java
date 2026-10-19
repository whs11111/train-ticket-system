package com.train.trainticketserver.mapper;

import com.train.trainticketserver.entity.Ticket;
import com.train.trainticketserver.vo.PriceVO;
import com.train.trainticketserver.vo.RouteStationVO;
import com.train.trainticketserver.vo.TicketSearchResultVO;
import com.train.trainticketserver.vo.TicketSearchVO;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface TicketMapper {

    @Select("""
        SELECT
            sch.schedule_id,
            t.train_no,
            s1.station_name AS start_station,
            CASE
                WHEN ts1.stop_order = 1 THEN '始'
                ELSE '过'
            END AS start_type,
            s2.station_name AS end_station,
            CASE
                WHEN ts2.stop_order = (
                    SELECT MAX(stop_order)
                    FROM train_station
                    WHERE train_id = t.train_id)
                THEN '终'
                ELSE '过'
            END AS end_type,
            ts1.departure_time,
            ts2.arrival_time,
            st.seat_type_id,
            st.type_name AS seat_type,
            tp.price,
            COUNT(se.seat_id) - COUNT(so.id) AS remain
        FROM train t
        JOIN train_station ts1
            ON t.train_id = ts1.train_id
        JOIN station s1
            ON ts1.station_id = s1.station_id
        JOIN train_station ts2
            ON t.train_id = ts2.train_id
        JOIN station s2
            ON ts2.station_id = s2.station_id
        JOIN train_schedule sch
            ON t.train_id = sch.train_id
        JOIN ticket_price tp
            ON tp.train_id = t.train_id
            AND tp.start_station_id = ts1.station_id
            AND tp.end_station_id = ts2.station_id
        JOIN seat_type st
            ON tp.seat_type_id = st.seat_type_id
        JOIN coach c
            ON c.train_id = t.train_id
        JOIN seat se
            ON se.coach_id = c.coach_id
            AND se.seat_type_id = st.seat_type_id
        LEFT JOIN seat_occupancy so
            ON so.schedule_id = sch.schedule_id
            AND so.seat_id = se.seat_id
            AND so.start_order < ts2.stop_order
            AND so.end_order > ts1.stop_order
        WHERE (s1.city = #{startPlace} OR s1.station_name = #{startPlace})
            AND (s2.city = #{endPlace} OR s2.station_name = #{endPlace})
            AND sch.run_date = #{date}
            AND ts1.stop_order < ts2.stop_order
        GROUP BY
            sch.schedule_id,
            t.train_no,
            s1.station_name,
            ts1.stop_order,
            s2.station_name,
            ts2.stop_order,
            ts1.departure_time,
            ts2.arrival_time,
            st.seat_type_id,
            st.type_name,
            tp.price
        ORDER BY
            ts1.departure_time,
            st.seat_type_id;
        """)
    List<TicketSearchResultVO> search(
            @Param("startPlace") String startPlace,
            @Param("endPlace") String endPlace,
            @Param("date") LocalDate date
    );

    @Select("""
        SELECT
            sch.schedule_id,
            t.train_no,
            s1.station_name AS start_station,
            CASE
                WHEN ts1.stop_order = 1 THEN '始'
                ELSE '过'
            END AS start_type,
            s2.station_name AS end_station,
            CASE
                WHEN ts2.stop_order = (
                    SELECT MAX(stop_order)
                    FROM train_station
                    WHERE train_id = t.train_id
                )
                THEN '终'
                ELSE '过'
            END AS end_type,
            ts1.departure_time,
            ts2.arrival_time,
            st.seat_type_id,
            st.type_name AS seat_type,
            tp.price,
            COUNT(se.seat_id) - COUNT(so.id) AS remain
        FROM train t
        JOIN train_station ts1
            ON t.train_id = ts1.train_id
        JOIN station s1
            ON ts1.station_id = s1.station_id
        JOIN train_station ts2
            ON t.train_id = ts2.train_id
        JOIN station s2
            ON ts2.station_id = s2.station_id
        JOIN train_schedule sch
            ON t.train_id = sch.train_id
        JOIN ticket_price tp
            ON tp.train_id = t.train_id
            AND tp.start_station_id = ts1.station_id
            AND tp.end_station_id = ts2.station_id
        JOIN seat_type st
            ON tp.seat_type_id = st.seat_type_id
        JOIN coach c
            ON c.train_id = t.train_id
        JOIN seat se
            ON se.coach_id = c.coach_id
            AND se.seat_type_id = st.seat_type_id
        LEFT JOIN seat_occupancy so
            ON so.schedule_id = sch.schedule_id
            AND so.seat_id = se.seat_id
            AND so.start_order < ts2.stop_order
            AND so.end_order > ts1.stop_order
        WHERE
            s1.station_name = #{startStation}
            AND s2.station_name = #{endStation}
            AND sch.run_date = #{date}
            AND ts1.stop_order < ts2.stop_order
        GROUP BY
            sch.schedule_id,
            t.train_no,
            s1.station_name,
            ts1.stop_order,
            s2.station_name,
            ts2.stop_order,
            ts1.departure_time,
            ts2.arrival_time,
            st.seat_type_id,
            st.type_name,
            tp.price
        ORDER BY
            ts1.departure_time,
            st.seat_type_id
        """)
    List<TicketSearchResultVO> searchChange(
            @Param("startStation") String startStation,
            @Param("endStation") String endStation,
            @Param("date") LocalDate date
    );

    @Insert("""
        INSERT INTO ticket
        (
            order_id,
            schedule_id,
            seat_id,
            start_station_id,
            end_station_id,
            price,
            status
        )
        VALUES
           (#{orderId},
            #{scheduleId},
            #{seatId},
            #{startStationId},
            #{endStationId},
            #{price},
            #{status}
        )
        """)
    @Options(
            useGeneratedKeys = true,
            keyProperty = "ticketId"
    )
    int insert(Ticket ticket);

    @Select("""
        SELECT *
        FROM ticket
        WHERE order_id = #{orderId}
        """)
    Ticket findByOrderId(@Param("orderId") Long orderId);

    @Update("""
        UPDATE ticket
        SET status = '已取消'
        WHERE order_id = #{orderId}
    """)
    int cancelTicket(@Param("orderId") Long orderId);

    @Update("""
        UPDATE ticket
        SET status = '已支付'
        WHERE order_id = #{orderId}
    """)
    void payTicket(@Param("orderId") Long orderId);

    @Update("""
        UPDATE ticket
        SET status = '已改签'
        WHERE order_id = #{orderId}
    """)
    void changeTicket(@Param("orderId") Long orderId);

    @Update("""
        UPDATE ticket
        SET status = '已退票'
        WHERE order_id = #{orderId}
    """)
    void refundTicket(@Param("orderId") Long orderId);

    @Update("""
        UPDATE ticket
        SET status = '改签票'
        WHERE order_id = #{orderId}
    """)
    void payChangeTicket(@Param("orderId") Long orderId);

    @Select("""
    SELECT
        t1.schedule_id AS first_schedule_id,
        t2.schedule_id AS second_schedule_id,

        tr1.train_no AS first_train_no,
        tr2.train_no AS second_train_no,

        s_start.station_name AS start_station,
        s_transfer.station_name AS transfer_station,
        s_end.station_name AS end_station,

        ts_start.departure_time AS total_departure_time,
        ts_transfer1.arrival_time AS first_arrival_time,
        ts_transfer2.departure_time AS second_departure_time,
        ts_end.arrival_time AS total_arrival_time,

        ts_start.stop_order AS first_start_order,
        ts_transfer1.stop_order AS first_transfer_order,
        ts_transfer2.stop_order AS second_transfer_order,
        ts_end.stop_order AS second_end_order

    FROM train_schedule t1

    JOIN train_schedule t2
        ON t1.run_date = t2.run_date
        AND t1.schedule_id <> t2.schedule_id

    JOIN train tr1
        ON t1.train_id = tr1.train_id

    JOIN train tr2
        ON t2.train_id = tr2.train_id

    JOIN train_station ts_start
        ON tr1.train_id = ts_start.train_id

    JOIN station s_start
        ON ts_start.station_id = s_start.station_id
        AND (s_start.city = #{startPlace} OR s_start.station_name = #{startPlace})

    JOIN train_station ts_transfer1
        ON tr1.train_id = ts_transfer1.train_id

    JOIN station s_transfer
        ON ts_transfer1.station_id = s_transfer.station_id

    JOIN train_station ts_transfer2
        ON tr2.train_id = ts_transfer2.train_id
        AND ts_transfer2.station_id = ts_transfer1.station_id

    JOIN train_station ts_end
        ON tr2.train_id = ts_end.train_id

    JOIN station s_end
        ON ts_end.station_id = s_end.station_id
        AND (s_end.city = #{endPlace} OR s_end.station_name = #{endPlace})

    WHERE t1.run_date = #{date}

      AND ts_start.stop_order < ts_transfer1.stop_order

      AND ts_transfer2.stop_order < ts_end.stop_order

      AND TIMESTAMPDIFF(
          MINUTE,
          ts_transfer1.arrival_time,
          ts_transfer2.departure_time
      ) >= 20

    ORDER BY
        TIMESTAMPDIFF(
            MINUTE,
            ts_start.departure_time,
            ts_end.arrival_time
        ) ASC,

        ts_start.departure_time ASC

    LIMIT 20
    """)
    List<Map<String, Object>> searchTransfer(
            @Param("startPlace") String startPlace,
            @Param("endPlace") String endPlace,
            @Param("date") LocalDate date
    );

    @Select("""
    SELECT
        st.seat_type_id,
        st.type_name AS seat_type,
        tp.price,
    
        COUNT(se.seat_id) -
        COUNT(
            CASE
                WHEN so.id IS NOT NULL THEN 1
            END
        ) AS remain
    
    FROM train_schedule sch
    
    JOIN train_station ts1
        ON sch.train_id = ts1.train_id
        AND ts1.stop_order = #{startOrder}
    
    JOIN train_station ts2
        ON sch.train_id = ts2.train_id
        AND ts2.stop_order = #{endOrder}
    
    JOIN ticket_price tp
        ON tp.train_id = sch.train_id
        AND tp.start_station_id = ts1.station_id
        AND tp.end_station_id = ts2.station_id
    
    JOIN seat_type st
        ON tp.seat_type_id = st.seat_type_id
    
    JOIN coach c
        ON c.train_id = sch.train_id
    
    JOIN seat se
        ON se.coach_id = c.coach_id
        AND se.seat_type_id = st.seat_type_id
    
    LEFT JOIN seat_occupancy so
        ON so.schedule_id = sch.schedule_id
        AND so.seat_id = se.seat_id
        AND so.start_order < ts2.stop_order
        AND so.end_order > ts1.stop_order
    
    WHERE sch.schedule_id = #{scheduleId}
    
    GROUP BY
        st.seat_type_id,
        st.type_name,
        tp.price
    
    ORDER BY
        st.seat_type_id
    """)
    List<PriceVO> findTransferSeatPrices(
            @Param("scheduleId") Long scheduleId,
            @Param("startOrder") Integer startOrder,
            @Param("endOrder") Integer endOrder
    );

    @Select("""
    SELECT
        s.station_name AS stationName,
        ts.arrival_time AS arrivalTime,
        ts.departure_time AS departureTime,
        CASE
            WHEN ts.arrival_time IS NULL
              OR ts.departure_time IS NULL
            THEN NULL
            WHEN TIMESTAMPDIFF(MINUTE,ts.arrival_time,ts.departure_time) < 60
            THEN CONCAT(
                TIMESTAMPDIFF(MINUTE,ts.arrival_time,ts.departure_time),
                '分'
            )
            ELSE CONCAT(
                FLOOR(
                    TIMESTAMPDIFF(MINUTE,ts.arrival_time,ts.departure_time) / 60),
                '小时',
                MOD(
                    TIMESTAMPDIFF(MINUTE,ts.arrival_time,ts.departure_time),60),
                '分'
            )
        END AS stopDuration
    FROM train_schedule sch
    INNER JOIN train t
        ON sch.train_id = t.train_id
    INNER JOIN train_station ts
        ON t.train_id = ts.train_id
    INNER JOIN station s
        ON ts.station_id = s.station_id
    WHERE sch.schedule_id = #{scheduleId}
      AND sch.run_date = #{date}
    ORDER BY ts.stop_order
    """)
    List<RouteStationVO> getRoute(
            @Param("scheduleId") Long scheduleId,
            @Param("date") String date
    );
}
