package com.train.trainticketserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("refund")
public class Refund {

    @TableId(type = IdType.AUTO)
    private Long refundId;


    private Long orderId;


    private BigDecimal amount;


    private LocalDateTime refundTime;


    private String reason;
}
