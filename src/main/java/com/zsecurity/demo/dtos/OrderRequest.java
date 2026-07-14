package com.zsecurity.demo.dtos;

import com.zsecurity.demo.entity.UserAddressDetails;
import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {

    private UserAddressDetails userAddressDetails;
    private List<ResponseOrderDetails.OrderDto> orderDtoList;

    private String paymentMethod;   // "COD" / "ONLINE"
    private PaymentDetails paymentDetails;
}
