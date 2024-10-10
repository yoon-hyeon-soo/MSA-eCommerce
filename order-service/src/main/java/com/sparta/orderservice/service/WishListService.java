package com.sparta.orderservice.service;

import com.sparta.orderservice.client.ProductFeignClient;
import com.sparta.orderservice.client.UserFeignClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.sparta.orderservice.dto.OrderDto;
import com.sparta.orderservice.dto.OrderItemDto;
import com.sparta.orderservice.dto.ProductDto;
import com.sparta.orderservice.dto.UserResponseDto;
import com.sparta.orderservice.dto.WishListItemDto;
import com.sparta.orderservice.entity.WishList;
import com.sparta.orderservice.entity.WishListItem;
import com.sparta.orderservice.exception.ProductOutOfStockException;
import com.sparta.orderservice.repository.WishListItemRepository;
import com.sparta.orderservice.repository.WishListRepository;
import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;

@Service
public class WishListService {

    private final UserFeignClient userFeignClient;
    private final ProductFeignClient productFeignClient;
    private final WishListRepository wishListRepository;
    private final WishListItemRepository wishListItemRepository;
    private final OrderService orderService;

    public WishListService(UserFeignClient userFeignClient, ProductFeignClient productFeignClient,
                           WishListRepository wishListRepository, WishListItemRepository wishListItemRepository, OrderService orderService
                           ) {
        this.userFeignClient = userFeignClient;
        this.productFeignClient = productFeignClient;
        this.wishListRepository = wishListRepository;
        this.wishListItemRepository = wishListItemRepository;
        this.orderService = orderService;
    }

    // 헤더에서 사용자 ID 추출
    private Long extractUserIdFromRequest(HttpServletRequest request) {
        String userIdHeader = request.getHeader("x-claim-userid");
        if (userIdHeader == null) {
            throw new RuntimeException("헤더에 사용자 ID가 없습니다.");
        }
        try {
            return Long.parseLong(userIdHeader);
        } catch (NumberFormatException e) {
            throw new RuntimeException("헤더에 있는 사용자 ID 형식이 잘못되었습니다.");
        }
    }

    private UserResponseDto getCurrentUser() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Long userId = extractUserIdFromRequest(request);
        return userFeignClient.getUserById(userId);
    }

    // 위시리스트에 항목 추가
    public void addItemToWishList(WishListItemDto wishListItemDto) {
        UserResponseDto user = getCurrentUser();

        // 제품 정보를 FeignClient로부터 조회
        ProductDto product = productFeignClient.getProductById(wishListItemDto.getProductId());
        if (product == null) {
            throw new IllegalArgumentException("제품을 찾을 수 없습니다.");
        }

        // 사용자의 위시리스트 찾기
        WishList wishList = wishListRepository.findByUserId(user.getId()).orElse(null);
        if (wishList == null) {
            // 위시리스트가 없다면 새로 생성
            wishList = new WishList();
            wishList.setUserId(user.getId());
            wishListRepository.save(wishList);
        }

        // WishListItem 생성 및 저장
        WishListItem wishListItem = WishListItem.builder()
                .wishList(wishList)
                .productId(product.getId())
                .quantity(wishListItemDto.getQuantity())
                .build();

        wishListItemRepository.save(wishListItem);
    }

    // 위시리스트 항목 조회
    public List<WishListItemDto> getWishListItems() {
        UserResponseDto user = getCurrentUser();

        WishList wishList = wishListRepository.findByUserId(user.getId()).orElse(null);
        if (wishList == null) {
            return new ArrayList<>();
        }

        List<WishListItem> wishListItems = wishListItemRepository.findAllByWishList(wishList);
        List<WishListItemDto> wishListItemDtos = new ArrayList<>();
        for (WishListItem item : wishListItems) {
            wishListItemDtos.add(new WishListItemDto(
                    item.getWishList().getId(),
                    item.getId(),
                    item.getProductId(),
                    item.getQuantity()
            ));
        }
        return wishListItemDtos;
    }

    // 위시리스트 항목 업데이트
    public void updateWishListItem(Long id, WishListItemDto wishListItemDto) {
        UserResponseDto user = getCurrentUser();
        WishList wishList = wishListRepository.findByUserId(user.getId()).orElseThrow(() -> new IllegalArgumentException("위시리스트를 찾을 수 없습니다."));

        WishListItem wishListItem = wishListItemRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("위시리스트 항목을 찾을 수 없습니다."));

        if (!wishListItem.getWishList().getId().equals(wishList.getId())) {
            throw new IllegalArgumentException("위시리스트 항목이 현재 사용자의 위시리스트에 없습니다.");
        }

        wishListItem.setQuantity(wishListItemDto.getQuantity());
        wishListItemRepository.save(wishListItem);
    }

    // 위시리스트 항목 삭제
    public void deleteWishListItem(Long id) {
        UserResponseDto user = getCurrentUser();
        WishList wishList = wishListRepository.findByUserId(user.getId()).orElseThrow(() -> new IllegalArgumentException("위시리스트를 찾을 수 없습니다."));

        WishListItem wishListItem = wishListItemRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("위시리스트 항목을 찾을 수 없습니다."));

        if (!wishListItem.getWishList().getId().equals(wishList.getId())) {
            throw new IllegalArgumentException("위시리스트 항목이 현재 사용자의 위시리스트에 없습니다.");
        }

        wishListItemRepository.delete(wishListItem);
    }

    // 위시리스트에서 주문 생성
    @Transactional
    public OrderDto createOrderFromWishList(List<Long> wishListItemIds) {
        UserResponseDto user = getCurrentUser();

        WishList wishList = wishListRepository.findByUserId(user.getId()).orElseThrow(() -> new IllegalArgumentException("사용자의 위시리스트가 존재하지 않습니다."));

        List<WishListItem> wishListItems = wishListItemRepository.findAllById(wishListItemIds);
        if (wishListItems.isEmpty()) {
            throw new IllegalArgumentException("선택된 위시리스트 항목이 없습니다.");
        }

        List<OrderItemDto> orderItemDtos = new ArrayList<>();
        for (WishListItem wishListItem : wishListItems) {
            if (!wishListItem.getWishList().getId().equals(wishList.getId())) {
                throw new IllegalArgumentException("위시리스트 항목이 현재 사용자의 위시리스트에 없습니다.");
            }

            ProductDto product = productFeignClient.getProductById(wishListItem.getProductId());

            if (product.getStockQuantity() < wishListItem.getQuantity()) {
                throw new ProductOutOfStockException("주문할 수 없습니다. 제품: " + product.getName() + "의 재고가 부족합니다.");
            }

            OrderItemDto orderItemDto = new OrderItemDto(
                    null, // ID는 생성 시 자동으로 설정
                    product.getId(),
                    wishListItem.getQuantity(),
                    "주문 완료"
            );
            orderItemDtos.add(orderItemDto);

            wishListItemRepository.delete(wishListItem);
        }
// 주문 생성
        try {
            return orderService.createOrder(orderItemDtos);
        } catch (Exception e) {
            // 예외 발생 시 로깅 및 추가적인 처리를 고려할 수 있음
            throw new RuntimeException("주문 생성 실패: " + e.getMessage(), e);
        }
    }
}
