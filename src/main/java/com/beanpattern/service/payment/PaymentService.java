package com.beanpattern.service.payment;

import com.beanpattern.config.PaymentProperties;
import com.beanpattern.entity.OrderEntity;
import com.beanpattern.model.PaymentCreateResult;
import org.springframework.stereotype.Service;

/**
 * 支付服务，按配置选择支付网关。
 */
@Service
public class PaymentService {

    private final PaymentProperties paymentProperties;
    private final MockPaymentGateway mockPaymentGateway;
    private final WechatPaymentGateway wechatPaymentGateway;

    public PaymentService(PaymentProperties paymentProperties,
                          MockPaymentGateway mockPaymentGateway,
                          WechatPaymentGateway wechatPaymentGateway) {
        this.paymentProperties = paymentProperties;
        this.mockPaymentGateway = mockPaymentGateway;
        this.wechatPaymentGateway = wechatPaymentGateway;
    }

    public PaymentCreateResult createPayment(OrderEntity order) {
        String provider = paymentProperties.getProvider();
        if (provider == null || provider.isBlank() || "mock".equalsIgnoreCase(provider)) {
            return mockPaymentGateway.createPayment(order);
        }
        if ("midas".equalsIgnoreCase(provider)) {
            return wechatPaymentGateway.createPayment(order);
        }
        throw new IllegalArgumentException("不支持的支付网关: " + provider);
    }
}
