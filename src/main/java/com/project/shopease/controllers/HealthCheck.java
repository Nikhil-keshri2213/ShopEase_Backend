package com.project.shopease.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class HealthCheck {

    @GetMapping
    public String Test(){
        return "Testing... Health Check";
    }
}
