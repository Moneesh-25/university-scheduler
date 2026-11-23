package com.university.scheduler;

import com.university.scheduler.algorithm.core.*;
import com.university.scheduler.algorithm.graph.GraphColoringScheduler;
import com.university.scheduler.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class GraphColoringSchedulerTest {

    @Test
    @DisplayName("Graph Coloring should generate valid assignments with zero hard conflicts")
    void testGraphColoringGeneratesConflictFreeSchedule() {
        Department dept = new Department("IT", "Information Technology");
        dept.setId(1L);

        Faculty f1 = new Faculty("F1", "Dr. A", "a@u.edu", dept, "AI", 18);
        f1.setId(1L);
        Faculty f2 = new Faculty("F2", "Dr. B", "b@u.edu", dept, "Networks", 18);
        f2.setId(2L);

        Classroom r1 = new Classroom("R1", "Block", 50, RoomType.CLASSROOM, true);
        r1.setId(1L);
        Classroom r2 = new Classroom("R2", "Block", 50, RoomType.CLASSROOM, true);
        r2.setId(2L);

        StudentGroup g1 = new StudentGroup("G1", "Group 1", dept, 1, 30);
        g1.setId(1L);
        StudentGroup g2 = new StudentGroup("G2", "Group 2", dept, 1, 30);
        g2.setId(2L);

        Course c1 = new Course("C1", "Course 1", dept, 1, 3, f1, RoomType.CLASSROOM, 2, 60);
        c1.setId(1L);
        Course c2 = new Course("C2", "Course 2", dept, 1, 3, f2, RoomType.CLASSROOM, 2, 60);
        c2.setId(2L);

        List<Activity> activities = List.of(
                new Activity(c1, g1, f1, 1),
                new Activity(c1, g1, f1, 2),
                new Activity(c2, g2, f2, 1),
                new Activity(c2, g2, f2, 2)
        );

        List<TimeSlot> slots = List.of(
                new TimeSlot(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), 1, false, false),
                new TimeSlot(DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 0), 2, false, false),
                new TimeSlot(DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), 1, false, false),
                new TimeSlot(DayOfWeek.TUESDAY, LocalTime.of(10, 0), LocalTime.of(11, 0), 2, false, false)
        );
        for (long i = 0; i < slots.size(); i++) {
            slots.get((int) i).setId(i + 1);
        }

        ConstraintChecker checker = new ConstraintChecker(Collections.emptySet());
        GraphColoringScheduler scheduler = new GraphColoringScheduler();

        ScheduleResult result = scheduler.schedule(activities, slots, List.of(r1, r2), checker);

        assertNotNull(result);
        assertEquals("GRAPH_COLORING", result.getAlgorithmName());
        assertEquals(4, result.getScheduledCount());
        assertEquals(0, result.getUnassignedActivities().size());
        assertTrue(result.getSoftConstraintScore() > 0.0);

        // Verify pairwise validity
        List<ScheduleAssignment> assignments = result.getAssignments();
        for (int i = 0; i < assignments.size(); i++) {
            ScheduleAssignment a1 = assignments.get(i);
            for (int j = i + 1; j < assignments.size(); j++) {
                ScheduleAssignment a2 = assignments.get(j);
                if (a1.getTimeSlot().getId().equals(a2.getTimeSlot().getId())) {
                    assertNotEquals(a1.getClassroom().getId(), a2.getClassroom().getId(), "Room conflict detected!");
                    assertNotEquals(a1.getActivity().getFaculty().getId(), a2.getActivity().getFaculty().getId(), "Faculty conflict detected!");
                    assertNotEquals(a1.getActivity().getStudentGroup().getId(), a2.getActivity().getStudentGroup().getId(), "Group conflict detected!");
                }
            }
        }
    }
}
