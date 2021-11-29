package com.edm.estore.paymentsservice.paymentsservice.command;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import com.edm.estore.core.core.commands.ProcessPaymentCommand;
import com.edm.estore.core.core.events.PaymentProcessedEvent;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.BeanUtils;

@Aggregate
public class PaymentAggregate {

    @AggregateIdentifier
    private String paymentId;
    private String orderId;

    public PaymentAggregate() {
    }

    @CommandHandler
    public PaymentAggregate(ProcessPaymentCommand processPaymentCommand) {
        // make it handle the ProcessPaymentCommand, and publish the
        // PaymentProcessedEvent.
        Date date = new Date();
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int month = localDate.getMonthValue();
        int year = localDate.getYear();

        if (processPaymentCommand.getPaymentDetails().getCvv().length() != 3) {
            throw new IllegalArgumentException("Invalid CVV");
        }
        if (processPaymentCommand.getPaymentDetails().getCardNumber().length() < 10) {
            throw new IllegalArgumentException("Invalid card number");
        }
        if (processPaymentCommand.getPaymentDetails().getValidUntilYear() < year) {
            throw new IllegalArgumentException("Card year expired.");
        }
        if (processPaymentCommand.getPaymentDetails().getValidUntilYear() == year
                && processPaymentCommand.getPaymentDetails().getValidUntilMonth() < month) {
            throw new IllegalArgumentException("Card month expired.");
        }

        // cria objeto do evento para publicacao
        PaymentProcessedEvent paymentProcessedEvent = new PaymentProcessedEvent();
        // //copia atributos
        BeanUtils.copyProperties(processPaymentCommand, paymentProcessedEvent);
        // //publica evento
        AggregateLifecycle.apply(paymentProcessedEvent);
    }

    @EventSourcingHandler
    public void on(PaymentProcessedEvent paymentProcessedEvent) {
        // atualiza agregado com atributos do evento
        this.paymentId = paymentProcessedEvent.getPaymentId();
        this.orderId = paymentProcessedEvent.getOrderId();
    }

}
