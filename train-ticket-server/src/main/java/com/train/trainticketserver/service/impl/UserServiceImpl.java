package com.train.trainticketserver.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.train.trainticketserver.dto.UserLoginDTO;
import com.train.trainticketserver.dto.UserRegisterDTO;
import com.train.trainticketserver.entity.User;
import com.train.trainticketserver.mapper.UserMapper;
import com.train.trainticketserver.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;


    public UserServiceImpl(UserMapper userMapper) {

        this.userMapper = userMapper;

    }

    @Override
    public User register(UserRegisterDTO dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setName(dto.getName());
        user.setPhone(dto.getPhone());
        user.setIdnum(dto.getIdnum());
        user.setRole("USER");
        save(user);
        return user;
    }
    @Override
    public User login(UserLoginDTO dto) {
        User user = lambdaQuery()
                .eq(User::getUsername, dto.getUsername())
                .one();
        if (user == null) { throw new RuntimeException("用户不存在");}
        if (!user.getPassword().equals(dto.getPassword())) {throw new RuntimeException("密码错误");}
        return user;
    }

    @Override
    public User getUserInfo(Long userId) {

        return userMapper.findById(userId);

    }

    @Override
    public void updateUser(User user) {

        User updateUser = new User();
        updateUser.setUserId(user.getUserId());
        updateUser.setName(user.getName());
        updateUser.setPhone(user.getPhone());
        updateUser.setIdnum(user.getIdnum());
        userMapper.updateById(updateUser);

    }

    @Override
    public void deleteUser(Long userId) {

        userMapper.deleteById(userId);

    }

    @Override
    public void adminUpdate(User user) {
        User oldUser = getById(user.getUserId());
        if (oldUser == null) {
            throw new RuntimeException("用户不存在");
        }
        User exist = lambdaQuery()
                .eq(User::getUsername, user.getUsername())
                .one();
        if (exist != null && !exist.getUserId().equals(user.getUserId())) {
            throw new RuntimeException("用户名已存在");
        }
        User updateUser = new User();
        updateUser.setUserId(user.getUserId());
        updateUser.setUsername(user.getUsername());
        updateUser.setName(user.getName());
        updateUser.setPhone(user.getPhone());
        updateUser.setIdnum(user.getIdnum());
        updateUser.setRole(user.getRole());
        userMapper.updateById(updateUser);
    }
}
