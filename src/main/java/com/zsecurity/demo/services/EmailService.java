package com.zsecurity.demo.services;

import com.zsecurity.demo.entity.*;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Async;

import java.util.List;

@Component
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private String wrapHtml(String content) {
        return "<!DOCTYPE html>" +
                "<html lang=\"en\">" +
                "<head>" +
                "<style>" +
                "body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; background: #ffffff; padding: 30px; border-radius: 8px; box-shadow: 0 4px 10px rgba(0,0,0,0.05); border-top: 5px solid #2874f0; }" +
                "h1 { color: #222; font-size: 24px; margin-bottom: 20px; }" +
                "h2 { color: #444; font-size: 20px; margin-top: 30px; border-bottom: 1px solid #eee; padding-bottom: 10px; }" +
                "p { font-size: 16px; line-height: 1.6; color: #555; margin-bottom: 15px; }" +
                ".details-table { width: 100%; border-collapse: collapse; margin: 20px 0; }" +
                ".details-table th, .details-table td { padding: 12px; border-bottom: 1px solid #eee; text-align: left; }" +
                ".details-table th { background-color: #fafafa; color: #333; font-weight: 600; }" +
                ".highlight { font-weight: bold; color: #2874f0; }" +
                ".total-row td { font-size: 18px; font-weight: bold; background-color: #fafafa; color: #000; }" +
                ".footer { margin-top: 40px; padding-top: 20px; border-top: 1px solid #eee; text-align: center; color: #999; font-size: 14px; }" +
                ".btn { display: inline-block; padding: 12px 24px; background-color: #2874f0; color: white !important; text-decoration: none; border-radius: 5px; font-weight: bold; margin-top: 20px; }" +
                ".alert { padding: 15px; background-color: #fff3cd; color: #856404; border-radius: 5px; margin: 20px 0; border-left: 5px solid #ffeeba; }" +
                ".success { background-color: #d4edda; color: #155724; border-left: 5px solid #c3e6cb; padding: 15px; border-radius: 5px; margin: 20px 0; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class=\"container\">" +
                content +
                "<div class=\"footer\">" +
                "&copy; " + java.time.Year.now().getValue() + " Kirani Store. All rights reserved.<br>" +
                "Thank you for shopping with us!" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(wrapHtml(htmlContent), true);
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            System.err.println("Failed to send HTML email: " + e.getLocalizedMessage());
        }
    }

    @Async
    public void emailGenerate(Users users) {
        String content = "<h1>🎉 Welcome to Kirani Store!</h1>" +
                "<p>Hello <strong>" + users.getUsername() + "</strong>,</p>" +
                "<p>We're thrilled to have you join our community! 🚀</p>" +
                "<p>Start exploring our features and make the most out of your journey with us.</p>" +
                "<a href=\"#\" class=\"btn\">Explore Now</a>";
        sendHtmlEmail(users.getEmail(), "🎉 Welcome to Kirani Store!", content);
    }

    @Async
    public void emailToReset(Users users, String otp) {
        String content = "<h1>🔑 Password Reset Request</h1>" +
                "<p>Hi <strong>" + users.getUsername() + "</strong>,</p>" +
                "<p>We received a request to reset your password.</p>" +
                "<div style=\"text-align: center; margin: 30px 0;\">" +
                "<span style=\"font-size: 32px; font-weight: bold; letter-spacing: 5px; color: #2874f0;\">" + otp + "</span>" +
                "</div>" +
                "<div class=\"alert\">⚠️ This OTP will expire in 10 minutes.</div>" +
                "<p>If you didn't request this, please ignore this email.</p>";
        sendHtmlEmail(users.getEmail(), "🔑 Password Reset Request", content);
    }

    @Async
    public void resetSuccessfulMessage(String email) {
        String content = "<h1>✅ Password Reset Successful</h1>" +
                "<p>Hello,</p>" +
                "<div class=\"success\">Your password has been successfully reset. You can now log in with your new password.</div>" +
                "<p>If you didn't perform this action, please contact our support team immediately.</p>";
        sendHtmlEmail(email, "✅ Password Reset Successful", content);
    }

    @Async
    public void generateMailForCreatingOrder(Users user, Orders order, List<OrderItems> items) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>🛒 Order Confirmed!</h1>");
        sb.append("<p>Hello <strong>").append(user.getUsername()).append("</strong>,</p>");
        sb.append("<p>Thank you for your order! Here's your order summary:</p>");
        
        sb.append("<h2>Order Details (#").append(order.getId()).append(")</h2>");
        sb.append("<table class=\"details-table\">");
        sb.append("<tr><th>Status</th><td><span style=\"color: #28a745; font-weight: bold;\">").append(order.getStatus()).append("</span></td></tr>");
        sb.append("<tr><th>Payment Mode</th><td>").append(order.getPaymentMethod()).append("</td></tr>");
        sb.append("</table>");

        sb.append("<h2>Items Ordered</h2>");
        sb.append("<table class=\"details-table\">");
        sb.append("<tr><th>Item</th><th>Qty</th><th>Price</th></tr>");
        
        for (OrderItems item : items) {
            sb.append("<tr>");
            sb.append("<td>").append(item.getProdId().getTitle()).append("</td>");
            sb.append("<td>").append(item.getQuantity()).append("</td>");
            sb.append("<td>₹").append(String.format("%.2f", item.getPrice())).append("</td>");
            sb.append("</tr>");
        }
        
        if (order.getDiscountAmount() > 0) {
            sb.append("<tr><td colspan=\"2\" style=\"text-align: right;\">Discount:</td><td style=\"color: #28a745;\">-₹").append(order.getDiscountAmount()).append("</td></tr>");
        }
        
        sb.append("<tr class=\"total-row\"><td colspan=\"2\" style=\"text-align: right;\">Total:</td><td>₹").append(String.format("%.2f", order.getTotalPrice())).append("</td></tr>");
        sb.append("</table>");

        UserAddressDetails addr = order.getUserAddressDetails();
        if (addr != null) {
            sb.append("<h2>Delivery Address</h2>");
            sb.append("<p>");
            sb.append("<strong>").append(addr.getFullName()).append("</strong><br>");
            sb.append(addr.getAddress()).append(", ").append(addr.getCity()).append("<br>");
            sb.append("Pincode: ").append(addr.getPincode()).append("<br>");
            sb.append("Phone: ").append(addr.getPhone());
            sb.append("</p>");
        }

        sb.append("<p>We'll notify you once your order is shipped. 🚚</p>");
        
        sendHtmlEmail(user.getEmail(), "🛒 Order Confirmed! Order #" + order.getId(), sb.toString());
    }

    @Async
    public void emailForAbandonedCart(Users user, List<Products> randomProducts) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>🛍️ You left something behind!</h1>");
        sb.append("<p>Hey <strong>").append(user.getUsername()).append("</strong>! 👋</p>");
        sb.append("<p>You have items waiting in your cart at Kirani Store.</p>");
        sb.append("<p>While you're at it, check out these special offers we handpicked just for you:</p>");
        
        sb.append("<h2>🔥 Handpicked Deals For You</h2>");
        sb.append("<table class=\"details-table\">");
        for (Products p : randomProducts) {
            String tag = p.isDealOfWeek() ? "Deal of the Week"
                    : p.isFestivalOffer() ? "Festival Offer"
                    : "Special Offer";
            sb.append("<tr>");
            sb.append("<td><strong>").append(p.getTitle()).append("</strong><br><span style=\"color: #888; font-size: 12px;\">").append(tag).append("</span></td>");
            sb.append("<td><span class=\"highlight\">₹").append(String.format("%.2f", p.getPrice())).append("</span></td>");
            sb.append("</tr>");
        }
        sb.append("</table>");
        
        sb.append("<p>Don't miss out — these deals won't last long! ⏳</p>");
        sb.append("<a href=\"#\" class=\"btn\">Return to Cart</a>");

        sendHtmlEmail(user.getEmail(), "🛍️ You left something behind! Special picks just for you", sb.toString());
    }

    @Async
    public void emailForFestivalOffer(Users user, Festival festival) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>🎉 ").append(festival.getName()).append(" Offers Are Live!</h1>");
        sb.append("<p>Hey <strong>").append(user.getUsername()).append("</strong>! 🎊</p>");
        sb.append("<p>Today is ").append(festival.getName()).append(" and we have exclusive deals just for you!</p>");
        
        sb.append("<div style=\"background: linear-gradient(135deg, #f6d365 0%, #fda085 100%); padding: 20px; border-radius: 8px; color: #fff; text-align: center; margin: 20px 0;\">");
        sb.append("<h2 style=\"color: #fff; margin-top: 0; border: none;\">🛍️ ").append(festival.getName()).append("</h2>");
        sb.append("<p style=\"color: #fff; font-size: 18px;\">Valid: ").append(festival.getStartDate()).append(" → ").append(festival.getEndDate()).append("</p>");
        sb.append("</div>");
        
        sb.append("<p>Hurry! Festival deals are live for a limited time only. ⏳</p>");
        sb.append("<p>Visit Kirani Store now and grab your favourites before they're gone!</p>");
        sb.append("<a href=\"#\" class=\"btn\">Shop Now</a>");

        sendHtmlEmail(user.getEmail(), "🎉 Festival Alert: " + festival.getName() + " Offers Are Live!", sb.toString());
    }

    @Async
    public void emailForOrderStatusUpdate(Users user, Orders order, String stage, String desc) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>").append(getSubjectForStage(stage)).append("</h1>");
        sb.append("<p>Hello <strong>").append(user.getUsername()).append("</strong>,</p>");
        sb.append("<p>").append(getBodyIntroForStage(stage)).append("</p>");
        
        sb.append("<h2>Order Details (#").append(order.getId()).append(")</h2>");
        sb.append("<table class=\"details-table\">");
        sb.append("<tr><th>Status</th><td><span class=\"highlight\">").append(stage).append("</span></td></tr>");
        sb.append("<tr><th>Payment Mode</th><td>").append(order.getPaymentMethod()).append("</td></tr>");
        if (desc != null && !desc.isBlank()) {
            sb.append("<tr><th>Note</th><td>").append(desc).append("</td></tr>");
        }
        sb.append("</table>");

        if (stage.equals("Refund Initiated") || stage.equals("Refund Processing") || stage.equals("Refund Completed")) {
            sb.append("<h2>💰 Refund Info</h2>");
            sb.append("<table class=\"details-table\">");
            sb.append("<tr><th>Amount</th><td>₹").append(String.format("%.2f", order.getTotalPrice())).append("</td></tr>");
            if (order.getRefundUpi() != null && !order.getRefundUpi().isBlank()) {
                sb.append("<tr><th>UPI</th><td>").append(order.getRefundUpi()).append("</td></tr>");
            }
            if (order.getBankAccountNumber() != null && !order.getBankAccountNumber().isBlank()) {
                sb.append("<tr><th>Bank</th><td>").append(order.getBankAccountNumber()).append("</td></tr>");
            }
            sb.append("</table>");
        }

        sb.append("<p>For any queries, feel free to reach out to us.</p>");

        sendHtmlEmail(user.getEmail(), getSubjectForStage(stage) + " | Order #" + order.getId(), sb.toString());
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