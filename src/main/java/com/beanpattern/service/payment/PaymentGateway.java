package com.beanpattern.service.payment;

import com.beanpattern.entity.OrderEntity;
import com.beanpattern.model.PaymentCreateResult;

/**
 * 支付网关抽象。
 */
public interface PaymentGateway {

    PaymentCreateResult createPayment(OrderEntity order);
}
