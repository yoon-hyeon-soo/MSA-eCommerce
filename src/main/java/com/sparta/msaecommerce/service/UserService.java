package com.sparta.msaecommerce.service;

import com.sparta.msaecommerce.dto.SignupRequestDto;
import com.sparta.msaecommerce.entity.User;
import com.sparta.msaecommerce.jwt.JwtUtil;
import com.sparta.msaecommerce.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public void signup(SignupRequestDto requestDto) {
        String email = requestDto.getEmail();
        String password = passwordEncoder.encode(requestDto.getPassword());

        // email 중복확인
        Optional<User> checkEmail = userRepository.findByEmail(email);
        if (checkEmail.isPresent()) {
            throw new IllegalArgumentException("중복된 Email 입니다.");
        }

        // 사용자 등록
        User user = new User(requestDto.getName(),  requestDto.getPhoneNumber(), requestDto.getAddress(),requestDto.getEmail(),password);
        userRepository.save(user);
    }
}