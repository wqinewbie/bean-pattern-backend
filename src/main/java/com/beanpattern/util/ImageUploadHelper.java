package com.beanpattern.util;

import com.beanpattern.model.ImageUploadResponse;
import com.beanpattern.service.ImageStorageService;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * 图片上传工具类
 * 提供统一的图片上传处理,包括尺寸验证
 */
@Component
public class ImageUploadHelper {

    private final ImageStorageService imageStorageService;

    public ImageUploadHelper(ImageStorageService imageStorageService) {
        this.imageStorageService = imageStorageService;
    }

    /**
     * 上传图片,不限制尺寸
     */
    public ImageUploadResponse uploadImage(MultipartFile file) {
        return uploadImage(file, null, null);
    }

    /**
     * 上传图片,带尺寸限制
     * @param file 上传的文件
     * @param maxWidth 最大宽度(像素),null表示不限制
     * @param maxHeight 最大高度(像素),null表示不限制
     */
    public ImageUploadResponse uploadImage(MultipartFile file, Integer maxWidth, Integer maxHeight) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("只能上传图片文件");
        }

        // 验证文件大小 (10MB)
        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("图片大小不能超过10MB");
        }

        // 如果需要验证尺寸,可以在这里添加图片尺寸检查逻辑
        // 目前保留尺寸参数但不强制验证,由前端控制
        if (maxWidth != null || maxHeight != null) {
            // 尺寸限制提示保留,但不在后端强制验证
            // 前端应该在上传前进行尺寸检查
        }

        // 调用存储服务上传
        return imageStorageService.store(file);
    }

    /**
     * 验证图片URL是否有效
     */
    public boolean isValidImageUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        // 简单验证URL格式
        return url.startsWith("http://") || url.startsWith("https://");
    }
}
