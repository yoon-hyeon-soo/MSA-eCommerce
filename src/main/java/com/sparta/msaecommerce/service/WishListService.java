package com.sparta.msaecommerce.service;

import com.sparta.msaecommerce.dto.WishListItemDto;
import com.sparta.msaecommerce.entity.Product;
import com.sparta.msaecommerce.entity.User;
import com.sparta.msaecommerce.entity.WishList;
import com.sparta.msaecommerce.entity.WishListItem;
import com.sparta.msaecommerce.repository.ProductRepository;
import com.sparta.msaecommerce.repository.UserRepository;
import com.sparta.msaecommerce.repository.WishListItemRepository;
import com.sparta.msaecommerce.repository.WishListRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class WishListService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final WishListRepository wishListRepository;
    private final WishListItemRepository wishListItemRepository;

    public WishListService(UserRepository userRepository, ProductRepository productRepository,
                           WishListRepository wishListRepository, WishListItemRepository wishListItemRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.wishListRepository = wishListRepository;
        this.wishListItemRepository = wishListItemRepository;
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
}
