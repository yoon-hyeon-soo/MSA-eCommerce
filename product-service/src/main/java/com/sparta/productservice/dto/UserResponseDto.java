package com.sparta.productservice.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponseDto {
    private Long id;
    private String username;
    private String email;
    // 필요한 필드 추가
}

