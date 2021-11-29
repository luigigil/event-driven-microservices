package com.edm.estore.paymentsservice.paymentsservice.core.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "payments")
public class PaymentEntity {
    
    @Id
    private String paymentId;
    
    @Column
    public String orderId;
    
}
