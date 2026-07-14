package com.zsecurity.demo.services;


import com.zsecurity.demo.dtos.AnalyticsCategoryDTO;
import com.zsecurity.demo.dtos.AnalyticsResponseDTO;
import com.zsecurity.demo.entity.Categories;
import com.zsecurity.demo.entity.Products;
import com.zsecurity.demo.repositories.CateRepo;
import com.zsecurity.demo.repositories.OrderRepo;
import com.zsecurity.demo.repositories.ProdRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
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
    
    

    @Cacheable(value = "trendingCategories", key = "#limit", sync = true)
    public List<AnalyticsCategoryDTO> getTrendingCategories(int limit) {
        return orderRepository.findTrendingCategories(PageRequest.of(0, limit));
    }

    @Cacheable(value = "trendingProducts", key = "#limit", sync = true)
    public List<AnalyticsResponseDTO> getTrendingProducts(int limit) {
        return orderRepository.findTrendingProducts(PageRequest.of(0, limit));
    }

    @Cacheable(value = "mostBought", key = "#limit", sync = true)
    public List<AnalyticsResponseDTO> getMostBought(int limit) {
        return orderRepository.findMostBought(PageRequest.of(0, limit));
    }

    public List<AnalyticsResponseDTO> getBuyAgain(Long userId, int limit) {
        return orderRepository.findBuyAgain(userId, PageRequest.of(0, limit));
    }


}