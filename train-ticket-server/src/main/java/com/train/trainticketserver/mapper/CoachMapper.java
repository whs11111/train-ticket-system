package com.train.trainticketserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.train.trainticketserver.entity.Coach;
import com.train.trainticketserver.vo.CoachConfigVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CoachMapper extends BaseMapper<Coach> {

    @Select("""
        SELECT *
        FROM coach
        WHERE coach_id = #{coachId}
        """)
    Coach findById(@Param("coachId") Long coachId);

    @Insert("""
        INSERT INTO coach (
            train_id,
            coach_no
        )
        VALUES (
            #{trainId},
            #{coachNo}
        )
        """)
    @Options(
            useGeneratedKeys = true,
            keyProperty = "coachId",
            keyColumn = "coach_id"
    )
    int insert(Coach coach);

    @Select("""
        SELECT
            coach_id,
            train_id,
            coach_no
        FROM coach
        WHERE train_id = #{trainId}
        ORDER BY coach_no
        """)
    List<Coach> findByTrainId(Long trainId);

    @Select("""
    SELECT
        COUNT(DISTINCT CASE WHEN s.seat_type_id = 1 THEN c.coach_id END) AS businessCount,
        COUNT(DISTINCT CASE WHEN s.seat_type_id = 2 THEN c.coach_id END) AS firstCount,
        COUNT(DISTINCT CASE WHEN s.seat_type_id = 3 THEN c.coach_id END) AS secondCount
    FROM coach c
    JOIN seat s ON c.coach_id = s.coach_id
    WHERE c.train_id = #{trainId}
    """)
    CoachConfigVO findConfig(Long trainId);



}