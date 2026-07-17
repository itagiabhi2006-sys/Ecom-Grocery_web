package com.zsecurity.demo.controllers;

import com.zsecurity.demo.dtos.RazorpayOrderRequest;
import com.zsecurity.demo.dtos.RefundRequest;
import com.zsecurity.demo.dtos.ResponseOrderDetails;
import com.zsecurity.demo.entity.*;
import com.zsecurity.demo.exceptions.IncorrectOldPasswordException;
import com.zsecurity.demo.exceptions.UserAlreadyExistsWithEmail;
import com.zsecurity.demo.repositories.OrderRepo;
import com.zsecurity.demo.repositories.UserRepo;
import com.zsecurity.demo.services.TempTokenService;
import com.zsecurity.demo.services.ImageServices;
import com.zsecurity.demo.services.AuthServices;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "https://ecom-grocery-web.vercel.app"}, allowCredentials = "true")
public class UserController {

    @Autowired
    AuthServices authServices;

    @Autowired
    OrderRepo orderRepo;

    @Autowired
    ImageServices imageServices;

    @Autowired
    TempTokenService tempTokenService;

    @Autowired
    UserRepo userRepo;

    @PostMapping("/reg")
    public ResponseEntity<Users> register(@Valid @RequestBody Users users) throws UserAlreadyExistsWithEmail {
        return ResponseEntity.ok(authServices.register(users));
    }

    @PostMapping("/login")
    public ResponseEntity<RefundRequest.UserDTO> logIn(@Valid @RequestBody Users users, HttpServletResponse response){
        Users users1 = userRepo.findByEmail(users.getEmail()).orElseThrow();
        if(users1.getRoles().name().equals("USER")){
            return ResponseEntity.ok(authServices.logIn(users, response));
        }
        else{
            throw new RuntimeException("Invalid Admin Credentials");
        }
    }

    @PostMapping("/forget-password")
    public ResponseEntity<String> forgetPassword(@Valid @RequestBody RazorpayOrderRequest.AppRequest appRequest){
        return ResponseEntity.ok(authServices.forgetPassword(appRequest));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResponseOrderDetails.ResetOtp resetOtp) throws Exception {
        return ResponseEntity.ok(authServices.ResetPassword(resetOtp));
    }

    @GetMapping("/me")
    public ResponseEntity<RefundRequest.UserDTO> getCurrentUser(@AuthenticationPrincipal Users user) {
        RefundRequest.UserDTO userDTO = RefundRequest.UserDTO.builder()
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .imageURL(user.getImageLink())
                .gender(user.getGender())
                .dob(user.getDob())
                .lastName(user.getLastName())
                .roles(user.getRoles().name())
                .build();
        return ResponseEntity.ok(userDTO);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<String> refreshToken(HttpServletRequest request, HttpServletResponse response){
        return ResponseEntity.ok(authServices.refreshToken(request, response));
    }

    @PostMapping("/logoutt")
    public ResponseEntity<String> logOutcont(HttpServletResponse response, HttpServletRequest request){
        return ResponseEntity.ok(authServices.logOutcont(request, response));
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePasswords(@RequestBody ResponseOrderDetails.ChangePassword changePassword) throws IncorrectOldPasswordException {
        return ResponseEntity.ok(authServices.changePasswords(changePassword));
    }

    @PostMapping("/upload")
    public ResponseEntity<String> getImage(@RequestParam("file") MultipartFile file, @RequestParam("name") String name) throws IOException {
        return ResponseEntity.ok(imageServices.getImageUrl(file, name));
    }

    @PostMapping("/change")
    public ResponseEntity<String> changeImg(@RequestParam("file") MultipartFile file, @RequestParam("name") String name) throws IOException {
        return ResponseEntity.ok(imageServices.changeImg(file, name));
    }

    @PostMapping("/remove")
    public ResponseEntity<String> removeImage(@RequestParam("name") String name) throws IOException {
        imageServices.removeImage(name);
        return ResponseEntity.ok("Image removed successfully: " + name);
    }

    @PostMapping("/verify-temptoken")
    public ResponseEntity<RefundRequest.UserDTO> verifyJwt(@RequestParam("token") String token){
        return ResponseEntity.ok(tempTokenService.verifyAndDeleteToken(token));
    }

    @PostMapping("/update-profile")
    public ResponseEntity<String> updateProfile(@RequestBody RefundRequest.UserDTO userDTO){
        Users users = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        authServices.updateProfile(userDTO, users.getEmail());
        return ResponseEntity.ok("Profile updated successfully");
    }

    @GetMapping("/api/admin/users")
    public ResponseEntity<List<Users>> getAllUser(){
        return ResponseEntity.ok(authServices.getAllUSer());
    }

    @GetMapping("/api/admin/analytics/overview")
    public ResponseEntity<ResponseOrderDetails.GetAllUserOverview> getAllUserOverview(){
        return ResponseEntity.ok(authServices.getAllUserOverview());
    }

    @PutMapping("/api/admin/users/{id}/deactivate")
    public ResponseEntity<String> deactivateUSer(@PathVariable long id){
        authServices.deactivateUSer(id);
        return ResponseEntity.ok("User deactivated successfully with ID: " + id);
    }

    @DeleteMapping("/api/admin/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable long id){
        authServices.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully with ID: " + id);
    }

    @GetMapping("/api/user/{id}")
    public ResponseEntity<Users> getUserByID(@PathVariable long id){
        return ResponseEntity.ok(authServices.getUserByID(id));
    }

    @PostMapping("/order/cod-refund-details/{orderId}")
    public ResponseEntity<String> submitRefundDetails(@PathVariable Integer orderId,
                                                      @RequestBody RefundRequest req){
        return ResponseEntity.ok(authServices.submitRefundDetails(orderId, req));
    }
}