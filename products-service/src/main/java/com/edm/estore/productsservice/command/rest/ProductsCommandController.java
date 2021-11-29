package com.edm.estore.productsservice.command.rest;

import java.util.UUID;

import javax.validation.Valid;

import com.edm.estore.productsservice.command.CreateProductCommand;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class ProductsCommandController {
    
    private final Environment env;
    private final CommandGateway commandGateway;

    @Autowired
    public ProductsCommandController(Environment env, CommandGateway commandGateway){
        this.env = env;
        this.commandGateway = commandGateway;
    }

    @PostMapping
    public String createProduct(@Valid @RequestBody CreateProductsRestModel createProductsRestModel) {
        CreateProductCommand createProductCommand = CreateProductCommand.builder()
        .price(createProductsRestModel.getPrice())
        .quantity(createProductsRestModel.getQuantity())
        .title(createProductsRestModel.getTitle())
        .productId(UUID.randomUUID().toString()).build();

        String returnValue;
        
        returnValue = commandGateway.sendAndWait(createProductCommand);

        return returnValue;
    }

    @PutMapping
    public String updateProduct() {
        return "HTTP PUT is handled";
    }

    @DeleteMapping
    public String deleteProduct() {
        return "HTTP DELETE is handled";
    }

}
