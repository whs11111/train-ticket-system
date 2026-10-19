package com.train.trainticketserver.dto;

import lombok.Data;

@Data
public class PaymentDTO {

    private String payMethod;

    private Long oldOrderId;

}