package com.sparta.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WishListItemDto {

    private Long wishListId; // 위시리스트 ID
    private Long wishListItemId; // 위시리스트 항목 ID
    private Long productId; // 상품 ID
    private Integer quantity; // 수량
}
