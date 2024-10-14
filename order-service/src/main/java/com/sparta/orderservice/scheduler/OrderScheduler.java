package com.sparta.orderservice.scheduler;

import com.sparta.orderservice.client.ProductFeignClient;
import com.sparta.orderservice.entity.OrderItem;
import com.sparta.orderservice.entity.OrderStatus;
import com.sparta.orderservice.repository.OrderItemRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OrderScheduler {

    private final OrderItemRepository orderItemRepository;
    private final ProductFeignClient productFeignClient;

    public OrderScheduler(OrderItemRepository orderItemRepository,ProductFeignClient productFeignClient) {
        this.orderItemRepository = orderItemRepository;
        this.productFeignClient = productFeignClient;
    }

    @Scheduled(cron = "0 0 0 * * *")  // 매일 자정에 실행
    public void updateOrderStatus() {
        List<OrderItem> orderItems = orderItemRepository.findAll();

        for (OrderItem orderItem : orderItems) {
            // 배송 상태 업데이트 (D+1에 배송 중, D+2에 배송 완료)
            if (orderItem.getStatus().equals(OrderStatus.주문완료.name())) {
                if (orderItem.getCreatedAt().plusDays(1).isBefore(LocalDateTime.now())) {
                    orderItem.updateStatus(OrderStatus.배송중.name());
                }
            } else if (orderItem.getStatus().equals(OrderStatus.배송중.name())) {
                if (orderItem.getCreatedAt().plusDays(2).isBefore(LocalDateTime.now())) {
                    orderItem.updateStatus(OrderStatus.배송완료.name());
                }
            } else if (orderItem.getStatus().equals(OrderStatus.반품중.name())) {
                if (orderItem.getCreatedAt().plusDays(1).isBefore(LocalDateTime.now())) {
                    orderItem.updateStatus(OrderStatus.반품완료.name());
                    // 재고 복구 - FeignClient로 Product 서비스 호출
                    productFeignClient.increaseStock(orderItem.getProductId(), orderItem.getQuantity());
                }
            }

            orderItemRepository.save(orderItem);
        }
    }
}
