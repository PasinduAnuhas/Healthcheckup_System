package com.nutrihealth.entity;

public enum UserRole {
    ROLE_PATIENT("Patient"),
    ROLE_DOCTOR("Doctor"),
    ROLE_NUTRITIONIST("Nutritionist"),
    ROLE_PATIENT_CARE_COORDINATOR("Patient Care Coordinator"),
    ROLE_CLINIC_ADMIN("Clinic Administrator"),
    ROLE_IT_OPERATIONS_OFFICER("IT Operations Officer");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCleanRoleName() {
        return this.name().replace("ROLE_", "");
    }
}
