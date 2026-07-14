package com.zsecurity.demo.repositories;

import com.zsecurity.demo.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface WishlistRepo extends JpaRepository<Wishlist, Long> {

    List<Wishlist> findByUserId(Long userId);

    Optional<Wishlist> findByUserIdAndProductId(Long userId, int productId);

    void deleteByUserIdAndProductId(Long userId, int productId);
}