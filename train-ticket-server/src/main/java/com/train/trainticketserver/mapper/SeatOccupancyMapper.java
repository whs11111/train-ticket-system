package com.train.trainticketserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.train.trainticketserver.entity.Coach;
import com.train.trainticketserver.entity.SeatOccupancy;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SeatOccupancyMapper extends BaseMapper<SeatOccupancy> {

    @Insert("""
        INSERT INTO seat_occupancy
           (schedule_id,
            seat_id,
            start_order,
            end_order)
        VALUES
           (#{scheduleId},
            #{seatId},
            #{startOrder},
            #{endOrder})
        """)
    int insert(SeatOccupancy seatOccupancy);

    @Delete("""
        DELETE FROM seat_occupancy
        WHERE schedule_id = #{scheduleId}
          AND seat_id = #{seatId}
          AND start_order = #{startOrder}
          AND end_order = #{endOrder}
        """)
    int deleteOccupancy(
            @Param("scheduleId") Long scheduleId,
            @Param("seatId") Long seatId,
            @Param("startOrder") Integer startOrder,
            @Param("endOrder") Integer endOrder
    );

}