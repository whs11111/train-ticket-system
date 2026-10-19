package com.train.trainticketserver.service;

import com.train.trainticketserver.vo.CoachConfigVO;

public interface CoachService {

    CoachConfigVO getConfig(Long trainId);
}
