package com.sparta.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "WishLists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishList extends TimestampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @OneToMany(mappedBy = "wishList", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WishListItem> wishListItems;
}
