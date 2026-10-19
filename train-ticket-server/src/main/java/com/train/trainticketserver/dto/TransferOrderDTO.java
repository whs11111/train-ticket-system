package com.train.trainticketserver.dto;

import lombok.Data;

@Data
public class TransferOrderDTO {

    private Long userId;

    private String date;

    private Long firstScheduleId;
    private String firstTrainNo;
    private String firstStartStation;
    private String firstEndStation;
    private String firstDepartureTime;
    private String firstArrivalTime;
    private Long firstSeatTypeId;
    private String firstSeatColumn;

    private Long secondScheduleId;
    private String secondTrainNo;
    private String secondStartStation;
    private String secondEndStation;
    private String secondDepartureTime;
    private String secondArrivalTime;
    private Long secondSeatTypeId;
    private String secondSeatColumn;
}