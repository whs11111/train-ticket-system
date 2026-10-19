package com.train.trainticketserver.controller;

import com.train.trainticketserver.dto.OrderDTO;
import com.train.trainticketserver.dto.PaymentDTO;
import com.train.trainticketserver.dto.TransferOrderDTO;
import com.train.trainticketserver.service.OrderService;
import com.train.trainticketserver.vo.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService){

        this.orderService = orderService;

    }

    @PostMapping("/create")
    public OrderResultVO createOrder(@RequestBody OrderDTO dto){

        return orderService.createOrder(dto);

    }


    @PostMapping("/createTransfer")
    public TransferOrderResultVO createTransferOrder(
            @RequestBody TransferOrderDTO dto
    ) {
        return orderService.createTransferOrder(dto);
    }

    @DeleteMapping("/cancel/{orderId}")
    public void cancelOrder(@PathVariable Long orderId) {

        orderService.cancelOrder(orderId);

    }

    @PutMapping("/change/nopay/{orderId}")
    public void changeWithoutPay(@PathVariable Long orderId, @RequestBody PaymentDTO dto){

        orderService.changeWithoutPay(orderId,dto);

    }

    @GetMapping("/detail/{orderId}")
    public OrderDetailVO getOrderDetail(@PathVariable Long orderId){

        return orderService.getOrderDetail(orderId);

    }

    @GetMapping("/findOrderList")
    public List<OrderListVO> findOrderList(@RequestParam Long userId) {
        return orderService.findOrderList(userId);
    }

    @GetMapping("/list")
    public List<OrderVO> list() {

        return orderService.list();

    }


}