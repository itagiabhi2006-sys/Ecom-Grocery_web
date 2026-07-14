package com.zsecurity.demo.repositories;

import com.zsecurity.demo.entity.Categories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CateRepo extends JpaRepository<Categories,Integer> {

}
