package com.edm.estore.productsservice.command;

import com.edm.estore.productsservice.core.data.ProductLookupEntity;
import com.edm.estore.productsservice.core.data.ProductLookupRepository;
import com.edm.estore.productsservice.core.events.ProductCreatedEvent;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.ResetHandler;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("product-group")
public class ProductLookupEventsHandler {

    private final ProductLookupRepository productLookupRepository;

    public ProductLookupEventsHandler(ProductLookupRepository productLookupRepository) {
        this.productLookupRepository = productLookupRepository;
    }

    @EventHandler
    public void on(ProductCreatedEvent productCreatedEvent) {
        ProductLookupEntity productLookupEntity = new ProductLookupEntity(productCreatedEvent.getProductId(),
                productCreatedEvent.getTitle());

        productLookupRepository.save(productLookupEntity);
    }

    @ResetHandler
    public void reset() {
        productLookupRepository.deleteAll();
    }

}
