package com.university.scheduler.dto;

import com.university.scheduler.entity.RoomType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class CourseDto {
    private Long id;

    @NotBlank(message = "Course code is required")
    private String courseCode;

    @NotBlank(message = "Course name is required")
    private String courseName;

    @NotNull(message = "Department ID is required")
    private Long departmentId;
    private String departmentName;
    private String departmentCode;

    @NotNull(message = "Semester is required")
    @Min(1)
    private Integer semester;

    @NotNull(message = "Credits is required")
    @Min(1)
    private Integer credits;

    private Long assignedFacultyId;
    private String assignedFacultyName;

    @NotNull(message = "Required room type is required")
    private RoomType requiredRoomType = RoomType.CLASSROOM;

    @NotNull(message = "Weekly sessions is required")
    @Min(1)
    private Integer weeklySessions = 3;

    @NotNull(message = "Session duration is required")
    @Min(30)
    private Integer sessionDurationMinutes = 60;

    private List<Long> studentGroupIds;
    private List<String> studentGroupNames;

    public CourseDto() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
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

    public Integer getCredits() {
        return credits;
    }

    public void setCredits(Integer credits) {
        this.credits = credits;
    }

    public Long getAssignedFacultyId() {
        return assignedFacultyId;
    }

    public void setAssignedFacultyId(Long assignedFacultyId) {
        this.assignedFacultyId = assignedFacultyId;
    }

    public String getAssignedFacultyName() {
        return assignedFacultyName;
    }

    public void setAssignedFacultyName(String assignedFacultyName) {
        this.assignedFacultyName = assignedFacultyName;
    }

    public RoomType getRequiredRoomType() {
        return requiredRoomType;
    }

    public void setRequiredRoomType(RoomType requiredRoomType) {
        this.requiredRoomType = requiredRoomType;
    }

    public Integer getWeeklySessions() {
        return weeklySessions;
    }

    public void setWeeklySessions(Integer weeklySessions) {
        this.weeklySessions = weeklySessions;
    }

    public Integer getSessionDurationMinutes() {
        return sessionDurationMinutes;
    }

    public void setSessionDurationMinutes(Integer sessionDurationMinutes) {
        this.sessionDurationMinutes = sessionDurationMinutes;
    }

    public List<Long> getStudentGroupIds() {
        return studentGroupIds;
    }

    public void setStudentGroupIds(List<Long> studentGroupIds) {
        this.studentGroupIds = studentGroupIds;
    }

    public List<String> getStudentGroupNames() {
        return studentGroupNames;
    }

    public void setStudentGroupNames(List<String> studentGroupNames) {
        this.studentGroupNames = studentGroupNames;
    }
}
