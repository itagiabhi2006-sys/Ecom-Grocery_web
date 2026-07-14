package com.zsecurity.demo.repositories;

import com.zsecurity.demo.entity.FestivalProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FestivalProductRepo extends JpaRepository<FestivalProduct, Integer> {

    List<FestivalProduct> findByFestivalId(Integer festivalId);
}