package com.beanpattern.model;

import java.util.Map;

/**
 * 小程序支付调起参数。
 */
public class PaymentCreateResult {

    private String orderNo;
    private String status;
    private String provider;
    private boolean mock;
    private Map<String, String> payParams;

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public boolean isMock() {
        return mock;
    }

    public void setMock(boolean mock) {
        this.mock = mock;
    }

    public Map<String, String> getPayParams() {
        return payParams;
    }

    public void setPayParams(Map<String, String> payParams) {
        this.payParams = payParams;
    }
}
