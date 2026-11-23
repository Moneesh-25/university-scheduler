package com.university.scheduler;

import com.university.scheduler.algorithm.backtracking.BacktrackingScheduler;
import com.university.scheduler.algorithm.core.*;
import com.university.scheduler.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class BacktrackingSchedulerTest {

    @Test
    @DisplayName("Backtracking should systematically find a valid schedule and handle constraints")
    void testBacktrackingSchedulerSolvesCSP() {
        Department dept = new Department("CSE", "Computer Science");
        dept.setId(1L);

        Faculty f1 = new Faculty("F1", "Dr. X", "x@u.edu", dept, "AI", 18);
        f1.setId(1L);
        Faculty f2 = new Faculty("F2", "Dr. Y", "y@u.edu", dept, "Databases", 18);
        f2.setId(2L);

        Classroom r1 = new Classroom("LH-1", "Block", 60, RoomType.CLASSROOM, true);
        r1.setId(1L);
        Classroom lab1 = new Classroom("LAB-1", "Block", 40, RoomType.LABORATORY, true);
        lab1.setId(2L);

        StudentGroup g1 = new StudentGroup("G1", "Group 1", dept, 1, 35);
        g1.setId(1L);

        Course c1 = new Course("CS1", "Theory Course", dept, 1, 3, f1, RoomType.CLASSROOM, 2, 60);
        c1.setId(1L);
        Course c2 = new Course("CS2P", "Lab Course", dept, 1, 2, f2, RoomType.LABORATORY, 1, 120);
        c2.setId(2L);

        List<Activity> activities = List.of(
                new Activity(c1, g1, f1, 1),
                new Activity(c1, g1, f1, 2),
                new Activity(c2, g1, f2, 1) // Lab course: requires lab1
        );

        List<TimeSlot> slots = List.of(
                new TimeSlot(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), 1, false, false),
                new TimeSlot(DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 0), 2, false, false),
                new TimeSlot(DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), 1, false, false)
        );
        for (long i = 0; i < slots.size(); i++) {
            slots.get((int) i).setId(i + 1);
        }

        ConstraintChecker checker = new ConstraintChecker(Collections.emptySet());
        BacktrackingScheduler scheduler = new BacktrackingScheduler();

        ScheduleResult result = scheduler.schedule(activities, slots, List.of(r1, lab1), checker);

        assertNotNull(result);
        assertEquals("BACKTRACKING", result.getAlgorithmName());
        assertEquals(3, result.getScheduledCount());
        assertEquals(0, result.getUnassignedActivities().size());

        // Verify lab course is indeed placed in laboratory
        for (ScheduleAssignment a : result.getAssignments()) {
            if (a.getActivity().getRequiredRoomType() == RoomType.LABORATORY) {
                assertEquals(RoomType.LABORATORY, a.getClassroom().getRoomType());
            }
        }
    }
}
