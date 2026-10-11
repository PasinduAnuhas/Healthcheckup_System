package com.nutrihealth.repository;

import com.nutrihealth.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {
    List<Food> findByCategory(String category);
    List<Food> findByNameContainingIgnoreCase(String name);
    List<Food> findByHealthy(boolean healthy);
}
