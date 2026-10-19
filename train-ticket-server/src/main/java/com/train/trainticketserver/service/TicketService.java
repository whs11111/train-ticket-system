package com.train.trainticketserver.service;

import com.train.trainticketserver.dto.RouteRequestDTO;
import com.train.trainticketserver.dto.TicketSearchDTO;
import com.train.trainticketserver.vo.RouteStationVO;
import com.train.trainticketserver.vo.TicketSearchVO;
import com.train.trainticketserver.vo.TransferTicketVO;

import java.util.List;

public interface TicketService {

    List<TicketSearchVO> search(TicketSearchDTO dto);
    List<TicketSearchVO> searchChange(TicketSearchDTO dto);
    List<TransferTicketVO> searchTransfer(TicketSearchDTO dto);
    List<RouteStationVO> getRoute(RouteRequestDTO request);

}