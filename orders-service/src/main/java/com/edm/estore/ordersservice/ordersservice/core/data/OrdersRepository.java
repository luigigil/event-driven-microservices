package com.edm.estore.ordersservice.ordersservice.core.data;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdersRepository extends JpaRepository<OrderEntity, String> {

    public OrderEntity findByOrderId(String orderId);
    
}
