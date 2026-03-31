package com.beanpattern.model;

/**
 * 图片上传响应。
 * imageUrl / originalUrl 均指向同一个可公开访问的图片 URL，
 * 小程序端用 data.imageUrl，后端内部用 originalUrl。
 */
public class ImageUploadResponse {

    private String originalUrl;
    private String fileName;

    public ImageUploadResponse() {
    }

    public ImageUploadResponse(String originalUrl, String fileName) {
        this.originalUrl = originalUrl;
        this.fileName = fileName;
    }

    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }

    /** 别名：与 originalUrl 相同，供小程序端 data.imageUrl 使用 */
    public String getImageUrl() { return originalUrl; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
}
