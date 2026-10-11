-- =======================================================
-- NutriHealth Database Schema (MySQL 8.0+)
-- Web-Based Diet Planning & Health Check-up System
-- =======================================================

CREATE DATABASE IF NOT EXISTS nutrihealth CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE nutrihealth;

-- 1. Roles Table
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB;

-- 2. Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(120) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(25),
    address VARCHAR(255),
    date_of_birth DATE,
    gender VARCHAR(20),
    role VARCHAR(50) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    reset_token VARCHAR(255),
    reset_token_expiry TIMESTAMP NULL
) ENGINE=InnoDB;

-- 3. Patient Profiles
CREATE TABLE IF NOT EXISTS patient_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    blood_group VARCHAR(10),
    emergency_contact VARCHAR(100),
    height_cm DOUBLE,
    current_weight_kg DOUBLE,
    target_weight_kg DOUBLE,
    allergies TEXT,
    medical_conditions TEXT,
    activity_level VARCHAR(50),
    daily_calorie_target INT DEFAULT 2000,
    assigned_doctor_id BIGINT,
    assigned_nutritionist_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_doctor_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (assigned_nutritionist_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 4. Doctor Profiles
CREATE TABLE IF NOT EXISTS doctor_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    specialization VARCHAR(100) NOT NULL,
    license_number VARCHAR(100) NOT NULL,
    qualification VARCHAR(150),
    department VARCHAR(100),
    consultation_room VARCHAR(50),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 5. Nutritionist Profiles
CREATE TABLE IF NOT EXISTS nutritionist_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    qualification VARCHAR(150) NOT NULL,
    certification_number VARCHAR(100) NOT NULL,
    experience_years INT DEFAULT 0,
    consultation_room VARCHAR(50),
    specialization_areas VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 6. Health Records
CREATE TABLE IF NOT EXISTS health_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT,
    record_date DATE NOT NULL,
    weight_kg DOUBLE NOT NULL,
    height_cm DOUBLE NOT NULL,
    systolic_bp INT,
    diastolic_bp INT,
    blood_sugar_mg_dl DOUBLE,
    heart_rate_bpm INT,
    temperature_c DOUBLE,
    bmi DOUBLE,
    status_summary VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 7. Health Assessments
CREATE TABLE IF NOT EXISTS health_assessments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    assessment_date DATE NOT NULL,
    symptoms TEXT,
    diagnosis VARCHAR(255),
    clinical_recommendations TEXT,
    medications TEXT,
    next_checkup_date DATE,
    status VARCHAR(50) DEFAULT 'COMPLETED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 8. Food Database
CREATE TABLE IF NOT EXISTS foods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    category VARCHAR(50) NOT NULL,
    serving_size VARCHAR(100) DEFAULT '100g',
    calories_per_100g INT NOT NULL,
    protein_g DOUBLE DEFAULT 0,
    carbs_g DOUBLE DEFAULT 0,
    fat_g DOUBLE DEFAULT 0,
    fiber_g DOUBLE DEFAULT 0,
    description TEXT,
    is_healthy BOOLEAN DEFAULT TRUE,
    allergens VARCHAR(255)
) ENGINE=InnoDB;

-- 9. Diet Plans
CREATE TABLE IF NOT EXISTS diet_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    nutritionist_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    objective VARCHAR(255),
    daily_calorie_target INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    recommended_foods TEXT,
    restricted_foods TEXT,
    nutritionist_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (nutritionist_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 10. Diet Plan Meals
CREATE TABLE IF NOT EXISTS diet_plan_meals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    diet_plan_id BIGINT NOT NULL,
    meal_type VARCHAR(50) NOT NULL,
    food_name VARCHAR(150) NOT NULL,
    portion_size VARCHAR(100),
    calories INT NOT NULL,
    protein_g DOUBLE DEFAULT 0,
    carbs_g DOUBLE DEFAULT 0,
    fats_g DOUBLE DEFAULT 0,
    instructions TEXT,
    FOREIGN KEY (diet_plan_id) REFERENCES diet_plans(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 11. Food Intake Logs
CREATE TABLE IF NOT EXISTS food_intakes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    log_date DATE NOT NULL,
    meal_type VARCHAR(50) NOT NULL,
    food_item VARCHAR(150) NOT NULL,
    quantity VARCHAR(100),
    calories INT NOT NULL,
    protein_g DOUBLE DEFAULT 0,
    carbs_g DOUBLE DEFAULT 0,
    fat_g DOUBLE DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 12. Meal Recommendations
CREATE TABLE IF NOT EXISTS meal_recommendations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    nutritionist_id BIGINT NOT NULL,
    meal_type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    calorie_estimate INT,
    nutrition_benefits TEXT,
    dietary_restriction_friendly BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (nutritionist_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 13. Appointments
CREATE TABLE IF NOT EXISTS appointments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT,
    nutritionist_id BIGINT,
    appointment_date DATE NOT NULL,
    appointment_time VARCHAR(20) NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    reason TEXT NOT NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (nutritionist_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 14. Messages
CREATE TABLE IF NOT EXISTS messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    recipient_id BIGINT NOT NULL,
    subject VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 15. Notifications
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    link_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 16. Audit Logs
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_email VARCHAR(120),
    user_role VARCHAR(50),
    action VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    ip_address VARCHAR(45),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;


-- 17. Patient Health Progress Entries (CRUD)
CREATE TABLE IF NOT EXISTS health_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    progress_date DATE NOT NULL,
    weight_kg DOUBLE NOT NULL,
    height_cm DOUBLE,
    bmi DOUBLE,
    systolic_bp INT,
    diastolic_bp INT,
    blood_sugar_mg_dl DOUBLE,
    heart_rate_bpm INT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 18. Saved Clinic Report Definitions (CRUD)
CREATE TABLE IF NOT EXISTS clinic_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    report_type VARCHAR(80) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;
