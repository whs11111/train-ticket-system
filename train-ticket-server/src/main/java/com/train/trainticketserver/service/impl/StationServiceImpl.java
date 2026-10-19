package com.train.trainticketserver.service.impl;

import com.train.trainticketserver.entity.Station;
import com.train.trainticketserver.mapper.StationMapper;
import com.train.trainticketserver.service.StationService;
import com.train.trainticketserver.vo.StationSearchVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StationServiceImpl implements StationService {

    private final StationMapper stationMapper;
    public StationServiceImpl(StationMapper stationMapper) {

        this.stationMapper = stationMapper;

    }

    @Override
    public List<Station> list() {
        return stationMapper.selectList(null);
    }

    @Override
    public void add(Station station) {
        Station old = stationMapper.findByName(station.getStationName());
        if(old != null){throw new RuntimeException("车站已存在");}
        stationMapper.insert(station);
    }

    @Override
    public void update(Station station) {
        stationMapper.updateById(station);
    }

    @Override
    public void delete(Long stationId) {
        if(stationMapper.countTrainStationUse(stationId)>0){
            throw new RuntimeException("该车站正在被列车使用，不能删除");
        }
        stationMapper.deleteById(stationId);
    }

    @Override
    public List<StationSearchVO> search(String keyword) {
        List<StationSearchVO> result = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            return result;}
        keyword = keyword.trim();
        List<String> cities = stationMapper.searchCities(keyword);
        for (String city : cities) {
            result.add(new StationSearchVO("city",city,city));}
        List<Station> stations = stationMapper.searchStations(keyword);
        for (Station station : stations) {
            result.add(new StationSearchVO(
                    "station",
                    station.getStationName(),
                    station.getCity()
            ));
        }
        return result;
    }
}