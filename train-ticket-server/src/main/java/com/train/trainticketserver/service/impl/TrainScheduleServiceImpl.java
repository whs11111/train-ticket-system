package com.train.trainticketserver.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.train.trainticketserver.entity.SeatOccupancy;
import com.train.trainticketserver.entity.TrainSchedule;
import com.train.trainticketserver.mapper.SeatOccupancyMapper;
import com.train.trainticketserver.mapper.TrainScheduleMapper;
import com.train.trainticketserver.service.TrainScheduleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TrainScheduleServiceImpl implements TrainScheduleService {

    private final TrainScheduleMapper trainScheduleMapper;
    private final SeatOccupancyMapper seatOccupancyMapper;

    public TrainScheduleServiceImpl(
            TrainScheduleMapper trainScheduleMapper,
            SeatOccupancyMapper seatOccupancyMapper
    ) {
        this.trainScheduleMapper = trainScheduleMapper;
        this.seatOccupancyMapper = seatOccupancyMapper;
    }

    @Override
    public List<TrainSchedule> list(Long trainId) {

        return trainScheduleMapper.selectList(
                new LambdaQueryWrapper<TrainSchedule>().eq(TrainSchedule::getTrainId, trainId).orderByAsc(TrainSchedule::getRunDate)
        );

    }

    @Override
    @Transactional
    public void update(Long trainId, List<TrainSchedule> newSchedules) {
        List<TrainSchedule> oldSchedules =
                trainScheduleMapper.selectList(
                        new LambdaQueryWrapper<TrainSchedule>().
                                eq(TrainSchedule::getTrainId, trainId));
        Set<LocalDate> oldDates = new HashSet<>();
        for (TrainSchedule schedule : oldSchedules) {
            oldDates.add(schedule.getRunDate());}
        Set<LocalDate> newDates = new HashSet<>();
        for (TrainSchedule schedule : newSchedules) {
            newDates.add(schedule.getRunDate());
        }
        for (TrainSchedule schedule : oldSchedules) {
            if (!newDates.contains(schedule.getRunDate())) {
                Long count = seatOccupancyMapper.selectCount(
                        new LambdaQueryWrapper<SeatOccupancy>().
                                eq(SeatOccupancy::getScheduleId, schedule.getScheduleId()));
                if (count > 0) {
                    throw new RuntimeException("该开行日期已生成座位，不能删除");
                }
                trainScheduleMapper.deleteById(schedule.getScheduleId());
            }
        }
        for (LocalDate date : newDates) {
            if (!oldDates.contains(date)) {
                TrainSchedule schedule = new TrainSchedule();
                schedule.setTrainId(trainId);
                schedule.setRunDate(date);
                trainScheduleMapper.insert(schedule);
            }
        }
    }
}