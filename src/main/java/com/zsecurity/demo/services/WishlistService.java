package com.zsecurity.demo.services;

import com.zsecurity.demo.entity.Cart;
import com.zsecurity.demo.entity.Products;
import com.zsecurity.demo.entity.Wishlist;
import com.zsecurity.demo.repositories.ProdRepo;
import com.zsecurity.demo.repositories.UserRepo;
import com.zsecurity.demo.repositories.WishlistRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class WishlistService {

    @Autowired
    private WishlistRepo repo;

    @Autowired
    ProdServices prodServices;

    @Autowired
    ProdRepo prodRepo;

    @Autowired
    UserRepo userRepo;

    public void addToWishlist(Long userId, int productId) {

        if (repo.findByUserIdAndProductId(userId, productId).isPresent()) {
            return; // already exists
        }

        Wishlist w = new Wishlist();
        w.setUserId(userId);
        w.setProductId(productId);
        w.setCreatedAt(LocalDateTime.now());

        repo.save(w);
    }


    @Transactional
    public void removeFromWishlist(Long userId, int productId) {
        repo.deleteByUserIdAndProductId(userId, productId);
    }

    public List<Products> getWishlistProductIds(Long userId) {
        List<Integer> prodIds =  repo.findByUserId(userId)
                .stream()
                .map(Wishlist::getProductId)
                .toList();
        return prodRepo.findAllById(prodIds);
    }

    @Transactional
    public void moveToCart(Long userId, int productId) {

        Products products = prodRepo.findById(productId).get();
        Cart cart = new Cart();
        cart.setProducts(products);
        cart.setQuantity(1);
        cart.setPrices(products.getPrice());
        prodServices.addItemToCart(List.of(cart), userId.intValue());

        repo.deleteByUserIdAndProductId(userId, productId);
    }


    @Transactional
    public void moveAllToCart(Long userId) {

        List<Wishlist> wishlistItems = repo.findByUserId(userId);

        List<Cart> cartList = new ArrayList<>();

        for (Wishlist w : wishlistItems) {
            Cart cart = new Cart();
            cart.setProducts(prodRepo.findById(w.getProductId()).get());
            cart.setQuantity(1);
            cart.setPrices(prodRepo.findById(w.getProductId()).get().getPrice());
            cartList.add(cart);

        }

        // reuse your existing method
        prodServices.addItemToCart(cartList, userId.intValue());

        // clear wishlist
        repo.deleteAll(wishlistItems);
    }
}
