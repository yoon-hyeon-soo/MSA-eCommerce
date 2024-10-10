package com.sparta.orderservice.repository;

import com.sparta.orderservice.entity.WishList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WishListRepository extends JpaRepository<WishList, Long> {

    // 사용자 ID로 WishList 조회
    Optional<WishList> findByUserId(Long userId);
}
