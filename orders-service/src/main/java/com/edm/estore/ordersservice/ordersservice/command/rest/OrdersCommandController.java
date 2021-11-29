package com.edm.estore.ordersservice.ordersservice.command.rest;

import java.util.UUID;

import com.edm.estore.ordersservice.ordersservice.command.commands.CreateOrderCommand;
import com.edm.estore.ordersservice.ordersservice.core.model.OrderStatus;
import com.edm.estore.ordersservice.ordersservice.core.model.OrderSummary;
import com.edm.estore.ordersservice.ordersservice.query.FindOrderQuery;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.SubscriptionQueryResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrdersCommandController {

    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;

    public OrdersCommandController(CommandGateway commandGateway, QueryGateway queryGateway) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
    }

    @PostMapping
    public OrderSummary createOrder(@RequestBody CreateOrderRestModel createOrderRestModel) {

        String userId = "27b95829-4f3f-4ddf-8983-151ba010e35b";
        String orderId = UUID.randomUUID().toString();

        // cria createOrderCommand utilizando builder
        CreateOrderCommand createOrderCommand = CreateOrderCommand.builder().orderId(orderId).userId(userId)
                .productId(createOrderRestModel.getProductId()).quantity(createOrderRestModel.getQuantity())
                .addressId(createOrderRestModel.getAddressId()).orderStatus(OrderStatus.CREATED).build();

        SubscriptionQueryResult<OrderSummary, OrderSummary> queryResult = queryGateway.subscriptionQuery(
                new FindOrderQuery(orderId), ResponseTypes.instanceOf(OrderSummary.class),
                ResponseTypes.instanceOf(OrderSummary.class));

        try {
            // envia comando pro commandGateway pelo metodo sendAndWait
            commandGateway.sendAndWait(createOrderCommand);
            return queryResult.updates().blockFirst();
        } finally {
            queryResult.close();
        }

    }

}
