package com.train.trainticketserver.service;

import com.train.trainticketserver.entity.Station;
import com.train.trainticketserver.vo.StationSearchVO;

import java.util.List;

public interface StationService {

    List<Station> list();
    void add(Station station);
    void update(Station station);
    void delete(Long stationId);
    List<StationSearchVO> search(String keyword);

}