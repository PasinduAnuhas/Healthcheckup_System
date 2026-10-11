package com.nutrihealth.repository;

import com.nutrihealth.entity.User;
import com.nutrihealth.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    List<User> findByRole(UserRole role);
    long countByRole(UserRole role);
    Optional<User> findByResetToken(String resetToken);
}
