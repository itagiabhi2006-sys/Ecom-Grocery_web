package com.zsecurity.demo.dtos;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductQuantityResponse {
    private int productId;
    private String productName;
    private String productImage;
    private int quantity;
    private double price;
}

