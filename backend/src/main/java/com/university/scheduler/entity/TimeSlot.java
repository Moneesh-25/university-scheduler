package com.university.scheduler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.Objects;

@Entity
@Table(name = "time_slots", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"day_of_week", "start_time", "end_time"})
})
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Day of week is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 15)
    private DayOfWeek dayOfWeek;

    @NotNull(message = "Start time is required")
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "slot_order")
    private Integer slotOrder;

    @Column(name = "is_break", nullable = false)
    private boolean isBreak = false;

    @Column(name = "is_lunch", nullable = false)
    private boolean isLunch = false;

    public TimeSlot() {}

    public TimeSlot(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime, Integer slotOrder, boolean isBreak, boolean isLunch) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.slotOrder = slotOrder;
        this.isBreak = isBreak;
        this.isLunch = isLunch;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Integer getSlotOrder() {
        return slotOrder;
    }

    public void setSlotOrder(Integer slotOrder) {
        this.slotOrder = slotOrder;
    }

    public boolean isBreak() {
        return isBreak;
    }

    public void setBreak(boolean aBreak) {
        isBreak = aBreak;
    }

    public boolean isLunch() {
        return isLunch;
    }

    public void setLunch(boolean lunch) {
        isLunch = lunch;
    }

    public String getFormattedTime() {
        return startTime + " - " + endTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeSlot timeSlot)) return false;
        return Objects.equals(id, timeSlot.id) ||
                (dayOfWeek == timeSlot.dayOfWeek &&
                 Objects.equals(startTime, timeSlot.startTime) &&
                 Objects.equals(endTime, timeSlot.endTime));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dayOfWeek, startTime, endTime);
    }
}
