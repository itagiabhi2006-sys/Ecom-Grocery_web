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


        for (UserProductInteraction i : interactions) {
            int score = (i.getViewCount() * 2) + (i.getClickCount());
            if (score < THRESHOLD) continue;
            if (i.getLastUpdated() != null &&
                    i.getLastUpdated().isBefore(LocalDateTime.now().minusDays(3))) {
                continue;
            }
            Products product = prodRepo.findById(i.getProductId()).orElse(null);
            if (product == null) continue;

            //  Only recommend if offer exists
            if (product.isDealOfWeek() || product.isFestivalOffer() || product.isNormalOffer()) {
                result.add(product);
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
        List<Products> products = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            products.add(prodRepo.findById(interactions.get(i).getProductId()).get());
        }
        return products;

    }


}
