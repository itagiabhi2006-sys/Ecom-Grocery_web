package com.zsecurity.demo.controllers;

import com.zsecurity.demo.entity.Products;
import com.zsecurity.demo.services.MLService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ml")
public class MLController {

    @Autowired
    private MLService mlService;

    @GetMapping("/recommend")
    public ResponseEntity<List<Products>> getRecommendation(@RequestParam String product) {
        return ResponseEntity.ok(mlService.getRecommendation(product));
    }
    @GetMapping("/similar/{productId}")
    public String similar(@PathVariable int productId) {
        return mlService.getSimilarProducts(productId);
    }

    @GetMapping("/bundles")
    public String bundles() {
        return mlService.getSmartBundles();
    }

    @PostMapping("/cart-recommend")
    public ResponseEntity<List<Products>> cartRecommend(
            @RequestBody List<String> items) {

        return ResponseEntity.ok(
                mlService.getCartRecommendation(items));
    }


}