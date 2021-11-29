package com.edm.estore.productsservice.query;

import java.util.ArrayList;
import java.util.List;

import com.edm.estore.productsservice.core.data.ProductEntity;
import com.edm.estore.productsservice.core.data.ProductsRepository;
import com.edm.estore.productsservice.query.rest.ProductRestModel;

import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class ProductsQueryHandler {

    private final ProductsRepository productsRepository;

    public ProductsQueryHandler(ProductsRepository productsRepository) {
        this.productsRepository = productsRepository;
    }

    @QueryHandler
    public List<ProductRestModel> findProducts(FindProductsQuery findProductsQuery) {
        
        List<ProductRestModel> productRestModels = new ArrayList<>();

        List<ProductEntity> storedProducts = productsRepository.findAll();

        for(ProductEntity productEntity : storedProducts) {
            ProductRestModel productRestModel = new ProductRestModel();
            BeanUtils.copyProperties(productEntity, productRestModel);
            productRestModels.add(productRestModel);
        }

        return productRestModels;
    }
}
