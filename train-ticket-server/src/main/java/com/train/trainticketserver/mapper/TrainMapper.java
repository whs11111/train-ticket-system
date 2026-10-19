package com.train.trainticketserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.train.trainticketserver.entity.Coach;
import com.train.trainticketserver.entity.Train;
import com.train.trainticketserver.vo.TrainVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TrainMapper extends BaseMapper<Train> {
    @Select("SELECT * FROM train WHERE train_id = #{trainId} FOR UPDATE")
    Train lockById(@Param("trainId") Long trainId);

    @Select("""
        SELECT *
        FROM train
        WHERE train_id = #{trainId}
        """)
    Train findById(@Param("trainId") Long trainId);

    @Select("""
        SELECT
            t.train_id,
            t.train_no,
            t.train_type,
            t.start_station_id AS startStationId,
            t.end_station_id AS endStationId,
            s1.station_name AS startStationName,
            s2.station_name AS endStationName
        FROM train t
        LEFT JOIN station s1
          ON t.start_station_id=s1.station_id
        LEFT JOIN station s2
          ON t.end_station_id=s2.station_id
        """)
    List<TrainVO> selectTrainList();

    @Update("""
        UPDATE train
            SET start_station_id = #{startStationId},end_station_id = #{endStationId}
            WHERE train_id = #{trainId}
        """)
    void updateStation(Long trainId,Long startStationId,Long endStationId);

    @Insert("""
        INSERT INTO train (
            train_no,
            train_type
        )
        VALUES (
            #{trainNo},
            #{trainType}
        )
        """)
    @Options(
            useGeneratedKeys = true,
            keyProperty = "trainId",
            keyColumn = "train_id"
    )
    int insert(Train train);
}
