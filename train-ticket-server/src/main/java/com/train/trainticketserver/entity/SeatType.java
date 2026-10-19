package com.train.trainticketserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("seat_type")
public class SeatType {

    @TableId(type = IdType.AUTO)
    private Long seatTypeId;

    private String typeName;

}