//package com.sparta.msaecommerce.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//import java.util.List;
//
//@Entity
//@Table(name = "WishLists")
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class WishList extends TimestampedEntity {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "id")
//    private Long id;
//
//    @OneToOne
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user;
//
//    @OneToMany(mappedBy = "wishlist", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<WishListItem> wishListItems;
//}
