package com.edm.estore.ordersservice.ordersservice.core.events;

import com.edm.estore.ordersservice.ordersservice.core.model.OrderStatus;

import lombok.Value;

@Value
public class OrderApprovedEvent {
    private final String orderId;
    private final OrderStatus orderStatus = OrderStatus.APPROVED;    
}
