package com.train.trainticketserver.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
public class TicketSearchResultVO {

    private Long scheduleId;

    private String trainNo;

    private String startStation;

    private String endStation;

    private LocalTime departureTime;

    private LocalTime arrivalTime;

    private Long seatTypeId;

    private String seatType;

    private BigDecimal price;

    private String startType;

    private String endType;

    private Integer remain;
}