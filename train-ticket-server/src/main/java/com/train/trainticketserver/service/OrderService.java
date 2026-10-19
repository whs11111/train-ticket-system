package com.train.trainticketserver.service;

import com.train.trainticketserver.dto.OrderDTO;
import com.train.trainticketserver.dto.PaymentDTO;
import com.train.trainticketserver.dto.TransferOrderDTO;
import com.train.trainticketserver.vo.*;

import java.util.List;

public interface OrderService {

    OrderResultVO createOrder(OrderDTO dto);
    void cancelOrder(Long orderId);
    void changeWithoutPay(Long orderId, PaymentDTO dto);
    OrderDetailVO getOrderDetail(Long orderId);
    List<OrderListVO> findOrderList(Long userId);
    List<OrderVO> list();
    TransferOrderResultVO createTransferOrder(TransferOrderDTO dto);

}