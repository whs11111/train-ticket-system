package com.train.trainticketserver.vo;

import lombok.Data;

@Data
public class StationSearchVO {

    private String type;

    private String name;

    private String city;

    public StationSearchVO() {
    }

    public StationSearchVO(String type, String name, String city) {
        this.type = type;
        this.name = name;
        this.city = city;
    }
}