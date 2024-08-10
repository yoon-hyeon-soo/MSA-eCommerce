package com.sparta.msaecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "Users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends TimestampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

//    @Column(name = "email_verified", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
//    private Boolean emailVerified = false;

    @OneToMany(mappedBy = "user")
    private List<Order> orders;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private WishList wishlist;
}
