package com.university.scheduler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Course code is required")
    @Column(name = "course_code", nullable = false, unique = true, length = 30)
    private String courseCode;

    @NotBlank(message = "Course name is required")
    @Column(name = "course_name", nullable = false, length = 120)
    private String courseName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @NotNull(message = "Semester is required")
    @Min(value = 1, message = "Semester must be >= 1")
    @Column(nullable = false)
    private Integer semester;

    @NotNull(message = "Credits is required")
    @Min(value = 1, message = "Credits must be >= 1")
    @Column(nullable = false)
    private Integer credits;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "faculty_id")
    private Faculty assignedFaculty;

    @NotNull(message = "Required room type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "required_room_type", nullable = false, length = 20)
    private RoomType requiredRoomType = RoomType.CLASSROOM;

    @NotNull(message = "Weekly sessions count is required")
    @Min(value = 1, message = "Weekly sessions must be at least 1")
    @Column(name = "weekly_sessions", nullable = false)
    private Integer weeklySessions = 3;

    @NotNull(message = "Session duration is required")
    @Min(value = 30, message = "Session duration must be at least 30 minutes")
    @Column(name = "session_duration_minutes", nullable = false)
    private Integer sessionDurationMinutes = 60;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "course_student_groups",
        joinColumns = @JoinColumn(name = "course_id"),
        inverseJoinColumns = @JoinColumn(name = "student_group_id")
    )
    private Set<StudentGroup> studentGroups = new HashSet<>();

    public Course() {}

    public Course(String courseCode, String courseName, Department department, Integer semester, Integer credits,
                  Faculty assignedFaculty, RoomType requiredRoomType, Integer weeklySessions, Integer sessionDurationMinutes) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.department = department;
        this.semester = semester;
        this.credits = credits;
        this.assignedFaculty = assignedFaculty;
        this.requiredRoomType = requiredRoomType;
        this.weeklySessions = weeklySessions;
        this.sessionDurationMinutes = sessionDurationMinutes;
    }

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

    public Integer getCredits() {
        return credits;
    }

    public void setCredits(Integer credits) {
        this.credits = credits;
    }

    public Faculty getAssignedFaculty() {
        return assignedFaculty;
    }

    public void setAssignedFaculty(Faculty assignedFaculty) {
        this.assignedFaculty = assignedFaculty;
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

    public Set<StudentGroup> getStudentGroups() {
        return studentGroups;
    }

    public void setStudentGroups(Set<StudentGroup> studentGroups) {
        this.studentGroups = studentGroups;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Course course)) return false;
        return Objects.equals(id, course.id) || Objects.equals(courseCode, course.courseCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, courseCode);
    }
}
