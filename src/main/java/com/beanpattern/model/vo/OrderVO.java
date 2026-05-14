package com.beanpattern.model.vo;

import com.beanpattern.entity.OrderEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 小程序订单展示数据。
 */
public class OrderVO {

    private Long id;
    private String orderNo;
    private String productType;
    private String productName;
    private String packageCode;
    private BigDecimal amount;
    private String status;
    private String deliverStatus;
    private LocalDateTime paidAt;
    private LocalDateTime expireAt;
    private LocalDateTime createdAt;

    public static OrderVO from(OrderEntity order) {
        OrderVO vo = new OrderVO();
        vo.id = order.getId();
        vo.orderNo = order.getOrderNo();
        vo.productType = order.getProductType();
        vo.productName = order.getPlanName() != null && !order.getPlanName().isBlank()
                ? order.getPlanName()
                : order.getPackageCode();
        vo.packageCode = order.getPackageCode();
        vo.amount = order.getAmount();
        vo.status = order.getStatus();
        vo.deliverStatus = order.getDeliverStatus();
        vo.paidAt = order.getPaidAt();
        vo.expireAt = order.getExpireAt();
        vo.createdAt = order.getCreatedAt();
        return vo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getPackageCode() { return packageCode; }
    public void setPackageCode(String packageCode) { this.packageCode = packageCode; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDeliverStatus() { return deliverStatus; }
    public void setDeliverStatus(String deliverStatus) { this.deliverStatus = deliverStatus; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
    public LocalDateTime getExpireAt() { return expireAt; }
    public void setExpireAt(LocalDateTime expireAt) { this.expireAt = expireAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
