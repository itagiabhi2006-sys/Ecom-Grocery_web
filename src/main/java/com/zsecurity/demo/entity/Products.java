package com.zsecurity.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class Products {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String title;
    private String description;
    private double price;
    private String imageURL;
    private int stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnore
    private Categories categories;
    private boolean dealOfWeek = false;
    private double discountPercent;
    private boolean festivalOffer;
    private int discountFinalPrice;
    private  int margin;
    private boolean normalOffer;
    private String brand;

}
