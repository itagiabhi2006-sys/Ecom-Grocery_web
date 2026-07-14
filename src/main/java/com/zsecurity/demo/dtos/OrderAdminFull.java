package com.zsecurity.demo.dtos;

import com.zsecurity.demo.entity.UserAddressDetails;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderAdminFull {

    private int orderId;

    private RefundRequest.UserDTO user;
    private UserAddressDetails address;

    private double totalPrice;

    private String status;
    private String paymentMode;
    private String refundStatus;

    private LocalDateTime orderedAt;

    private List<ProductQuantityResponse> products;
    private int discountAmount;
    private String reasonForReturn;
}
