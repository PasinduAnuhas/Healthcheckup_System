package com.nutrihealth.repository;

import com.nutrihealth.entity.NutritionistProfile;
import com.nutrihealth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NutritionistProfileRepository extends JpaRepository<NutritionistProfile, Long> {
    Optional<NutritionistProfile> findByUser(User user);
    Optional<NutritionistProfile> findByUserId(Long userId);
}
