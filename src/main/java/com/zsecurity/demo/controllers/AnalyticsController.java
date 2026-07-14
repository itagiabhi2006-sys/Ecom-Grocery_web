package com.zsecurity.demo.controllers;

import com.zsecurity.demo.dtos.AnalyticsCategoryDTO;
import com.zsecurity.demo.dtos.AnalyticsResponseDTO;
import com.zsecurity.demo.services.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/trending-categories")
    public ResponseEntity<List<AnalyticsCategoryDTO>> trendingCategories(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(analyticsService.getTrendingCategories(limit));
    }

    @GetMapping("/trending-products")
    public ResponseEntity<List<AnalyticsResponseDTO>> trendingProducts(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(analyticsService.getTrendingProducts(limit));
    }

    @GetMapping("/most-bought")
    public ResponseEntity<List<AnalyticsResponseDTO>> mostBought(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(analyticsService.getMostBought(limit));
    }

    @GetMapping("/buy-again/{userId}")
    public ResponseEntity<List<AnalyticsResponseDTO>> buyAgain(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(analyticsService.getBuyAgain(userId, limit));
    }
}