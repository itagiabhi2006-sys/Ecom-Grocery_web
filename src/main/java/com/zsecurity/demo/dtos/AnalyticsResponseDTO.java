package com.zsecurity.demo.dtos;


import com.zsecurity.demo.entity.Products;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnalyticsResponseDTO {
    private int id;
    private String title;
    private String description;
    private double price;
    private String imageURL;
    private int stock;
    private long totalSold;

    public AnalyticsResponseDTO(int id, String title, String description, double price, String imageURL, int stock, long totalSold) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.imageURL = imageURL;
        this.stock = stock;
        this.totalSold = totalSold;
    }

    public AnalyticsResponseDTO(Products product, long totalSold) {
        this.id = product.getId();
        this.title = product.getTitle();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.imageURL = product.getImageURL();
        this.stock = product.getStock();
        this.totalSold = totalSold;
    }
}
