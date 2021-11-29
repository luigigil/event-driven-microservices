package com.edm.estore.productsservice.query;

import com.edm.estore.core.core.events.ProductReservationCancelledEvent;
import com.edm.estore.core.core.events.ProductReservedEvent;
import com.edm.estore.productsservice.core.data.ProductEntity;
import com.edm.estore.productsservice.core.data.ProductsRepository;
import com.edm.estore.productsservice.core.events.ProductCreatedEvent;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.ResetHandler;
import org.axonframework.messaging.interceptors.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("product-group")
public class ProductsEventsHandler {

    private final ProductsRepository productsRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductsEventsHandler.class);

    public ProductsEventsHandler(ProductsRepository productsRepository) {
        this.productsRepository = productsRepository;
    }

    @ExceptionHandler(resultType = IllegalArgumentException.class)
    public void handle(IllegalArgumentException ex) {
        // Log error message
    }

    @ExceptionHandler(resultType = Exception.class)
    public void handle(Exception ex) throws Exception {
        throw ex;
    }

    @EventHandler
    public void on(ProductCreatedEvent event) throws Exception {
        ProductEntity productEntity = new ProductEntity();
        BeanUtils.copyProperties(event, productEntity);

        try {
            productsRepository.save(productEntity);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }

    }

    @EventHandler
    public void on(ProductReservationCancelledEvent productReservationCancelledEvent) {
        ProductEntity currentlyStoredProduct = this.productsRepository.findByProductId(productReservationCancelledEvent.getProductId());

        LOGGER.debug("ProductReservationCancelledEvent: Current product quantity: {}", productReservationCancelledEvent.getQuantity());

        int newQuantity = currentlyStoredProduct.getQuantity() + productReservationCancelledEvent.getQuantity();
        currentlyStoredProduct.setQuantity(newQuantity);
        
        productsRepository.save(currentlyStoredProduct);
        
        LOGGER.debug("ProductReservationCancelledEvent: New product quantity: {}", productReservationCancelledEvent.getQuantity());
    }

    @EventHandler
    public void on(ProductReservedEvent productReservedEvent) {
        ProductEntity entity = productsRepository.findByProductId(productReservedEvent.getProductId());

        LOGGER.debug("ProductReservedEvent: Current product quantity: {}", entity.getQuantity());

        entity.setQuantity(entity.getQuantity() - productReservedEvent.getQuantity());
        productsRepository.save(entity);

        LOGGER.debug("ProductReservedEvent: New product quantity: {}", entity.getQuantity());

        LOGGER.info("ProductReservedEvent is called for orderId: {} and productId = {}",
                productReservedEvent.getOrderId(), productReservedEvent.getProductId());
    }

    @ResetHandler
    public void reset() {
        productsRepository.deleteAll();
    }

}
