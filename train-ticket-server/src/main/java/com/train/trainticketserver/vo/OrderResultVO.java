package com.train.trainticketserver.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderResultVO {

    private Long orderId;

    private String seatNo;

    private Boolean seatChanged;

    private String message;

    private BigDecimal price;

    private String name;

    private String trainNo;

    private String startStation;

    private String endStation;

    private String departureTime;

    private String arrivalTime;

    private String seatType;

    private Integer coachNo;

    private LocalDateTime createTime;

}