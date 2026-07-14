package com.zsecurity.demo.dtos;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderAdminLite {

    private int orderId;

    private String fullName;
    private String email;

    private double totalPrice;
    private String status;
    private String paymentMode;

    private LocalDateTime orderedAt;
}
