package com.train.trainticketserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.train.trainticketserver.entity.TrainStation;
import com.train.trainticketserver.vo.TrainStationVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TrainStationMapper extends BaseMapper<TrainStation> {

    @Select("""
        SELECT stop_order
        FROM train_station ts
        JOIN station s
            ON ts.station_id = s.station_id
        WHERE ts.train_id = (
            SELECT train_id
            FROM train_schedule
            WHERE schedule_id = #{scheduleId})
        AND s.station_name = #{stationName}
        """)
    Integer findStopOrder(
            @Param("scheduleId") Long scheduleId,
            @Param("stationName") String stationName
    );

    @Select("""
        SELECT ts.stop_order
        FROM train_station ts
        WHERE ts.train_id = (
            SELECT train_id
            FROM train_schedule
            WHERE schedule_id = #{scheduleId}
        )
        AND ts.station_id = #{stationId}
        """)
    Integer findStopOrderById(
            @Param("scheduleId") Long scheduleId,
            @Param("stationId") Long stationId
    );

    @Select("""
        SELECT ts.*
        FROM train_station ts
        WHERE ts.train_id = (
            SELECT train_id
            FROM train_schedule
            WHERE schedule_id = #{scheduleId}
        )
        AND ts.station_id = #{stationId}
        """)
    TrainStation findByScheduleAndStation(
            @Param("scheduleId") Long scheduleId,
            @Param("stationId") Long stationId
    );

    @Select("""
        SELECT *
        FROM train_station
        WHERE train_id = #{trainId}
        ORDER BY stop_order
        """)
    List<TrainStation> findByTrainId(Long trainId);

    @Select("""
        SELECT
            ts.id,
            ts.train_id,
            ts.station_id,
            s.station_name,
            ts.arrival_time,
            ts.departure_time,
            ts.stop_order
        FROM train_station ts
        JOIN station s 
            ON ts.station_id = s.station_id
        WHERE ts.train_id = #{trainId}
        ORDER BY ts.stop_order
        """)
    List<TrainStationVO> findStationListByTrainId(Long trainId);

    @Delete("""
        DELETE FROM train_station
        WHERE train_id = #{trainId}
        """)
    void deleteByTrainId(Long trainId);

}