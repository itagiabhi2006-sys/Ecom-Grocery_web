package com.zsecurity.demo.controllers;

import com.razorpay.RazorpayException;
import com.zsecurity.demo.dtos.RazorpayOrderRequest;
import com.zsecurity.demo.entity.Orders;
import com.zsecurity.demo.repositories.OrderRepo;
import com.zsecurity.demo.services.PaymentService;
import com.zsecurity.demo.services.RazorpayService;
import com.zsecurity.demo.services.AuthServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payment")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private RazorpayService razorpayService;

    @Autowired
    OrderRepo orderRepo;

    @Autowired
    AuthServices authServices;

    @Autowired
    PaymentService paymentService;

    @PostMapping("/create-razorpay-order")
    public ResponseEntity<?> createOrder(@RequestBody RazorpayOrderRequest req) {
        try {
            return ResponseEntity.ok(razorpayService.createOrder(req.getAmount()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed: " + e.getMessage());
        }
    }

    @PostMapping("/admin/refund/{orderId}")
    public ResponseEntity<Map<String, Object>> refundOrder(@PathVariable Integer orderId) throws RazorpayException {
        return ResponseEntity.ok(paymentService.refundOrder(orderId));
    }

    @GetMapping("/admin/refund-status/{refundId}")
    public ResponseEntity<Map<String, Object>> checkRefund(@PathVariable String refundId) throws RazorpayException {
        return ResponseEntity.ok(razorpayService.checkRefundStatus(refundId));
    }

    @PostMapping("/admin/approve-cod-refund/{orderId}")
    public ResponseEntity<String> approveCodRefund(@PathVariable Integer orderId){
        return ResponseEntity.ok(paymentService.approveCodRefund(orderId));
    }

    @PostMapping("/admin/cod-refund-complete/{orderId}")
    public ResponseEntity<String> completeCodRefund(@PathVariable Integer orderId){
        return ResponseEntity.ok(paymentService.completeCodRefund(orderId));
    }
}