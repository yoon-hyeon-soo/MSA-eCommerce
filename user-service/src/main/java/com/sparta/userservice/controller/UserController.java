package com.sparta.userservice.controller;

import com.sparta.userservice.dto.LoginRequestDto;
import com.sparta.userservice.dto.SignupRequestDto;
import com.sparta.userservice.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<String> createUser(@RequestBody SignupRequestDto signupRequestDto) throws BadRequestException {
        // 회원가입 처리
        userService.signup(signupRequestDto);

        // 응답 생성
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("회원가입 성공");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequestDto loginRequestDto, HttpServletResponse response) throws BadRequestException {
        // 로그인 처리
        String token = userService.login(loginRequestDto);

        // 응답 생성
        return ResponseEntity.ok(token);
    }
}
