package com.zsecurity.demo.controllers;

import com.zsecurity.demo.dtos.OrderAdminFull;
import com.zsecurity.demo.dtos.OrderAdminLite;
import com.zsecurity.demo.dtos.RefundRequest;
import com.zsecurity.demo.dtos.ResponseOrderDetails;
import com.zsecurity.demo.entity.*;
import com.zsecurity.demo.repositories.ProdRepo;
import com.zsecurity.demo.repositories.UserRepo;
import com.zsecurity.demo.services.AdminService;
import com.zsecurity.demo.services.FestivalService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
public class AdminController {

    @Autowired
    AdminService adminService;

    @Autowired
    UserRepo userRepo;

    @Autowired
    ProdRepo prodRepo;

    @Autowired
    private FestivalService festivalService;

    @PostMapping("/admin/login")
    public ResponseEntity<RefundRequest.UserDTO> logIn(@Valid @RequestBody Users users, HttpServletResponse response){
        return ResponseEntity.ok(adminService.logIn(users, response));
    }

    @GetMapping("/admin/get-products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ResponseOrderDetails.ShowProd>> getAllProducts() {
        List<Products> products = adminService.getAllProducts();
        List<ResponseOrderDetails.ShowProd> response = adminService.mapToShowProd(products);
        return ResponseEntity.ok(response);
    }

    @GetMapping("admin/get-categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Categories>> getAllAdminCategories(){
        return new ResponseEntity<>(adminService.getAllCategories(), OK);
    }

    @PostMapping("admin/add-product")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Products> addProduct(@RequestBody ResponseOrderDetails.ProductDto products){
        return new ResponseEntity<>(adminService.addProduct(products), CREATED);
    }

    @PostMapping("admin/add-cate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Categories> addCategories(@RequestBody Categories categories){
        return new ResponseEntity<>(adminService.addCategories(categories), CREATED);
    }

    @DeleteMapping("admin/delete-user/{uid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUserById(@PathVariable int uid){
        adminService.deleteUserById(uid);
        return ResponseEntity.ok("User deleted successfully with ID: " + uid);
    }

    @GetMapping("admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RefundRequest.UserDTO>> getAllUserReg(){
        return ResponseEntity.ok(adminService.getAllUserReg());
    }

    @GetMapping("admin/users/disable/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> disableUser(@PathVariable String email){
        adminService.disableUser(email);
        return ResponseEntity.ok("User disabled successfully with email: " + email);
    }

    @GetMapping("/admin/orders")
    public ResponseEntity<List<OrderAdminLite>> getAllOrders() {
        return ResponseEntity.ok(adminService.getAllOrdersLite());
    }

    @GetMapping("/admin/orders/{orderId}")
    public ResponseEntity<OrderAdminFull> getOrder(@PathVariable int orderId) {
        return ResponseEntity.ok(adminService.getOrderById(orderId));
    }

    @GetMapping("admin/update-track-record/{orderId}/{stage}/{desc}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateTrackRecord(@PathVariable int orderId, @PathVariable String stage, @PathVariable String desc){
        adminService.updateTrackRecord(orderId, stage, desc);
        return ResponseEntity.ok("Track record updated successfully for order ID: " + orderId);
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<String> heartbeat() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userRepo.findByEmail(username).orElseThrow();

        user.setLastActiveAt(LocalDateTime.now());
        user.setIsActive(true);
        userRepo.save(user);

        return ResponseEntity.ok("updated");
    }

    @PatchMapping("/admin/product/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> patchProduct(
            @PathVariable int id,
            @RequestBody ResponseOrderDetails.ProductDto req
    ) {
        return ResponseEntity.ok(adminService.patchProduct(id, req));
    }

    @PostMapping("/admin/set-deal/{productId}/{discount}")
    public ResponseEntity<String> setDealOfWeek(@PathVariable int productId,
                                                @PathVariable double discount){
        adminService.setDealOfWeek(productId, discount);
        return ResponseEntity.ok("Deal of week set successfully for product ID: " + productId);
    }

    @PostMapping("/admin/remove-deal/{productId}")
    public ResponseEntity<String> removeDeal(@PathVariable Integer productId){
        adminService.removeDeal(productId);
        return ResponseEntity.ok("Deal removed successfully for product ID: " + productId);
    }

    @PostMapping("/add-festival")
    public ResponseEntity<Festival> addFestival(@RequestBody Festival request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(festivalService.addFestival(request));
    }

    @PostMapping("/assign-product-festival")
    public ResponseEntity<FestivalProduct> addFestivalProduct(@RequestBody FestivalProduct request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.addFestivalProduct(request));
    }

    @PostMapping("/add-normal-offer/{prodId}/{disPer}")
    public ResponseEntity<String> addNormalOfferToProduct(@PathVariable int prodId, @PathVariable double disPer){
        adminService.addNormalOfferToProduct(prodId, disPer);
        return ResponseEntity.ok("Normal offer added successfully to product ID: " + prodId);
    }

    @DeleteMapping("/admin/delete-product/{ProdId}")
    public ResponseEntity<String> deleteProd(@PathVariable int ProdId){
        adminService.deleteProdctById(ProdId);
        return ResponseEntity.ok("Product deleted successfully with ID: " + ProdId);
    }
}