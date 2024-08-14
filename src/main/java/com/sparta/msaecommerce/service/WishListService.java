package com.sparta.msaecommerce.service;

import com.sparta.msaecommerce.dto.OrderDto;
import com.sparta.msaecommerce.dto.OrderItemDto;
import com.sparta.msaecommerce.dto.WishListItemDto;
import com.sparta.msaecommerce.entity.*;
import com.sparta.msaecommerce.repository.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class WishListService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final WishListRepository wishListRepository;
    private final WishListItemRepository wishListItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public WishListService(UserRepository userRepository, ProductRepository productRepository,
                           WishListRepository wishListRepository, WishListItemRepository wishListItemRepository, OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.wishListRepository = wishListRepository;
        this.wishListItemRepository = wishListItemRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    public void addItemToWishList(WishListItemDto wishListItemDto) {
        // 현재 사용자 조회
        User user = getCurrentUser();

        // 제품 조회
        Product product = productRepository.findById(wishListItemDto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("제품을 찾을 수 없습니다."));

        // 사용자의 WishList 조회 또는 새로 생성
        WishList wishList = user.getWishList();
        if (wishList == null) {
            wishList = new WishList();
            wishList.setUser(user);
            wishListRepository.save(wishList);
        }

        // 새로운 항목 추가
        WishListItem wishListItem = WishListItem.builder()
                .wishList(wishList)
                .product(product)
                .quantity(wishListItemDto.getQuantity())
                .build();

        wishListItemRepository.save(wishListItem);
    }

    public List<WishListItemDto> getWishListItems() {
        // 현재 사용자 조회
        User user = getCurrentUser();

        WishList wishList = user.getWishList();
        if (wishList == null) {
            return new ArrayList<>();
        }

        // WishList의 모든 항목 조회
        List<WishListItem> wishListItems = wishListItemRepository.findAllByWishList(wishList);
        List<WishListItemDto> wishListItemDtos = new ArrayList<>();
        for (WishListItem item : wishListItems) {
            wishListItemDtos.add(new WishListItemDto(
                    item.getWishList().getId(),
                    item.getId(),
                    item.getProduct().getId(),
                    item.getQuantity()
            ));
        }
        return wishListItemDtos;
    }

    public void updateWishListItem(Long id, WishListItemDto wishListItemDto) {
        // 현재 사용자 조회
        User user = getCurrentUser();
        WishList wishList = user.getWishList();
        if (wishList == null) {
            throw new IllegalArgumentException("위시리스트를 찾을 수 없습니다.");
        }

        // WishListItem 조회
        WishListItem wishListItem = wishListItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("위시리스트 항목을 찾을 수 없습니다."));

        // 항목이 사용자의 WishList에 포함되어 있는지 확인
        if (!wishListItem.getWishList().equals(wishList)) {
            throw new IllegalArgumentException("위시리스트 항목이 현재 사용자의 위시리스트에 없습니다.");
        }

        // 수량 업데이트
        wishListItem.setQuantity(wishListItemDto.getQuantity());
        wishListItemRepository.save(wishListItem);
    }

    public void deleteWishListItem(Long id) {
        // 현재 사용자 조회
        User user = getCurrentUser();
        WishList wishList = user.getWishList();
        if (wishList == null) {
            throw new IllegalArgumentException("위시리스트를 찾을 수 없습니다.");
        }

        // WishListItem 조회
        WishListItem wishListItem = wishListItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("위시리스트 항목을 찾을 수 없습니다."));

        // 항목이 사용자의 WishList에 포함되어 있는지 확인
        if (!wishListItem.getWishList().equals(wishList)) {
            throw new IllegalArgumentException("위시리스트 항목이 현재 사용자의 위시리스트에 없습니다.");
        }

        // 항목 삭제
        wishListItemRepository.delete(wishListItem);
    }

    public OrderDto createOrderFromWishList(List<Long> wishListItemIds) {
        // 현재 사용자 조회
        User user = getCurrentUser();

        // 새로운 주문 생성
        Order order = Order.builder()
                .user(user)
                .orderDate(LocalDateTime.now())
                .status("주문 완료")
                .build();

        // 주문 항목 리스트
        List<OrderItem> orderItems = new ArrayList<>();

        for (Long wishListItemId : wishListItemIds) {
            WishListItem wishListItem = wishListItemRepository.findById(wishListItemId)
                    .orElseThrow(() -> new IllegalArgumentException("위시리스트 항목을 찾을 수 없습니다: " + wishListItemId));

            Product product = wishListItem.getProduct();

            // 새로운 주문 항목 생성
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(wishListItem.getQuantity())
                    .status("주문 완료")
                    .build();

            orderItems.add(orderItem);

            // 위시리스트 항목 삭제
            wishListItemRepository.delete(wishListItem);
        }

        // 주문과 주문 항목 저장
        order.setOrderItems(orderItems);
        orderRepository.save(order);
        orderItemRepository.saveAll(orderItems);

        // 주문 DTO 생성
        List<OrderItemDto> orderItemDtos = new ArrayList<>();
        for (OrderItem item : orderItems) {
            orderItemDtos.add(new OrderItemDto(
                    item.getId(),
                    item.getProduct().getId(),
                    item.getQuantity(),
                    item.getStatus()
            ));
        }

        return new OrderDto(order.getId(), order.getStatus(), orderItemDtos);
    }
}