package com.project.shopease.services;

import com.project.shopease.auth.entities.User;
import com.project.shopease.dto.WishlistDto;

import java.util.List;
import java.util.UUID;

public interface WishlistService {
    List<WishlistDto> getUserWishlist(User user);
    void addToWishlist(User user, UUID productId);
    void removeFromWishlist(User user, UUID productId);
}
