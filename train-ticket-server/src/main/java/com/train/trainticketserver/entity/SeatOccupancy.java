package com.train.trainticketserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("seat_occupancy")
public class SeatOccupancy {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long scheduleId;

    private Long seatId;

    private int startOrder;

    private int endOrder;

}