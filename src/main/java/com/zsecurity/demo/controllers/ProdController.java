package com.zsecurity.demo.controllers;

import com.zsecurity.demo.dtos.CategoryDto;
import com.zsecurity.demo.dtos.RefundRequest;
import com.zsecurity.demo.dtos.ResponseOrderDetails;
import com.zsecurity.demo.entity.*;
import com.zsecurity.demo.repositories.CartRepository;
import com.zsecurity.demo.repositories.ProdRepo;
import com.zsecurity.demo.repositories.WishlistRepo;
import com.zsecurity.demo.services.ProdServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
public class ProdController {

    @Autowired
    ProdServices services;

    @Autowired
    ProdRepo prodRepo;

    @Autowired
    CartRepository cartRepository;

    @Autowired
    WishlistRepo wishlistRepo;

    @GetMapping("/products")
    public ResponseEntity<List<Products>> getAllProducts(){
        return new ResponseEntity<>(services.getAllProducts(), OK);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDto>> getAllCategories(){
        return new ResponseEntity<>(services.getAllCategories(), OK);
    }

    @PostMapping("/add-product")
    public ResponseEntity<Products> addProduct(@RequestBody Products products){
        return new ResponseEntity<>(services.addProduct(products), CREATED);
    }

    @PostMapping("/create-order")
    public ResponseEntity<String> createOrder(@RequestBody RefundRequest.OrderFullDetails orderFullDetails){
        return new ResponseEntity<>(services.createOrder(orderFullDetails), CREATED);
    }

    @PostMapping("/add-cate")
    public ResponseEntity<Categories> addCategories(@RequestBody Categories categories){
        return new ResponseEntity<>(services.addCategories(categories), CREATED);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<Products> getProductById(@PathVariable int id) {
        return new ResponseEntity<>(services.getProductById(id), HttpStatus.OK);
    }

    @PostMapping("/add-user")
    public ResponseEntity<Users> addUser(@RequestBody Users users){
        return new ResponseEntity<>(services.addUser(users), CREATED);
    }

    @PutMapping("/assign-product-to-Category/{pid}/{cid}")
    public ResponseEntity<Products> assignCategoryToProduct(@PathVariable int pid, @PathVariable int cid){
        return new ResponseEntity<>(services.assignCategoryToProduct(pid, cid), OK);
    }

    @PutMapping("/placeOrder/{userId}/{orderId}")
    public ResponseEntity<Orders> placeOrder(@PathVariable int userId, @PathVariable int orderId){
        return new ResponseEntity<>(services.placeOrder(userId, orderId), OK);
    }

    @GetMapping("/categories/{id}/products")
    public ResponseEntity<List<Products>> getAllProductsBelongsTocategory(@PathVariable int id){
        return ResponseEntity.ok(services.getAllProductsBelongsTOcategory(id));
    }

    @GetMapping("/products/search")
    public ResponseEntity<List<Products>> searchProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String sort
    ) {
        return ResponseEntity.ok(services.searchProducts(q, minPrice, maxPrice, sort));
    }

    @GetMapping("/order-details/{userId}")
    public ResponseEntity<List<ResponseOrderDetails>> getOrderDetails(@PathVariable long userId){
        return ResponseEntity.ok(services.getOrderDetailsByUserId(userId));
    }

    @GetMapping("/cancel-order/{orderId}")
    public ResponseEntity<String> cancelOrder(@PathVariable int orderId){
        services.cancelOrder(orderId);
        return ResponseEntity.ok("Order cancelled successfully with ID: " + orderId);
    }

    @GetMapping("/get-order-status/{orderID}")
    public ResponseEntity<List<ResponseOrderDetails.TrackDto>> getOrderStatusTracking(@PathVariable int orderID){
        return ResponseEntity.ok(services.getOrderStatusTracking(orderID));
    }

    @GetMapping("/get-cart-details/{userId}")
    public ResponseEntity<List<Cart>> getCartItems(@PathVariable int userId) {
        return ResponseEntity.ok(services.getCartItems(userId));
    }

    @PostMapping("/add-to-cart/{userId}")
    public ResponseEntity<String> addItemToCart(@RequestBody List<Cart> cartList, @PathVariable int userId) {
        services.addItemToCart(cartList, userId);
        return ResponseEntity.status(CREATED).body("Items added to cart successfully for user ID: " + userId);
    }

    @DeleteMapping("/remove-from-cart/{cartID}")
    public ResponseEntity<String> removeFromCart(@PathVariable int cartID) {
        services.removeFromCart(cartID);
        return ResponseEntity.ok("Item removed from cart successfully with cart ID: " + cartID);
    }

    @PostMapping("/update-cart-item/{cartId}/{quantity}")
    public ResponseEntity<String> updateCart(@PathVariable int cartId, @PathVariable int quantity) {
        services.updateCart(cartId, quantity);
        return ResponseEntity.ok("Cart updated successfully for cart ID: " + cartId);
    }

    @GetMapping("/user-address-details/{userId}")
    public ResponseEntity<List<SavedAddress>> getUserAddressSaved(@PathVariable int userId){
        return ResponseEntity.ok(services.getUserAddressSaved(userId));
    }

    @DeleteMapping("/user-address-delete/{addressId}")
    public ResponseEntity<String> deleteSavedAddress(@PathVariable int addressId){
        services.deleteSavedAddress(addressId);
        return ResponseEntity.ok("Address deleted successfully with ID: " + addressId);
    }

    @PostMapping("/user-address-update/{addressId}")
    public ResponseEntity<SavedAddress> updateSavedAddress(@RequestBody SavedAddress userAddressDetails, @PathVariable int addressId){
        return ResponseEntity.ok(services.updateSavedAddress(userAddressDetails, addressId));
    }

    @PostMapping("/add-address")
    public ResponseEntity<SavedAddress> addAddress(@RequestBody SavedAddress savedAddress){
        return ResponseEntity.status(CREATED).body(services.addAddress(savedAddress));
    }

    @GetMapping("/ordered-user-details/{orderId}")
    public ResponseEntity<UserAddressDetails> orderedUserDetails(@PathVariable int orderId){
        return ResponseEntity.ok(services.orderedUserDetails(orderId));
    }

    @GetMapping("update-track-record/{orderId}/{desc}")
    public ResponseEntity<String> updateTrackRecord(@PathVariable int orderId, @PathVariable String desc){
        services.updateTrackRecord(orderId, desc);
        return ResponseEntity.ok("Track record updated successfully for order ID: " + orderId);
    }

    @PostMapping("/update-prod-update/{prodId}")
    public ResponseEntity<String> updateProd(@PathVariable int prodId, @RequestBody ResponseOrderDetails.ProductDto productDto){
        return ResponseEntity.ok(services.updateProd(prodId, productDto));
    }

    @GetMapping("/deals")
    public ResponseEntity<List<Products>> getDeals(){
        return ResponseEntity.ok(prodRepo.findByDealOfWeekTrue());
    }

    @GetMapping("/total-cart-item/{userId}")
    public ResponseEntity<Integer> getTotalNoCartItem(@PathVariable long userId){
        return ResponseEntity.ok(cartRepository.findCartByUserId(userId).size());
    }

    @GetMapping("/total-wishlist-item/{userId}")
    public ResponseEntity<Integer> getTotalNoWishListItem(@PathVariable long userId){
        return ResponseEntity.ok(wishlistRepo.findByUserId(userId).size());
    }
}