package com.edm.estore.ordersservice.ordersservice.command;

import javax.persistence.criteria.Order;

import com.edm.estore.ordersservice.ordersservice.command.commands.ApproveOrderCommand;
import com.edm.estore.ordersservice.ordersservice.command.commands.CreateOrderCommand;
import com.edm.estore.ordersservice.ordersservice.command.commands.RejectOrderCommand;
import com.edm.estore.ordersservice.ordersservice.core.events.OrderApprovedEvent;
import com.edm.estore.ordersservice.ordersservice.core.events.OrderCreatedEvent;
import com.edm.estore.ordersservice.ordersservice.core.events.OrderRejectedEvent;
import com.edm.estore.ordersservice.ordersservice.core.model.OrderStatus;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.hibernate.boot.model.source.spi.Orderable;
import org.springframework.beans.BeanUtils;

@Aggregate
public class OrderAggregate {

    @AggregateIdentifier
    private String orderId;
    private String productId;
    private int quantity;
    private String addressId;
    private OrderStatus orderStatus;

    public OrderAggregate() {

    }

    // assinatura para construtor de handlers de comandos
    @CommandHandler
    public OrderAggregate(CreateOrderCommand createOrderCommand){
        // cria objeto do evento para publicacao
        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent();
        //copia atributos
        BeanUtils.copyProperties(createOrderCommand, orderCreatedEvent);
        //publica evento
        AggregateLifecycle.apply(orderCreatedEvent);
    }

    //assinatura para tratamento de eventos
    @EventSourcingHandler
    public void on(OrderCreatedEvent orderCreatedEvent) {
        //atualiza agregado com atributos do evento
        this.orderId = orderCreatedEvent.getOrderId();
        this.productId = orderCreatedEvent.getProductId();
        this.quantity = orderCreatedEvent.getQuantity();
        this.addressId = orderCreatedEvent.getAddressId();
        this.orderStatus = orderCreatedEvent.getOrderStatus();
    }

    @CommandHandler
    public void handle(ApproveOrderCommand approveOrderCommand){
        // create and publish order approved event
        OrderApprovedEvent orderApprovedEvent = new OrderApprovedEvent(approveOrderCommand.getOrderId());

        AggregateLifecycle.apply(orderApprovedEvent);
    }

    @EventSourcingHandler
    protected void on(OrderApprovedEvent orderApprovedEvent) {
        this.orderStatus = orderApprovedEvent.getOrderStatus();
    }

    @CommandHandler
    public void handle(RejectOrderCommand rejectOrderCommand) {
        OrderRejectedEvent orderRejectedEvent = new OrderRejectedEvent(rejectOrderCommand.getOrderId(), rejectOrderCommand.getReason());

        AggregateLifecycle.apply(orderRejectedEvent);
    }

    @EventSourcingHandler
    public void on(OrderRejectedEvent orderRejectedEvent) {
        this.orderStatus = orderRejectedEvent.getOrderStatus();
    }


}
