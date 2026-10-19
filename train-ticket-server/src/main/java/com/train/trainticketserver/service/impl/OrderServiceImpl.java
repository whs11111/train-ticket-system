package com.train.trainticketserver.service.impl;

import com.train.trainticketserver.dto.OrderDTO;
import com.train.trainticketserver.dto.PaymentDTO;
import com.train.trainticketserver.dto.TransferOrderDTO;
import com.train.trainticketserver.entity.*;
import com.train.trainticketserver.mapper.*;
import com.train.trainticketserver.service.OrderService;
import com.train.trainticketserver.vo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final TrainScheduleMapper trainScheduleMapper;

    private final StationMapper stationMapper;

    private final TrainStationMapper trainStationMapper;

    private final TicketPriceMapper ticketPriceMapper;

    private final SeatMapper seatMapper;

    private final TicketOrderMapper ticketOrderMapper;

    private final TicketMapper ticketMapper;

    private final SeatOccupancyMapper seatOccupancyMapper;

    private final PaymentMapper paymentMapper;

    private final UserMapper userMapper;

    private final CoachMapper coachMapper;

    private final SeatTypeMapper seatTypeMapper;

    private final OrderMapper orderMapper;

    private final TrainMapper trainMapper;

    private final RefundMapper refundMapper;

    public OrderServiceImpl(
            TrainScheduleMapper trainScheduleMapper,
            StationMapper stationMapper,
            TrainStationMapper trainStationMapper,
            TicketPriceMapper ticketPriceMapper,
            SeatMapper seatMapper,
            TicketOrderMapper ticketOrderMapper,
            TicketMapper ticketMapper,
            SeatOccupancyMapper seatOccupancyMapper,
            UserMapper userMapper,
            CoachMapper coachMapper,
            SeatTypeMapper seatTypeMapper,
            TrainMapper trainMapper,
            PaymentMapper paymentMapper,
            OrderMapper orderMapper,
            RefundMapper refundMapper
    ) {

        this.trainScheduleMapper = trainScheduleMapper;
        this.stationMapper = stationMapper;
        this.trainStationMapper = trainStationMapper;
        this.ticketPriceMapper = ticketPriceMapper;
        this.seatMapper = seatMapper;
        this.ticketOrderMapper = ticketOrderMapper;
        this.ticketMapper = ticketMapper;
        this.seatOccupancyMapper = seatOccupancyMapper;
        this.userMapper = userMapper;
        this.coachMapper = coachMapper;
        this.seatTypeMapper = seatTypeMapper;
        this.trainMapper = trainMapper;
        this.paymentMapper = paymentMapper;
        this.orderMapper = orderMapper;
        this.refundMapper = refundMapper;


    }

    @Override
    @Transactional
    public OrderResultVO createOrder(OrderDTO dto) {
        TrainSchedule schedule = trainScheduleMapper.findById(dto.getScheduleId());
        Long trainId = schedule.getTrainId();
        Station startStation = stationMapper.findByName(dto.getStartStation());
        Station endStation = stationMapper.findByName(dto.getEndStation());
        Long startStationId = startStation.getStationId();
        Long endStationId = endStation.getStationId();
        Integer startOrder = trainStationMapper.findStopOrder(dto.getScheduleId(),dto.getStartStation());
        Integer endOrder = trainStationMapper.findStopOrder(dto.getScheduleId(),dto.getEndStation());
        BigDecimal price =
                ticketPriceMapper.findPrice(
                        trainId,
                        startStationId,
                        endStationId,
                        dto.getSeatTypeId()
                );
        Seat seat;
        boolean seatChanged = false;
        if(dto.getSeatColumn() != null){
            seat = seatMapper.findSeatByColumn(
                    dto.getScheduleId(),
                    dto.getSeatTypeId(),
                    dto.getSeatColumn(),
                    startOrder,
                    endOrder
            );
            if(seat == null){
                seat = seatMapper.findAutoSeat(
                        dto.getScheduleId(),
                        dto.getSeatTypeId(),
                        startOrder,
                        endOrder
                );
                seatChanged = true;
            }
        }else {
            seat = seatMapper.findAutoSeat(
                    dto.getScheduleId(),
                    dto.getSeatTypeId(),
                    startOrder,
                    endOrder
            );

        }
        if(seat == null){

            throw new RuntimeException("没有可用座位");

        }
        User user = userMapper.findById(dto.getUserId());
        Coach coach = coachMapper.findById(seat.getCoachId());
        SeatType seatType = seatTypeMapper.findById(seat.getSeatTypeId());
        Train train = trainMapper.findById(trainId);

        TicketOrder order = new TicketOrder();
        order.setUserId(dto.getUserId());
        order.setTotalPrice(price);
        order.setStatus("待支付");
        ticketOrderMapper.insert(order);
        TicketOrder orderInfo = ticketOrderMapper.findById(order.getOrderId());

        Ticket ticket = new Ticket();
        ticket.setOrderId(order.getOrderId());
        ticket.setScheduleId(dto.getScheduleId());
        ticket.setSeatId(seat.getSeatId());
        ticket.setStartStationId(startStationId);
        ticket.setEndStationId(endStationId);
        ticket.setPrice(price);
        ticket.setStatus("待支付");
        ticketMapper.insert(ticket);

        SeatOccupancy occupancy = new SeatOccupancy();
        occupancy.setScheduleId(dto.getScheduleId());
        occupancy.setSeatId(seat.getSeatId());
        occupancy.setStartOrder(startOrder);
        occupancy.setEndOrder(endOrder);
        seatOccupancyMapper.insert(occupancy);

        OrderResultVO result = new OrderResultVO();
        result.setOrderId(order.getOrderId());
        result.setSeatNo(seat.getSeatNo());
        result.setSeatChanged(seatChanged);
        result.setPrice(price);
        result.setName(user.getName());
        result.setTrainNo(train.getTrainNo());
        result.setStartStation(dto.getStartStation());
        result.setDepartureTime(dto.getDepartureTime());
        result.setArrivalTime(dto.getArrivalTime());
        result.setEndStation(dto.getEndStation());
        result.setSeatType(seatType.getTypeName());
        result.setCoachNo(coach.getCoachNo());
        result.setCreateTime(orderInfo.getCreateTime());
        if(seatChanged){
            result.setMessage("当前列车剩余座位无法满足您的需求，系统已为您自动分配座位");
        }else{
            result.setMessage("购票成功");
        }
        return result;
    }

    @Override
    @Transactional
    public TransferOrderResultVO createTransferOrder(TransferOrderDTO dto) {

        User user = userMapper.findById(dto.getUserId());

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        TrainSchedule firstSchedule =
                trainScheduleMapper.findById(dto.getFirstScheduleId());

        TrainSchedule secondSchedule =
                trainScheduleMapper.findById(dto.getSecondScheduleId());

        if (firstSchedule == null || secondSchedule == null) {
            throw new RuntimeException("车次信息不存在");
        }

        Long firstTrainId = firstSchedule.getTrainId();
        Long secondTrainId = secondSchedule.getTrainId();

        Station firstStartStation =
                stationMapper.findByName(dto.getFirstStartStation());

        Station firstEndStation =
                stationMapper.findByName(dto.getFirstEndStation());

        Station secondStartStation =
                stationMapper.findByName(dto.getSecondStartStation());

        Station secondEndStation =
                stationMapper.findByName(dto.getSecondEndStation());

        if (firstStartStation == null
                || firstEndStation == null
                || secondStartStation == null
                || secondEndStation == null) {
            throw new RuntimeException("车站信息不存在");
        }

        Long firstStartStationId = firstStartStation.getStationId();
        Long firstEndStationId = firstEndStation.getStationId();
        Long secondStartStationId = secondStartStation.getStationId();
        Long secondEndStationId = secondEndStation.getStationId();

        Integer firstStartOrder =
                trainStationMapper.findStopOrder(
                        dto.getFirstScheduleId(),
                        dto.getFirstStartStation()
                );

        Integer firstEndOrder =
                trainStationMapper.findStopOrder(
                        dto.getFirstScheduleId(),
                        dto.getFirstEndStation()
                );

        Integer secondStartOrder =
                trainStationMapper.findStopOrder(
                        dto.getSecondScheduleId(),
                        dto.getSecondStartStation()
                );

        Integer secondEndOrder =
                trainStationMapper.findStopOrder(
                        dto.getSecondScheduleId(),
                        dto.getSecondEndStation()
                );

        if (firstStartOrder == null
                || firstEndOrder == null
                || secondStartOrder == null
                || secondEndOrder == null) {
            throw new RuntimeException("车站不属于当前车次");
        }

        BigDecimal firstPrice =
                ticketPriceMapper.findPrice(
                        firstTrainId,
                        firstStartStationId,
                        firstEndStationId,
                        dto.getFirstSeatTypeId()
                );

        BigDecimal secondPrice =
                ticketPriceMapper.findPrice(
                        secondTrainId,
                        secondStartStationId,
                        secondEndStationId,
                        dto.getSecondSeatTypeId()
                );

        if (firstPrice == null || secondPrice == null) {
            throw new RuntimeException("车票价格不存在");
        }

        Seat firstSeat;
        boolean firstSeatChanged = false;

        if (dto.getFirstSeatColumn() != null
                && !dto.getFirstSeatColumn().isEmpty()) {

            firstSeat =
                    seatMapper.findSeatByColumn(
                            dto.getFirstScheduleId(),
                            dto.getFirstSeatTypeId(),
                            dto.getFirstSeatColumn(),
                            firstStartOrder,
                            firstEndOrder
                    );

            if (firstSeat == null) {
                firstSeat =
                        seatMapper.findAutoSeat(
                                dto.getFirstScheduleId(),
                                dto.getFirstSeatTypeId(),
                                firstStartOrder,
                                firstEndOrder
                        );

                firstSeatChanged = true;
            }

        } else {

            firstSeat =
                    seatMapper.findAutoSeat(
                            dto.getFirstScheduleId(),
                            dto.getFirstSeatTypeId(),
                            firstStartOrder,
                            firstEndOrder
                    );
        }

        if (firstSeat == null) {
            throw new RuntimeException("第一程没有可用座位");
        }

        Seat secondSeat;
        boolean secondSeatChanged = false;

        if (dto.getSecondSeatColumn() != null
                && !dto.getSecondSeatColumn().isEmpty()) {

            secondSeat =
                    seatMapper.findSeatByColumn(
                            dto.getSecondScheduleId(),
                            dto.getSecondSeatTypeId(),
                            dto.getSecondSeatColumn(),
                            secondStartOrder,
                            secondEndOrder
                    );

            if (secondSeat == null) {
                secondSeat =
                        seatMapper.findAutoSeat(
                                dto.getSecondScheduleId(),
                                dto.getSecondSeatTypeId(),
                                secondStartOrder,
                                secondEndOrder
                        );

                secondSeatChanged = true;
            }

        } else {

            secondSeat =
                    seatMapper.findAutoSeat(
                            dto.getSecondScheduleId(),
                            dto.getSecondSeatTypeId(),
                            secondStartOrder,
                            secondEndOrder
                    );
        }

        if (secondSeat == null) {
            throw new RuntimeException("第二程没有可用座位");
        }

        Coach firstCoach =
                coachMapper.findById(firstSeat.getCoachId());

        Coach secondCoach =
                coachMapper.findById(secondSeat.getCoachId());

        SeatType firstSeatType =
                seatTypeMapper.findById(firstSeat.getSeatTypeId());

        SeatType secondSeatType =
                seatTypeMapper.findById(secondSeat.getSeatTypeId());

        Train firstTrain =
                trainMapper.findById(firstTrainId);

        Train secondTrain =
                trainMapper.findById(secondTrainId);

        TicketOrder firstOrder = new TicketOrder();

        firstOrder.setUserId(dto.getUserId());
        firstOrder.setTotalPrice(firstPrice);
        firstOrder.setStatus("待支付");

        ticketOrderMapper.insert(firstOrder);

        Ticket firstTicket = new Ticket();

        firstTicket.setOrderId(firstOrder.getOrderId());
        firstTicket.setScheduleId(dto.getFirstScheduleId());
        firstTicket.setSeatId(firstSeat.getSeatId());
        firstTicket.setStartStationId(firstStartStationId);
        firstTicket.setEndStationId(firstEndStationId);
        firstTicket.setPrice(firstPrice);
        firstTicket.setStatus("待支付");

        ticketMapper.insert(firstTicket);

        SeatOccupancy firstOccupancy = new SeatOccupancy();

        firstOccupancy.setScheduleId(dto.getFirstScheduleId());
        firstOccupancy.setSeatId(firstSeat.getSeatId());
        firstOccupancy.setStartOrder(firstStartOrder);
        firstOccupancy.setEndOrder(firstEndOrder);

        seatOccupancyMapper.insert(firstOccupancy);

        TicketOrder secondOrder = new TicketOrder();

        secondOrder.setUserId(dto.getUserId());
        secondOrder.setTotalPrice(secondPrice);
        secondOrder.setStatus("待支付");

        ticketOrderMapper.insert(secondOrder);

        Ticket secondTicket = new Ticket();

        secondTicket.setOrderId(secondOrder.getOrderId());
        secondTicket.setScheduleId(dto.getSecondScheduleId());
        secondTicket.setSeatId(secondSeat.getSeatId());
        secondTicket.setStartStationId(secondStartStationId);
        secondTicket.setEndStationId(secondEndStationId);
        secondTicket.setPrice(secondPrice);
        secondTicket.setStatus("待支付");

        ticketMapper.insert(secondTicket);

        SeatOccupancy secondOccupancy = new SeatOccupancy();

        secondOccupancy.setScheduleId(dto.getSecondScheduleId());
        secondOccupancy.setSeatId(secondSeat.getSeatId());
        secondOccupancy.setStartOrder(secondStartOrder);
        secondOccupancy.setEndOrder(secondEndOrder);

        seatOccupancyMapper.insert(secondOccupancy);

        TicketOrder firstOrderInfo =
                ticketOrderMapper.findById(firstOrder.getOrderId());

        TicketOrder secondOrderInfo =
                ticketOrderMapper.findById(secondOrder.getOrderId());

        BigDecimal totalPrice =
                firstPrice.add(secondPrice);

        TransferOrderResultVO result =
                new TransferOrderResultVO();

        result.setFirstOrderId(firstOrder.getOrderId());
        result.setSecondOrderId(secondOrder.getOrderId());

        result.setFirstTicketId(firstTicket.getTicketId());
        result.setSecondTicketId(secondTicket.getTicketId());

        result.setFirstTrainNo(firstTrain.getTrainNo());
        result.setSecondTrainNo(secondTrain.getTrainNo());

        result.setFirstStartStation(dto.getFirstStartStation());
        result.setFirstEndStation(dto.getFirstEndStation());

        result.setSecondStartStation(dto.getSecondStartStation());
        result.setSecondEndStation(dto.getSecondEndStation());

        result.setFirstDepartureTime(dto.getFirstDepartureTime());
        result.setFirstArrivalTime(dto.getFirstArrivalTime());

        result.setSecondDepartureTime(dto.getSecondDepartureTime());
        result.setSecondArrivalTime(dto.getSecondArrivalTime());

        result.setFirstSeatType(firstSeatType.getTypeName());
        result.setSecondSeatType(secondSeatType.getTypeName());

        result.setFirstCoachNo(firstCoach.getCoachNo());
        result.setSecondCoachNo(secondCoach.getCoachNo());

        result.setFirstSeatNo(firstSeat.getSeatNo());
        result.setSecondSeatNo(secondSeat.getSeatNo());

        result.setFirstPrice(firstPrice);
        result.setSecondPrice(secondPrice);
        result.setTotalPrice(totalPrice);

        result.setName(user.getName());

        result.setFirstCreateTime(firstOrderInfo.getCreateTime());
        result.setSecondCreateTime(secondOrderInfo.getCreateTime());

        result.setFirstSeatChanged(firstSeatChanged);
        result.setSecondSeatChanged(secondSeatChanged);

        if (firstSeatChanged && secondSeatChanged) {
            result.setMessage(
                    "第一程和第二程所选座位均无法满足需求，系统已为您自动分配座位"
            );
        } else if (firstSeatChanged) {
            result.setMessage(
                    "第一程所选座位无法满足需求，系统已为您自动分配座位"
            );
        } else if (secondSeatChanged) {
            result.setMessage(
                    "第二程所选座位无法满足需求，系统已为您自动分配座位"
            );
        } else {
            result.setMessage("购票成功");
        }

        return result;
    }
    @Override
    @Transactional
    public void cancelOrder(Long orderId) {

        Ticket ticket = ticketMapper.findByOrderId(orderId);

        if (ticket == null) {
            throw new RuntimeException("订单不存在");
        }

        Integer startOrder =
                trainStationMapper.findStopOrderById(ticket.getScheduleId(),ticket.getStartStationId());

        Integer endOrder =
                trainStationMapper.findStopOrderById(ticket.getScheduleId(),ticket.getEndStationId());

        ticketOrderMapper.cancelOrder(orderId);
        ticketMapper.cancelTicket(orderId);
        seatOccupancyMapper.deleteOccupancy(
                ticket.getScheduleId(),
                ticket.getSeatId(),
                startOrder,
                endOrder
        );
    }

    @Override
    @Transactional
    public void changeWithoutPay(
            Long orderId,
            PaymentDTO dto
    ) {
        Long oldOrderId = dto.getOldOrderId();

        Ticket newTicket =
                ticketMapper.findByOrderId(orderId);

        Ticket oldTicket =
                ticketMapper.findByOrderId(oldOrderId);

        if (newTicket == null) {
            throw new RuntimeException("改签订单不存在");
        }

        if (oldTicket == null) {
            throw new RuntimeException("原订单不存在");
        }

        TicketOrder newOrder = ticketOrderMapper.findById(orderId);
        TicketOrder oldOrder = ticketOrderMapper.findById(oldOrderId);
        if (newOrder == null) {throw new RuntimeException("改签订单不存在");}
        if (oldOrder == null) {throw new RuntimeException("原订单不存在");}
        if (!"待支付".equals(newOrder.getStatus())) { return;}
        if (!"已支付".equals(oldOrder.getStatus())) {
            throw new RuntimeException("原订单状态不允许改签");}
        BigDecimal diffPrice = newTicket.getPrice().subtract(oldTicket.getPrice());
        if (diffPrice.compareTo(BigDecimal.ZERO) != 0) {
            throw new RuntimeException("当前改签订单不是等价改签");
        }
        Payment oldPayment = paymentMapper.findByOrderId(oldOrderId);
        if (oldPayment == null) {
            throw new RuntimeException("原订单支付记录不存在");}
        Payment newPayment = new Payment();
        newPayment.setOrderId(orderId);
        newPayment.setAmount(newTicket.getPrice());
        newPayment.setPayMethod(oldPayment.getPayMethod());
        newPayment.setOutTradeNo(oldPayment.getOutTradeNo());
        newPayment.setPayTime(oldPayment.getPayTime());

        paymentMapper.insert(newPayment);
        ticketOrderMapper.changeOrder(oldOrderId);
        ticketMapper.changeTicket(oldOrderId);
        Integer startOrder = trainStationMapper.findStopOrderById(
                        oldTicket.getScheduleId(),
                        oldTicket.getStartStationId());

        Integer endOrder = trainStationMapper.findStopOrderById(
                        oldTicket.getScheduleId(),
                        oldTicket.getEndStationId());

        seatOccupancyMapper.deleteOccupancy(oldTicket.getScheduleId(),
                    oldTicket.getSeatId(),startOrder,endOrder);
        ticketOrderMapper.payChangeOrder(orderId);
        ticketMapper.payChangeTicket(orderId);
    }
    @Override
    public OrderDetailVO getOrderDetail(Long orderId) {

        Ticket ticket = ticketMapper.findByOrderId(orderId);

        if(ticket == null){

            throw new RuntimeException("订单不存在");

        }

        TicketOrder order = ticketOrderMapper.findById(orderId);
        TrainSchedule schedule = trainScheduleMapper.findById(ticket.getScheduleId());
        Train train = trainMapper.findById(schedule.getTrainId());
        Station start = stationMapper.findById(ticket.getStartStationId());
        Station end = stationMapper.findById(ticket.getEndStationId());
        Seat seat = seatMapper.findById(ticket.getSeatId());
        Coach coach = coachMapper.findById(seat.getCoachId());
        Payment payment = paymentMapper.findByOrderId(orderId);
        TrainStation startStation = trainStationMapper.findByScheduleAndStation(
                ticket.getScheduleId(),ticket.getStartStationId());
        TrainStation endStation = trainStationMapper.findByScheduleAndStation(
                ticket.getScheduleId(),ticket.getEndStationId());
        SeatType seatType = seatTypeMapper.findById(seat.getSeatTypeId());
        User user = userMapper.findById(order.getUserId());

        OrderDetailVO vo = new OrderDetailVO();
        vo.setOrderId(orderId);
        vo.setTrainNo(train.getTrainNo());
        vo.setStartStation(start.getStationName());
        vo.setEndStation(end.getStationName());
        vo.setSeatNo(seat.getSeatNo());
        vo.setCoachNo(coach.getCoachNo());
        vo.setPrice(ticket.getPrice());
        vo.setStatus(order.getStatus());
        if (payment != null) {
            vo.setPayMethod(payment.getPayMethod());
            vo.setPayTime(payment.getPayTime());
        }
        vo.setTravelDate(schedule.getRunDate());
        vo.setDepartureTime(startStation.getDepartureTime().toString());
        vo.setArrivalTime(endStation.getArrivalTime().toString());
        vo.setSeatType(seatType.getTypeName());
        vo.setName(user.getName());
        return vo;

    }

    @Override
    public List<OrderListVO> findOrderList(Long userId) {
        return orderMapper.findOrderList(userId);
    }

    @Override
    public List<OrderVO> list() {
        return orderMapper.list();
    }

}