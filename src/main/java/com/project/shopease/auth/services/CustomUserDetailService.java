package com.project.shopease.auth.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import com.project.shopease.auth.entities.User;
import com.project.shopease.auth.repositories.UserDetailRepository;

@Service
public class CustomUserDetailService implements UserDetailsService {

    @Autowired
    private UserDetailRepository userDetailRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userDetailRepository.findByEmail(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username " + username);
        }
        return user;
    }

    public User getUserByEmail(String email) {
        return userDetailRepository.findByEmail(email);
    }
}
