package com.zsecurity.demo.dtos;

import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseOrderDetails {
    private int orderId;
    private LocalDateTime timeOfOrder;
    private double totalPrice;
    private String status;
    private List<ProductQuantityResponse> productList;
    private String paymentMode;
    private String refundUpi;
    private String bankAccountNumber;
    private String refundStatus;
    private String returnReason;
    private int discountAmount;

    @Component
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ApiError {

        private HttpStatus httpStatus;
        private String message;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ChangePassword {

        private String oldPassword;
        private String newPassword;
    }

    @Component
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GetAllUserOverview {
        private int totalUser;
        private int activeUser;
        private int dailySignup;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class OrderDto {

        private String description;
        private int id;
        private String imageURL;
        private double price;
        private int qty;
        private String title;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class OrderItemDto {

        private ProductDto prodId;
        private int quantity;
        private double price;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductDto {

        private int id;
        private String title;
        private String description;
        private double price;
        private String imageURL;
        private int catId;
        private int stock;
        private int margin;

    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TrackDto {
        private int orderId;
        private String Stage;
        private LocalDateTime updatedDate;
        private String description;


    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ShowProd {

        private String title;
        private String description;
        private double price;
        private String imageURL;
        private String category;
        private int stock;
        private int id;
        private int margin;
    }

    @Component
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @ToString
    public static class ResetOtp {
        private String otp;
        private String newPassword;
        private String confirmPassword;
        private String email;
    }
}

