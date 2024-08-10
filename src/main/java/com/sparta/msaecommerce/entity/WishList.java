package com.sparta.msaecommerce.entity;

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
    private Integer id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "wishlist")
    private List<WishListItem> wishlistItems;

    @ManyToMany
    @JoinTable(
            name = "WishListItems",
            joinColumns = @JoinColumn(name = "wishlist_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> products;
}
