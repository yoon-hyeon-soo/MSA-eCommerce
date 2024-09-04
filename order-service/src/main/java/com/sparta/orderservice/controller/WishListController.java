package com.sparta.orderservice.controller;


import com.sparta.orderservice.dto.OrderDto;
import com.sparta.orderservice.dto.WishListItemDto;
import com.sparta.orderservice.service.WishListService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
public class WishListController {

    private final WishListService wishListService;

    public WishListController(WishListService wishListService) {
        this.wishListService = wishListService;
    }

    @PostMapping("/items")
    public ResponseEntity<String> addItemToWishList(@RequestBody WishListItemDto wishListItemDto) {
        try {
            wishListService.addItemToWishList(wishListItemDto);
            return ResponseEntity.ok("위시리스트 항목이 성공적으로 추가되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/items")
    public ResponseEntity<List<WishListItemDto>> getWishListItems() {
        try {
            List<WishListItemDto> wishListItems = wishListService.getWishListItems();
            return ResponseEntity.ok(wishListItems);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<String> updateWishListItem(@PathVariable Long id,
                                                     @RequestBody WishListItemDto wishListItemDto) {
        try {
            wishListService.updateWishListItem(id, wishListItemDto);
            return ResponseEntity.ok("위시리스트 항목이 성공적으로 업데이트되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<String> deleteWishListItem(@PathVariable Long id) {
        try {
            wishListService.deleteWishListItem(id);
            return ResponseEntity.ok("위시리스트 항목이 성공적으로 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 위시리스트 항목을 주문으로 이동
    @PostMapping("/order")
    public ResponseEntity<OrderDto> createOrderFromWishList(
            @RequestBody List<Long> wishListItemIds) {
        try {
            OrderDto orderDto = wishListService.createOrderFromWishList(wishListItemIds);
            return ResponseEntity.ok(orderDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null); // 에러 처리 필요
        }
    }
}
