package com.train.trainticketserver.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PriceVO {

    private Long seatTypeId;

    private String seatType;

    private BigDecimal price;

    private Integer remain;


}
