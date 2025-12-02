package com.zhihuixuexi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * 文件上传配置
 */
@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 计算上传根目录的绝对路径（与 FileUploadController 保持一致）
        String absoluteUploadPath = Paths.get(uploadPath).toAbsolutePath().normalize().toString();

        // 注意：应用已配置 context-path=/api，这里资源路径只写成 /files/**
        // 最终访问 URL 为：http://localhost:8080/api/files/xxx
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + absoluteUploadPath + "/");
    }
}

