package com.train.trainticketserver.controller;


import com.train.trainticketserver.entity.TrainSchedule;
import com.train.trainticketserver.service.TrainScheduleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/schedule")
public class TrainScheduleController {

    private final TrainScheduleService trainScheduleService;

    public TrainScheduleController(TrainScheduleService trainScheduleService){

        this.trainScheduleService=trainScheduleService;

    }

    @GetMapping("/{trainId}")
    public List<TrainSchedule> list(@PathVariable Long trainId){

        return trainScheduleService.list(trainId);

    }

    @PutMapping("/{trainId}")
    public void update(@PathVariable Long trainId,@RequestBody List<TrainSchedule> schedules){

        trainScheduleService.update(trainId,schedules);

    }


}