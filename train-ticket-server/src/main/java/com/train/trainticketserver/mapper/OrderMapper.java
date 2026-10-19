package com.train.trainticketserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.train.trainticketserver.entity.TicketOrder;
import com.train.trainticketserver.vo.OrderListVO;
import com.train.trainticketserver.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<TicketOrder> {
    @Select("""
    SELECT
        o.order_id,
        t.train_no,
        sch.run_date AS travel_date,
        s1.station_name AS start_station,
        s2.station_name AS end_station,
        ts1.departure_time,
        ts2.arrival_time,
        st.type_name AS seat_type,
        c.coach_no,
        se.seat_no,
        ti.price,
        o.status,
        '成人票' AS ticket_type,
        p.pay_method
    FROM ticket_order o
    JOIN ticket ti
        ON o.order_id = ti.order_id
    JOIN train_schedule sch
        ON ti.schedule_id = sch.schedule_id
    JOIN train t
        ON sch.train_id = t.train_id
    JOIN station s1
        ON ti.start_station_id = s1.station_id
    JOIN station s2
        ON ti.end_station_id = s2.station_id
    JOIN train_station ts1
        ON t.train_id = ts1.train_id
        AND ts1.station_id = ti.start_station_id
    JOIN train_station ts2
        ON t.train_id = ts2.train_id
        AND ts2.station_id = ti.end_station_id
    JOIN seat se
        ON ti.seat_id = se.seat_id
    JOIN coach c
        ON se.coach_id = c.coach_id
    JOIN seat_type st
        ON se.seat_type_id = st.seat_type_id
    LEFT JOIN payment p
        ON o.order_id = p.order_id
    WHERE o.user_id = #{userId}
        AND o.status IN ('待支付','已支付','改签票','已退票')
    ORDER BY o.create_time DESC
    """)
    List<OrderListVO> findOrderList(Long userId);

    @Select("""
        SELECT
            o.order_id,
            u.username,
            u.name,
            u.phone,
            u.idnum,
            tr.train_no,
            ts.run_date,
            s1.station_name AS start_station_name,
            s2.station_name AS end_station_name,
            CONCAT(st.type_name,' ',LPAD(c.coach_no,2,'0'),'车',seat.seat_no) AS seat_info,
            t.price,
            o.status,
            o.create_time
        FROM ticket_order o
        LEFT JOIN ticket t
            ON o.order_id=t.order_id
        LEFT JOIN sys_user u
            ON o.user_id=u.user_id
        LEFT JOIN train_schedule ts
            ON t.schedule_id=ts.schedule_id
        LEFT JOIN train tr
            ON ts.train_id=tr.train_id
        LEFT JOIN station s1
            ON t.start_station_id=s1.station_id
        LEFT JOIN station s2
            ON t.end_station_id=s2.station_id
        LEFT JOIN seat seat
            ON t.seat_id=seat.seat_id
        LEFT JOIN coach c
            ON seat.coach_id=c.coach_id
        LEFT JOIN seat_type st
            ON seat.seat_type_id=st.seat_type_id
        ORDER BY o.create_time DESC
        """)
    List<OrderVO> list();



}
