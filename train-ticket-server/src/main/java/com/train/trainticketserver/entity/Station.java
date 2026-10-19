package com.train.trainticketserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("station")
public class Station {

    @TableId(type = IdType.AUTO)
    private Long stationId;

    private String stationName;

    private String city;
}