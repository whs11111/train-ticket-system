package com.train.trainticketserver.service.impl;

import com.train.trainticketserver.dto.RouteRequestDTO;
import com.train.trainticketserver.dto.TicketSearchDTO;
import com.train.trainticketserver.mapper.TicketMapper;
import com.train.trainticketserver.service.TicketService;
import com.train.trainticketserver.vo.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TicketServiceImpl implements TicketService {

    private final TicketMapper ticketMapper;

    public TicketServiceImpl(TicketMapper ticketMapper) {
        this.ticketMapper = ticketMapper;
    }

    @Override
    public List<TicketSearchVO> search(TicketSearchDTO dto) {

        List<TicketSearchResultVO> resultList =
                ticketMapper.search(
                        dto.getStartPlace(),
                        dto.getEndPlace(),
                        dto.getDate()
                );

        Map<String, TicketSearchVO> map = new LinkedHashMap<>();
        for (TicketSearchResultVO item : resultList) {
            String key = item.getScheduleId() + "_" + item.getStartStation()
                            + "_" + item.getEndStation();
            TicketSearchVO ticket = map.get(key);
            if (ticket == null) {
                ticket = new TicketSearchVO();
                ticket.setScheduleId(item.getScheduleId());
                ticket.setTrainNo(item.getTrainNo());
                ticket.setStartStation(item.getStartStation());
                ticket.setStartType(item.getStartType());
                ticket.setEndStation(item.getEndStation());
                ticket.setEndType(item.getEndType());
                ticket.setDepartureTime(item.getDepartureTime());
                ticket.setArrivalTime(item.getArrivalTime());
                long minutes = java.time.Duration
                        .between(item.getDepartureTime(),item.getArrivalTime())
                        .toMinutes();
                long hour = minutes / 60;
                long minute = minutes % 60;
                if (hour > 0) {
                    ticket.setDuration(hour + "时" + minute + "分");
                } else
                {ticket.setDuration(minute + "分");}
                ticket.setPrices(new ArrayList<>());
                map.put(key, ticket);
            }
            PriceVO priceVO = new PriceVO();
            priceVO.setSeatTypeId(item.getSeatTypeId());
            priceVO.setSeatType(item.getSeatType());
            priceVO.setPrice(item.getPrice());
            priceVO.setRemain(item.getRemain());
            ticket.getPrices().add(priceVO);
        }
        return new ArrayList<>(map.values());
    }


    @Override
    public List<TicketSearchVO> searchChange(TicketSearchDTO dto) {

        List<TicketSearchResultVO> resultList =
                ticketMapper.searchChange(
                        dto.getStartStation(),
                        dto.getEndStation(),
                        dto.getDate()
                );

        Map<Long, TicketSearchVO> map = new LinkedHashMap<>();
        for (TicketSearchResultVO item : resultList) {
            TicketSearchVO ticket = map.get(item.getScheduleId());
            if (ticket == null) {
                ticket = new TicketSearchVO();
                ticket.setScheduleId(item.getScheduleId());
                ticket.setTrainNo(item.getTrainNo());
                ticket.setStartStation(item.getStartStation());
                ticket.setStartType(item.getStartType());
                ticket.setEndStation(item.getEndStation());
                ticket.setEndType(item.getEndType());
                ticket.setDepartureTime(item.getDepartureTime());
                ticket.setArrivalTime(item.getArrivalTime());
                long minutes = java.time.Duration
                        .between(
                                item.getDepartureTime(),
                                item.getArrivalTime()
                        )
                        .toMinutes();

                long hour = minutes / 60;

                long minute = minutes % 60;

                if (hour > 0) {
                    ticket.setDuration(
                            hour + "时" + minute + "分"
                    );
                } else {
                    ticket.setDuration(
                            minute + "分"
                    );
                }
                ticket.setPrices(new ArrayList<>());
                map.put(
                        item.getScheduleId(),
                        ticket
                );
            }
            PriceVO priceVO = new PriceVO();
            priceVO.setSeatTypeId(item.getSeatTypeId());
            priceVO.setSeatType(item.getSeatType());
            priceVO.setPrice(item.getPrice());
            priceVO.setRemain(item.getRemain());
            ticket.getPrices().add(priceVO);
        }
        return new ArrayList<>(map.values());
    }

    @Override
    public List<TransferTicketVO> searchTransfer(TicketSearchDTO dto) {

        List<Map<String, Object>> resultList =
                ticketMapper.searchTransfer(
                        dto.getStartPlace(),
                        dto.getEndPlace(),
                        dto.getDate()
                );

        List<TransferTicketVO> result = new ArrayList<>();

        for (Map<String, Object> item : resultList) {

            TransferTicketVO vo = new TransferTicketVO();

            Long firstScheduleId = ((Number) item.get("first_schedule_id")).longValue();
            Long secondScheduleId = ((Number) item.get("second_schedule_id")).longValue();

            vo.setFirstScheduleId(firstScheduleId);
            vo.setSecondScheduleId(secondScheduleId);
            vo.setFirstTrainNo((String) item.get("first_train_no"));
            vo.setSecondTrainNo((String) item.get("second_train_no"));
            vo.setStartStation((String) item.get("start_station"));
            vo.setTransferStation((String) item.get("transfer_station"));
            vo.setEndStation((String) item.get("end_station"));

            LocalTime totalDepartureTime =
                    ((java.sql.Time) item.get("total_departure_time")).toLocalTime();

            LocalTime firstArrivalTime =
                    ((java.sql.Time) item.get("first_arrival_time")).toLocalTime();

            LocalTime secondDepartureTime =
                    ((java.sql.Time) item.get("second_departure_time")).toLocalTime();

            LocalTime totalArrivalTime =
                    ((java.sql.Time) item.get("total_arrival_time")).toLocalTime();
            
            vo.setTotalDepartureTime(totalDepartureTime);
            vo.setFirstArrivalTime(firstArrivalTime);
            vo.setSecondDepartureTime(secondDepartureTime);
            vo.setTotalArrivalTime(totalArrivalTime);

            long minutes = Duration.between(totalDepartureTime,totalArrivalTime).toMinutes();

            long hour = minutes / 60;
            long minute = minutes % 60;

            if (hour > 0) {
                vo.setTotalDuration(
                        hour + "时" + minute + "分"
                );
            } else {
                vo.setTotalDuration(
                        minute + "分"
                );
            }

            Integer firstStartOrder = ((Number) item.get("first_start_order")).intValue();

            Integer firstEndOrder = ((Number) item.get("first_transfer_order")).intValue();

            Integer secondStartOrder = ((Number) item.get("second_transfer_order")).intValue();

            Integer secondEndOrder = ((Number) item.get("second_end_order")).intValue();

            vo.setFirstSeats(ticketMapper.findTransferSeatPrices(firstScheduleId,firstStartOrder,firstEndOrder));
            vo.setSecondSeats(ticketMapper.findTransferSeatPrices(secondScheduleId,secondStartOrder, secondEndOrder));
            result.add(vo);
        }

        return result;
    }

    @Override
    public List<RouteStationVO> getRoute(RouteRequestDTO request) {
        return ticketMapper.getRoute(
                request.getScheduleId(),
                request.getDate()
        );
    }
}