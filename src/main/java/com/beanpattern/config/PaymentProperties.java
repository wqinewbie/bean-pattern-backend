package com.beanpattern.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付配置。
 */
@Component
@ConfigurationProperties(prefix = "payment")
public class PaymentProperties {

    private String provider = "mock";
    private final Mock mock = new Mock();
    private final Wechat wechat = new Wechat();

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Mock getMock() {
        return mock;
    }

    public Wechat getWechat() {
        return wechat;
    }

    public static class Mock {
        private boolean autoPaid = true;

        public boolean isAutoPaid() {
            return autoPaid;
        }

        public void setAutoPaid(boolean autoPaid) {
            this.autoPaid = autoPaid;
        }
    }

    public static class Wechat {
        private String appId = "";
        private String mchId = "";
        private String apiV3Key = "";
        private String privateKeyPath = "";
        private String merchantSerialNo = "";
        private String notifyUrl = "";

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getMchId() {
            return mchId;
        }

        public void setMchId(String mchId) {
            this.mchId = mchId;
        }

        public String getApiV3Key() {
            return apiV3Key;
        }

        public void setApiV3Key(String apiV3Key) {
            this.apiV3Key = apiV3Key;
        }

        public String getPrivateKeyPath() {
            return privateKeyPath;
        }

        public void setPrivateKeyPath(String privateKeyPath) {
            this.privateKeyPath = privateKeyPath;
        }

        public String getMerchantSerialNo() {
            return merchantSerialNo;
        }

        public void setMerchantSerialNo(String merchantSerialNo) {
            this.merchantSerialNo = merchantSerialNo;
        }

        public String getNotifyUrl() {
            return notifyUrl;
        }

        public void setNotifyUrl(String notifyUrl) {
            this.notifyUrl = notifyUrl;
        }
    }
}
