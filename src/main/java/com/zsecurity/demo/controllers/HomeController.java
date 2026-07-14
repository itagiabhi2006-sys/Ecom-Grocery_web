package com.zsecurity.demo.controllers;

import com.zsecurity.demo.dtos.HomeResponseDTO;
import com.zsecurity.demo.services.AnalyticsService;
import com.zsecurity.demo.services.FestivalService;
import com.zsecurity.demo.services.ProdServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/home")
public class HomeController {

    @Autowired
    private ProdServices prodServices;

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private FestivalService festivalService;

    @GetMapping("/data")
    public CompletableFuture<HomeResponseDTO> getHomePageData() {
        return CompletableFuture.supplyAsync(() -> prodServices.getAllCategories())
                .thenCombine(
                        CompletableFuture.supplyAsync(() -> analyticsService.getTrendingProducts(10)),
                        (categories, trendingProducts) -> {
                            HomeResponseDTO response = new HomeResponseDTO();
                            response.setCategories(categories);
                            response.setTrendingProducts(trendingProducts);
                            return response;
                        }
                )
                .thenCombine(
                        CompletableFuture.supplyAsync(() -> analyticsService.getMostBought(10)),
                        (response, mostBought) -> {
                            response.setMostBought(mostBought);
                            return response;
                        }
                )
                .thenCombine(
                        CompletableFuture.supplyAsync(() -> festivalService.getFestivalOffers()),
                        (response, festivalOffers) -> {
                            response.setFestivalOffers(festivalOffers);
                            return response;
                        }
                );
    }
}
