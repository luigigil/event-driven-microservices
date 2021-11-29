package com.edm.estore.ordersservice.ordersservice.query;

import com.edm.estore.ordersservice.ordersservice.core.data.OrderEntity;
import com.edm.estore.ordersservice.ordersservice.core.data.OrdersRepository;
import com.edm.estore.ordersservice.ordersservice.core.model.OrderSummary;

import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class OrderQueriesHandler {
    
    OrdersRepository ordersRepository;

    public OrderQueriesHandler(OrdersRepository ordersRepository) {
        this.ordersRepository = ordersRepository;        
    }

    @QueryHandler
    public OrderSummary findOrder(FindOrderQuery findOrderQuery) {
        OrderEntity orderEntity = ordersRepository.findByOrderId(findOrderQuery.getOrderId());
        return new OrderSummary(orderEntity.getOrderId(), orderEntity.getOrderStatus(), "");
    }
}
