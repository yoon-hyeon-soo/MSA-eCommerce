package com.sparta.msaecommerce.repository;

import com.sparta.msaecommerce.entity.User;
import com.sparta.msaecommerce.entity.WishList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WishListRepository extends JpaRepository<WishList, Long> {

    // 사용자에 대한 WishList 를 조회
    Optional<WishList> findByUser(User user);
}
