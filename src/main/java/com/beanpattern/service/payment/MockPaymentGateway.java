package com.beanpattern.service.payment;

import com.beanpattern.config.PaymentProperties;
import com.beanpattern.entity.OrderEntity;
import com.beanpattern.model.PaymentCreateResult;
import com.beanpattern.service.OrderService;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 开发期 mock 支付网关。
 */
@Component
public class MockPaymentGateway implements PaymentGateway {

    private final PaymentProperties paymentProperties;
    private final OrderService orderService;
    private final Environment environment;

    public MockPaymentGateway(PaymentProperties paymentProperties, OrderService orderService, Environment environment) {
        this.paymentProperties = paymentProperties;
        this.orderService = orderService;
        this.environment = environment;
    }

    @Override
    public PaymentCreateResult createPayment(OrderEntity order) {
        PaymentCreateResult result = new PaymentCreateResult();
        result.setOrderNo(order.getOrderNo());
        result.setProvider("mock");
        result.setMock(true);

        if (paymentProperties.getMock().isAutoPaid() && isAutoPayAllowed()) {
            orderService.handlePaymentCallback(order.getOrderNo(), "MOCK-" + order.getOrderNo());
            result.setStatus("PAID");
        } else {
            result.setStatus(order.getStatus());
        }

        return result;
    }

    private boolean isAutoPayAllowed() {
        String[] profiles = environment.getActiveProfiles();
        boolean prod = Arrays.stream(profiles).anyMatch("prod"::equalsIgnoreCase);
        return !prod;
    }
}
