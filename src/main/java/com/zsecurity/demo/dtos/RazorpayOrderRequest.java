package com.zsecurity.demo.dtos;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Data
public class RazorpayOrderRequest {
    private Integer amount;

    @Builder
    @Data
    @Component
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AppRequest {
        @Email(message = "Please enter valid email")
        private String email;


    }
}
