package com.edm.estore.productsservice.command;

import java.math.BigDecimal;

import com.edm.estore.core.core.commands.CancelProductReservationCommand;
import com.edm.estore.core.core.commands.ReserveProductCommand;
import com.edm.estore.core.core.events.ProductReservationCancelledEvent;
import com.edm.estore.core.core.events.ProductReservedEvent;
import com.edm.estore.productsservice.core.events.ProductCreatedEvent;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

// precisa ser o mesmo do bean na classe inicial
@Aggregate(snapshotTriggerDefinition = "productSnapshotTriggerDefinition")
public class ProductsAggregate {

    @AggregateIdentifier
    private String productId;
    private String title;
    private BigDecimal price;
    private Integer quantity;

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductsAggregate.class);
    
    public ProductsAggregate() {

    }

    @CommandHandler
    public ProductsAggregate(CreateProductCommand createProductCommand) {
        if (createProductCommand.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price cannot be less or equal than zero");
        }
        if (createProductCommand.getTitle() == null || createProductCommand.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }

        ProductCreatedEvent productCreatedEvent = new ProductCreatedEvent();
        BeanUtils.copyProperties(createProductCommand, productCreatedEvent);

        AggregateLifecycle.apply(productCreatedEvent);
    }

    @CommandHandler
    public void handle(ReserveProductCommand reserveProductCommand) {
        LOGGER.info("3 - RECEBI RESERVE PRODUCT COMMAND");
        if(quantity < reserveProductCommand.getQuantity()) {
            throw new IllegalArgumentException("Insuficient number of items available.");
        }

        ProductReservedEvent productReservedEvent = ProductReservedEvent.builder()
            .orderId(reserveProductCommand.getOrderId())
            .userId(reserveProductCommand.getUserId())
            .quantity(reserveProductCommand.getQuantity())
            .productId(reserveProductCommand.getProductId())
            .build();

        AggregateLifecycle.apply(productReservedEvent);
        LOGGER.info("4 - PUBLIQUEI PRODUCT RESERVED EVENT");

    }

    @CommandHandler
    public void handle(CancelProductReservationCommand cancelProductReservationCommand){
        ProductReservationCancelledEvent productReservationCancelledEvent = ProductReservationCancelledEvent.builder()
        .orderId(cancelProductReservationCommand.getOrderId())
        .productId(cancelProductReservationCommand.getProductId())
        .quantity(cancelProductReservationCommand.getQuantity())
        .reason(cancelProductReservationCommand.getReason())
        .userId(cancelProductReservationCommand.getUserId())
        .build();

        AggregateLifecycle.apply(productReservationCancelledEvent);
    }

    @EventSourcingHandler
    public void on(ProductReservationCancelledEvent productReservationCancelledEvent) {
        this.quantity += productReservationCancelledEvent.getQuantity();
    }

    @EventSourcingHandler
    public void on(ProductCreatedEvent productCreatedEvent) {
        this.price = productCreatedEvent.getPrice();
        this.productId = productCreatedEvent.getProductId();
        this.quantity = productCreatedEvent.getQuantity();
        this.title = productCreatedEvent.getTitle();
    }

    @EventSourcingHandler
    public void on(ProductReservedEvent productReservedEvent) {
        this.quantity -= productReservedEvent.getQuantity();
    }

}
