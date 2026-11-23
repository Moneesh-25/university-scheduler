package com.university.scheduler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Objects;

@Entity
@Table(name = "faculty")
public class Faculty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Faculty ID is required")
    @Column(name = "faculty_code", nullable = false, unique = true, length = 30)
    private String facultyCode;

    @NotBlank(message = "Faculty name is required")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(length = 150)
    private String specialization;

    @Column(name = "max_weekly_hours")
    private Integer maxWeeklyHours = 18;

    public Faculty() {}

    public Faculty(String facultyCode, String name, String email, Department department, String specialization, Integer maxWeeklyHours) {
        this.facultyCode = facultyCode;
        this.name = name;
        this.email = email;
        this.department = department;
        this.specialization = specialization;
        this.maxWeeklyHours = (maxWeeklyHours != null) ? maxWeeklyHours : 18;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFacultyCode() {
        return facultyCode;
    }

    public void setFacultyCode(String facultyCode) {
        this.facultyCode = facultyCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public Integer getMaxWeeklyHours() {
        return maxWeeklyHours;
    }

    public void setMaxWeeklyHours(Integer maxWeeklyHours) {
        this.maxWeeklyHours = maxWeeklyHours;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Faculty faculty)) return false;
        return Objects.equals(id, faculty.id) || Objects.equals(facultyCode, faculty.facultyCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, facultyCode);
    }
}
