package com.train.trainticketserver.service;

import com.train.trainticketserver.dto.TrainAddDTO;
import com.train.trainticketserver.dto.TrainStationDTO;
import com.train.trainticketserver.dto.TrainUpdateDTO;
import com.train.trainticketserver.entity.Train;
import com.train.trainticketserver.vo.TrainStationVO;
import com.train.trainticketserver.vo.TrainVO;

import java.util.List;

public interface TrainService {

    Train addTrain(TrainAddDTO trainAddDTO);
    List<TrainVO> listTrain();
    void updateTrain(TrainUpdateDTO trainUpdateDTO);
    void deleteTrain(Long trainId);
    List<TrainStationVO> getTrainStations(Long trainId);
    void updateTrainStations(Long trainId,List<TrainStationDTO> list);

}