package com.train.trainticketserver.dto;

import lombok.Data;

@Data
public class TrainUpdateDTO {

    private Long trainId;

    private String trainNo;

    private String trainType;

}