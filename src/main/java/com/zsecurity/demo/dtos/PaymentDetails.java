package com.zsecurity.demo.dtos;

import lombok.Data;

@Data
public class PaymentDetails {
    private String razorpay_order_id;
    private String razorpay_payment_id;
    private String razorpay_signature;
}
