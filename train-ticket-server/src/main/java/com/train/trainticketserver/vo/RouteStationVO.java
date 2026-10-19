package com.train.trainticketserver.vo;

import lombok.Data;

@Data
public class RouteStationVO {

    private String stationName;

    private String arrivalTime;

    private String departureTime;

    private String stopDuration;
}