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

    // Map<token, EmailAndExpiry>
    private static class TokenData {
        String email;
        long expiry;
        TokenData(String email, long expiry) {
            this.email = email;
            this.expiry = expiry;
        }
    }

    private final ConcurrentHashMap<String, TokenData> tokens = new ConcurrentHashMap<>();

    // Create token valid for 45 seconds
    public String createTempToken(String userId) {
        String token = UUID.randomUUID().toString();
        long expiry = System.currentTimeMillis() + 45 * 1000; // 45 sec
        tokens.put(token, new TokenData(userId, expiry));
        return token;
    }

    // Verify token and delete it (one-time use)
    public RefundRequest.UserDTO verifyAndDeleteToken(String token) {
        TokenData data = tokens.get(token);
        if (data == null || System.currentTimeMillis() > data.expiry) {
            tokens.remove(token);
            throw new RuntimeException("Invalid or expired token");
        }
        tokens.remove(token);
        Users users = userRepo.findByEmail(data.email).orElseThrow(() -> new RuntimeException("User not found"));
        return RefundRequest.UserDTO.builder().email(users.getEmail()).firstName(users.getFirstName()).id(users.getId()).build();
    }
}
