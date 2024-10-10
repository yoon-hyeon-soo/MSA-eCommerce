package com.sparta.orderservice.entity;

import com.sparta.productservice.entity.Product;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "OrderItems")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem extends TimestampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @JoinColumn(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "status", nullable = false)
    private String status;

    public void updateStatus(String status) {
        this.status = status;
    }
}
