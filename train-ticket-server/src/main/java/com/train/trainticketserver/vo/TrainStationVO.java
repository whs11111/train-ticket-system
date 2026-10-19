package com.train.trainticketserver.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
public class TrainStationVO {

    private Long id;

    private Long stationId;

    private String stationName;

    private LocalTime arrivalTime;

    private LocalTime departureTime;

    private Integer stopOrder;

    private BigDecimal businessPrice;

    private BigDecimal firstPrice;

    private BigDecimal secondPrice;
}