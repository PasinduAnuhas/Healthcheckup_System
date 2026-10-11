package com.nutrihealth.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "nutritionist_profiles")
public class NutritionistProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 150)
    private String qualification;

    @Column(name = "certification_number", nullable = false, length = 100)
    private String certificationNumber;

    @Column(name = "experience_years")
    private Integer experienceYears = 5;

    @Column(name = "consultation_room", length = 50)
    private String consultationRoom;

    @Column(name = "specialization_areas", length = 255)
    private String specializationAreas;

    public NutritionistProfile() {
    }

    public NutritionistProfile(User user, String qualification, String certificationNumber) {
        this.user = user;
        this.qualification = qualification;
        this.certificationNumber = certificationNumber;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public String getCertificationNumber() {
        return certificationNumber;
    }

    public void setCertificationNumber(String certificationNumber) {
        this.certificationNumber = certificationNumber;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public String getConsultationRoom() {
        return consultationRoom;
    }

    public void setConsultationRoom(String consultationRoom) {
        this.consultationRoom = consultationRoom;
    }

    public String getSpecializationAreas() {
        return specializationAreas;
    }

    public void setSpecializationAreas(String specializationAreas) {
        this.specializationAreas = specializationAreas;
    }
}
