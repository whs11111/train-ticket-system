package com.train.trainticketserver.dto;

import lombok.Data;

@Data
public class TrainAddDTO {

    private String trainNo;

    private String trainType;

    private Integer businessCount;

    private Integer firstCount;

    private Integer secondCount;
}
