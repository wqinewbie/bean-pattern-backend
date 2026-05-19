package com.beanpattern.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付配置 — 微信虚拟支付（米大师 Midas）。
 */
@Component
@ConfigurationProperties(prefix = "payment")
public class PaymentProperties {

    private String provider = "mock";
    private final Mock mock = new Mock();
    private final Midas midas = new Midas();

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public Mock getMock() { return mock; }
    public Midas getMidas() { return midas; }

    public static class Mock {
        private boolean autoPaid = true;
        public boolean isAutoPaid() { return autoPaid; }
        public void setAutoPaid(boolean autoPaid) { this.autoPaid = autoPaid; }
    }

    public static class Midas {
        /** 小程序 AppID */
        private String appId = "";
        /** 虚拟支付 OfferID */
        private String offerId = "";
        /** 正式环境 AppKey */
        private String appKey = "";
        /** 沙箱环境 AppKey */
        private String sandboxAppKey = "";
        /** 发货回调 URL */
        private String notifyUrl = "";
        /** 发货回调 Token（URL 验证用） */
        private String notifyToken = "";
        /** 是否使用沙箱（dev 环境默认 true） */
        private boolean sandbox = true;
        /** 道具直购默认 quantity */
        private int buyQuantity = 1;

        public String getAppId() { return appId; }
        public void setAppId(String appId) { this.appId = appId; }
        public String getOfferId() { return offerId; }
        public void setOfferId(String offerId) { this.offerId = offerId; }
        public String getAppKey() { return appKey; }
        public void setAppKey(String appKey) { this.appKey = appKey; }
        public String getSandboxAppKey() { return sandboxAppKey; }
        public void setSandboxAppKey(String sandboxAppKey) { this.sandboxAppKey = sandboxAppKey; }
        public String getNotifyUrl() { return notifyUrl; }
        public void setNotifyUrl(String notifyUrl) { this.notifyUrl = notifyUrl; }
        public String getNotifyToken() { return notifyToken; }
        public void setNotifyToken(String notifyToken) { this.notifyToken = notifyToken; }
        public boolean isSandbox() { return sandbox; }
        public void setSandbox(boolean sandbox) { this.sandbox = sandbox; }
        public int getBuyQuantity() { return buyQuantity; }
        public void setBuyQuantity(int buyQuantity) { this.buyQuantity = buyQuantity; }

        /** 当前环境生效的 AppKey */
        public String effectiveAppKey() {
            return sandbox && sandboxAppKey != null && !sandboxAppKey.isBlank()
                    ? sandboxAppKey : appKey;
        }

        public String appKeyForEnv(Integer env) {
            if (env != null && env == 1 && sandboxAppKey != null && !sandboxAppKey.isBlank()) {
                return sandboxAppKey;
            }
            return appKey;
        }
    }
}
