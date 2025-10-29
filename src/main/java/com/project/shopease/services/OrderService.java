package com.project.shopease.services;

import java.security.Principal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import com.project.shopease.auth.dto.OrderResponse;
import com.project.shopease.auth.entities.User;
import com.project.shopease.dto.OrderDetails;
import com.project.shopease.dto.OrderItemDetail;
import com.project.shopease.dto.OrderRequest;
import com.project.shopease.entities.Address;
import com.project.shopease.entities.Order;
import com.project.shopease.entities.OrderItem;
import com.project.shopease.entities.OrderStatus;
import com.project.shopease.entities.Payment;
import com.project.shopease.entities.PaymentStatus;
import com.project.shopease.entities.Product;
import com.project.shopease.repositories.OrderRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;

import jakarta.transaction.Transactional;

@Service
public class OrderService {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private PaymentIntentService paymentIntentService;

    @Transactional
    public OrderResponse createOrder(OrderRequest orderRequest, Principal principal) throws Exception {
        User user = (User) userDetailsService.loadUserByUsername(principal.getName());
        Address address = user.getAddressList().stream()
                .filter(address1 -> orderRequest.getAddressId().equals(address1.getId()))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Address not found"));

        Order order = Order.builder()
                .user(user)
                .address(address)
                .totalAmount(orderRequest.getTotalAmount())
                .orderDate(orderRequest.getOrderDate())
                .discount(orderRequest.getDiscount())
                .expectedDeliveryDate(orderRequest.getExpectedDeliveryDate())
                .paymentMethod(orderRequest.getPaymentMethod())
                .orderStatus(OrderStatus.PENDING)
                .build();

        List<OrderItem> orderItems = orderRequest.getOrderItemRequests().stream().map(orderItemRequest -> {
            try {
                Product product = productService.fetchProductById(orderItemRequest.getProductId());
                OrderItem orderItem = OrderItem.builder()
                        .product(product)
                        .productVariantId(orderItemRequest.getProductVariantId())
                        .quantity(orderItemRequest.getQuantity())
                        .itemPrice(orderItemRequest.getItemPrice())
                        .order(order)
                        .build();
                return orderItem;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).toList();
        
        order.setOrderItemList(orderItems);
        
        Payment payment = Payment.builder()
                .paymentStatus(PaymentStatus.PENDING)
                .paymentDate(new Date())
                .order(order)
                .amount(order.getTotalAmount())
                .paymentMethod(orderRequest.getPaymentMethod())
                .build();
        
        order.setPayment(payment);
        
        Order savedOrder = orderRepository.save(order);

        OrderResponse orderResponse = OrderResponse.builder()
                .paymentMethod(orderRequest.getPaymentMethod())
                .orderId(savedOrder.getId())
                .build();
                
        if (Objects.equals(orderRequest.getPaymentMethod(), "CARD")) {
            try {
                Map<String, String> credentials = paymentIntentService.createPaymentIntent(savedOrder);
                orderResponse.setCredentials(credentials);
            } catch (StripeException e) {
                throw new RuntimeException("Failed to create payment intent: " + e.getMessage(), e);
            }
        }

        return orderResponse;
    }

    @Transactional
    public Map<String, String> updateStatus(String paymentIntentId, String status) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            
            if (paymentIntent == null) {
                throw new IllegalArgumentException("PaymentIntent not found");
            }
            
            if (!"succeeded".equals(paymentIntent.getStatus())) {
                throw new IllegalArgumentException("Payment not succeeded. Current status: " + paymentIntent.getStatus());
            }
            
            Map<String, String> metadata = paymentIntent.getMetadata();
            if (metadata == null || !metadata.containsKey("orderId")) {
                throw new IllegalArgumentException("Order ID not found in payment metadata");
            }
            
            String orderId = metadata.get("orderId");
            if (orderId == null || orderId.isEmpty()) {
                throw new IllegalArgumentException("Order ID is empty in payment metadata");
            }
            
            Order order = orderRepository.findById(UUID.fromString(orderId))
                    .orElseThrow(() -> new BadRequestException("Order not found with ID: " + orderId));
            
            Payment payment = order.getPayment();
            if (payment == null) {
                throw new IllegalArgumentException("Payment record not found for order");
            }
            
            payment.setPaymentStatus(PaymentStatus.COMPLETED);
            payment.setPaymentMethod(paymentIntent.getPaymentMethod());
            
            order.setPaymentMethod(paymentIntent.getPaymentMethod());
            order.setOrderStatus(OrderStatus.IN_PROGRESS);
            order.setPayment(payment);
            
            Order savedOrder = orderRepository.save(order);
            
            Map<String, String> response = new HashMap<>();
            response.put("orderId", savedOrder.getId().toString());
            response.put("status", "success");
            response.put("message", "Payment confirmed and order updated successfully");
            
            return response;
            
        } catch (StripeException e) {
            throw new IllegalArgumentException("Stripe API error: " + e.getMessage(), e);
        } catch (BadRequestException e) {
            throw new IllegalArgumentException("Order not found: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error updating payment status: " + e.getMessage(), e);
        }
    }

    public List<OrderDetails> getOrdersByUser(String name) {
        User user = (User) userDetailsService.loadUserByUsername(name);
        List<Order> orders = orderRepository.findByUser(user);
        
        return orders.stream().map(order -> {
            return OrderDetails.builder()
                    .id(order.getId())
                    .orderDate(order.getOrderDate())
                    .orderStatus(order.getOrderStatus())
                    .shipmentNumber(order.getShipmentTrackingNumber())
                    .address(order.getAddress())
                    .totalAmount(order.getTotalAmount())
                    .orderItemList(getItemDetails(order.getOrderItemList()))
                    .expectedDeliveryDate(order.getExpectedDeliveryDate())
                    .build();
        }).toList();
    }

    private List<OrderItemDetail> getItemDetails(List<OrderItem> orderItemList) {
        return orderItemList.stream().map(orderItem -> {
            return OrderItemDetail.builder()
                    .id(orderItem.getId())
                    .itemPrice(orderItem.getItemPrice())
                    .product(orderItem.getProduct())
                    .productVariantId(orderItem.getProductVariantId())
                    .quantity(orderItem.getQuantity())
                    .build();
        }).toList();
    }

    @Transactional
    public void cancelOrder(UUID id, Principal principal) throws Exception {
        User user = (User) userDetailsService.loadUserByUsername(principal.getName());
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Order not found"));
        
        if (order.getUser().getId().equals(user.getId())) {
            if (order.getOrderStatus() == OrderStatus.CANCELLED) {
                throw new IllegalArgumentException("Order is already cancelled");
            }
            
            if (order.getOrderStatus() == OrderStatus.DELIVERED) {
                throw new IllegalArgumentException("Cannot cancel delivered order");
            }
            
            order.setOrderStatus(OrderStatus.CANCELLED);
            
            // Update payment status if payment was completed
            if (order.getPayment() != null && 
                order.getPayment().getPaymentStatus() == PaymentStatus.COMPLETED) {
                order.getPayment().setPaymentStatus(PaymentStatus.FAILED);
                // TODO: Add refund logic here
            }
            
            orderRepository.save(order);
        } else {
            throw new IllegalArgumentException("Unauthorized: You can only cancel your own orders");
        }
    }
}