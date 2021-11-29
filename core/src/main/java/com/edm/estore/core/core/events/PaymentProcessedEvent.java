package com.edm.estore.core.core.events;

import lombok.Data;

@Data
public class PaymentProcessedEvent {
    private String paymentId;
    private String orderId;
}
