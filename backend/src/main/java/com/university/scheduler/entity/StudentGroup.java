package com.university.scheduler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

@Entity
@Table(name = "student_groups")
public class StudentGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Group code is required")
    @Column(name = "group_code", nullable = false, unique = true, length = 30)
    private String groupCode;

    @NotBlank(message = "Group name is required")
    @Column(name = "group_name", nullable = false, length = 100)
    private String groupName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @NotNull(message = "Semester is required")
    @Min(value = 1, message = "Semester must be >= 1")
    @Column(nullable = false)
    private Integer semester;

    @NotNull(message = "Student count is required")
    @Min(value = 1, message = "Student count must be >= 1")
    @Column(name = "student_count", nullable = false)
    private Integer studentCount;

    public StudentGroup() {}

    public StudentGroup(String groupCode, String groupName, Department department, Integer semester, Integer studentCount) {
        this.groupCode = groupCode;
        this.groupName = groupName;
        this.department = department;
        this.semester = semester;
        this.studentCount = studentCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Integer getSemester() {
        return semester;
    }

    public void setSemester(Integer semester) {
        this.semester = semester;
    }

    public Integer getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(Integer studentCount) {
        this.studentCount = studentCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StudentGroup that)) return false;
        return Objects.equals(id, that.id) || Objects.equals(groupCode, that.groupCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, groupCode);
    }
}
