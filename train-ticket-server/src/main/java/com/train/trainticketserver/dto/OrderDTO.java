package com.train.trainticketserver.dto;

import lombok.Data;

@Data
public class OrderDTO {

    private Long scheduleId;

    private Long seatTypeId;

    private String startStation;

    private String endStation;

    private String seatColumn;

    private Long userId;

    private String departureTime;

    private String arrivalTime;

}