package com.edm.estore.productsservice.command.interceptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.BiFunction;

import com.edm.estore.productsservice.command.CreateProductCommand;
import com.edm.estore.productsservice.core.data.ProductLookupEntity;
import com.edm.estore.productsservice.core.data.ProductLookupRepository;

import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CreateProductCommandInterceptor implements MessageDispatchInterceptor<CommandMessage<?>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateProductCommandInterceptor.class);

    private final ProductLookupRepository productLookupRepository;

    public CreateProductCommandInterceptor(ProductLookupRepository productLookupRepository) {
        this.productLookupRepository = productLookupRepository;
    }

    @Override
    public BiFunction<Integer, CommandMessage<?>, CommandMessage<?>> handle(List<? extends CommandMessage<?>> arg0) {
        // TODO Auto-generated method stub
        return (index, command) -> {

            LOGGER.info("Intercepted command: {}", command.getPayload());

            if (CreateProductCommand.class.equals(command.getPayloadType())) {

                CreateProductCommand createProductCommand = (CreateProductCommand) command.getPayload();

                ProductLookupEntity productLookupEntity = productLookupRepository
                        .findByProductIdOrTitle(createProductCommand.getProductId(), createProductCommand.getTitle());

                if(productLookupEntity != null ){
                    throw new IllegalStateException(String.format("Product with productId: %s or title %s already exist", createProductCommand.getProductId(), createProductCommand.getTitle()));
                }

            }

            return command;
        };
    }
}
