package com.train.trainticketserver.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TicketSearchDTO {

    private String startPlace;
    private String endPlace;
    private String startStation;
    private String endStation;
    private LocalDate date;

}