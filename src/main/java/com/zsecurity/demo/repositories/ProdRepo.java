package com.zsecurity.demo.repositories;

import com.zsecurity.demo.entity.Products;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdRepo extends JpaRepository<Products,Integer> {

    Optional<Products> findByTitleIgnoreCase(String title);

    List<Products> findByCategories_Id(Integer categoryId);

    Optional<Products> findFirstByTitleContainingIgnoreCase(String title);

    List<Products> findByDealOfWeekTrue();

    List<Products> findByFestivalOfferTrue();

    Long countByCategories_Id(int id);

    List<Products> findByFestivalOfferTrueOrDealOfWeekTrueOrNormalOfferTrue();

    List<Products> findByBrandAndIdNot(String brand, int productId, Pageable pageable);
}
