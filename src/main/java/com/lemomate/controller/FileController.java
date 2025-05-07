package com.lemomate.controller;

import com.lemomate.dto.ApiResponse;
import com.lemomate.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/files")
public class FileController {

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        String fileName = fileStorageService.storeFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/files/")
                .path(fileName)
                .toUriString();

        Map<String, String> response = new HashMap<>();
        response.put("fileName", fileName);
        response.put("fileDownloadUri", fileDownloadUri);
        response.put("fileType", file.getContentType());
        response.put("size", String.valueOf(file.getSize()));

        return ResponseEntity.ok(new ApiResponse(true, "文件上传成功", response));
    }

    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        // 加载文件作为Resource
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        // 尝试确定文件的内容类型
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            // 如果无法确定内容类型，则不设置
        }

        // 如果无法确定内容类型，则默认为二进制流
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
