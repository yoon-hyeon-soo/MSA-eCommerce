package com.sparta.orderservice.client;

import com.sparta.orderservice.dto.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserFeignClient {

    @GetMapping("/api/v1/user/{id}")
    UserResponseDto getUserById(@PathVariable("id") Long id);
}
