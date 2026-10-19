package com.train.trainticketserver.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderListVO {

    private Long orderId;

    private String trainNo;

    private String travelDate;

    private String startStation;

    private String endStation;

    private String departureTime;

    private String arrivalTime;

    private String seatType;

    private Integer coachNo;

    private String seatNo;

    private BigDecimal price;

    private String status;

    private String ticketType;

    private String payMethod;

}