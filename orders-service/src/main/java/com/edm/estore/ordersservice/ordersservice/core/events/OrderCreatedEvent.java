package com.edm.estore.ordersservice.ordersservice.core.events;

import com.edm.estore.ordersservice.ordersservice.core.model.OrderStatus;

import lombok.Data;

@Data
public class OrderCreatedEvent {
    private String orderId;
    private String userId;
    private String productId;
    private int quantity;
    private String addressId;
    private OrderStatus orderStatus;
}