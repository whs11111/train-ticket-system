package com.train.trainticketserver.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.train.trainticketserver.dto.UserLoginDTO;
import com.train.trainticketserver.dto.UserRegisterDTO;
import com.train.trainticketserver.entity.User;

public interface UserService extends IService<User> {
    User register(UserRegisterDTO dto);
    User login(UserLoginDTO dto);
    User getUserInfo(Long userId);
    void updateUser(User user);
    void deleteUser(Long userId);
    void adminUpdate(User user);
}