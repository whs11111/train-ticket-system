package com.train.trainticketserver.controller;

import com.train.trainticketserver.service.CoachService;
import com.train.trainticketserver.vo.CoachConfigVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/coach")
public class CoachController {

    private final CoachService coachService;

    public CoachController(CoachService coachService) {
        this.coachService = coachService;
    }

    @GetMapping("/config")
    public CoachConfigVO getConfig(@RequestParam Long trainId) {
        return coachService.getConfig(trainId);
    }
}