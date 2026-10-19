package com.train.trainticketserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("ticket")
public class Ticket {

    @TableId(type = IdType.AUTO)
    private Long ticketId;

    private Long orderId;

    private Long scheduleId;

    private Long seatId;

    private Long startStationId;

    private Long endStationId;

    private BigDecimal price;

    private String status;

}