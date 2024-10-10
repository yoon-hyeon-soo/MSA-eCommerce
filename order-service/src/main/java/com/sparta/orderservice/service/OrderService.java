package com.sparta.orderservice.service;

import com.sparta.orderservice.client.ProductFeignClient;
import com.sparta.orderservice.client.UserFeignClient;
import com.sparta.orderservice.dto.OrderDto;
import com.sparta.orderservice.dto.OrderItemDto;
import com.sparta.orderservice.dto.ProductDto;
import com.sparta.orderservice.dto.UserResponseDto;
import com.sparta.orderservice.entity.Order;
import com.sparta.orderservice.entity.OrderItem;
import com.sparta.orderservice.entity.OrderStatus;
import com.sparta.orderservice.repository.OrderItemRepository;
import com.sparta.orderservice.repository.OrderRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final UserFeignClient userFeignClient;
    private final ProductFeignClient productFeignClient;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderService(UserFeignClient userFeignClient, ProductFeignClient productFeignClient,
                        OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.userFeignClient = userFeignClient;
        this.productFeignClient = productFeignClient;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
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

    public List<OrderDto> getUserOrders() {
        UserResponseDto currentUser = getCurrentUser();  // 현재 로그인된 유저를 가져옵니다.
        List<Order> orders = orderRepository.findByUserId(currentUser.getId());  // 해당 유저의 주문 목록을 조회합니다.

        // 각 주문을 OrderDto로 변환하여 반환합니다.
        return orders.stream()
                .map(this::createOrderDto)
                .collect(Collectors.toList());
    }

    public OrderDto createOrder(List<OrderItemDto> orderItemDtos) {
        UserResponseDto currentUser = getCurrentUser();

        Order order = Order.builder()
                .userId(currentUser.getId())
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.주문완료.name())
                .build();

        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemDto dto : orderItemDtos) {
            // FeignClient로 상품 정보를 가져옵니다.
            ProductDto product = productFeignClient.getProductById(dto.getProductId());
//                    .orElseThrow(() -> new IllegalArgumentException("제품을 찾을 수 없습니다: " + dto.getProductId()));

            // DTO로 입력받은 수량에 따라 재고 감소
            productFeignClient.decreaseStock(dto.getProductId(), dto.getQuantity());

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .productId(product.getId())
                    .quantity(dto.getQuantity())
                    .status(OrderStatus.주문완료.name())
                    .build();

            // 주문 항목 추가
            orderItems.add(orderItem);
        }

        // 주문 및 주문 항목 저장
        order.setOrderItems(orderItems);
        orderRepository.save(order);
        orderItemRepository.saveAll(orderItems);

        return createOrderDto(order);
    }

    public void cancelOrder(Long orderId) {
        UserResponseDto currentUser = getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));

        // 현재 사용자가 주문의 소유자인지 확인
        if (!order.getUserId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("주문 취소 권한이 없습니다.");
        }

        // 배송 중이 아닌 경우에만 주문 취소 가능
        if (order.getStatus().equals(OrderStatus.배송중.name()) || order.getStatus().equals(OrderStatus.배송완료.name())) {
            throw new IllegalArgumentException("주문 취소가 불가능합니다.");
        }

        // 재고 복구 및 주문 상태 변경
        for (OrderItem orderItem : order.getOrderItems()) {
            ProductDto product = productFeignClient.getProductById(orderItem.getProductId());
//                    .orElseThrow(() -> new IllegalArgumentException("제품을 찾을 수 없습니다: " + orderItem.getProductId()));
            // 재고 복구
            productFeignClient.increaseStock(orderItem.getProductId(), orderItem.getQuantity());

            orderItem.updateStatus(OrderStatus.취소완료.name());
        }

        order.updateStatus(OrderStatus.취소완료.name());
        orderRepository.save(order);
    }

    public void returnOrderItem(Long orderItemId) {
        UserResponseDto currentUser = getCurrentUser();

        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new IllegalArgumentException("주문 항목을 찾을 수 없습니다: " + orderItemId));

        Order order = orderItem.getOrder();
        // 현재 사용자가 주문의 소유자인지 확인
        if (!order.getUserId().equals(currentUser.getId())) {
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
            OrderItemDto dto = new OrderItemDto(orderItem.getId(), orderItem.getProductId(),
                    orderItem.getQuantity(),
                    orderItem.getStatus());
            orderItemDtos.add(dto);
        }

        return new OrderDto(order.getId(), order.getStatus(), orderItemDtos);
    }
}
