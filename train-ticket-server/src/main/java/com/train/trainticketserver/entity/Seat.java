package com.train.trainticketserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@TableName("seat")
public class Seat {

    @TableId(type = IdType.AUTO)
    private Long seatId;

    private Long coachId;

    private Long seatTypeId;

    private String seatNo;

}