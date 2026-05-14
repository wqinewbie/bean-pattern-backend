package com.beanpattern.model;

import com.beanpattern.entity.OrderEntity;

/**
 * 创建订单后的统一返回体。
 */
public class OrderPaymentResponse {

    private OrderEntity order;
    private String orderNo;
    private String status;
    private boolean mock;
    private PaymentCreateResult payment;

    public static OrderPaymentResponse from(OrderEntity order, PaymentCreateResult payment) {
        OrderPaymentResponse response = new OrderPaymentResponse();
        response.order = order;
        response.orderNo = order.getOrderNo();
        response.status = payment != null ? payment.getStatus() : order.getStatus();
        response.mock = payment != null && payment.isMock();
        response.payment = payment;
        return response;
    }

    public OrderEntity getOrder() {
        return order;
    }

    public void setOrder(OrderEntity order) {
        this.order = order;
    }

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

    public boolean isMock() {
        return mock;
    }

    public void setMock(boolean mock) {
        this.mock = mock;
    }

    public PaymentCreateResult getPayment() {
        return payment;
    }

    public void setPayment(PaymentCreateResult payment) {
        this.payment = payment;
    }
}
