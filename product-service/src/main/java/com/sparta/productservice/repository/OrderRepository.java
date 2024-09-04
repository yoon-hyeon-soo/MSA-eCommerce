package com.sparta.productservice.repository;

import com.sparta.productservice.entity.Order;
import com.sparta.productservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // 사용자의 주문 목록을 가져오는 메서드
    List<Order> findByUserId(Long userId);

    // 특정 상태의 주문 목록을 가져오는 메서드 (필요시)
    List<Order> findByStatus(String status);

    List<Order> findByUser(User user);
}
