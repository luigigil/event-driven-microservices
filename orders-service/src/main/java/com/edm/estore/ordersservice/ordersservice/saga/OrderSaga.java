package com.edm.estore.ordersservice.ordersservice.saga;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import com.edm.estore.core.core.commands.CancelProductReservationCommand;
import com.edm.estore.core.core.commands.ProcessPaymentCommand;
import com.edm.estore.core.core.commands.ReserveProductCommand;
import com.edm.estore.core.core.events.PaymentProcessedEvent;
import com.edm.estore.core.core.events.ProductReservationCancelledEvent;
import com.edm.estore.core.core.events.ProductReservedEvent;
import com.edm.estore.core.core.model.User;
import com.edm.estore.core.core.query.FetchUserPaymentDetailsQuery;
import com.edm.estore.ordersservice.ordersservice.command.commands.ApproveOrderCommand;
import com.edm.estore.ordersservice.ordersservice.command.commands.RejectOrderCommand;
import com.edm.estore.ordersservice.ordersservice.core.events.OrderApprovedEvent;
import com.edm.estore.ordersservice.ordersservice.core.events.OrderCreatedEvent;
import com.edm.estore.ordersservice.ordersservice.core.events.OrderRejectedEvent;
import com.edm.estore.ordersservice.ordersservice.core.model.OrderSummary;
import com.edm.estore.ordersservice.ordersservice.query.FindOrderQuery;

import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandResultMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.annotation.DeadlineHandler;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.axonframework.spring.stereotype.Saga;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Saga
public class OrderSaga {

    @Autowired
    private transient CommandGateway commandGateway;

    @Autowired
    private transient QueryGateway queryGateway;

    @Autowired
    private transient DeadlineManager deadlineManager;

