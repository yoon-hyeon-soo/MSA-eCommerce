package com.sparta.userservice.service;


import com.sparta.userservice.dto.LoginRequestDto;
import com.sparta.userservice.dto.SignupRequestDto;
import com.sparta.userservice.entity.User;

import java.util.Optional;

public interface UserService {
    User signup(SignupRequestDto createUserRequestDto);

    String login(LoginRequestDto userCommonDto);

    Optional<User> findById(Long id); // findById 메서드 추가
}