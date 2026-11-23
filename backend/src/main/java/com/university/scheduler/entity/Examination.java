package com.university.scheduler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "examinations", indexes = {
    @Index(name = "idx_exam_date_slot", columnList = "exam_date, time_slot_id"),
    @Index(name = "idx_exam_invigilator", columnList = "invigilator_id"),
    @Index(name = "idx_exam_room", columnList = "classroom_id")
})
public class Examination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Course is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @NotNull(message = "Student group is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_group_id", nullable = false)
    private StudentGroup studentGroup;

    @NotNull(message = "Exam date is required")
    @Column(name = "exam_date", nullable = false)
    private LocalDate examDate;

    @NotNull(message = "Time slot is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "time_slot_id", nullable = false)
    private TimeSlot timeSlot;

    @NotNull(message = "Classroom is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    @NotNull(message = "Invigilator is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "invigilator_id", nullable = false)
    private Faculty invigilator;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes = 180;

    @Column(nullable = false)
    private Integer semester;

    @Column(nullable = false, length = 20)
    private String status = "SCHEDULED";

    public Examination() {}

    public Examination(Course course, StudentGroup studentGroup, LocalDate examDate, TimeSlot timeSlot,
                       Classroom classroom, Faculty invigilator, Integer durationMinutes, Integer semester) {
        this.course = course;
        this.studentGroup = studentGroup;
        this.examDate = examDate;
        this.timeSlot = timeSlot;
        this.classroom = classroom;
        this.invigilator = invigilator;
        this.durationMinutes = (durationMinutes != null) ? durationMinutes : 180;
        this.semester = semester;
        this.status = "SCHEDULED";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public StudentGroup getStudentGroup() {
        return studentGroup;
    }

    public void setStudentGroup(StudentGroup studentGroup) {
        this.studentGroup = studentGroup;
    }

    public LocalDate getExamDate() {
        return examDate;
    }

    public void setExamDate(LocalDate examDate) {
        this.examDate = examDate;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }

    public Classroom getClassroom() {
        return classroom;
    }

    public void setClassroom(Classroom classroom) {
        this.classroom = classroom;
    }

    public Faculty getInvigilator() {
        return invigilator;
    }

    public void setInvigilator(Faculty invigilator) {
        this.invigilator = invigilator;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Integer getSemester() {
        return semester;
    }

    public void setSemester(Integer semester) {
        this.semester = semester;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Examination that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
