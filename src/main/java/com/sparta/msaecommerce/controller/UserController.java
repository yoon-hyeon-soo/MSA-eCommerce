package com.sparta.msaecommerce.controller;

import com.sparta.msaecommerce.dto.LoginRequestDto;
import com.sparta.msaecommerce.dto.SignupRequestDto;
import com.sparta.msaecommerce.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody SignupRequestDto requestDto) {
        try {
            userService.signup(requestDto);
            return ResponseEntity.ok("회원가입 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequestDto loginRequestDto) {
        // JwtAuthenticationFilter가 로그인 요청을 처리하고 JWT를 생성
        // 클라이언트는 JWT를 응답에서 쿠키 또는 헤더를 통해 받게 됩니다.
        // 로그인 요청 처리 후 성공적으로 JWT가 설정되면, 이 메서드는 단순히 로그인 성공 메시지를 반환
        return ResponseEntity.ok("로그인 성공");
    }
}
