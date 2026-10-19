package com.train.trainticketserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.train.trainticketserver.entity.TrainSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TrainScheduleMapper extends BaseMapper<TrainSchedule> {

    @Select("""
        SELECT *
        FROM train_schedule
        WHERE schedule_id = #{scheduleId}
        """)
    TrainSchedule findById(
            @Param("scheduleId") Long scheduleId
    );

}