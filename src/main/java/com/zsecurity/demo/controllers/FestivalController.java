package com.zsecurity.demo.controllers;

import com.zsecurity.demo.entity.Festival;
import com.zsecurity.demo.entity.FestivalProduct;
import com.zsecurity.demo.entity.Products;
import com.zsecurity.demo.repositories.FestivalProductRepo;
import com.zsecurity.demo.repositories.FestivalRepo;
import com.zsecurity.demo.repositories.ProdRepo;
import com.zsecurity.demo.services.FestivalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
public class FestivalController {

    @Autowired
    private FestivalService festivalService;

    @Autowired
    private ProdRepo prodRepo;

    @Autowired
    FestivalRepo festivalRepo;

    @Autowired
    FestivalProductRepo festivalProductRepo;

    @GetMapping("/festival/offers")
    public ResponseEntity<List<Products>> getFestivalOffers() {
        return ResponseEntity.ok(festivalService.getFestivalOffers());
    }

    @GetMapping("/current-festival")
    public ResponseEntity<Optional<Festival>> getCurrentFestival() {
        return ResponseEntity.ok(festivalService.getCurrentFestival());
    }

    @GetMapping("/get-all-festivals")
    public ResponseEntity<List<Festival>> getAllFestival() {
        return ResponseEntity.ok(festivalRepo.findAll());
    }

    @Scheduled(cron = "0 59 23 * * ?", zone = "Asia/Kolkata")
    public void switchFestival() {
        festivalService.switchFestival();
    }
}