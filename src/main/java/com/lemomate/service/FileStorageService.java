package com.lemomate.service;

import com.lemomate.config.FileStorageConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(FileStorageConfig fileStorageConfig) {
        this.fileStorageLocation = Paths.get(fileStorageConfig.getUploadDir())
                .toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("无法创建文件上传目录", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        // 规范化文件名
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        
        try {
            // 检查文件名是否包含无效字符
            if (originalFileName.contains("..")) {
                throw new RuntimeException("文件名包含无效路径序列 " + originalFileName);
            }

            // 生成唯一文件名
            String fileExtension = "";
            if (originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + fileExtension;
            
            // 复制文件到目标位置
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new RuntimeException("无法存储文件 " + originalFileName, ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("文件未找到 " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("文件未找到 " + fileName, ex);
        }
    }
}
