package com.project.shopease.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.project.shopease.services.FileUploadService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/file")
@CrossOrigin
public class FileUploadController {
    
    @Autowired
    FileUploadService fileUploadService;
    
    @PostMapping("/uploadImageWithUrl")
    public ResponseEntity<?> fileUploadWithUrl(
            @RequestParam(value = "file", required = true) MultipartFile file, 
            @RequestParam(value = "fileName", required = true) String fileName) {
        
        String fileUrl = fileUploadService.getFileUrl(file, fileName);
        
        if (fileUrl != null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "File uploaded successfully");
            response.put("fileUrl", fileUrl);
            
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("message", "File upload failed");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}