package com.sparta.msaecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {

    private Long orderId;              // 주문 ID
    private String status;             // 주문 상태
    private List<OrderItemDto> orderItems; // 주문 항목 리스트
}
