package com.edm.estore.usersservice.usersservice.query;

import com.edm.estore.core.core.model.PaymentDetails;
import com.edm.estore.core.core.model.User;
import com.edm.estore.core.core.query.FetchUserPaymentDetailsQuery;

import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class UserEventsHandler {
    
    @QueryHandler
    public User fetchUserPaymentDetailsQuery(FetchUserPaymentDetailsQuery fetchUserPaymentDetailsQuery) {
        PaymentDetails paymentDetails = PaymentDetails.builder()
        .cardNumber("1234567890")
        .cvv("123")
        .name("DNOOP SOGG")
        .validUntilMonth(12)
        .validUntilYear(2030)
        .build();

        User userRest = User.builder()
        .firstName("DNOOP")
        .lastName("SOGG")
        .userId(fetchUserPaymentDetailsQuery.getUserId())
        .paymentDetails(paymentDetails)
        .build();

        return userRest;
    }
}
