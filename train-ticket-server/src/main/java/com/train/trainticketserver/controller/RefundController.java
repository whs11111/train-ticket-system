package com.train.trainticketserver.controller;

import com.train.trainticketserver.service.RefundService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/refund")
public class RefundController {

    private final RefundService refundService;

    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    @PutMapping("/{orderId}")
    public void refund(@PathVariable Long orderId) {

        refundService.refundOrder(orderId);

    }

}
