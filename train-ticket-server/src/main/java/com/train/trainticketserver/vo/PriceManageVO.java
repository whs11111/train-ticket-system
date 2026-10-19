package com.train.trainticketserver.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PriceManageVO {

    private Long trainId;

    private String trainNo;


    private Long startStationId;

    private String startStationName;

    private Long endStationId;

    private String endStationName;

    private BigDecimal businessPrice;

    private BigDecimal firstPrice;

    private BigDecimal secondPrice;

}