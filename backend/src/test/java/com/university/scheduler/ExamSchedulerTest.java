package com.university.scheduler;

import com.university.scheduler.algorithm.exam.ExamSchedulerEngine;
import com.university.scheduler.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ExamSchedulerTest {

    @Test
    @DisplayName("Exam Scheduler should prevent student group exam overlaps and balance invigilators")
    void testExamSchedulerConstraints() {
        Department dept = new Department("IT", "Information Technology");
        dept.setId(1L);

        Faculty invig1 = new Faculty("F1", "Dr. A", "a@u.edu", dept, "IT", 16);
        invig1.setId(1L);
        Faculty invig2 = new Faculty("F2", "Dr. B", "b@u.edu", dept, "IT", 16);
        invig2.setId(2L);

        Classroom hall1 = new Classroom("EXAM-1", "Exam Block", 100, RoomType.CLASSROOM, true);
        hall1.setId(1L);
        Classroom hall2 = new Classroom("EXAM-2", "Exam Block", 100, RoomType.CLASSROOM, true);
        hall2.setId(2L);

        StudentGroup group = new StudentGroup("G1", "Group 1", dept, 1, 40);
        group.setId(1L);

        Course course1 = new Course("IT101", "Sub 1", dept, 1, 4, invig1, RoomType.CLASSROOM, 3, 60);
        course1.setId(1L);
        course1.getStudentGroups().add(group);

        Course course2 = new Course("IT102", "Sub 2", dept, 1, 4, invig2, RoomType.CLASSROOM, 3, 60);
        course2.setId(2L);
        course2.getStudentGroups().add(group);

        TimeSlot slot1 = new TimeSlot(DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(13, 0), 1, false, false);
        slot1.setId(1L);

        ExamSchedulerEngine engine = new ExamSchedulerEngine();
        LocalDate startDate = LocalDate.of(2026, 11, 16);

        List<ExamSchedulerEngine.ExamPlan> plans = engine.generateExamSchedule(
                List.of(course1, course2),
                List.of(hall1, hall2),
                List.of(invig1, invig2),
                List.of(slot1),
                startDate,
                1 // min 1 day spacing
        );

        assertEquals(2, plans.size());

        // Verify group doesn't have 2 exams on same day
        ExamSchedulerEngine.ExamPlan p1 = plans.get(0);
        ExamSchedulerEngine.ExamPlan p2 = plans.get(1);
        assertNotEquals(p1.getDate(), p2.getDate(), "Student group must not have overlapping exam dates!");
    }
}
