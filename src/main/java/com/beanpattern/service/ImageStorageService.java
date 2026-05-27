package com.beanpattern.service;

import com.beanpattern.config.AppProperties;
import com.beanpattern.model.ImageUploadResponse;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class ImageStorageService {

    private final AppProperties appProperties;
    private volatile S3Client s3Client;

    public ImageStorageService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public ImageUploadResponse store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }
        // 统一生成对象名（key），避免文件名碰撞
        String extension = guessExtension(file.getOriginalFilename());
        String key = UUID.randomUUID() + extension;

        System.out.println("[ImageStorageService] store() called, originalFilename=" + file.getOriginalFilename() + ", key=" + key);

        // 只要 s3 配置齐全就走对象存储（你要求"不准备把图片存到本地"，所以这里把本地当兜底）
        if (isS3Configured()) {
            System.out.println("[ImageStorageService] S3 is configured, uploading to S3...");
            uploadToS3(file, key);
            String publicUrl = buildPublicUrl(key);
            System.out.println("[ImageStorageService] Upload success, publicUrl=" + publicUrl);
            return new ImageUploadResponse(publicUrl, key);
        }

        // 兜底：如果你还没配 s3 配置，仍然不让代码彻底报错
        // 注意：真实生产请确保 s3 配置正确；本兜底仍会依赖本地 uploads。
        System.err.println("[ImageStorageService] S3 is NOT configured!");
        throw new IllegalStateException("S3 is not configured. Please fill app.s3.* in application-dev.yaml / application-prod.yaml");
    }

    private boolean isS3Configured() {
        AppProperties.S3 s3 = appProperties.getS3();
        boolean hasBucket = StringUtils.hasText(s3.getBucket());
        boolean hasAccessKey = StringUtils.hasText(s3.getAccessKey());
        boolean hasSecretKey = StringUtils.hasText(s3.getSecretKey());
        boolean hasPublicBaseUrl = StringUtils.hasText(s3.getPublicBaseUrl());

        System.out.println("[ImageStorageService] S3 Config Check:");
        System.out.println("  - bucket: " + (hasBucket ? s3.getBucket() : "NOT SET"));
        System.out.println("  - accessKey: " + (hasAccessKey ? "SET (length=" + s3.getAccessKey().length() + ")" : "NOT SET"));
        System.out.println("  - secretKey: " + (hasSecretKey ? "SET (length=" + s3.getSecretKey().length() + ")" : "NOT SET"));
        System.out.println("  - publicBaseUrl: " + (hasPublicBaseUrl ? s3.getPublicBaseUrl() : "NOT SET"));
        System.out.println("  - endpoint: " + (StringUtils.hasText(s3.getEndpoint()) ? s3.getEndpoint() : "NOT SET"));
        System.out.println("  - region: " + s3.getRegion());

        return hasBucket && hasAccessKey && hasSecretKey && hasPublicBaseUrl;
    }

    private void uploadToS3(MultipartFile file, String key) {
        AppProperties.S3 s3 = appProperties.getS3();

        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType)) {
            contentType = "application/octet-stream";
        }

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(s3.getBucket())
                .key(key)
                // 你要求"公开读"：这里给对象设置 PUBLIC_READ ACL。
                // 若 AWS 开启了 Block Public Access，可能需要改为配置 bucket policy/CDN（后续再处理）。
                .acl(ObjectCannedACL.PUBLIC_READ)
                .contentType(contentType)
                .build();

        long size = file.getSize();
        RequestBody body;
        try {
            if (size >= 0) {
                InputStream inputStream = file.getInputStream();
                body = RequestBody.fromInputStream(inputStream, size);
            } else {
                // 大多数上传场景都有 size，这里兜底处理 size=-1 的情况
                body = RequestBody.fromBytes(file.getBytes());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read uploaded file", e);
        }

        // 同步上传（当前为"上传-处理-返回 URL"的简单链路）
        s3Client().putObject(request, body);
    }

    public StoredImage readPublicUrl(String publicUrl) {
        String key = resolveKeyFromPublicUrl(publicUrl);
        ResponseBytes<GetObjectResponse> object = s3Client().getObjectAsBytes(GetObjectRequest.builder()
                .bucket(appProperties.getS3().getBucket())
                .key(key)
                .build());
        String contentType = object.response().contentType();
        if (!StringUtils.hasText(contentType)) {
            contentType = "application/octet-stream";
        }
        return new StoredImage(object.asByteArray(), contentType);
    }

    private String resolveKeyFromPublicUrl(String publicUrl) {
        if (!StringUtils.hasText(publicUrl)) {
            throw new IllegalArgumentException("image url is empty");
        }

        AppProperties.S3 s3 = appProperties.getS3();
        String publicBaseUrl = trimTrailingSlash(s3.getPublicBaseUrl());
        if (!StringUtils.hasText(publicBaseUrl)) {
            throw new IllegalStateException("S3 publicBaseUrl is empty");
        }

        URI requested = URI.create(publicUrl);
        URI allowedBase = URI.create(publicBaseUrl);
        if (!equalsIgnoreCase(requested.getScheme(), allowedBase.getScheme())
                || !equalsIgnoreCase(requested.getHost(), allowedBase.getHost())) {
            throw new IllegalArgumentException("image url host is not allowed");
        }

        String basePath = trimTrailingSlash(allowedBase.getPath());
        String path = requested.getPath();
        if (basePath == null) basePath = "";
        if (!path.startsWith(basePath)) {
            throw new IllegalArgumentException("image url path is not allowed");
        }

        String key = path.substring(basePath.length());
        while (key.startsWith("/")) {
            key = key.substring(1);
        }
        if (s3.isUrlIncludeBucket() && key.startsWith(s3.getBucket() + "/")) {
            key = key.substring(s3.getBucket().length() + 1);
        }
        key = URLDecoder.decode(key, StandardCharsets.UTF_8);
        if (!StringUtils.hasText(key) || key.contains("..")) {
            throw new IllegalArgumentException("invalid image key");
        }
        return key;
    }

    private S3Client s3Client() {
        if (s3Client != null) {
            return s3Client;
        }
        synchronized (this) {
            if (s3Client != null) {
                return s3Client;
            }

            AppProperties.S3 s3 = appProperties.getS3();
            String region = StringUtils.hasText(s3.getRegion()) ? s3.getRegion() : "us-east-1";

            AwsBasicCredentials credentials = AwsBasicCredentials.create(s3.getAccessKey(), s3.getSecretKey());
            StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);

            // MinIO / S3 兼容对象存储：
            // - MinIO 通常需要 endpointOverride + path-style
            // - AWS 真 S3 可以不填 endpoint，走默认
            // AWS SDK v2：S3Client 是接口，builder() 返回的是 S3ClientBuilder
            software.amazon.awssdk.services.s3.S3ClientBuilder builder = S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(credentialsProvider)
                    .serviceConfiguration(S3Configuration.builder()
                            .pathStyleAccessEnabled(s3.isPathStyleAccess())
                            .build());

            if (StringUtils.hasText(s3.getEndpoint())) {
                builder.endpointOverride(URI.create(s3.getEndpoint()));
            }

            s3Client = builder.build();
            return s3Client;
        }
    }

    private String buildPublicUrl(String key) {
        AppProperties.S3 s3 = appProperties.getS3();
        String base = s3.getPublicBaseUrl();
        if (!StringUtils.hasText(base)) {
            base = appProperties.getBaseUrl();
        }
        if (!StringUtils.hasText(base)) {
            throw new IllegalStateException("publicBaseUrl is empty; cannot build image URL");
        }

        base = trimTrailingSlash(base);
        if (s3.isUrlIncludeBucket()) {
            return base + "/" + s3.getBucket() + "/" + key;
        }
        return base + "/" + key;
    }

    private String trimTrailingSlash(String s) {
        if (s == null) {
            return null;
        }
        while (s.endsWith("/")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    private boolean equalsIgnoreCase(String a, String b) {
        return a == null ? b == null : a.equalsIgnoreCase(b);
    }

    public record StoredImage(byte[] bytes, String contentType) {}

    private String guessExtension(String originalFilename) {
        if (!StringUtils.hasText(originalFilename) || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.'));
    }
}
