package com.train.trainticketserver.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TransferSearchDTO {

    private String startCity;

    private String endCity;

    private LocalDate date;
}