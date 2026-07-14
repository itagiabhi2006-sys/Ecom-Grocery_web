package com.zsecurity.demo.repositories;

import com.zsecurity.demo.entity.Cart;
import com.zsecurity.demo.entity.Products;
import com.zsecurity.demo.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {
    List<Cart> findByUserAndIsOrderedFalse(Users user);


    Cart findByUserIdAndProductsId(int userId, int productId);

    List<Cart> findCartByUserId(long userId);


}
