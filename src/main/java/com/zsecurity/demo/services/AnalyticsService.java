package com.zsecurity.demo.services;


import com.zsecurity.demo.dtos.AnalyticsCategoryDTO;
import com.zsecurity.demo.dtos.AnalyticsResponseDTO;
import com.zsecurity.demo.entity.Categories;
import com.zsecurity.demo.entity.Products;
import com.zsecurity.demo.repositories.CateRepo;
import com.zsecurity.demo.repositories.OrderRepo;
import com.zsecurity.demo.repositories.ProdRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnalyticsService {

    @Autowired
    private OrderRepo orderRepository;

    @Autowired
    ProdRepo productRepo;

    @Autowired
    CateRepo cateRepo;
    
    

    public List<AnalyticsCategoryDTO> getTrendingCategories(int limit) {

        return orderRepository.findTrendingCategories(limit)
                .stream()
                .map(row -> {

                    int categoryId = ((Number) row[0]).intValue();
                    Long count = ((Number) row[2]).longValue();

                    Categories category = cateRepo.findById(categoryId)
                            .orElseThrow();

                    return new AnalyticsCategoryDTO(category, count);
                })
                .toList();
    }

    private AnalyticsResponseDTO mapToProductDTO(Object[] row) {

        int productId = ((Number) row[0]).intValue();
        Long count = ((Number) row[2]).longValue();

        Products product = productRepo.findById(productId)
                .orElseThrow();

        return new AnalyticsResponseDTO(product, count);
    }

    public List<AnalyticsResponseDTO> getTrendingProducts(int limit) {
        return orderRepository.findTrendingProducts(limit)
                .stream()
                .map(this::mapToProductDTO)
                .toList();
    }

    public List<AnalyticsResponseDTO> getMostBought(int limit) {
        return orderRepository.findMostBought(limit)
                .stream()
                .map(this::mapToProductDTO)
                .toList();
    }

    public List<AnalyticsResponseDTO> getBuyAgain(Long userId, int limit) {
        return orderRepository.findBuyAgain(userId, limit)
                .stream()
                .map(this::mapToProductDTO)
                .toList();
    }


}