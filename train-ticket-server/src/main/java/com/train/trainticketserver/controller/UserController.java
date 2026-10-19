package com.train.trainticketserver.controller;

import com.train.trainticketserver.dto.UserLoginDTO;
import com.train.trainticketserver.dto.UserRegisterDTO;
import com.train.trainticketserver.entity.User;
import com.train.trainticketserver.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/list")
    public List<User> list() {
        return userService.list();
    }

    @PostMapping("/register")
    public User register(@RequestBody UserRegisterDTO dto) {
        return userService.register(dto);
    }

    @PostMapping("/login")
    public User login(@RequestBody UserLoginDTO dto) {
        return userService.login(dto);
    }

    @GetMapping("/info")
    public User getUserInfo(@RequestParam Long userId) {
        return userService.getUserInfo(userId);
    }

    @PutMapping("/update")
    public void updateUser(@RequestBody User user) {
        userService.updateUser(user);
    }

    @DeleteMapping("/admin/{userId}")
    public void delete(@PathVariable Long userId){
        userService.deleteUser(userId);
    }

    @PutMapping("/admin/update")
    public void adminUpdate(@RequestBody User user){
        userService.adminUpdate(user);
    }

    @PostMapping("/logout")
    public void logout(){

    }
}