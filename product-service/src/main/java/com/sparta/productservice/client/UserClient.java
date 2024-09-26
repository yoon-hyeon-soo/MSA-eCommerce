package com.sparta.productservice.client;

import com.sparta.productservice.dto.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/api/v1/user/{id}")
    UserResponseDto getUserById(@PathVariable("id") Long id);
}
