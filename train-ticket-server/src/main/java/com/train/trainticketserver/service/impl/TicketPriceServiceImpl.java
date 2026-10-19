package com.train.trainticketserver.service.impl;

import com.train.trainticketserver.entity.Station;
import com.train.trainticketserver.entity.TicketPrice;
import com.train.trainticketserver.entity.Train;
import com.train.trainticketserver.entity.TrainStation;
import com.train.trainticketserver.mapper.StationMapper;
import com.train.trainticketserver.mapper.TicketPriceMapper;
import com.train.trainticketserver.mapper.TrainMapper;
import com.train.trainticketserver.mapper.TrainStationMapper;
import com.train.trainticketserver.service.TicketPriceService;
import com.train.trainticketserver.vo.PriceManageVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TicketPriceServiceImpl implements TicketPriceService{

    private final TrainMapper trainMapper;
    private final TrainStationMapper trainStationMapper;
    private final StationMapper stationMapper;
    private final TicketPriceMapper ticketPriceMapper;

    public TicketPriceServiceImpl(
            TrainMapper trainMapper,
            TrainStationMapper trainStationMapper,
            StationMapper stationMapper,
            TicketPriceMapper ticketPriceMapper
    ){

        this.trainMapper=trainMapper;
        this.trainStationMapper=trainStationMapper;
        this.stationMapper=stationMapper;
        this.ticketPriceMapper=ticketPriceMapper;

    }

    @Override
    public List<PriceManageVO> manageList() {
        List<PriceManageVO> result = new ArrayList<>();

        List<Train> trains = trainMapper.selectList(null);
        List<Station> stations = stationMapper.selectList(null);
        List<TicketPrice> prices = ticketPriceMapper.selectList(null);

        Map<Long, Station> stationMap = stations.stream()
                .collect(Collectors.toMap(
                        Station::getStationId,
                        s -> s
                ));

        Map<String, TicketPrice> priceMap = prices.stream()
                .collect(Collectors.toMap(
                        p -> p.getTrainId() + "_" +
                                p.getStartStationId() + "_" +
                                p.getEndStationId() + "_" +
                                p.getSeatTypeId(),
                        p -> p,
                        (a, b) -> a
                ));

        for (Train train : trains) {
            List<TrainStation> trainStations =
                    trainStationMapper.findByTrainId(train.getTrainId());

            for (int i = 0; i < trainStations.size() - 1; i++) {
                for (int j = i + 1; j < trainStations.size(); j++) {

                    TrainStation start = trainStations.get(i);
                    TrainStation end = trainStations.get(j);

                    Station startStation =
                            stationMap.get(start.getStationId());

                    Station endStation =
                            stationMap.get(end.getStationId());

                    if (startStation == null || endStation == null) {
                        continue;
                    }

                    PriceManageVO vo = new PriceManageVO();

                    vo.setTrainId(train.getTrainId());
                    vo.setTrainNo(train.getTrainNo());

                    vo.setStartStationId(start.getStationId());
                    vo.setStartStationName(startStation.getStationName());

                    vo.setEndStationId(end.getStationId());
                    vo.setEndStationName(endStation.getStationName());

                    TicketPrice businessPrice = priceMap.get(
                            train.getTrainId() + "_" +
                                    start.getStationId() + "_" +
                                    end.getStationId() + "_1"
                    );

                    TicketPrice firstPrice = priceMap.get(
                            train.getTrainId() + "_" +
                                    start.getStationId() + "_" +
                                    end.getStationId() + "_2"
                    );

                    TicketPrice secondPrice = priceMap.get(
                            train.getTrainId() + "_" +
                                    start.getStationId() + "_" +
                                    end.getStationId() + "_3"
                    );

                    if (businessPrice != null) {
                        vo.setBusinessPrice(businessPrice.getPrice());
                    }

                    if (firstPrice != null) {
                        vo.setFirstPrice(firstPrice.getPrice());
                    }

                    if (secondPrice != null) {
                        vo.setSecondPrice(secondPrice.getPrice());
                    }

                    result.add(vo);
                }
            }
        }

        return result;
    }
    @Override
    @Transactional
    public void savePrice(PriceManageVO vo) {
        persistPrices(Collections.singletonList(vo), false);
    }

    @Override
    @Transactional
    public void savePrices(List<PriceManageVO> prices) {
        persistPrices(prices, true);
    }

    private void persistPrices(List<PriceManageVO> prices, boolean completeLine) {
        if (prices == null || prices.isEmpty() || prices.get(0) == null
                || prices.get(0).getTrainId() == null) {
            throw invalidPrice("请提供车次和相邻区间票价");
        }
        Long trainId = prices.get(0).getTrainId();
        // Serialize price edits for the same train until the transaction commits.
        if (trainMapper.lockById(trainId) == null) {
            throw invalidPrice("车次不存在");
        }
        List<TrainStation> stations = trainStationMapper.findByTrainId(trainId);
        if (stations.size() < 2) {
            throw invalidPrice("线路至少需要两个车站");
        }
        Map<String, PriceManageVO> submitted = new HashMap<>();
        Set<String> segments = new HashSet<>();
        for (int i = 0; i < stations.size() - 1; i++) {
            segments.add(segmentKey(stations.get(i).getStationId(), stations.get(i + 1).getStationId()));
        }
        for (PriceManageVO price : prices) {
            if (price == null || !trainId.equals(price.getTrainId())) {
                throw invalidPrice("一次只能保存同一车次的票价");
            }
            String key = segmentKey(price.getStartStationId(), price.getEndStationId());
            if (!segments.contains(key) || submitted.putIfAbsent(key, price) != null) {
                throw invalidPrice("票价必须对应线路中不重复的相邻区间");
            }
            validatePrice(price.getBusinessPrice());
            validatePrice(price.getFirstPrice());
            validatePrice(price.getSecondPrice());
        }
        if (completeLine && submitted.size() != stations.size() - 1) {
            throw invalidPrice("请提交线路中全部相邻区间的票价");
        }

        BigDecimal[][] segmentPrices = new BigDecimal[stations.size() - 1][3];
        for (int i = 0; i < stations.size() - 1; i++) {
            Long start = stations.get(i).getStationId();
            Long end = stations.get(i + 1).getStationId();
            PriceManageVO price = submitted.get(segmentKey(start, end));
            for (int seat = 0; seat < 3; seat++) {
                BigDecimal amount = price == null
                        ? ticketPriceMapper.findPrice(trainId, start, end, (long) seat + 1)
                        : switch (seat) {
                            case 0 -> price.getBusinessPrice();
                            case 1 -> price.getFirstPrice();
                            default -> price.getSecondPrice();
                        };
                validatePrice(amount);
                segmentPrices[i][seat] = amount;
            }
        }

        // Rebuild every forward interval from adjacent segments, including zero fares.
        for (int start = 0; start < stations.size() - 1; start++) {
            BigDecimal[] totals = {BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO};
            for (int end = start + 1; end < stations.size(); end++) {
                for (int seat = 0; seat < 3; seat++) {
                    totals[seat] = totals[seat].add(segmentPrices[end - 1][seat]);
                    validatePrice(totals[seat]);
                    saveOne(trainId, stations.get(start).getStationId(),
                            stations.get(end).getStationId(), (long) seat + 1, totals[seat]);
                }
            }
        }
    }

    private String segmentKey(Long start, Long end) {
        return start + "_" + end;
    }

    private void validatePrice(BigDecimal price) {
        if (price == null || price.signum() < 0 || price.stripTrailingZeros().scale() > 0
                || price.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) > 0) {
            throw invalidPrice("各相邻区间票价不能为空，必须为非负整数且不能超过整数存储范围");
        }
    }

    private ResponseStatusException invalidPrice(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private void saveOne(
            Long trainId,
            Long startStationId,
            Long endStationId,
            Long seatTypeId,
            BigDecimal price
    ){
        TicketPrice old = ticketPriceMapper.findOne(trainId,startStationId,endStationId,seatTypeId);
        if(old == null){
            TicketPrice ticketPrice=new TicketPrice();
            ticketPrice.setTrainId(trainId);
            ticketPrice.setStartStationId(startStationId);
            ticketPrice.setEndStationId(endStationId);
            ticketPrice.setSeatTypeId(seatTypeId);
            ticketPrice.setPrice(price);
            ticketPriceMapper.insert(ticketPrice);
        }else{
            old.setPrice(price);
            ticketPriceMapper.updateById(old);

        }

    }
}
