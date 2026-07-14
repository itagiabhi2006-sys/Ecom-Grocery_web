package com.zsecurity.demo.dtos;
import com.zsecurity.demo.entity.Categories;
import lombok.Data;


@Data
public class AnalyticsCategoryDTO {

    private int id;
    private String name;
    private String imageURL;
    private long totalOrders;

    public AnalyticsCategoryDTO(int id, String name, String imageURL, long totalOrders) {
        this.id = id;
        this.name = name;
        this.imageURL = imageURL;
        this.totalOrders = totalOrders;
    }

    public AnalyticsCategoryDTO(Categories category, long totalOrders) {
        this.id = category.getId();
        this.name = category.getName();
        this.imageURL = category.getImageURL();
        this.totalOrders = totalOrders;
    }

}