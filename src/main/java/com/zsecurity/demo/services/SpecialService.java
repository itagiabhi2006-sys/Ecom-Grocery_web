package com.zsecurity.demo.services;

import com.zsecurity.demo.entity.Cart;
import com.zsecurity.demo.entity.Festival;
import com.zsecurity.demo.entity.Products;
import com.zsecurity.demo.entity.Users;
import com.zsecurity.demo.repositories.CartRepository;
import com.zsecurity.demo.repositories.ProdRepo;
import com.zsecurity.demo.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class SpecialService {

    @Autowired
    CartRepository cartRepository;


    @Autowired
    UserRepo userRepo;

    @Autowired
    ProdRepo prodRepo;

    @Autowired
    EmailService emailService;

    @Autowired
    FestivalService festivalService;

    private static final int RANDOM_PRODUCT_COUNT = 3;


    public int getOfferAmount(long userId) {

        List<Cart> carts = cartRepository.findCartByUserId(userId);

        double total = 0.0;

        for (Cart c : carts) {
            Products p = c.getProducts();

            if (p.isFestivalOffer() || p.isDealOfWeek() || p.isNormalOffer()) continue;

            total += p.getPrice() * c.getQuantity();
        }

        if (total < 1000) return 0;

        double percentage;

        if (total >= 3000) {
            percentage = 0.35;
        } else if (total >= 2000) {
            percentage = 0.30;
        } else {
            percentage = 0.25;
        }

        int offerAmount = 0;

        for (Cart c : carts) {

            Products p = c.getProducts();

            if (p.isFestivalOffer() || p.isDealOfWeek()) continue;

            offerAmount += (int) (p.getMargin() * percentage * c.getQuantity());
        }

        return offerAmount;
    }

    public void sendAbandonedCartEmails() {

        // 1. Get all offer products pool for random picks
        List<Products> offerProducts = prodRepo.findAll().stream()
                .filter(p -> p.isDealOfWeek() || p.isFestivalOffer() || p.isNormalOffer())
                .collect(Collectors.toList());

        if (offerProducts.isEmpty()) return;

        // 2. Get all users who have pending cart items
        List<Users> allUsers = userRepo.findAll();

        for (Users user : allUsers) {

            List<Cart> cartItems = cartRepository.findByUserAndIsOrderedFalse(user);

            // Skip users with empty carts
            if (cartItems == null || cartItems.isEmpty()) continue;

            Collections.shuffle(offerProducts);
            List<Products> randomPicks = offerProducts.stream()
                    .limit(Math.min(RANDOM_PRODUCT_COUNT, offerProducts.size()))
                    .collect(Collectors.toList());
            emailService.emailForAbandonedCart(user, randomPicks);
        }
    }

    public void sendFestivalOfferEmails() {
        Optional<Festival> festivalOpt = festivalService.getCurrentFestival();
        if (festivalOpt.isEmpty()) return;

        Festival festival = festivalOpt.get();
        List<Users> allUsers = userRepo.findAll();

        for (Users user : allUsers) {
            emailService.emailForFestivalOffer(user, festival);
        }
    }
}
