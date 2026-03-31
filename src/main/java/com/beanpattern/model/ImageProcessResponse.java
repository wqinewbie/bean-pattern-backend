package com.beanpattern.model;

public class ImageProcessResponse {

    private String originalUrl;
    private String processedUrl;

    public ImageProcessResponse() {
    }

    public ImageProcessResponse(String originalUrl, String processedUrl) {
        this.originalUrl = originalUrl;
        this.processedUrl = processedUrl;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getProcessedUrl() {
        return processedUrl;
    }

    public void setProcessedUrl(String processedUrl) {
        this.processedUrl = processedUrl;
    }
}
