package com.sparta.msaecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "Products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends TimestampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false, columnDefinition = "DECIMAL(10, 2)")
    private Double price;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @ManyToMany(mappedBy = "products")
    private List<WishList> wishlists;

    @OneToMany(mappedBy = "product")
    private List<OrderItem> orderItems;
}
