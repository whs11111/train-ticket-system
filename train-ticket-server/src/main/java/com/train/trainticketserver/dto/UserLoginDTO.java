package com.train.trainticketserver.dto;

import lombok.Data;

@Data
public class UserLoginDTO {
    private String username;
    private String password;
}