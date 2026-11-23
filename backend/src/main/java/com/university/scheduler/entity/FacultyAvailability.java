package com.university.scheduler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

@Entity
@Table(name = "faculty_availabilities", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"faculty_id", "time_slot_id"})
})
public class FacultyAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "time_slot_id", nullable = false)
    private TimeSlot timeSlot;

    @Column(name = "is_available", nullable = false)
    private boolean available = true;

    public FacultyAvailability() {}

    public FacultyAvailability(Faculty faculty, TimeSlot timeSlot, boolean available) {
        this.faculty = faculty;
        this.timeSlot = timeSlot;
        this.available = available;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Faculty getFaculty() {
        return faculty;
    }

    public void setFaculty(Faculty faculty) {
        this.faculty = faculty;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FacultyAvailability that)) return false;
        return Objects.equals(id, that.id) ||
                (Objects.equals(faculty, that.faculty) && Objects.equals(timeSlot, that.timeSlot));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, faculty, timeSlot);
    }
}
