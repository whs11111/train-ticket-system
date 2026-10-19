package com.train.trainticketserver.vo;

import lombok.Data;

@Data
public class CoachConfigVO {

    private Integer businessCount;

    private Integer firstCount;

    private Integer secondCount;

    private Integer totalCoachCount;

    private Integer totalSeatCount;
}