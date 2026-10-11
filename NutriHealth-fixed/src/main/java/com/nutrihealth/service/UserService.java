package com.nutrihealth.service;

import com.nutrihealth.entity.*;
import com.nutrihealth.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final NutritionistProfileRepository nutritionistProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository,
                       PatientProfileRepository patientProfileRepository,
                       DoctorProfileRepository doctorProfileRepository,
                       NutritionistProfileRepository nutritionistProfileRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.nutritionistProfileRepository = nutritionistProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmailIgnoreCase(email);
    }

    public User registerUser(String email, String rawPassword, String fullName, String phone, UserRole role) {
        // Authentication normalizes usernames to lower-case. Registration must
        // store the same canonical value, otherwise a user who registers with
        // e.g. "John@Example.com" cannot log in because the authentication
        // lookup searches for "john@example.com".
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
        String normalizedName = fullName == null ? "" : fullName.trim();
        String normalizedPhone = phone == null ? "" : phone.trim();

        if (normalizedEmail.isBlank()) {
            throw new IllegalArgumentException("Email address is required.");
        }
        if (!normalizedEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }
        if (role == null) {
            throw new IllegalArgumentException("Please select a valid account role.");
        }
        if (existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setFullName(normalizedName);
        user.setPhone(normalizedPhone);
        user.setRole(role);
        user.setActive(true);

        User savedUser = userRepository.save(user);

        // Auto-initialize profile based on role
        if (role == UserRole.ROLE_PATIENT) {
            PatientProfile profile = new PatientProfile(savedUser);
            User assignedDoctor = userRepository.findByRole(UserRole.ROLE_DOCTOR).stream().findFirst().orElse(null);
            User assignedNutritionist = userRepository.findByRole(UserRole.ROLE_NUTRITIONIST).stream().findFirst().orElse(null);
            if (assignedDoctor != null) {
                profile.setAssignedDoctor(assignedDoctor);
            }
            if (assignedNutritionist != null) {
                profile.setAssignedNutritionist(assignedNutritionist);
            }
            patientProfileRepository.save(profile);
        } else if (role == UserRole.ROLE_DOCTOR) {
            DoctorProfile profile = new DoctorProfile(savedUser, "General Medicine", "DOC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            doctorProfileRepository.save(profile);
        } else if (role == UserRole.ROLE_NUTRITIONIST) {
            NutritionistProfile profile = new NutritionistProfile(savedUser, "Clinical Nutritionist", "NUT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            nutritionistProfileRepository.save(profile);
        }

        return savedUser;
    }

    public void updateUserProfile(Long userId, String fullName, String phone, String address, String gender) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setAddress(address);
        user.setGender(gender);
        userRepository.save(user);
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password does not match.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public String generateResetToken(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("No user found with email: " + email));
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(2));
        userRepository.save(user);
        return token;
    }

    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired password reset token."));
        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Reset token has expired. Please request a new one.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }

    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(Long userId, String fullName, String email, String phone, String address, String gender, UserRole role, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
        userRepository.findByEmailIgnoreCase(normalizedEmail).ifPresent(existing -> {
            if (!existing.getId().equals(userId)) throw new IllegalArgumentException("Another account already uses this email.");
        });
        user.setFullName(fullName == null ? "" : fullName.trim());
        user.setEmail(normalizedEmail);
        user.setPhone(phone);
        user.setAddress(address);
        user.setGender(gender);
        if (role != null) user.setRole(role);
        user.setActive(active);
        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        userRepository.delete(user);
    }

    public void toggleUserActive(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    public long countByRole(UserRole role) {
        return userRepository.countByRole(role);
    }

    public long countAllUsers() {
        return userRepository.count();
    }
}
