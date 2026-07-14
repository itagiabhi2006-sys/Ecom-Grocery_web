package com.zsecurity.demo.services;

import com.zsecurity.demo.entity.Festival;
import com.zsecurity.demo.entity.FestivalProduct;
import com.zsecurity.demo.entity.Products;
import com.zsecurity.demo.repositories.FestivalProductRepo;
import com.zsecurity.demo.repositories.FestivalRepo;
import com.zsecurity.demo.repositories.ProdRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FestivalService {

    @Autowired
    private FestivalRepo festivalRepo;

    @Autowired
    private FestivalProductRepo festivalProductRepo;

    @Autowired
    private ProdRepo productRepo;

    public Optional<Festival> getCurrentFestival() {
        LocalDate today = LocalDate.now();
        return festivalRepo
                .findByStartDateLessThanEqualAndEndDateGreaterThanEqual(today, today);
    }




    @Cacheable(value = "festivalOffers", sync = true)
    public List<Products> getFestivalOffers() {
        Optional<Festival> festivalOpt = getCurrentFestival();
        if (festivalOpt.isEmpty()) return Collections.emptyList();
        Festival festival = festivalOpt.get();
        List<FestivalProduct> mappings =
                festivalProductRepo.findByFestivalId(festival.getId());

        List<Integer> productIds = mappings.stream()
                .map(FestivalProduct::getProductId)
                .collect(Collectors.toList());

        return productRepo.findAllById(productIds);
    }



    public Festival addFestival(Festival request) {
        Festival festival = new Festival();
        festival.setName(request.getName());
        festival.setStartDate(request.getStartDate());
        festival.setEndDate(request.getEndDate());
        festival.setRegion(request.getRegion());
        return festivalRepo.save(festival);
    }

    public FestivalProduct addFestivalProduct(FestivalProduct request) {
        FestivalProduct fp = new FestivalProduct();
        fp.setFestivalId(request.getFestivalId());
        fp.setProductId(request.getProductId());
        fp.setBaseDiscount(request.getBaseDiscount());
        return festivalProductRepo.save(fp);

    }

    public void switchFestival() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        Optional<Festival> todayFest =
                festivalRepo.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(today, today);

        Optional<Festival> tomorrowFest =
                festivalRepo.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(tomorrow, tomorrow);


        if (todayFest.isPresent() && tomorrowFest.isPresent()
                && todayFest.get().getId().equals(tomorrowFest.get().getId())) {

            System.out.println("Festival continues → no change");
            return;
        }


        if (todayFest.isPresent()) {

            List<Products> products = productRepo.findByFestivalOfferTrue();

            for (Products product : products) {
                product.setDiscountFinalPrice(0);
                product.setDiscountPercent(0);
                product.setFestivalOffer(false);
                productRepo.save(product);
            }
        }

        if (tomorrowFest.isPresent()) {
            List<FestivalProduct> mappings =
                    festivalProductRepo.findByFestivalId(tomorrowFest.get().getId());
            for (FestivalProduct mapping : mappings) {
                Products product = productRepo.findById(mapping.getProductId()).orElse(null);
                if (product != null) {
                    double discount = mapping.getBaseDiscount();
                    product.setDiscountPercent(discount);
                    product.setFestivalOffer(true);
                    double discounted =
                            product.getPrice() - (product.getPrice() * discount / 100);
                    product.setDiscountFinalPrice((int) discounted);
                    productRepo.save(product);
                }
            }
        }
    }
}
