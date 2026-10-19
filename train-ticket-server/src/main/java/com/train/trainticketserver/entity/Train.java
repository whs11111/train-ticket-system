package com.train.trainticketserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("train")
public class Train {

    @TableId(type = IdType.AUTO)
    private Long trainId;

    private String trainNo;

    private String trainType;

    private Long startStationId;

    private Long endStationId;

}