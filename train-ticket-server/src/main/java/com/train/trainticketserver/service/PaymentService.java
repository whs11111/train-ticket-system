package com.train.trainticketserver.service;

import java.math.BigDecimal;

public interface PaymentService {

    String createPayment(Long orderId);

    void paySuccess(Long orderId, String payMethod);

    String createTransferPayment(Long firstOrderId, Long secondOrderId);

    void payTransferSuccess(
            Long firstOrderId,
            Long secondOrderId,
            BigDecimal paidAmount,
            String payMethod
    );

    String createChangePayment(Long orderId, Long oldOrderId);

    void changeWithPay(Long orderId, Long oldOrderId, String payMethod);

    void refund(Long orderId);

    void changeRefund(Long orderId, Long oldOrderId);
}
