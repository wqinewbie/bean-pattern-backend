package com.beanpattern.service.payment;

import com.beanpattern.config.PaymentProperties;
import com.beanpattern.entity.OrderEntity;
import com.beanpattern.model.PaymentCreateResult;
import org.springframework.stereotype.Component;

/**
 * 微信支付网关骨架，主体变更并开通商户号后补齐 JSAPI 下单和签名。
 */
@Component
public class WechatPaymentGateway implements PaymentGateway {

    private final PaymentProperties paymentProperties;

    public WechatPaymentGateway(PaymentProperties paymentProperties) {
        this.paymentProperties = paymentProperties;
    }

    @Override
    public PaymentCreateResult createPayment(OrderEntity order) {
        if (paymentProperties.getWechat().getMchId() == null || paymentProperties.getWechat().getMchId().isBlank()) {
            throw new IllegalStateException("微信支付商户号未配置");
        }
        throw new UnsupportedOperationException("微信支付 JSAPI 下单尚未接入，请先使用 payment.provider=mock");
    }
}
