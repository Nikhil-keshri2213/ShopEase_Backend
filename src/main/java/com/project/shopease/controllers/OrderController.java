package com.project.shopease.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.shopease.auth.dto.OrderResponse;
import com.project.shopease.dto.OrderDetails;
import com.project.shopease.dto.OrderRequest;
import com.project.shopease.services.OrderService;

import java.security.Principal;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/order")
public class OrderController {
    
    @Autowired
    private OrderService orderService;

    @GetMapping("/user")
    public ResponseEntity<?> getOrderByUser(Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("User not authenticated"));
            }
            
            List<OrderDetails> orders = orderService.getOrdersByUser(principal.getName());
            
            if (orders == null || orders.isEmpty()) {
                return ResponseEntity.ok(new ArrayList<OrderDetails>());
            }
            
            return new ResponseEntity<>(orders, HttpStatus.OK);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch orders: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable UUID id, Principal principal) {
        try {
            if (id == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("Order ID is required"));
            }
            
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("User not authenticated"));
            }
            
            List<OrderDetails> orders = orderService.getOrdersByUser(principal.getName());
            OrderDetails order = orders.stream()
                    .filter(o -> o.getId().equals(id))
                    .findFirst()
                    .orElse(null);
            
            if (order == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Order not found"));
            }
            
            return ResponseEntity.ok(order);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch order: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest orderRequest, Principal principal) {
        try {
            if (orderRequest == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("Order request cannot be null"));
            }
            
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("User not authenticated"));
            }
            
            OrderResponse orderResponse = orderService.createOrder(orderRequest, principal);
            return new ResponseEntity<>(orderResponse, HttpStatus.CREATED);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to create order: " + e.getMessage()));
        }
    }

    @PostMapping("/update-payment")
    public ResponseEntity<?> updatePaymentStatus(@RequestBody Map<String, String> request) {
        try {
            if (request == null || !request.containsKey("paymentIntent")) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Payment intent ID is required"));
            }
            
            String paymentIntentId = request.get("paymentIntent");
            String status = request.getOrDefault("status", "succeeded");
            
            if (paymentIntentId == null || paymentIntentId.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Payment intent ID cannot be empty"));
            }
            
            Map<String, String> response = orderService.updateStatus(paymentIntentId, status);
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to update payment status: " + e.getMessage()));
        }
    }

    @PostMapping("/cancel/{id}")
    public ResponseEntity<?> cancelOrder(@PathVariable UUID id, Principal principal) {
        try {
            if (id == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("Order ID is required"));
            }
            
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("User not authenticated"));
            }
            
            orderService.cancelOrder(id, principal);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Order cancelled successfully");
            response.put("orderId", id.toString());
            
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to cancel order: " + e.getMessage()));
        }
    }

   
    
    // Helper method to create consistent error response
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        error.put("timestamp", new Date().toString());
        return error;
    }
}