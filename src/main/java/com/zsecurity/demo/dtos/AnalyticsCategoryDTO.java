package com.zsecurity.demo.dtos;
import com.zsecurity.demo.entity.Categories;
import lombok.Data;


@Data
public class AnalyticsCategoryDTO {

    private int id;
    private String name;
    private String imageURL;
    private long totalOrders;

    public AnalyticsCategoryDTO(Categories category, long totalOrders) {
        this.id = category.getId();
        this.name = category.getName();
        this.imageURL = category.getImageURL();
        this.totalOrders = totalOrders;
    }

}