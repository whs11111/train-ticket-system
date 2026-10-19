package com.train.trainticketserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TrainSchedule {

    @TableId(type = IdType.AUTO)
    private Long scheduleId;

    private Long trainId;

    private LocalDate runDate;

}