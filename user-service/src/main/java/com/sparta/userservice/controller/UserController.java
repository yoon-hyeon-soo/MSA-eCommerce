package com.sparta.userservice.controller;

import com.sparta.userservice.dto.LoginRequestDto;
import com.sparta.userservice.dto.SignupRequestDto;
import com.sparta.userservice.dto.UserResponseDto;
import com.sparta.userservice.entity.User;
import com.sparta.userservice.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

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
    // 사용자 정보 조회 메서드 추가
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        Optional<User> userOptional = userService.findById(id); // UserService에 findById 메서드 추가 필요
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            UserResponseDto userResponseDto = new UserResponseDto(user.getId(), user.getUsername(), user.getEmail());
            return ResponseEntity.ok(userResponseDto);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

}
