package com.sparta.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {

    private Long id;           // 주문 항목 ID
    private Long productId;   // 상품 ID
    private Integer quantity; // 수량
    private String status;    // 주문 항목 상태
}
