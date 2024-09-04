package com.sparta.productservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequestDto {
    private String name;
    private String password;
    private String email;
    private String phoneNumber;
    private String address;
}
