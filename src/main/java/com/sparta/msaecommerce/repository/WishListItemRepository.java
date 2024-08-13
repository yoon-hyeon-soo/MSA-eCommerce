package com.sparta.msaecommerce.repository;

import com.sparta.msaecommerce.entity.WishList;
import com.sparta.msaecommerce.entity.WishListItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishListItemRepository extends JpaRepository<WishListItem, Long> {

    // 특정 WishList 와 Product 에 대한 항목을 조회 (ProductId를 사용)
    Optional<WishListItem> findByWishListAndProductId(WishList wishList, Long productId);

    // 특정 WishList 의 모든 항목을 조회
    List<WishListItem> findAllByWishList(WishList wishList);
}
