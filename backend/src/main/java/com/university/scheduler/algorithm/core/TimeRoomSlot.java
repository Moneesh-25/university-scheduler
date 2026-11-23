package com.university.scheduler.algorithm.core;

import com.university.scheduler.entity.Classroom;
import com.university.scheduler.entity.TimeSlot;

import java.util.Objects;

/**
 * Represents a possible assignment value for an Activity:
 * a specific TimeSlot in a specific Classroom.
 */
public class TimeRoomSlot {
    private final TimeSlot timeSlot;
    private final Classroom classroom;

    public TimeRoomSlot(TimeSlot timeSlot, Classroom classroom) {
        this.timeSlot = timeSlot;
        this.classroom = classroom;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public Classroom getClassroom() {
        return classroom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeRoomSlot that)) return false;
        return Objects.equals(timeSlot.getId(), that.timeSlot.getId()) &&
                Objects.equals(classroom.getId(), that.classroom.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeSlot.getId(), classroom.getId());
    }

    @Override
    public String toString() {
        return timeSlot.getDayOfWeek() + " " + timeSlot.getStartTime() + " @ " + classroom.getRoomNumber();
    }
}
