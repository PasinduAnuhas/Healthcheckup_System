-- =======================================================
-- NutriHealth Initial Seed Data
-- =======================================================

-- Roles
INSERT IGNORE INTO roles (id, name) VALUES 
(1, 'ROLE_PATIENT'),
(2, 'ROLE_DOCTOR'),
(3, 'ROLE_NUTRITIONIST'),
(4, 'ROLE_PATIENT_CARE_COORDINATOR'),
(5, 'ROLE_CLINIC_ADMIN'),
(6, 'ROLE_IT_OPERATIONS_OFFICER');

-- Common Foods Database
INSERT IGNORE INTO foods (id, name, category, serving_size, calories_per_100g, protein_g, carbs_g, fat_g, fiber_g, description, is_healthy, allergens) VALUES
(1, 'Rolled Oats', 'GRAINS', '100g', 389, 16.9, 66.3, 6.9, 10.6, 'Whole grain rich in beta-glucan soluble fiber for healthy cholesterol.', TRUE, 'Gluten'),
(2, 'Greek Yogurt (Plain)', 'DAIRY', '150g', 97, 10.0, 3.6, 5.0, 0.0, 'High-protein probiotic yogurt promoting gut and metabolic health.', TRUE, 'Lactose'),
(3, 'Wild Salmon Fillet', 'PROTEIN', '120g', 208, 20.4, 0.0, 13.4, 0.0, 'Rich in Omega-3 fatty acids for cardiovascular and joint health.', TRUE, 'Fish'),
(4, 'Fresh Blueberries', 'FRUITS', '100g', 57, 0.7, 14.5, 0.3, 2.4, 'Antioxidant-rich berries that support glucose stability and vascular integrity.', TRUE, 'None'),
(5, 'Steamed Broccoli', 'VEGETABLES', '100g', 34, 2.8, 6.6, 0.4, 2.6, 'Cruciferous vegetable packed with sulforaphane, folate, and Vitamin C.', TRUE, 'None'),
(6, 'Hass Avocado', 'HEALTHY_FATS', '100g', 160, 2.0, 8.5, 14.7, 6.7, 'Monounsaturated healthy fats supporting hormone production and satiety.', TRUE, 'None'),
(7, 'Organic Quinoa', 'GRAINS', '100g', 120, 4.4, 21.3, 1.9, 2.8, 'Complete plant protein containing all nine essential amino acids.', TRUE, 'None'),
(8, 'Chicken Breast (Skinless)', 'PROTEIN', '100g', 165, 31.0, 0.0, 3.6, 0.0, 'Lean high-yield protein source ideal for muscle preservation and recovery.', TRUE, 'None'),
(9, 'Raw Almonds', 'HEALTHY_FATS', '30g', 579, 21.2, 21.6, 49.9, 12.5, 'Nutrient-dense nut with Vitamin E, magnesium, and dietary fiber.', TRUE, 'Tree Nuts'),
(10, 'Baby Spinach', 'VEGETABLES', '100g', 23, 2.9, 3.6, 0.4, 2.2, 'Leafy green high in iron, calcium, and carotenoids for cellular vitality.', TRUE, 'None');
