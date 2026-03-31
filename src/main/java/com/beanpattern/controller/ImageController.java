package com.beanpattern.controller;

import com.beanpattern.model.ApiResponse;
import com.beanpattern.model.ImageUploadResponse;
import com.beanpattern.service.ImageStorageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 图片上传接口。
 * POST /api/image/upload  只负责把文件存入对象存储并返回公开 URL。
 * 任务记录由各业务接口（BeadController）负责创建，上传本身不落库。
 */
@RestController
@RequestMapping("/api/image")
public class ImageController {

    private final ImageStorageService imageStorageService;

    public ImageController(ImageStorageService imageStorageService) {
        this.imageStorageService = imageStorageService;
    }

    /**
     * 上传图片到对象存储，返回可公开访问的 URL。
     * 头像、原图、结果图、图纸都走这个接口，不创建任务记录。
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ImageUploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        ImageUploadResponse upload = imageStorageService.store(file);
        return ApiResponse.ok(upload);
    }
}