    @Autowired
    private transient QueryUpdateEmitter queryUpdateEmitter;

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderSaga.class);

    private final String PAYMENT_PROCESSING_TIMEOUT_DEADLINE = "payment-processing-deadline";

    private String scheduleId;

    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderCreatedEvent orderCreatedEvent) {
        ReserveProductCommand reserveProductCommand = ReserveProductCommand.builder()
                .orderId(orderCreatedEvent.getOrderId()).quantity(orderCreatedEvent.getQuantity())
                .userId(orderCreatedEvent.getUserId()).productId(orderCreatedEvent.getProductId()).build();

        LOGGER.info("1 - ORDER CREATED EVENT");
        LOGGER.info("OrderCreatedEvent handled for orderId: {} and productId = {}", reserveProductCommand.getOrderId(),
                reserveProductCommand.getProductId());

        commandGateway.send(reserveProductCommand, new CommandCallback<ReserveProductCommand, Object>() {
            @Override
            public void onResult(CommandMessage<? extends ReserveProductCommand> commandMessage,
                    CommandResultMessage<? extends Object> commandResultMessage) {
                if (commandResultMessage.isExceptional()) {
                    // start compensational transaction
                    RejectOrderCommand rejectOrderCommand = new RejectOrderCommand(
                            orderCreatedEvent.getOrderId(),
                            commandResultMessage.exceptionResult().getMessage());

                    commandGateway.send(rejectOrderCommand);
                }
            }
        });
        LOGGER.info("2 - PUBLIQUEI RESERVE PRODUCT COMMAND");
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(ProductReservedEvent productReservedEvent) {
        LOGGER.info("5 - RECEBI PRODUCT RESERVED EVENT");

        // process user payment
        LOGGER.info("ProductReservedEvent is called for orderId: {} and productId = {}",
                productReservedEvent.getOrderId(), productReservedEvent.getProductId());

        FetchUserPaymentDetailsQuery fetchUserPaymentDetailsQuery = new FetchUserPaymentDetailsQuery(
                productReservedEvent.getUserId());

        User userPaymentDetails = null;

        try {
            userPaymentDetails = queryGateway.query(fetchUserPaymentDetailsQuery, ResponseTypes.instanceOf(User.class))
                    .join();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());

            // TODO start compensating transaction
            cancelProductReservation(productReservedEvent, e.getMessage());
            return;
        }

        if (userPaymentDetails == null) {
            // TODO start compensating transaction
            cancelProductReservation(productReservedEvent, "Could no fetch user payment details");
            return;
        }

        LOGGER.info("Successfully fetched user payment details for user {}", userPaymentDetails.getFirstName());

        scheduleId = this.deadlineManager.schedule(Duration.of(120, ChronoUnit.MINUTES),
                PAYMENT_PROCESSING_TIMEOUT_DEADLINE, productReservedEvent);

        ProcessPaymentCommand processPaymentCommand = ProcessPaymentCommand.builder()
                .orderId(productReservedEvent.getOrderId()).paymentDetails(userPaymentDetails.getPaymentDetails())
                .paymentId(UUID.randomUUID().toString()).build();

        String result = null;
        try {
            result = commandGateway.sendAndWait(processPaymentCommand);
        } catch (Exception e) {
            LOGGER.error((e.getMessage()));
            // Start compensation transaction
            cancelProductReservation(productReservedEvent, e.getMessage());
            return;
        }

        if (result == null) {
            LOGGER.info("The ProcessPaymentCommand resulted in NULL. Initiating a compensation transaction");
            // Start compensationg transaction
            cancelProductReservation(productReservedEvent,
                    "Could not process user payment with provided payment details");
        }

    }

    private void cancelProductReservation(ProductReservedEvent productReservedEvent, String reason) {

        cancelDeadline();

        CancelProductReservationCommand cancelProductReservationCommand = CancelProductReservationCommand.builder()
                .orderId(productReservedEvent.getOrderId()).userId(productReservedEvent.getUserId())
                .quantity(productReservedEvent.getQuantity()).reason(reason)
                .productId(productReservedEvent.getProductId()).build();

        commandGateway.send(cancelProductReservationCommand);
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(PaymentProcessedEvent paymentProcessedEvent) {

        cancelDeadline();

        ApproveOrderCommand approveOrderCommand = new ApproveOrderCommand(paymentProcessedEvent.getOrderId());

        commandGateway.send(approveOrderCommand);
    }

    private void cancelDeadline() {
        if (scheduleId != null) {
            this.deadlineManager.cancelSchedule(PAYMENT_PROCESSING_TIMEOUT_DEADLINE, scheduleId);
            scheduleId = null;
        }

    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(ProductReservationCancelledEvent productReservationCancelledEvent) {
        RejectOrderCommand rejectOrderCommand = new RejectOrderCommand(productReservationCancelledEvent.getOrderId(),
                productReservationCancelledEvent.getReason());

        commandGateway.send(rejectOrderCommand);
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderRejectedEvent orderRejectedEvent) {
        LOGGER.info("Successfully rejected order with id {}", orderRejectedEvent.getOrderId());
        queryUpdateEmitter.emit(FindOrderQuery.class, query -> true, new OrderSummary(orderRejectedEvent.getOrderId(),
                orderRejectedEvent.getOrderStatus(), orderRejectedEvent.getReason()));
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderApprovedEvent orderApprovedEvent) {
        LOGGER.info("Order is approved. Order saga is complete for orderId {}", orderApprovedEvent.getOrderId());
        // SagaLifecycle.end();
        queryUpdateEmitter.emit(FindOrderQuery.class, query -> true,
                new OrderSummary(orderApprovedEvent.getOrderId(), orderApprovedEvent.getOrderStatus(), ""));
    }

    @DeadlineHandler(deadlineName = PAYMENT_PROCESSING_TIMEOUT_DEADLINE)
    public void handlePayment(ProductReservedEvent productReservedEvent) {
        LOGGER.info(
                "Payment Processing Deadline took place. Sending a compensating command to cancel the product reservation.");

        cancelProductReservation(productReservedEvent, "Payment Timeout");
    }

}
