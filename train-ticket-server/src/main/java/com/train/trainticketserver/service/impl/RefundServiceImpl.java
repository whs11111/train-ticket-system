package com.train.trainticketserver.service.impl;

import com.train.trainticketserver.entity.Refund;
import com.train.trainticketserver.entity.Ticket;
import com.train.trainticketserver.mapper.*;
import com.train.trainticketserver.service.RefundService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RefundServiceImpl implements RefundService {

    private final RefundMapper refundMapper;

    private final TicketMapper ticketMapper;

    private final TicketOrderMapper ticketOrderMapper;

    private final TrainStationMapper trainStationMapper;

    private final SeatOccupancyMapper seatOccupancyMapper;

    public RefundServiceImpl(
            RefundMapper refundMapper,
            TicketMapper ticketMapper,
            TicketOrderMapper ticketOrderMapper,
            TrainStationMapper trainStationMapper,
            SeatOccupancyMapper seatOccupancyMapper
    ) {

        this.refundMapper = refundMapper;
        this.ticketMapper = ticketMapper;
        this.ticketOrderMapper = ticketOrderMapper;
        this.trainStationMapper = trainStationMapper;
        this.seatOccupancyMapper = seatOccupancyMapper;
    }

    @Override
    @Transactional
    public void refundOrder(Long orderId) {

        Ticket ticket = ticketMapper.findByOrderId(orderId);

        if (ticket == null) {
            throw new RuntimeException("订单不存在");
        }

        Integer startOrder =
                trainStationMapper.findStopOrderById(
                        ticket.getScheduleId(),
                        ticket.getStartStationId());

        Integer endOrder =
                trainStationMapper.findStopOrderById(
                        ticket.getScheduleId(),
                        ticket.getEndStationId());

        ticketOrderMapper.refundOrder(orderId);
        ticketMapper.refundTicket(orderId);
        seatOccupancyMapper.deleteOccupancy(ticket.getScheduleId(),ticket.getSeatId(),startOrder,endOrder);

        Refund refund = new Refund();
        refund.setOrderId(orderId);
        refund.setAmount(ticket.getPrice());
        refund.setRefundTime(LocalDateTime.now());
        refund.setReason("退票");
        refundMapper.insert(refund);
    }

}