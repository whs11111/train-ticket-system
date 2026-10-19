package com.train.trainticketserver.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class OrderVO {

    private Long orderId;

    private String username;

    private String name;

    private String phone;

    private String idnum;

    private String trainNo;

    private LocalDate runDate;

    private String startStationName;

    private String endStationName;

    private String seatInfo;

    private BigDecimal price;

    private String status;

    private LocalDateTime createTime;

}