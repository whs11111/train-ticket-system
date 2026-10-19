package com.train.trainticketserver.vo;

import lombok.Data;

import java.time.LocalTime;
import java.util.List;

@Data
public class TicketSearchVO {

    private Long scheduleId;

    private String trainNo;

    private String startStation;

    private String startType;

    private String endStation;

    private String endType;

    private LocalTime departureTime;

    private LocalTime arrivalTime;

    private String duration;

    private List<PriceVO> prices;
}