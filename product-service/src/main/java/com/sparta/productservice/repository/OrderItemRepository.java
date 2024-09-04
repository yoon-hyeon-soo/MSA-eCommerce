package com.sparta.productservice.repository;

import com.sparta.productservice.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    // 특정 주문에 속한 주문 항목을 가져오는 메서드
    List<OrderItem> findByOrderId(Long orderId);
}
