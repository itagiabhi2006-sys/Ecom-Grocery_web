package com.zsecurity.demo.services;

import com.zsecurity.demo.dtos.RefundRequest;
import com.zsecurity.demo.entity.Users;
import com.zsecurity.demo.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

@Service
public class TempTokenService {

    @Autowired
    UserRepo userRepo;

    // Map<token, expiryTimestamp>
    private final ConcurrentHashMap<String, Long> tokens = new ConcurrentHashMap<>();

    // Create token valid for 45 seconds
    String email = "";
    public String createTempToken(String userId) {
        email = userId;
        String token = UUID.randomUUID().toString();
        long expiry = System.currentTimeMillis() + 45 * 1000; // 45 sec
        tokens.put(token, expiry);
        return token;
    }

    // Verify token and delete it (one-time use)
    public RefundRequest.UserDTO verifyAndDeleteToken(String token) {
        Long expiry = tokens.get(token);
        if (expiry == null || System.currentTimeMillis() > expiry) {
            tokens.remove(token);
            throw  new RuntimeException("default msg");
        }
        tokens.remove(token);
        Users users = userRepo.findByEmail(email).orElseThrow();
        return RefundRequest.UserDTO.builder().email(users.getEmail()).firstName(users.getFirstName()).id(users.getId()).build();
    }
}
