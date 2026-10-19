package com.train.trainticketserver.controller;

import com.train.trainticketserver.dto.TrainAddDTO;
import com.train.trainticketserver.dto.TrainStationDTO;
import com.train.trainticketserver.dto.TrainUpdateDTO;
import com.train.trainticketserver.service.TrainService;
import com.train.trainticketserver.vo.TrainStationVO;
import com.train.trainticketserver.vo.TrainVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/train")
public class TrainController {

    private final TrainService trainService;

    public TrainController(TrainService trainService) {
        this.trainService = trainService;
    }

    @PostMapping("/add")
    public void addTrain(@RequestBody TrainAddDTO trainAddDTO) {

        trainService.addTrain(trainAddDTO);

    }

    @GetMapping("/list")
    public List<TrainVO> listTrain(){

        return trainService.listTrain();

    }

    @PutMapping("/update")
    public void updateTrain(@RequestBody TrainUpdateDTO trainUpdateDTO){

        trainService.updateTrain(trainUpdateDTO);

    }

    @DeleteMapping("/delete/{trainId}")
    public void deleteTrain(@PathVariable Long trainId){

        trainService.deleteTrain(trainId);

    }

    @GetMapping("/{trainId}/stations")
    public List<TrainStationVO> getTrainStations(@PathVariable Long trainId){

        return trainService.getTrainStations(trainId);

    }

    @PutMapping("/{trainId}/stations")
    public void updateTrainStations(@PathVariable Long trainId,@RequestBody List<TrainStationDTO> list){

        trainService.updateTrainStations(trainId,list);

    }

}
