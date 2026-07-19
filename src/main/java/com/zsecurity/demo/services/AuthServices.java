package com.zsecurity.demo.services;

import com.zsecurity.demo.dtos.RazorpayOrderRequest;
import com.zsecurity.demo.dtos.RefundRequest;
import com.zsecurity.demo.dtos.ResponseOrderDetails;
import com.zsecurity.demo.entity.*;
import com.zsecurity.demo.enums.Roles;
import com.zsecurity.demo.exceptions.*;
import com.zsecurity.demo.repositories.OrderRepo;
import com.zsecurity.demo.repositories.UserRepo;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Email;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class AuthServices {

    @Autowired
    UserRepo userRepo;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtService jwtService;

    @Autowired
    EmailService emailService;

    @Autowired
    OrderRepo orderRepo;

    // ------------------------------------------------------------------
    // Cross-site cookie helpers
    //
    // jakarta.servlet.http.Cookie has no setSameSite() method, so the
    // SameSite attribute has to be written as a raw Set-Cookie header.
    // On Railway (and most PaaS deployments) the frontend and backend
    // live on different subdomains, which makes every API call
    // cross-site from the browser's point of view. For a cross-site
    // cookie to be accepted AND sent back on later requests it must be
    // Secure + SameSite=None. Without this, login sets the cookie, the
    // browser silently drops it on the next request, and every
    // auth-dependent endpoint returns 400/401.
    // ------------------------------------------------------------------
    private void addAuthCookie(HttpServletResponse response, String name, String value, int maxAgeSeconds) {
        String cookieHeader = String.format(
                "%s=%s; Path=/; Max-Age=%d; HttpOnly; Secure; SameSite=None",
                name, value, maxAgeSeconds
        );
        response.addHeader("Set-Cookie", cookieHeader);
    }

    private void clearAuthCookie(HttpServletResponse response, String name) {
        String cookieHeader = String.format(
                "%s=; Path=/; Max-Age=0; HttpOnly; Secure; SameSite=None",
                name
        );
        response.addHeader("Set-Cookie", cookieHeader);
    }

    private Cookie findCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(cookie -> name.equals(cookie.getName()))
                .findFirst()
                .orElse(null);
    }

    public Users register(Users users) throws UserAlreadyExistsWithEmail {
        Users users1 = userRepo.findByEmail(users.getEmail()).orElse(null);
        if (users1 == null) {
            users.setRoles(Roles.USER);
            users.setIsActive(true);
            users.setPasswords(passwordEncoder.encode(users.getPassword()));
            users.setOtpVerificationTime(null);
            users.setOtp(null);
            emailService.emailGenerate(users);
            return userRepo.save(users);
        } else {
            throw new UserAlreadyExistsWithEmail("User with email " + users1.getEmail() + " is already Exists");
        }
    }

    public RefundRequest.UserDTO logIn(Users users, HttpServletResponse response) {
        if (!(users.getIsActive())) {
            throw new RuntimeException();
        } else {
            try {
                Authentication authentication = authenticationManager
                        .authenticate(new UsernamePasswordAuthenticationToken(users.getUsername(), users.getPassword()));

                Boolean b = authentication.isAuthenticated();
                log.info("{}", b);

                String token = jwtService.generateToken(users);
                addAuthCookie(response, "jwtToken", token, 10 * 60);

                String refToken = jwtService.generateRefreshToken(users);
                addAuthCookie(response, "refToken", refToken, 2592000);

                Users users1 = userRepo.findByEmail(users.getEmail()).orElseThrow();
                users1.setIsActiveNow(true);
                userRepo.save(users1);

                return RefundRequest.UserDTO.builder()
                        .email(users1.getEmail())
                        .firstName(users1.getFirstName())
                        .id(users1.getId())
                        .roles(users1.getRoles().name())
                        .build();
            } catch (BadCredentialsException e) {
                throw new BadCredentialsException("Email or Password Incorrect");
            }
        }
    }

    String email = "";

    @Transactional
    public String forgetPassword(RazorpayOrderRequest.AppRequest appRequest) {
        String email = appRequest.getEmail();

        Users users = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(5);

        if (users.getOtp() != null) {
            users.setOtp(null);
            users.setOtpVerificationTime(null);
        }

        users.setOtp(otp);
        users.setOtpVerificationTime(expirationTime);

        try {
            emailService.emailToReset(users, otp);
            userRepo.save(users);
            return "OTP sent successfully";
        } catch (Exception e) {
            throw new RuntimeException("Failed to process forget password request", e);
        }
    }

    public String ResetPassword(ResponseOrderDetails.ResetOtp resetOtp) throws Exception {
        Users users = userRepo.findByEmail(resetOtp.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Wrong Credentials"));

        if (!users.getOtp().equals(resetOtp.getOtp())) {
            throw new InvalidOtpException("Invalid! OTP");
        }

        if (users.getOtpVerificationTime().isBefore(LocalDateTime.now())) {
            throw new InvalidOtpExpired("OTP Expired");
        }

        if (!resetOtp.getNewPassword().equals(resetOtp.getConfirmPassword())) {
            throw new Exception("Passwords doesn't match");
        }

        users.setOtp(null);
        users.setOtpVerificationTime(null);
        users.setPasswords(passwordEncoder.encode(resetOtp.getNewPassword()));
        userRepo.save(users);
        emailService.resetSuccessfulMessage(users.getEmail());
        return "Password reset successful!";
    }

    @Transactional
    public String refreshToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie refCookie = findCookie(request, "refToken");

        if (refCookie == null) {
            // No refresh cookie arrived at all - can't refresh anything.
            throw new RuntimeException("No refresh token present");
        }

        String email = jwtService.getEmailByToken(refCookie.getValue());
        Users users = userRepo.findByEmail(email).orElseThrow();

        if (!(users.getIsActiveNow())) {
            users.setIsActiveNow(true);
            userRepo.save(users);
        }

        // Invalidate old cookies, issue fresh ones
        String newToken = jwtService.generateToken(users);
        addAuthCookie(response, "jwtToken", newToken, 10 * 60);

        String newRefToken = jwtService.generateRefreshToken(users);
        addAuthCookie(response, "refToken", newRefToken, 2592000);

        return "";
    }

    public String logOutcont(HttpServletRequest request, HttpServletResponse response) {
        Cookie refCookie = findCookie(request, "refToken");

        if (refCookie != null) {
            String email = jwtService.getEmailByToken(refCookie.getValue());
            Users users = userRepo.findByEmail(email).orElse(null);
            if (users != null) {
                users.setIsActiveNow(false);
                userRepo.save(users);
            }
        }

        clearAuthCookie(response, "refToken");
        clearAuthCookie(response, "jwtToken");

        return "Logged out SuccessFully";
    }

    public String changePasswords(ResponseOrderDetails.ChangePassword changePassword) throws IncorrectOldPasswordException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = userDetails.getUsername();
        Users users1 = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));

        if (users1 != null && passwordEncoder.matches(changePassword.getOldPassword(), users1.getPasswords())) {
            users1.setPasswords(passwordEncoder.encode(changePassword.getNewPassword()));
            userRepo.save(users1);
            return "Password Changed Successfully";
        } else {
            throw new IncorrectOldPasswordException("Mismatch in passwords");
        }
    }

    public void updateProfile(RefundRequest.UserDTO userDTO, @Email(message = "Please enter valid email") String email) {
        Users users = userRepo.findByEmail(email).orElse(null);
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setSkipNullEnabled(true);
        userDTO.setId(users.getId());
        System.out.println(userDTO);
        mapper.map(userDTO, users);
        assert users != null;
        userRepo.save(users);
    }

    public List<Users> getAllUSer() {
        return userRepo.findAll();
    }

    public void deactivateUSer(long id) {
        Users users = userRepo.findById(id).orElse(null);
        assert users != null;
        userRepo.save(users);
    }

    public void deleteUser(long id) {
        userRepo.deleteById(id);
    }

    public Users getUserByID(long id) {
        return userRepo.findById(id).orElseThrow();
    }

    public ResponseOrderDetails.GetAllUserOverview getAllUserOverview() {
        List<Users> usersList = userRepo.findAll();
        return ResponseOrderDetails.GetAllUserOverview.builder()
                .activeUser(usersList.size())
                .totalUser(usersList.size())
                .dailySignup(usersList.size())
                .build();
    }

    @Scheduled(fixedRate = 60000)
    public void updateOfflineUsers() {
        System.out.println("Scheduler executed at: " + LocalDateTime.now());

        LocalDateTime threshold = LocalDateTime.now().minusSeconds(40);

        List<Users> users = userRepo.findAll();
        for (Users user : users) {
            if (user.getLastActiveAt() != null &&
                    user.getLastActiveAt().isBefore(threshold)) {
                System.out.println(user.getEmail());
                user.setIsActiveNow(false);
                userRepo.save(user);
            }
            System.out.println(user.getId());
        }
    }

    public String submitRefundDetails(Integer orderId, RefundRequest req) {
        Orders order = orderRepo.findById(orderId).orElseThrow();

        // Check if order is COD
        if (!"COD".equals(order.getPaymentMethod())) {
            return "Refund details only required for COD orders";
        }

        // Check if order delivered
        if (!"Returned".equals(order.getStatus())) {
            return "Refund allowed only after delivery";
        }

        // Check if refund already requested
        if (order.getRefundStatus() != null) {
            return "Refund request already submitted";
        }

        // Check refund method
        if ("UPI".equals(req.getMethod())) {
            if (req.getUpi() == null || req.getUpi().isEmpty()) {
                return "UPI ID required";
            }
            order.setRefundUpi(req.getUpi());
        }

        if ("BANK".equals(req.getMethod())) {
            if (req.getAccountNumber() == null || req.getIfsc() == null) {
                return "Bank account details required";
            }
            order.setBankAccountNumber(req.getAccountNumber());
            order.setBankIfsc(req.getIfsc());
        }

        order.setRefundMethod(req.getMethod());
        order.setRefundStatus("PENDING");

        orderRepo.save(order);

        return "Refund details submitted successfully";
    }
}