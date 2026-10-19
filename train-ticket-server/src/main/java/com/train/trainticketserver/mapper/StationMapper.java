package com.train.trainticketserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.train.trainticketserver.entity.Station;
import com.train.trainticketserver.vo.StationSearchVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StationMapper extends BaseMapper<Station> {

    @Select("""
        SELECT *
        FROM station
        WHERE station_name = #{stationName}
    """)
    Station findByName(
            @Param("stationName") String stationName
    );

    @Select("""
        SELECT *
        FROM station
        WHERE station_id = #{stationId}
    """)
    Station findById(
            @Param("stationId") Long stationId
    );

    @Select("""
        SELECT COUNT(*)
        FROM train_station
        WHERE station_id = #{id}
    """)
    int countTrainStationUse(Long id);

    @Select("""
        SELECT DISTINCT city
        FROM station
        WHERE city LIKE CONCAT('%', #{keyword}, '%')
        ORDER BY city
        """)
    List<String> searchCities(String keyword);

    @Select("""
        SELECT station_id, station_name, city
        FROM station
        WHERE station_name LIKE CONCAT('%', #{keyword}, '%')
           OR city LIKE CONCAT('%', #{keyword}, '%')
        ORDER BY city, station_name
        """)
    List<Station> searchStations(String keyword);

}