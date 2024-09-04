package com.sparta.userservice.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class SignupRequestDto {
    private String name;
    private String password;
    private String email;
    private String phoneNumber;
    private String address;
}
