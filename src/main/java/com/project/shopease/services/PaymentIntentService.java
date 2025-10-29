package com.project.shopease.services;

import com.project.shopease.auth.entities.User;
import com.project.shopease.entities.Order;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PaymentIntentService {

    public Map<String, String> createPaymentIntent(Order order) throws StripeException {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        
        if (order.getTotalAmount() == null || order.getTotalAmount() <= 0) {
            throw new IllegalArgumentException("Invalid order amount");
        }
        
        User user = order.getUser();
        if (user == null) {
            throw new IllegalArgumentException("Order must have a user");
        }
        
        // Prepare metadata
        Map<String, String> metaData = new HashMap<>();
        metaData.put("orderId", order.getId().toString());
        metaData.put("userId", user.getId().toString());
        metaData.put("userEmail", user.getEmail() != null ? user.getEmail() : "");
        
        // Calculate amount in smallest currency unit (paise for INR)
        // Assuming totalAmount is in USD, converting to INR and then to paise
        long amountInPaise = (long) (order.getTotalAmount() * 100 * 80);
        
        // Create payment intent parameters
        PaymentIntentCreateParams paymentIntentCreateParams = PaymentIntentCreateParams.builder()
                .setAmount(amountInPaise)
                .setCurrency("inr")
                .putAllMetadata(metaData)
                .setDescription("ShopEase Order Payment - Order ID: " + order.getId())
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                )
                .build();
        
        // Create the payment intent
        PaymentIntent paymentIntent = PaymentIntent.create(paymentIntentCreateParams);
        
        // Prepare response
        Map<String, String> response = new HashMap<>();
        response.put("client_secret", paymentIntent.getClientSecret());
        response.put("payment_intent_id", paymentIntent.getId());
        response.put("amount", String.valueOf(amountInPaise));
        response.put("currency", "inr");
        
        return response;
    }
}