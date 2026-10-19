package com.train.trainticketserver.controller;

import com.train.trainticketserver.service.TicketPriceService;
import com.train.trainticketserver.vo.PriceManageVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/price")
public class TicketPriceController {

    private final TicketPriceService ticketPriceService;

    public TicketPriceController(TicketPriceService ticketPriceService){

        this.ticketPriceService = ticketPriceService;

    }

    @GetMapping("/manage")
    public List<PriceManageVO> manage(){

        return ticketPriceService.manageList();

    }

    @PostMapping("/save")
    public void save(@RequestBody PriceManageVO vo){

        ticketPriceService.savePrice(vo);

    }

    @PostMapping("/saveBatch")
    public void saveBatch(@RequestBody List<PriceManageVO> prices) {
        ticketPriceService.savePrices(prices);
    }

}
