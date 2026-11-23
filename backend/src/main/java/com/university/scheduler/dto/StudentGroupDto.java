package com.university.scheduler.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class StudentGroupDto {
    private Long id;

    @NotBlank(message = "Group code is required")
    private String groupCode;

    @NotBlank(message = "Group name is required")
    private String groupName;

    @NotNull(message = "Department ID is required")
    private Long departmentId;
    private String departmentName;
    private String departmentCode;

    @NotNull(message = "Semester is required")
    @Min(1)
    private Integer semester;

    @NotNull(message = "Student count is required")
    @Min(1)
    private Integer studentCount;

    private List<Long> enrolledCourseIds;

    public StudentGroupDto() {}

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

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
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

    public List<Long> getEnrolledCourseIds() {
        return enrolledCourseIds;
    }

    public void setEnrolledCourseIds(List<Long> enrolledCourseIds) {
        this.enrolledCourseIds = enrolledCourseIds;
    }
}
