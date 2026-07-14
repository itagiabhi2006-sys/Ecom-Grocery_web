package com.zsecurity.demo.repositories;

import com.zsecurity.demo.entity.SavedAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavedAdressRepo extends JpaRepository<SavedAddress,Integer> {

    List<SavedAddress> findByUserId(int userid);
    boolean existsByFullName(String name);
}
