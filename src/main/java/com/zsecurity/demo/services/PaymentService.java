package com.zsecurity.demo.services;

import com.razorpay.RazorpayException;
import com.zsecurity.demo.entity.Orders;
import com.zsecurity.demo.repositories.OrderRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PaymentService {

    @Autowired
    OrderRepo orderRepo;

    @Autowired
    RazorpayService razorpayService;

    public Map<String, Object> refundOrder(Integer orderId) throws RazorpayException {
        Orders order = orderRepo.findById(orderId).orElseThrow();

        Map<String, Object> refundResponse = razorpayService.refundOrder(
                order.getRazorpayPaymentId(),
                order.getTotalPrice()
        );

        order.setRefundId((String) refundResponse.get("refundId"));
        order.setRefundStatus((String) refundResponse.get("status"));

        orderRepo.save(order);

        return refundResponse;
    }

    public String approveCodRefund(Integer orderId) {
        Orders order = orderRepo.findById(orderId).orElseThrow();

        if(!order.getPaymentMethod().equals("COD")){
            return "This order is not COD";
        }

        if(order.getRefundMethod() == null){
            return "Customer has not submitted refund details";
        }

        order.setRefundStatus("APPROVED");
        orderRepo.save(order);

        return "COD refund approved by admin";
    }

    public String completeCodRefund(Integer orderId) {

        Orders order = orderRepo.findById(orderId).orElseThrow();

        if(!order.getPaymentMethod().equals("COD")){
            return "This order is not COD";
        }

        if(!"APPROVED".equals(order.getRefundStatus())){
            return "Refund must be approved first";
        }

        order.setRefundStatus("COMPLETED");
        orderRepo.save(order);

        return "COD refund marked as completed";
    }
}
