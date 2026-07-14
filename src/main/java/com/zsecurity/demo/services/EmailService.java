package com.zsecurity.demo.services;

import com.zsecurity.demo.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void emailGenerate(Users users) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(users.getEmail());
            message.setSubject("🎉 Welcome to Abhi's App!");
            message.setText(
                    "Hello " + users.getUsername() + ",\n\n" +
                            "We're thrilled to have you join our community! 🚀\n" +
                            "Start exploring our features and make the most out of your journey with us.\n\n" +
                            "Warm regards,\n" +
                            "Abhi's App Team"
            );
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

    public void emailToReset(Users users, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(users.getEmail());
            message.setSubject("🔑 Password Reset Request");
            message.setText(
                    "Hi " + users.getUsername() + ",\n\n" +
                            "We received a request to reset your password. \n" +
                            "Your One-Time Password (OTP) is: " + otp + "\n\n" +
                            "⚠️ This OTP will expire in 10 minutes.\n\n" +
                            "If you didn't request this, please ignore this email.\n\n" +
                            "Regards,\n" +
                            "Abhi's App Team"
            );
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

    public void resetSuccessfulMessage(String email) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("✅ Password Reset Successful");
            message.setText(
                    "Hello,\n\n" +
                            "Your password has been successfully reset. You can now log in with your new password.\n\n" +
                            "If you didn't perform this action, please contact our support team immediately.\n\n" +
                            "Best regards,\n" +
                            "Abhi's App Security Team"
            );
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

    public void generateMailForCreatingOrder(Users user, Orders order, List<OrderItems> items) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("🛒 Order Confirmed! Order #" + order.getId());

            StringBuilder sb = new StringBuilder();
            sb.append("Hello ").append(user.getUsername()).append(",\n\n");
            sb.append("Thank you for your order! Here's your order summary:\n");
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            sb.append("Order ID     : #").append(order.getId()).append("\n");
            sb.append("Order Status : ").append(order.getStatus()).append("\n");
            sb.append("Payment Mode : ").append(order.getPaymentMethod()).append("\n");
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            sb.append("📦 Items Ordered:\n\n");

            for (OrderItems item : items) {
                sb.append("  • ").append(item.getProdId().getTitle())
                        .append(" x").append(item.getQuantity())
                        .append("  →  ₹").append(String.format("%.2f", item.getPrice()))
                        .append("\n");
            }

            sb.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

            if (order.getDiscountAmount() > 0) {
                sb.append("Discount Applied : -₹").append(order.getDiscountAmount()).append("\n");
            }

            sb.append("Total Amount     :  ₹").append(String.format("%.2f", order.getTotalPrice())).append("\n");
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

            UserAddressDetails addr = order.getUserAddressDetails();
            sb.append("📍 Delivery Address:\n");
            sb.append("   ").append(addr.getFullName()).append("\n");
            sb.append("   ").append(addr.getAddress()).append(", ").append(addr.getCity()).append("\n");
            sb.append("   Pincode: ").append(addr.getPincode()).append("\n");
            sb.append("   Phone  : ").append(addr.getPhone()).append("\n\n");

            sb.append("We'll notify you once your order is shipped. 🚚\n\n");
            sb.append("Warm regards,\n");
            sb.append("Kirani Store Team");

            message.setText(sb.toString());
            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

    public void emailForAbandonedCart(Users user, List<Products> randomProducts) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("🛍️ You left something behind! Special picks just for you");

            StringBuilder sb = new StringBuilder();
            sb.append("Hey ").append(user.getUsername()).append("! 👋\n\n");
            sb.append("You have items waiting in your cart at Kirani Store.\n");
            sb.append("While you're at it, check out these special offers:\n\n");
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            sb.append("🔥 Handpicked Deals For You:\n\n");

            for (Products p : randomProducts) {
                String tag = p.isDealOfWeek() ? "Deal of the Week"
                        : p.isFestivalOffer() ? "Festival Offer"
                        : "Special Offer";
                sb.append("  • ").append(p.getTitle())
                        .append("  |  ₹").append(String.format("%.2f", p.getPrice()))
                        .append("  [").append(tag).append("]")
                        .append("\n");
            }

            sb.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            sb.append("Don't miss out — these deals won't last long! ⏳\n\n");
            sb.append("Warm regards,\n");
            sb.append("Kirani Store Team");

            message.setText(sb.toString());
            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

    public void emailForFestivalOffer(Users user, Festival festival) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("🎉 Festival Alert: " + festival.getName() + " Offers Are Live!");

            StringBuilder sb = new StringBuilder();
            sb.append("Hey ").append(user.getUsername()).append("! 🎊\n\n");
            sb.append("Today is ").append(festival.getName()).append(" and we have exclusive deals just for you!\n\n");
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            sb.append("🛍️  Festival: ").append(festival.getName()).append("\n");
            sb.append("📅  Valid   : ").append(festival.getStartDate()).append(" → ").append(festival.getEndDate()).append("\n");
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
            sb.append("Hurry! Festival deals are live for a limited time only. ⏳\n");
            sb.append("Visit Kirani Store now and grab your favourites before they're gone!\n\n");
            sb.append("Warm regards,\n");
            sb.append("Kirani Store Team");

            message.setText(sb.toString());
            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

    public void emailForOrderStatusUpdate(Users user, Orders order, String stage, String desc) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject(getSubjectForStage(stage) + " | Order #" + order.getId());

            StringBuilder sb = new StringBuilder();
            sb.append("Hello ").append(user.getUsername()).append(",\n\n");
            sb.append(getBodyIntroForStage(stage)).append("\n\n");
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            sb.append("Order ID     : #").append(order.getId()).append("\n");
            sb.append("Status       : ").append(stage).append("\n");
            sb.append("Payment Mode : ").append(order.getPaymentMethod()).append("\n");

            if (desc != null && !desc.isBlank()) {
                sb.append("Note         : ").append(desc).append("\n");
            }

            if (stage.equals("Refund Initiated") || stage.equals("Refund Processing") || stage.equals("Refund Completed")) {
                sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                sb.append("💰 Refund Info:\n");
                sb.append("   Amount : ₹").append(String.format("%.2f", order.getTotalPrice())).append("\n");
                if (order.getRefundUpi() != null && !order.getRefundUpi().isBlank()) {
                    sb.append("   UPI    : ").append(order.getRefundUpi()).append("\n");
                }
                if (order.getBankAccountNumber() != null && !order.getBankAccountNumber().isBlank()) {
                    sb.append("   Bank   : ").append(order.getBankAccountNumber()).append("\n");
                }
            }

            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
            sb.append("For any queries, feel free to reach out to us.\n\n");
            sb.append("Warm regards,\n");
            sb.append("Kirani Store Team");

            message.setText(sb.toString());
            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

    private String getSubjectForStage(String stage) {
        return switch (stage) {
            case "Ordered"           -> "✅ Order Confirmed";
            case "Packed"            -> "📦 Order Packed";
            case "Shipped"           -> "🚚 Order Shipped";
            case "Out for Delivery"  -> "🏃 Out for Delivery";
            case "Delivered"         -> "🎉 Order Delivered";
            case "Canceled"          -> "❌ Order Canceled";
            case "Return Requested"  -> "↩️ Return Requested";
            case "Return Picked Up"  -> "🔄 Return Picked Up";
            case "Returned"          -> "✔️ Item Returned";
            case "Refund Initiated"  -> "💸 Refund Initiated";
            case "Refund Processing" -> "⏳ Refund Processing";
            case "Refund Completed"  -> "✅ Refund Completed";
            default                  -> "📋 Order Update";
        };
    }

    private String getBodyIntroForStage(String stage) {
        return switch (stage) {
            case "Ordered"           -> "Your order has been successfully placed. We'll get it ready for you soon!";
            case "Packed"            -> "Great news! Your order has been packed and is ready to ship.";
            case "Shipped"           -> "Your order is on its way! Sit tight, it'll be there soon. 🚚";
            case "Out for Delivery"  -> "Your order is out for delivery today. Please be available to receive it!";
            case "Delivered"         -> "Your order has been delivered. Enjoy your purchase! 😊";
            case "Canceled"          -> "Your order has been canceled as requested. We hope to serve you again soon.";
            case "Return Requested"  -> "We've received your return request and will arrange a pickup shortly.";
            case "Return Picked Up"  -> "Your return has been picked up and is on its way back to us.";
            case "Returned"          -> "We've received your returned item. Your refund will be processed soon.";
            case "Refund Initiated"  -> "Your refund has been initiated and is being processed.";
            case "Refund Processing" -> "Your refund is currently being processed. It'll reflect soon.";
            case "Refund Completed"  -> "Your refund has been successfully completed. 💰";
            default                  -> "There's an update on your order. Please check the details below.";
        };
    }
}