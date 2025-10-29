package com.project.shopease.auth.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.shopease.auth.config.JWTTokenHelper;
import com.project.shopease.auth.entities.User;
import com.project.shopease.auth.services.OAuth2Service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@CrossOrigin
@RequestMapping("/oauth2")
public class OAuth2Controller {

    @Autowired
    OAuth2Service oAuth2Service;

    @Autowired
    private JWTTokenHelper jwtTokenHelper;

    @GetMapping("/success")
    public void callBackOAuth2(@AuthenticationPrincipal OAuth2User oauth2user, HttpServletResponse response) throws IOException {
        String username = oauth2user.getAttribute("email");
        User user = oAuth2Service.getUser(username);
        
        if(null == user){
            user = oAuth2Service.createUser(oauth2user, username);
        }

        String token = jwtTokenHelper.generateToken(user.getUsername());
        response.sendRedirect("http://localhost:5731/oauth2/callback?token="+token);
    }
}