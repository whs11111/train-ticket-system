package com.train.trainticketserver.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class OrderDetailVO {

    private Long orderId;

    private String trainNo;

    private String startStation;

    private String endStation;

    private String departureTime;

    private String arrivalTime;

    private LocalDate travelDate;

    private String seatType;

    private Integer coachNo;

    private String seatNo;

    private String name;

    private BigDecimal price;

    private String status;

    private String payMethod;

    private LocalDateTime payTime;

}