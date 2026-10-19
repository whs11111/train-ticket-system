package com.train.trainticketserver.service;

import com.train.trainticketserver.vo.PriceManageVO;

import java.util.List;

public interface TicketPriceService {
    List<PriceManageVO> manageList();
    void savePrice(PriceManageVO vo);
    void savePrices(List<PriceManageVO> prices);
}
