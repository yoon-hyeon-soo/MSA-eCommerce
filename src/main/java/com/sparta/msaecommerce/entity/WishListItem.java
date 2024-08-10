package com.sparta.msaecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "WishListItems")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishListItem extends TimestampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "wishlist_id", nullable = false)
    private WishList wishlist;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;
}
