package com.zsecurity.demo.dtos;


import com.zsecurity.demo.entity.UserAddressDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefundRequest {

    private String method; // UPI or BANK
    private String upi;

    private String accountNumber;
    private String ifsc;

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDTO {

        private long id;
        private String firstName;
        private String email;
        private String imageURL;
        private String lastName;
        private LocalDate dob;
        private String gender;
        private String roles;
        private Boolean isActive = true;
        private Boolean isActiveNow = false;

    }

    public static class OrderRequest {
        private UserAddressDetails userAddressDetails;
        private List<ResponseOrderDetails.OrderDto> orders;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemRequest {
        private int productId;
        private int quantity;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderFullDetails {

        private long userId;
        private int addressId;
        private String paymentMethod;
        private Map<String, String> paymentDetails;
        private List<OrderItemRequest> items;
        private boolean applyOffer;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class OrderAdmin {

        private int orderID;
        private UserDTO userId;
        private String paymentMode;
        private double totalPrice;
        private LocalDateTime orderedAt;
        private String status;
        private int addressId;
        private UserAddressDetails userAddressDetails;
        private List<ResponseOrderDetails> ResponseOrderDetails;
        private String refundStatus;
        private String refundMethod;
        private String refundUpi;
        private String bankAccountNumber;
        private String bankIfsc;
        private String refundId;
        private String returnReason;

    }
}
