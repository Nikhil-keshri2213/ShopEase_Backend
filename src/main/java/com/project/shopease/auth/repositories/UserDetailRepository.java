package com.project.shopease.auth.repositories;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.project.shopease.auth.entities.User;

@Repository
public interface UserDetailRepository extends JpaRepository<User, UUID> {
    User findByEmail(String email);
}
