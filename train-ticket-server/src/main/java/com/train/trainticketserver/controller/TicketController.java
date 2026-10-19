package com.train.trainticketserver.controller;

import com.train.trainticketserver.dto.RouteRequestDTO;
import com.train.trainticketserver.dto.TicketSearchDTO;
import com.train.trainticketserver.service.TicketService;
import com.train.trainticketserver.vo.RouteStationVO;
import com.train.trainticketserver.vo.TicketSearchVO;
import com.train.trainticketserver.vo.TransferTicketVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ticket")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping("/search")
    public List<TicketSearchVO> search(@RequestBody TicketSearchDTO dto) {

        return ticketService.search(dto);

    }

    @PostMapping("/searchChange")
    public List<TicketSearchVO> searchChange(@RequestBody TicketSearchDTO dto){

        return ticketService.searchChange(dto);

    }

    @PostMapping("/searchTransfer")
    public List<TransferTicketVO> searchTransfer(
            @RequestBody TicketSearchDTO dto) {

        return ticketService.searchTransfer(dto);
    }

    @PostMapping("/route")
    public List<RouteStationVO> getRoute(@RequestBody RouteRequestDTO request) {
        return ticketService.getRoute(request);
    }
}