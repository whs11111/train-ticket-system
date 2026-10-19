package com.train.trainticketserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.train.trainticketserver.entity.SeatType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SeatTypeMapper extends BaseMapper<SeatType> {

    @Select("""
        SELECT *
        FROM seat_type
        WHERE seat_type_id = #{seatTypeId}
        """)
    SeatType findById(@Param("seatTypeId") Long seatTypeId);

}