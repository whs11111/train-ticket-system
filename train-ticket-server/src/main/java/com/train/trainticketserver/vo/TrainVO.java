package com.train.trainticketserver.vo;

import com.train.trainticketserver.entity.TrainSchedule;
import lombok.Data;

import java.util.List;

@Data
public class TrainVO {

    private Long trainId;

    private String trainNo;

    private String trainType;

    private String startStationName;

    private String endStationName;

    private Long startStationId;

    private Long endStationId;

    private List<TrainSchedule> scheduleList;
}