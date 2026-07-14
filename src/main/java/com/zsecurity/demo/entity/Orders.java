package com.zsecurity.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "userId")
    private Users userId;

    private double totalPrice;

    @CreationTimestamp
    private LocalDateTime orderedAt;

    private String status;

    @OneToMany(mappedBy = "orderId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItems> items = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "user_Address")
    private UserAddressDetails userAddressDetails;

    private String description;

    // Razorpay fields
    private String paymentMethod;
    private String paymentStatus;      // PENDING / COMPLETED
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;

    private String refundMethod; // UPI or BANK
    private String refundUpi;
    private String bankAccountNumber;
    private String bankIfsc;
    private String refundStatus; // NONE, REQUESTED, APPROVED, COMPLETED
    private String refundId;

    private String returnReason;

    private int discountAmount;

}
