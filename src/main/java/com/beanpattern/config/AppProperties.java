package com.beanpattern.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String uploadDir;
    private String baseUrl;
    private final Wechat wechat = new Wechat();
    private final Ai ai = new Ai();
    private final S3 s3 = new S3();

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

    /**
     * 对象存储（S3 兼容）：用于存放上传后的图片。
     * <p>
     * 本地开发建议使用 MinIO（S3 兼容）并通过它返回公开可访问的 URL；
     * 生产可切换到 AWS S3 或其它 S3 兼容对象存储。
     */
    public static class S3 {
        /**
         * S3 端点，例如 MinIO：http://127.0.0.1:9000
         * 如果留空，则 AWS SDK 使用 AWS 默认端点。
         */
        private String endpoint;

        private String accessKey;
        private String secretKey;

        /**
         * Bucket 名称（桶）
         */
        private String bucket;

        /**
         * AWS 区域（MinIO 通常填 us-east-1 即可）
         */
        private String region;

        /**
         * 是否启用 path-style：MinIO 通常需要 true（/bucket/key）。
         */
        private boolean pathStyleAccess = true;

        /**
         * 返回 URL 是否包含 bucket 段：publicBaseUrl + /bucket + /key
         * - MinIO 建议 true
         * - AWS 虚拟主机风格建议 false（并把 publicBaseUrl 设置成 bucket host）
         */
        private boolean urlIncludeBucket = true;

        /**
         * 对外可访问的 URL 根地址（必须公开可访问，否则小程序图片加载会失败）
         * 例如：
         * - MinIO：http://127.0.0.1:9000
         * - AWS：https://bucket.s3.us-east-1.amazonaws.com
         */
        private String publicBaseUrl;

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getAccessKey() {
            return accessKey;
        }

        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public boolean isPathStyleAccess() {
            return pathStyleAccess;
        }

        public void setPathStyleAccess(boolean pathStyleAccess) {
            this.pathStyleAccess = pathStyleAccess;
        }

        public boolean isUrlIncludeBucket() {
            return urlIncludeBucket;
        }

        public void setUrlIncludeBucket(boolean urlIncludeBucket) {
            this.urlIncludeBucket = urlIncludeBucket;
        }

        public String getPublicBaseUrl() {
            return publicBaseUrl;
        }

        public void setPublicBaseUrl(String publicBaseUrl) {
            this.publicBaseUrl = publicBaseUrl;
        }
    }

    public static class Wechat {
        private String appId;
        private String appSecret;

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getAppSecret() {
            return appSecret;
        }

        public void setAppSecret(String appSecret) {
            this.appSecret = appSecret;
        }
    }

    public static class Ai {
        private String apiUrl;
        private String apiKey;

        public String getApiUrl() {
            return apiUrl;
        }

        public void setApiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
    }
}
