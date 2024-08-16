package com.sparta.msaecommerce.service;

import com.sparta.msaecommerce.dto.OrderDto;
import com.sparta.msaecommerce.dto.OrderItemDto;
import com.sparta.msaecommerce.entity.*;
import com.sparta.msaecommerce.repository.OrderItemRepository;
import com.sparta.msaecommerce.repository.OrderRepository;
import com.sparta.msaecommerce.repository.ProductRepository;
import com.sparta.msaecommerce.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderService(UserRepository userRepository, ProductRepository productRepository,
                        OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
    public List<OrderDto> getUserOrders() {
        User user = getCurrentUser();  // 현재 로그인된 유저를 가져옵니다.
        List<Order> orders = orderRepository.findByUser(user);  // 해당 유저의 주문 목록을 조회합니다.

        // 각 주문을 OrderDto로 변환하여 반환합니다.
        return orders.stream()
                .map(this::createOrderDto)
                .collect(Collectors.toList());
    }

    public OrderDto createOrder(List<OrderItemDto> orderItemDtos) {
        User user = getCurrentUser();

        Order order = Order.builder()
                .user(user)
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.주문완료.name())
                .build();

        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemDto dto : orderItemDtos) {
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("제품을 찾을 수 없습니다: " + dto.getProductId()));

            // DTO로 입력받은 수량에 따라 재고 감소
            product.decreaseStock(dto.getQuantity());

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(dto.getQuantity())
                    .status(OrderStatus.주문완료.name())
                    .build();

            // 주문 항목 추가
            orderItems.add(orderItem);

            // 재고가 변경된 상품 저장
            productRepository.save(product);
        }

        // 주문 및 주문 항목 저장
        order.setOrderItems(orderItems);
        orderRepository.save(order);
        orderItemRepository.saveAll(orderItems);

        return createOrderDto(order);
    }

    public void cancelOrder(Long orderId) {

        User currentUser = getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));


        // 현재 사용자가 주문의 소유자인지 확인
        if (!order.getUser().equals(currentUser)) {
            throw new IllegalArgumentException("주문 취소 권한이 없습니다.");
        }

        // 배송 중이 아닌 경우에만 주문 취소 가능
        if (order.getStatus().equals(OrderStatus.배송중.name()) || order.getStatus().equals(OrderStatus.배송완료.name())) {
            throw new IllegalArgumentException("주문 취소가 불가능합니다.");
        }

        // 재고 복구 및 주문 상태 변경
        for (OrderItem orderItem : order.getOrderItems()) {
            Product product = orderItem.getProduct();
            product.increaseStock(orderItem.getQuantity());
            productRepository.save(product);

            orderItem.updateStatus(OrderStatus.취소완료.name());
        }

        order.updateStatus(OrderStatus.취소완료.name());
        orderRepository.save(order);
    }

    public void returnOrderItem(Long orderItemId) {
        User currentUser = getCurrentUser();

        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new IllegalArgumentException("주문 항목을 찾을 수 없습니다: " + orderItemId));

        Order order = orderItem.getOrder();
        // 현재 사용자가 주문의 소유자인지 확인
        if (!order.getUser().equals(currentUser)) {
            throw new IllegalArgumentException("주문 항목 반품 권한이 없습니다.");
        }

        // 배송 완료된 주문 항목만 반품 가능
        if (!orderItem.getStatus().equals(OrderStatus.배송완료.name())) {
            throw new IllegalArgumentException("반품이 불가능합니다.");
        }

        // 반품 처리
        orderItem.updateStatus(OrderStatus.반품중.name());
        orderItemRepository.save(orderItem);

        // D+1에 재고에 반영되는 스케줄러에서 상태를 "반품완료"로 변경하고 재고 복구
    }

    private OrderDto createOrderDto(Order order) {
        List<OrderItemDto> orderItemDtos = new ArrayList<>();
        for (OrderItem orderItem : order.getOrderItems()) {
            OrderItemDto dto = new OrderItemDto(orderItem.getId(),orderItem.getProduct().getId(),
                    orderItem.getQuantity(),
                    orderItem.getStatus());
            orderItemDtos.add(dto);
        }

        return new OrderDto(order.getId(), order.getStatus(), orderItemDtos);
    }
}
