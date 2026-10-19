package com.train.trainticketserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.train.trainticketserver.dto.TrainAddDTO;
import com.train.trainticketserver.dto.TrainStationDTO;
import com.train.trainticketserver.dto.TrainUpdateDTO;
import com.train.trainticketserver.entity.*;
import com.train.trainticketserver.mapper.*;
import com.train.trainticketserver.service.TrainService;
import com.train.trainticketserver.vo.TrainStationVO;
import com.train.trainticketserver.vo.TrainVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrainServiceImpl implements TrainService {

    private final TrainStationMapper trainStationMapper;
    private final TrainMapper trainMapper;
    private final TrainScheduleMapper trainScheduleMapper;
    private final SeatOccupancyMapper seatOccupancyMapper;
    private final CoachMapper coachMapper;
    private final SeatMapper seatMapper;
    private final TicketPriceMapper ticketPriceMapper;

    public TrainServiceImpl(
            TrainMapper trainMapper,
            TrainStationMapper trainStationMapper,
            TrainScheduleMapper trainScheduleMapper,
            SeatOccupancyMapper seatOccupancyMapper,
            CoachMapper coachMapper,
            SeatMapper seatMapper,
            TicketPriceMapper ticketPriceMapper

    ) {
        this.trainMapper = trainMapper;
        this.trainStationMapper = trainStationMapper;
        this.trainScheduleMapper = trainScheduleMapper;
        this.seatOccupancyMapper = seatOccupancyMapper;
        this.coachMapper = coachMapper;
        this.seatMapper = seatMapper;
        this.ticketPriceMapper = ticketPriceMapper;
    }

    @Override
    public List<TrainStationVO> getTrainStations(Long trainId) {

        List<TrainStationVO> list =
                trainStationMapper.findStationListByTrainId(trainId);

        for (int i = 0; i < list.size() - 1; i++) {

            TrainStationVO start = list.get(i);
            TrainStationVO end = list.get(i + 1);

            start.setBusinessPrice(
                    ticketPriceMapper.findPrice(
                            trainId,
                            start.getStationId(),
                            end.getStationId(),
                            1L
                    )
            );

            start.setFirstPrice(
                    ticketPriceMapper.findPrice(
                            trainId,
                            start.getStationId(),
                            end.getStationId(),
                            2L
                    )
            );

            start.setSecondPrice(
                    ticketPriceMapper.findPrice(
                            trainId,
                            start.getStationId(),
                            end.getStationId(),
                            3L
                    )
            );
        }

        return list;
    }
    @Override
    @Transactional
    public void updateTrainStations(Long trainId,List<TrainStationDTO> list) {
        trainStationMapper.deleteByTrainId(trainId);
        for(TrainStationDTO dto : list){
            TrainStation trainStation = new TrainStation();
            trainStation.setTrainId(trainId);
            trainStation.setStationId(dto.getStationId());
            trainStation.setArrivalTime(dto.getArrivalTime());
            trainStation.setDepartureTime(dto.getDepartureTime());
            trainStation.setStopOrder(dto.getStopOrder());
            trainStationMapper.insert(trainStation);
        }
        if(!list.isEmpty()){
            Long startStationId = list.get(0).getStationId();
            Long endStationId = list.get(list.size()-1).getStationId();
            trainMapper.updateStation(trainId,startStationId,endStationId);
        }
    }

    @Override
    @Transactional
    public Train addTrain(TrainAddDTO dto) {
        System.out.println("商务座数量：" + dto.getBusinessCount());
        System.out.println("一等座数量：" + dto.getFirstCount());
        System.out.println("二等座数量：" + dto.getSecondCount());

        Train train = new Train();
        train.setTrainNo(dto.getTrainNo());
        train.setTrainType(dto.getTrainType());
        trainMapper.insert(train);
        Long trainId = train.getTrainId();
        int coachNo = 1;
        coachNo = generateCoaches(trainId,coachNo,dto.getBusinessCount(),1L);
        coachNo = generateCoaches(trainId,coachNo,dto.getFirstCount(),2L);
        generateCoaches(trainId,coachNo,dto.getSecondCount(),3L);
        return train;
    }

    private int generateCoaches(Long trainId,int startCoachNo,Integer count,Long seatTypeId) {
        if (count == null || count <= 0) {return startCoachNo;}
        int coachNo = startCoachNo;
        String[] seatColumns = {"A", "B", "C", "E", "F"};
        for (int i = 0; i < count; i++) {
            Coach coach = new Coach();
            coach.setTrainId(trainId);
            coach.setCoachNo(coachNo);
            coachMapper.insert(coach);
            Long coachId = coach.getCoachId();
            for (int seatRow = 1; seatRow <= 10; seatRow++) {
                for (String column : seatColumns) {
                    Seat seat = new Seat();
                    seat.setCoachId(coachId);
                    seat.setSeatNo(String.format("%02d%s", seatRow, column));
                    seat.setSeatTypeId(seatTypeId);
                    seatMapper.insert(seat);
                }
            }
            coachNo++;
        }
        return coachNo;
    }

    @Override
    public List<TrainVO> listTrain() {

        List<TrainVO> list = trainMapper.selectTrainList();

        for(TrainVO vo:list){
            vo.setScheduleList(
                    trainScheduleMapper.selectList(
                            new LambdaQueryWrapper<TrainSchedule>()
                                    .eq(TrainSchedule::getTrainId,vo.getTrainId())
                                    .orderByAsc(TrainSchedule::getRunDate)
                    )
            );

        }
        return list;

    }

    @Override
    public void updateTrain(TrainUpdateDTO dto) {

        Train train = new Train();
        train.setTrainId(dto.getTrainId());
        train.setTrainNo(dto.getTrainNo());
        train.setTrainType(dto.getTrainType());
        trainMapper.updateById(train);

    }


    @Override
    @Transactional
    public void deleteTrain(Long trainId) {

        List<TrainSchedule> schedules = trainScheduleMapper.selectList(
                new LambdaQueryWrapper<TrainSchedule>()
                        .eq(TrainSchedule::getTrainId, trainId)
        );

        for (TrainSchedule schedule : schedules) {

            Long count = seatOccupancyMapper.selectCount(
                    new LambdaQueryWrapper<SeatOccupancy>()
                            .eq(
                                    SeatOccupancy::getScheduleId,
                                    schedule.getScheduleId()
                            )
            );

            if (count > 0) {
                throw new RuntimeException("该车次已有售票记录，不能删除");
            }
        }

        ticketPriceMapper.delete(
                new LambdaQueryWrapper<TicketPrice>()
                        .eq(TicketPrice::getTrainId, trainId)
        );

        for (TrainSchedule schedule : schedules) {

            seatOccupancyMapper.delete(
                    new LambdaQueryWrapper<SeatOccupancy>()
                            .eq(
                                    SeatOccupancy::getScheduleId,
                                    schedule.getScheduleId()
                            )
            );
        }

        trainStationMapper.delete(
                new LambdaQueryWrapper<TrainStation>()
                        .eq(TrainStation::getTrainId, trainId)
        );

        List<Coach> coaches = coachMapper.selectList(
                new LambdaQueryWrapper<Coach>()
                        .eq(Coach::getTrainId, trainId)
        );

        for (Coach coach : coaches) {

            seatMapper.delete(
                    new LambdaQueryWrapper<Seat>()
                            .eq(Seat::getCoachId, coach.getCoachId())
            );
        }

        coachMapper.delete(
                new LambdaQueryWrapper<Coach>()
                        .eq(Coach::getTrainId, trainId)
        );

        trainScheduleMapper.delete(
                new LambdaQueryWrapper<TrainSchedule>()
                        .eq(TrainSchedule::getTrainId, trainId)
        );

        trainMapper.deleteById(trainId);
    }

}