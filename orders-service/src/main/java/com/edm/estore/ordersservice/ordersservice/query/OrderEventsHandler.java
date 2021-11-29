package com.edm.estore.ordersservice.ordersservice.query;

import com.edm.estore.ordersservice.ordersservice.core.data.OrderEntity;
import com.edm.estore.ordersservice.ordersservice.core.data.OrdersRepository;
import com.edm.estore.ordersservice.ordersservice.core.events.OrderApprovedEvent;
import com.edm.estore.ordersservice.ordersservice.core.events.OrderCreatedEvent;
import com.edm.estore.ordersservice.ordersservice.core.events.OrderRejectedEvent;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class OrderEventsHandler {
    
    private final OrdersRepository ordersRepository;

    public OrderEventsHandler(OrdersRepository ordersRepository) {
        this.ordersRepository = ordersRepository;
    }

    // assinar metodo on() para tratar evento
    @EventHandler
    public void on(OrderCreatedEvent event) {
        //instancia nova entity
        OrderEntity orderEntity = new OrderEntity();
        //copia atributos para de event para orderEntity
        BeanUtils.copyProperties(event, orderEntity);
        //salva no banco
        ordersRepository.save(orderEntity);
    }

    @EventHandler
    public void on(OrderApprovedEvent orderApprovedEvent) {
        OrderEntity orderEntity = ordersRepository.findByOrderId(orderApprovedEvent.getOrderId());
        
        if(orderEntity == null) {
            //TODO do something 
            return;
        }

        orderEntity.setOrderStatus(orderApprovedEvent.getOrderStatus());

        ordersRepository.save(orderEntity);
    }

    @EventHandler
    public void on(OrderRejectedEvent orderRejectedEvent) {
        OrderEntity orderEntity = ordersRepository.findByOrderId(orderRejectedEvent.getOrderId());

        if(orderEntity == null) {
            //TODO do something
            return;
        }

        orderEntity.setOrderStatus(orderRejectedEvent.getOrderStatus());

        ordersRepository.save(orderEntity);
    }
}
