package com.zsecurity.demo.controllers;

import com.zsecurity.demo.entity.Cart;
import com.zsecurity.demo.entity.Festival;
import com.zsecurity.demo.entity.Products;
import com.zsecurity.demo.entity.Users;
import com.zsecurity.demo.repositories.CartRepository;
import com.zsecurity.demo.repositories.ProdRepo;
import com.zsecurity.demo.repositories.UserRepo;
import com.zsecurity.demo.services.EmailService;
import com.zsecurity.demo.services.FestivalService;
import com.zsecurity.demo.services.SpecialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class SpecialController {

    @Autowired
    SpecialService specialService;

    @Autowired
    ProdRepo prodRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    CartRepository cartRepository;

    @GetMapping("/special-offer/{userId}")
    public ResponseEntity<Integer> specialOffer(@PathVariable long userId){
        return ResponseEntity.ok(specialService.getOfferAmount(userId));
    }

    @GetMapping("/all-offers")
    public ResponseEntity<List<Products>> getAllOffer(){
        return ResponseEntity.ok(prodRepo.findByFestivalOfferTrueOrDealOfWeekTrueOrNormalOfferTrue());
    }

    @Scheduled(cron = "0 0 10 */2 * *")
    public void sendAbandonedCartEmails() {
        specialService.sendAbandonedCartEmails();
    }

    // Runs every day at 9:00 AM
    @Scheduled(cron = "0 0 9 * * *")
    public void sendFestivalOfferEmails() {
        specialService.sendFestivalOfferEmails();
    }
}