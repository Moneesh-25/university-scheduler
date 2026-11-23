package com.university.scheduler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "timetable_entries", indexes = {
    @Index(name = "idx_batch_id", columnList = "schedule_batch_id"),
    @Index(name = "idx_slot_faculty", columnList = "time_slot_id, faculty_id"),
    @Index(name = "idx_slot_room", columnList = "time_slot_id, classroom_id"),
    @Index(name = "idx_slot_group", columnList = "time_slot_id, student_group_id")
})
public class TimetableEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "schedule_batch_id", nullable = false, length = 50)
    private String scheduleBatchId;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_group_id", nullable = false)
    private StudentGroup studentGroup;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "time_slot_id", nullable = false)
    private TimeSlot timeSlot;

    @Column(name = "algorithm_used", length = 30)
    private String algorithmUsed = "BACKTRACKING";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public TimetableEntry() {}

    public TimetableEntry(String scheduleBatchId, Course course, Faculty faculty, Classroom classroom,
                          StudentGroup studentGroup, TimeSlot timeSlot, String algorithmUsed) {
        this.scheduleBatchId = scheduleBatchId;
        this.course = course;
        this.faculty = faculty;
        this.classroom = classroom;
        this.studentGroup = studentGroup;
        this.timeSlot = timeSlot;
        this.algorithmUsed = algorithmUsed;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getScheduleBatchId() {
        return scheduleBatchId;
    }

    public void setScheduleBatchId(String scheduleBatchId) {
        this.scheduleBatchId = scheduleBatchId;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Faculty getFaculty() {
        return faculty;
    }

    public void setFaculty(Faculty faculty) {
        this.faculty = faculty;
    }

    public Classroom getClassroom() {
        return classroom;
    }

    public void setClassroom(Classroom classroom) {
        this.classroom = classroom;
    }

    public StudentGroup getStudentGroup() {
        return studentGroup;
    }

    public void setStudentGroup(StudentGroup studentGroup) {
        this.studentGroup = studentGroup;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }

    public String getAlgorithmUsed() {
        return algorithmUsed;
    }

    public void setAlgorithmUsed(String algorithmUsed) {
        this.algorithmUsed = algorithmUsed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimetableEntry that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
