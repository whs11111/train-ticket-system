package com.train.trainticketserver.vo;

import lombok.Data;

import java.time.LocalTime;
import java.util.List;

@Data
public class TransferTicketVO {

    private Long firstScheduleId;

    private Long secondScheduleId;

    private String firstTrainNo;

    private String secondTrainNo;

    private String startStation;

    private String transferStation;

    private String endStation;

    private LocalTime totalDepartureTime;

    private LocalTime firstArrivalTime;

    private LocalTime secondDepartureTime;

    private LocalTime totalArrivalTime;

    private String totalDuration;

    private List<PriceVO> firstSeats;

    private List<PriceVO> secondSeats;
}