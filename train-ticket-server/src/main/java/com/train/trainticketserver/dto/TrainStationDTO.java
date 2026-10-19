package com.train.trainticketserver.dto;

import lombok.Data;

import java.time.LocalTime;

@Data
public class TrainStationDTO {

    private Long stationId;

    private LocalTime arrivalTime;

    private LocalTime departureTime;

    private Integer stopOrder;


}

