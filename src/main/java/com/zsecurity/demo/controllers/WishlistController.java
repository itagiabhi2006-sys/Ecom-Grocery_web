package com.zsecurity.demo.controllers;

import com.zsecurity.demo.entity.Products;
import com.zsecurity.demo.services.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService service;

    @PostMapping("/add")
    public ResponseEntity<String> add(@RequestParam Long userId, @RequestParam int productId) {
        service.addToWishlist(userId, productId);
        return ResponseEntity.status(HttpStatus.CREATED).body("Product added to wishlist successfully");
    }

    @DeleteMapping("/remove")
    public ResponseEntity<String> remove(@RequestParam Long userId, @RequestParam int productId) {
        service.removeFromWishlist(userId, productId);
        return ResponseEntity.ok("Product removed from wishlist successfully");
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Products>> getWishlist(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getWishlistProductIds(userId));
    }

    @PostMapping("/move-to-cart")
    public ResponseEntity<String> moveToCart(@RequestParam Long userId, @RequestParam int productId) {
        service.moveToCart(userId, productId);
        return ResponseEntity.ok("Product moved to cart successfully");
    }

    @PostMapping("/move-all-to-cart/{userId}")
    public ResponseEntity<String> moveAllToCart(@PathVariable Long userId) {
        service.moveAllToCart(userId);
        return ResponseEntity.ok("All items moved to cart successfully");
    }
}