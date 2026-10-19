package com.train.trainticketserver.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransferOrderResultVO {

    private Long firstOrderId;

    private Long secondOrderId;

    private Long firstTicketId;

    private Long secondTicketId;

    private String firstTrainNo;

    private String secondTrainNo;

    private String firstStartStation;

    private String firstEndStation;

    private String secondStartStation;

    private String secondEndStation;

    private String firstDepartureTime;

    private String firstArrivalTime;

    private String secondDepartureTime;

    private String secondArrivalTime;

    private String firstSeatType;

    private String secondSeatType;

    private Integer firstCoachNo;

    private Integer secondCoachNo;

    private String firstSeatNo;

    private String secondSeatNo;

    private BigDecimal firstPrice;

    private BigDecimal secondPrice;

    private BigDecimal totalPrice;

    private String name;

    private LocalDateTime firstCreateTime;

    private LocalDateTime secondCreateTime;

    private boolean firstSeatChanged;

    private boolean secondSeatChanged;

    private String message;
}
