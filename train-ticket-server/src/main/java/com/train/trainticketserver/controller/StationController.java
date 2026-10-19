package com.train.trainticketserver.controller;

import com.train.trainticketserver.entity.Station;
import com.train.trainticketserver.service.StationService;
import com.train.trainticketserver.vo.StationSearchVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/station")
public class StationController {

    private final StationService stationService;

    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    @GetMapping("/list")
    public List<Station> list() {
        return stationService.list();
    }

    @PostMapping("/add")
    public void add(@RequestBody Station station) {
        stationService.add(station);
    }

    @PutMapping("/update")
    public void update(@RequestBody Station station) {
        stationService.update(station);
    }

    @DeleteMapping("/delete/{stationId}")
    public void delete(@PathVariable Long stationId) {
        stationService.delete(stationId);
    }

    @GetMapping("/search")
    public List<StationSearchVO> search(@RequestParam String keyword) {
        return stationService.search(keyword);
    }

}