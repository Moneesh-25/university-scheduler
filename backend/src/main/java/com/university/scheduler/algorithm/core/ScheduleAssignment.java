package com.university.scheduler.algorithm.core;

import com.university.scheduler.entity.Classroom;
import com.university.scheduler.entity.TimeSlot;

public class ScheduleAssignment {
    private final Activity activity;
    private final TimeRoomSlot slot;

    public ScheduleAssignment(Activity activity, TimeRoomSlot slot) {
        this.activity = activity;
        this.slot = slot;
    }

    public Activity getActivity() {
        return activity;
    }

    public TimeRoomSlot getSlot() {
        return slot;
    }

    public TimeSlot getTimeSlot() {
        return slot.getTimeSlot();
    }

    public Classroom getClassroom() {
        return slot.getClassroom();
    }
}
