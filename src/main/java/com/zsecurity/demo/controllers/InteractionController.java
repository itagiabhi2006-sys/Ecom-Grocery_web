package com.zsecurity.demo.controllers;

import com.zsecurity.demo.entity.Products;
import com.zsecurity.demo.entity.UserProductInteraction;
import com.zsecurity.demo.repositories.InteractionRepo;
import com.zsecurity.demo.services.InteractionService;
import com.zsecurity.demo.services.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/track")
public class InteractionController {

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private RecommendationService recommendationService;

    @PostMapping("/view")
    public ResponseEntity<String> trackView(@RequestParam Long userId,
                                            @RequestParam int productId) {
        interactionService.track(userId, productId, "VIEW");
        return ResponseEntity.ok("View tracked successfully for user: " + userId + ", product: " + productId);
    }

    @PostMapping("/click")
    public ResponseEntity<String> trackClick(@RequestParam Long userId,
                                             @RequestParam int productId) {
        interactionService.track(userId, productId, "CLICK");
        return ResponseEntity.ok("Click tracked successfully for user: " + userId + ", product: " + productId);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Set<Products>> getRecommendations(@PathVariable Long userId) {
        return ResponseEntity.ok(recommendationService.getRecommendations(userId));
    }

    @GetMapping("/frequently-viewed/{userId}")
    public ResponseEntity<List<Products>> frequentlyViewed(@PathVariable Long userId) {
        return ResponseEntity.ok(recommendationService.FrequentlyViewed(userId));
    }

    @Autowired
    private InteractionRepo interactionRepo;

    // Runs every day at 2:00 AM
    @Scheduled(cron = "0 0 2 * * *")
    public void deleteLeastSeenInteractions() {
        interactionService.deleteLeastSeenInteractions();
    }
}