package com.train.trainticketserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.train.trainticketserver.entity.TicketPrice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface TicketPriceMapper extends BaseMapper<TicketPrice> {

    @Select("""
        SELECT price
        FROM ticket_price
        WHERE train_id = #{trainId}
        AND start_station_id = #{startStationId}
        AND end_station_id = #{endStationId}
        AND seat_type_id = #{seatTypeId}
        """)
    BigDecimal findPrice(
            @Param("trainId") Long trainId,
            @Param("startStationId") Long startStationId,
            @Param("endStationId") Long endStationId,
            @Param("seatTypeId") Long seatTypeId
    );

    @Select("""
        select *
        from ticket_price
        where train_id=#{trainId}
        and start_station_id=#{startStationId}
        and end_station_id=#{endStationId}
        and seat_type_id=#{seatTypeId}
        """)
    TicketPrice findOne(Long trainId,Long startStationId,Long endStationId,Long seatTypeId);

}