package com.beanpattern.model;

import jakarta.validation.constraints.NotBlank;

public class ImageProcessRequest {

    @NotBlank(message = "imageUrl is required")
    private String imageUrl;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
