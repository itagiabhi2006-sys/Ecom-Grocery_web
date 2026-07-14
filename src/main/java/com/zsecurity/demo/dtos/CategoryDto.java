package com.zsecurity.demo.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
public class CategoryDto {
    private int id;
    private String name;
    private String imageURL;
    private Long productCount;

}
