package com.train.trainticketserver.service;


import com.train.trainticketserver.entity.TrainSchedule;

import java.util.List;


public interface TrainScheduleService {

    List<TrainSchedule> list(Long trainId);
    void update(Long trainId,List<TrainSchedule> schedules);

}