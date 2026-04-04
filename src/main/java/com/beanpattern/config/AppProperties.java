package com.beanpattern.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String uploadDir;
    private String baseUrl;
    private final Wechat wechat = new Wechat();
    private final Ai ai = new Ai();
    private final S3 s3 = new S3();
    private final Sms sms = new Sms();

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Wechat getWechat() {
        return wechat;
    }

    public Ai getAi() {
        return ai;
    }

    public S3 getS3() {
        return s3;
    }

    public Sms getSms() {
        return sms;
    }

    public static class S3 {
        private String endpoint;
        private String accessKey;
        private String secretKey;
        private String bucket;
        private String region;
        private boolean pathStyleAccess = true;
        private boolean urlIncludeBucket = true;
        private String publicBaseUrl;

        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
        public String getAccessKey() { return accessKey; }
        public void setAccessKey(String accessKey) { this.accessKey = accessKey; }
        public String getSecretKey() { return secretKey; }
        public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
        public String getBucket() { return bucket; }
        public void setBucket(String bucket) { this.bucket = bucket; }
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        public boolean isPathStyleAccess() { return pathStyleAccess; }
        public void setPathStyleAccess(boolean pathStyleAccess) { this.pathStyleAccess = pathStyleAccess; }
        public boolean isUrlIncludeBucket() { return urlIncludeBucket; }
        public void setUrlIncludeBucket(boolean urlIncludeBucket) { this.urlIncludeBucket = urlIncludeBucket; }
        public String getPublicBaseUrl() { return publicBaseUrl; }
        public void setPublicBaseUrl(String publicBaseUrl) { this.publicBaseUrl = publicBaseUrl; }
    }

    public static class Wechat {
        private String appId;
        private String appSecret;

        public String getAppId() { return appId; }
        public void setAppId(String appId) { this.appId = appId; }
        public String getAppSecret() { return appSecret; }
        public void setAppSecret(String appSecret) { this.appSecret = appSecret; }
    }

    public static class Ai {
        private String apiUrl;
        private String apiKey;

        public String getApiUrl() { return apiUrl; }
        public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    }

    public static class Sms {
        private boolean enabled;
        private String secretId;
        private String secretKey;
        private String sdkAppId;
        private String signName;
        private String templateId;
        private String region = "ap-guangzhou";
        private int codeTtlSeconds = 300;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getSecretId() { return secretId; }
        public void setSecretId(String secretId) { this.secretId = secretId; }
        public String getSecretKey() { return secretKey; }
        public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
        public String getSdkAppId() { return sdkAppId; }
        public void setSdkAppId(String sdkAppId) { this.sdkAppId = sdkAppId; }
        public String getSignName() { return signName; }
        public void setSignName(String signName) { this.signName = signName; }
        public String getTemplateId() { return templateId; }
        public void setTemplateId(String templateId) { this.templateId = templateId; }
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        public int getCodeTtlSeconds() { return codeTtlSeconds; }
        public void setCodeTtlSeconds(int codeTtlSeconds) { this.codeTtlSeconds = codeTtlSeconds; }
    }
}
