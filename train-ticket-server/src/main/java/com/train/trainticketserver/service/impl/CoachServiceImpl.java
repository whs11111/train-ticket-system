package com.train.trainticketserver.service.impl;

import com.train.trainticketserver.mapper.CoachMapper;
import com.train.trainticketserver.service.CoachService;
import com.train.trainticketserver.vo.CoachConfigVO;
import org.springframework.stereotype.Service;

@Service
public class CoachServiceImpl implements CoachService {

    private final CoachMapper coachMapper;

    public CoachServiceImpl(CoachMapper coachMapper) {
        this.coachMapper = coachMapper;
    }

    @Override
    public CoachConfigVO getConfig(Long trainId) {

        CoachConfigVO vo = coachMapper.findConfig(trainId);

        if (vo == null) {
            vo = new CoachConfigVO();
            vo.setBusinessCount(0);
            vo.setFirstCount(0);
            vo.setSecondCount(0);
        }

        int businessCount = vo.getBusinessCount() == null ? 0 : vo.getBusinessCount();
        int firstCount = vo.getFirstCount() == null ? 0 : vo.getFirstCount();
        int secondCount = vo.getSecondCount() == null ? 0 : vo.getSecondCount();

        vo.setBusinessCount(businessCount);
        vo.setFirstCount(firstCount);
        vo.setSecondCount(secondCount);

        int totalCoachCount = businessCount + firstCount + secondCount;

        vo.setTotalCoachCount(totalCoachCount);
        vo.setTotalSeatCount(totalCoachCount * 50);

        return vo;
    }
}