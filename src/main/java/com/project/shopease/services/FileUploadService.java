package com.project.shopease.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

@Service
public class FileUploadService {
    
    @Value("${cloudinary.cloud.name}")
    private String cloudName;
    
    @Value("${cloudinary.api.key}")
    private String apiKey;
    
    @Value("${cloudinary.api.secret}")
    private String apiSecret;
    
    private Cloudinary cloudinary;
    
    @PostConstruct
    public void init() {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", cloudName,
            "api_key", apiKey,
            "api_secret", apiSecret
        ));
    }
    
    public int uploadFile(MultipartFile file, String fileName) {
        try {
            // Remove file extension from fileName for public_id
            String publicId = fileName.contains(".") 
                ? fileName.substring(0, fileName.lastIndexOf(".")) 
                : fileName;
            
            Map<?,?> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                    "public_id", publicId,
                    "resource_type", "auto" // auto-detects image/video/raw
                )
            );
            
            // You can get the URL of uploaded file like this:
            // String url = (String) uploadResult.get("secure_url");
            
            return 200; // Success
        } catch (IOException e) {
            e.printStackTrace();
            return 500;
        } catch (Exception e) {
            e.printStackTrace();
            return 500;
        }
    }
    
    // Optional: Method to get the uploaded file URL
    public String getFileUrl(MultipartFile file, String fileName) {
        try {
            String publicId = fileName.contains(".") 
                ? fileName.substring(0, fileName.lastIndexOf(".")) 
                : fileName;
            
            Map<?,?> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap("public_id", publicId, "resource_type", "auto")
            );
            
            return (String) uploadResult.get("secure_url");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}