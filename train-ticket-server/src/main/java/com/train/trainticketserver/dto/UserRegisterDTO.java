package com.train.trainticketserver.dto;
import lombok.Data;

@Data
public class UserRegisterDTO {

    private String username;

    private String password;

    private String name;

    private String phone;

    private String idnum;

}