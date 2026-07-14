package com.zsecurity.demo.dtos;

import com.zsecurity.demo.entity.Products;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HomeResponseDTO {
    private List<CategoryDto> categories;
    private List<AnalyticsResponseDTO> trendingProducts;
    private List<AnalyticsResponseDTO> mostBought;
    private List<Products> festivalOffers;
}
