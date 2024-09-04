package com.sparta.userservice.service;


import com.sparta.userservice.dto.LoginRequestDto;
import com.sparta.userservice.dto.SignupRequestDto;
import com.sparta.userservice.entity.User;

public interface UserService {
    User signup(SignupRequestDto createUserRequestDto);

    String login(LoginRequestDto userCommonDto);
}