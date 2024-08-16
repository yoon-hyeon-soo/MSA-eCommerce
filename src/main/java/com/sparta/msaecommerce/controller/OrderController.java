package com.sparta.msaecommerce.controller;

import com.sparta.msaecommerce.dto.OrderDto;
import com.sparta.msaecommerce.dto.OrderItemDto;
import com.sparta.msaecommerce.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<OrderDto>> getUserOrders() {
        List<OrderDto> userOrders = orderService.getUserOrders();
        return ResponseEntity.ok(userOrders);
    }

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@RequestBody List<OrderItemDto> orderItemDtos) {
        OrderDto orderDto = orderService.createOrder(orderItemDtos);
        return ResponseEntity.ok(orderDto);
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<String> cancelOrder(@PathVariable Long orderId) {
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok("주문이 취소되었습니다.");
    }

    @PostMapping("/return/{orderItemId}")
    public ResponseEntity<String> returnOrderItem(@PathVariable Long orderItemId) {
        orderService.returnOrderItem(orderItemId);
        return ResponseEntity.ok("반품이 신청되었습니다.");
    }
}
