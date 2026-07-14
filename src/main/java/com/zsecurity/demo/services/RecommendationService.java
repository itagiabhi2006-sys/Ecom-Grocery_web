package com.zsecurity.demo.services;

import com.zsecurity.demo.entity.Cart;
import com.zsecurity.demo.entity.Products;
import com.zsecurity.demo.entity.UserProductInteraction;
import com.zsecurity.demo.repositories.CartRepository;
import com.zsecurity.demo.repositories.CateRepo;
import com.zsecurity.demo.repositories.InteractionRepo;
import com.zsecurity.demo.repositories.ProdRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    @Autowired
    private InteractionRepo interactionRepo;

    @Autowired
    private ProdRepo prodRepo;

    @Autowired
    CartRepository cartRepository;

    private static final int THRESHOLD = 3;

    public Set<Products> getRecommendations(Long userId) {

        List<UserProductInteraction> interactions =
                interactionRepo.findByUserId(userId);

        Set<Products> result = new HashSet<>();

        List<Cart> carts = cartRepository.findCartByUserId(userId);

        List<Integer> productIdsToFetch = new ArrayList<>();

        for (UserProductInteraction i : interactions) {
            int score = (i.getViewCount() * 2) + (i.getClickCount());
            if (score < THRESHOLD) continue;
            if (i.getLastUpdated() != null &&
                    i.getLastUpdated().isBefore(LocalDateTime.now().minusDays(3))) {
                continue;
            }
            productIdsToFetch.add(i.getProductId());
        }

        if (!productIdsToFetch.isEmpty()) {
            List<Products> fetchedProducts = prodRepo.findAllById(productIdsToFetch);
            for (Products product : fetchedProducts) {
                if (product.isDealOfWeek() || product.isFestivalOffer() || product.isNormalOffer()) {
                    result.add(product);
                }
            }
        }

        for (Cart c : carts) {
            Products product = c.getProducts();
            //  Only recommend if offer exists
            if (product.isDealOfWeek() || product.isFestivalOffer() || product.isNormalOffer()) {
                    result.add(product);
            }
        }
        return result;
    }


    public List<Products> FrequentlyViewed(Long userId) {

        List<UserProductInteraction> interactions =
                interactionRepo.findTopViewed();
        if(interactions.isEmpty()){
            return new ArrayList<>();
        }
        
        List<Integer> productIds = interactions.stream()
                .limit(6)
                .map(UserProductInteraction::getProductId)
                .collect(Collectors.toList());
                
        return prodRepo.findAllById(productIds);
    }


}
