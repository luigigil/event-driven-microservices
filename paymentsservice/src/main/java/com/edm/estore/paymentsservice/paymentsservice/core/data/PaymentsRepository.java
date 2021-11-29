package com.edm.estore.paymentsservice.paymentsservice.core.data;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentsRepository extends JpaRepository<PaymentEntity, String>  {
    
}
