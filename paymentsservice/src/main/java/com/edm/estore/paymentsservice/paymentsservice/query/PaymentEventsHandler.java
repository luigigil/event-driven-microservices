package com.edm.estore.paymentsservice.paymentsservice.query;

import com.edm.estore.core.core.events.PaymentProcessedEvent;
import com.edm.estore.paymentsservice.paymentsservice.core.data.PaymentEntity;
import com.edm.estore.paymentsservice.paymentsservice.core.data.PaymentsRepository;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventsHandler {
 
    private final PaymentsRepository repository;
    
    public PaymentEventsHandler(PaymentsRepository repository) {
        this.repository = repository;
    }

    @EventHandler
    public void on(PaymentProcessedEvent paymentProcessedEvent){
        PaymentEntity paymentEntity = new PaymentEntity();
        BeanUtils.copyProperties(paymentProcessedEvent, paymentEntity);
        this.repository.save(paymentEntity);
    }
}
