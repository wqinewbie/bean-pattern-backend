package com.beanpattern.model;

/**
 * 支付创建结果 — 同时兼容 mock 和微信虚拟支付（Midas）。
 */
public class PaymentCreateResult {

    private String orderNo;
    private String status;
    private String provider;
    private boolean mock;

    /** 虚拟支付：Midas signData JSON 字符串 */
    private String signData;
    /** 虚拟支付：支付签名 paySig */
    private String paySig;
    /** 虚拟支付：用户态签名 signature */
    private String signature;
    /** 虚拟支付模式：short_series_goods / short_series_coin */
    private String mode;

    // ---- getters / setters ----

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public boolean isMock() { return mock; }
    public void setMock(boolean mock) { this.mock = mock; }

    public String getSignData() { return signData; }
    public void setSignData(String signData) { this.signData = signData; }
    public String getPaySig() { return paySig; }
    public void setPaySig(String paySig) { this.paySig = paySig; }
    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
}
