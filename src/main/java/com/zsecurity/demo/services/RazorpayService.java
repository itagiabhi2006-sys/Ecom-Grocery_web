package com.zsecurity.demo.services;

import com.zsecurity.demo.config.RazorpayConfig;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Order;
import com.zsecurity.demo.dtos.RefundRequest;
import com.zsecurity.demo.entity.Orders;
import com.zsecurity.demo.repositories.OrderRepo;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.HashMap;
import java.util.Map;

@Service
public class RazorpayService {

    @Autowired
    private RazorpayConfig razorpayConfig;

    @Autowired
    OrderRepo orderRepo;

    public Map<String, Object> createOrder(Integer amount) throws RazorpayException {
        RazorpayClient client = new RazorpayClient(
                razorpayConfig.getKeyId(),
                razorpayConfig.getKeySecret()
        );

        JSONObject orderReq = new JSONObject();
        orderReq.put("amount", amount);
        orderReq.put("currency", "INR");
        orderReq.put("receipt", "order_" + System.currentTimeMillis());

        Order order = client.orders.create(orderReq);

        Map<String, Object> response = new HashMap<>();
        response.put("orderId", order.get("id"));
        response.put("amount", order.get("amount"));
        response.put("currency", order.get("currency"));
        response.put("receipt", order.get("receipt"));

        return response;
    }

    public boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
            String data = orderId + "|" + paymentId;

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    razorpayConfig.getKeySecret().getBytes(), "HmacSHA256"
            );
            mac.init(secretKeySpec);

            byte[] hash = mac.doFinal(data.getBytes());
            String generated = bytesToHex(hash);

            return generated.equals(signature);

        } catch (Exception e) {
            return false;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    public Map<String, Object> refundOrder(String paymentId, double amount) throws RazorpayException {
        RazorpayClient client = new RazorpayClient(
                razorpayConfig.getKeyId(),
                razorpayConfig.getKeySecret()
        );

        JSONObject refundRequest = new JSONObject();
        refundRequest.put("amount", amount * 100);

        com.razorpay.Refund refund = client.payments.refund(paymentId, refundRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("refundId", refund.get("id"));
        response.put("paymentId", refund.get("payment_id"));
        response.put("amount", refund.get("amount"));
        response.put("status", refund.get("status"));
        response.put("createdAt", refund.get("created_at"));

        return response;
    }

    public Map<String, Object> checkRefundStatus(String refundId) throws RazorpayException {
        RazorpayClient client = new RazorpayClient(
                razorpayConfig.getKeyId(),
                razorpayConfig.getKeySecret()
        );

        com.razorpay.Refund refund = client.refunds.fetch(refundId);

        Map<String, Object> response = new HashMap<>();
        response.put("refundId", refund.get("id"));
        response.put("paymentId", refund.get("payment_id"));
        response.put("amount", refund.get("amount"));
        response.put("status", refund.get("status"));
        response.put("createdAt", refund.get("created_at"));

        return response;
    }
}