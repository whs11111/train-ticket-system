package com.train.trainticketserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("ticket_price")
public class TicketPrice {

    @TableId(type = IdType.AUTO)
    private Long priceId;

    private Long trainId;

    private Long startStationId;

    private Long endStationId;

    private Long seatTypeId;

    private BigDecimal price;

}