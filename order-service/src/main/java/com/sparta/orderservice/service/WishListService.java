package com.sparta.orderservice.service;

import com.sparta.orderservice.dto.OrderDto;
import com.sparta.orderservice.dto.OrderItemDto;
import com.sparta.orderservice.dto.WishListItemDto;
import com.sparta.orderservice.entity.Product;
import com.sparta.orderservice.entity.User;
import com.sparta.orderservice.entity.WishList;
import com.sparta.orderservice.entity.WishListItem;
import com.sparta.orderservice.exception.ProductOutOfStockException;
import com.sparta.orderservice.repository.ProductRepository;
import com.sparta.orderservice.repository.UserRepository;
import com.sparta.orderservice.repository.WishListItemRepository;
import com.sparta.orderservice.repository.WishListRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


import java.util.ArrayList;
import java.util.List;

@Service
public class WishListService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final WishListRepository wishListRepository;
    private final WishListItemRepository wishListItemRepository;
    private final OrderService orderService;

    public WishListService(UserRepository userRepository, ProductRepository productRepository,
                           WishListRepository wishListRepository, WishListItemRepository wishListItemRepository, OrderService orderService) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.wishListRepository = wishListRepository;
        this.wishListItemRepository = wishListItemRepository;
        this.orderService = orderService;
    }

    private int getCurrentUserId() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String userIdHeader = request.getHeader("x-claim-userid");
        if (userIdHeader == null) {
            throw new RuntimeException("헤더에 사용자 ID가 없습니다.");
        }

        try {
            return Integer.parseInt(userIdHeader);
        } catch (NumberFormatException e) {
            throw new RuntimeException("헤더에 있는 사용자 ID 형식이 잘못되었습니다.");
        }
    }

    private User getCurrentUser() {
        int userId = getCurrentUserId();
        return userRepository.findById((long) userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    public void addItemToWishList(WishListItemDto wishListItemDto) {
        User user = getCurrentUser();

        Product product = productRepository.findById(wishListItemDto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("제품을 찾을 수 없습니다."));

        WishList wishList = user.getWishList();
        if (wishList == null) {
            wishList = new WishList();
            wishList.setUser(user);
            wishListRepository.save(wishList);
        }

        WishListItem wishListItem = WishListItem.builder()
                .wishList(wishList)
                .product(product)
                .quantity(wishListItemDto.getQuantity())
                .build();

        wishListItemRepository.save(wishListItem);
    }

    public List<WishListItemDto> getWishListItems() {
        User user = getCurrentUser();

        WishList wishList = user.getWishList();
        if (wishList == null) {
            return new ArrayList<>();
        }

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
        User user = getCurrentUser();
        WishList wishList = user.getWishList();
        if (wishList == null) {
            throw new IllegalArgumentException("위시리스트를 찾을 수 없습니다.");
        }

        WishListItem wishListItem = wishListItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("위시리스트 항목을 찾을 수 없습니다."));

        if (!wishListItem.getWishList().equals(wishList)) {
            throw new IllegalArgumentException("위시리스트 항목이 현재 사용자의 위시리스트에 없습니다.");
        }

        wishListItem.setQuantity(wishListItemDto.getQuantity());
        wishListItemRepository.save(wishListItem);
    }

    public void deleteWishListItem(Long id) {
        User user = getCurrentUser();
        WishList wishList = user.getWishList();
        if (wishList == null) {
            throw new IllegalArgumentException("위시리스트를 찾을 수 없습니다.");
        }

        WishListItem wishListItem = wishListItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("위시리스트 항목을 찾을 수 없습니다."));

        if (!wishListItem.getWishList().equals(wishList)) {
            throw new IllegalArgumentException("위시리스트 항목이 현재 사용자의 위시리스트에 없습니다.");
        }

        wishListItemRepository.delete(wishListItem);
    }

    @Transactional
    public OrderDto createOrderFromWishList(List<Long> wishListItemIds) {
        User user = getCurrentUser(); // 현재 사용자 정보 확인

        WishList wishList = user.getWishList();
        if (wishList == null) {
            throw new IllegalArgumentException("사용자의 위시리스트가 존재하지 않습니다.");
        }

        List<WishListItem> wishListItems = wishListItemRepository.findAllById(wishListItemIds);

        if (wishListItems.isEmpty()) {
            throw new IllegalArgumentException("선택된 위시리스트 항목이 없습니다.");
        }

        List<OrderItemDto> orderItemDtos = new ArrayList<>();
        for (WishListItem wishListItem : wishListItems) {
            if (!wishListItem.getWishList().equals(wishList)) {
                throw new IllegalArgumentException("위시리스트 항목이 현재 사용자의 위시리스트에 없습니다.");
            }

            Product product = wishListItem.getProduct();

            // 재고 감소 로직 호출
            try {
                product.decreaseStock(wishListItem.getQuantity());  // 재고 감소
            } catch (IllegalArgumentException e) {
                // 재고 부족 예외 발생 시 처리
                throw new ProductOutOfStockException("주문할 수 없습니다. 제품: " + product.getName() + "의 재고가 부족합니다.");
            }
            productRepository.save(product); // 변경된 재고 저장

            OrderItemDto orderItemDto = new OrderItemDto(
                    null, // id는 null로 설정, 생성 시 자동으로 생성됨
                    product.getId(),
                    wishListItem.getQuantity(),
                    "주문 완료"
            );
            orderItemDtos.add(orderItemDto);

            // 위시리스트 항목 삭제
            wishListItemRepository.delete(wishListItem);
        }

        // 주문 생성
        return orderService.createOrder(orderItemDtos);
    }

}
