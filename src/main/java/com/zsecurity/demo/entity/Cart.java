package com.zsecurity.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Products products;

    private int quantity;

    private double prices;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;

    private boolean isOrdered = false;
}
