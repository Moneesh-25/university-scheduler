package com.university.scheduler;

import com.university.scheduler.algorithm.core.*;
import com.university.scheduler.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ConstraintCheckerTest {

    private Department dept;
    private Faculty fac1;
    private Faculty fac2;
    private Classroom room1;
    private Classroom lab1;
    private StudentGroup group1;
    private StudentGroup group2;
    private Course theoryCourse;
    private Course labCourse;
    private TimeSlot slotMon1;
    private TimeSlot breakSlot;

    @BeforeEach
    void setUp() {
        dept = new Department("IT", "Information Technology");
        dept.setId(1L);

        fac1 = new Faculty("FAC-001", "Dr. Raman", "raman@univ.edu", dept, "Algorithms", 16);
        fac1.setId(10L);

        fac2 = new Faculty("FAC-002", "Dr. Priya", "priya@univ.edu", dept, "Systems", 16);
        fac2.setId(20L);

        room1 = new Classroom("LH-101", "Main Block", 50, RoomType.CLASSROOM, true);
        room1.setId(100L);

        lab1 = new Classroom("LAB-201", "Lab Block", 40, RoomType.LABORATORY, true);
        lab1.setId(200L);

        group1 = new StudentGroup("MTECH-1", "M.Tech Y1", dept, 1, 30);
        group1.setId(300L);

        group2 = new StudentGroup("BTECH-3", "B.Tech Y3", dept, 5, 45);
        group2.setId(400L);

        theoryCourse = new Course("IT701", "Algorithms", dept, 1, 4, fac1, RoomType.CLASSROOM, 3, 60);
        theoryCourse.setId(500L);

        labCourse = new Course("IT702P", "Algorithms Lab", dept, 1, 2, fac1, RoomType.LABORATORY, 2, 120);
        labCourse.setId(600L);

        slotMon1 = new TimeSlot(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), 1, false, false);
        slotMon1.setId(1000L);

        breakSlot = new TimeSlot(DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(10, 20), 2, true, false);
        breakSlot.setId(2000L);
    }

    @Test
    @DisplayName("Should reject scheduling during a break period")
    void testBreakSlotViolation() {
        ConstraintChecker checker = new ConstraintChecker(Collections.emptySet());
        Activity act = new Activity(theoryCourse, group1, fac1, 1);
        TimeRoomSlot slot = new TimeRoomSlot(breakSlot, room1);

        String violation = checker.checkHardConstraintViolation(act, slot, Collections.emptyList());
        assertNotNull(violation);
        assertTrue(violation.contains("break period"));
    }

    @Test
    @DisplayName("Should reject when classroom capacity is smaller than student group size")
    void testInsufficientCapacityViolation() {
        ConstraintChecker checker = new ConstraintChecker(Collections.emptySet());
        Classroom smallRoom = new Classroom("SR-1", "Block", 20, RoomType.CLASSROOM, true);
        smallRoom.setId(999L);

        Activity act = new Activity(theoryCourse, group1, fac1, 1); // group1 has 30 students, smallRoom has 20
        TimeRoomSlot slot = new TimeRoomSlot(slotMon1, smallRoom);

        String violation = checker.checkHardConstraintViolation(act, slot, Collections.emptyList());
        assertNotNull(violation);
        assertTrue(violation.contains("insufficient"));
    }

    @Test
    @DisplayName("Should reject when a lab course is assigned to a standard lecture classroom")
    void testLaboratoryRequirementViolation() {
        ConstraintChecker checker = new ConstraintChecker(Collections.emptySet());
        Activity act = new Activity(labCourse, group1, fac1, 1); // lab course
        TimeRoomSlot slot = new TimeRoomSlot(slotMon1, room1); // CLASSROOM, not LABORATORY

        String violation = checker.checkHardConstraintViolation(act, slot, Collections.emptyList());
        assertNotNull(violation);
        assertTrue(violation.contains("LABORATORY"));
    }

    @Test
    @DisplayName("Should reject when faculty is unavailable at the designated slot")
    void testFacultyUnavailableViolation() {
        Set<String> unavailable = Set.of(fac1.getId() + ":" + slotMon1.getId());
        ConstraintChecker checker = new ConstraintChecker(unavailable);

        Activity act = new Activity(theoryCourse, group1, fac1, 1);
        TimeRoomSlot slot = new TimeRoomSlot(slotMon1, room1);

        String violation = checker.checkHardConstraintViolation(act, slot, Collections.emptyList());
        assertNotNull(violation);
        assertTrue(violation.contains("not available"));
    }

    @Test
    @DisplayName("Should detect faculty double-booking conflict at same time slot")
    void testFacultyDoubleBookingViolation() {
        ConstraintChecker checker = new ConstraintChecker(Collections.emptySet());

        Activity act1 = new Activity(theoryCourse, group1, fac1, 1);
        TimeRoomSlot slot1 = new TimeRoomSlot(slotMon1, room1);
        ScheduleAssignment existing = new ScheduleAssignment(act1, slot1);

        // Course 2 has same faculty fac1, student group that fits lab capacity (30 <= 40), different room lab1, same time slot
        Course course2 = new Course("IT801", "Cloud", dept, 1, 3, fac1, RoomType.LABORATORY, 2, 60);
        course2.setId(700L);
        StudentGroup group3 = new StudentGroup("MTECH-2", "M.Tech Y2", dept, 2, 25);
        group3.setId(450L);
        Activity act2 = new Activity(course2, group3, fac1, 1);
        TimeRoomSlot slot2 = new TimeRoomSlot(slotMon1, lab1);

        String violation = checker.checkHardConstraintViolation(act2, slot2, List.of(existing));
        assertNotNull(violation);
        assertTrue(violation.contains("already teaching"), "Expected 'already teaching' violation but was: " + violation);
    }

    @Test
    @DisplayName("Should detect classroom double-booking conflict at same time slot")
    void testClassroomDoubleBookingViolation() {
        ConstraintChecker checker = new ConstraintChecker(Collections.emptySet());

        Activity act1 = new Activity(theoryCourse, group1, fac1, 1);
        TimeRoomSlot slot1 = new TimeRoomSlot(slotMon1, room1);
        ScheduleAssignment existing = new ScheduleAssignment(act1, slot1);

        // Different course, different faculty fac2, same classroom room1, same time slot
        Course course2 = new Course("IT802", "Security", dept, 1, 3, fac2, RoomType.CLASSROOM, 2, 60);
        course2.setId(800L);
        Activity act2 = new Activity(course2, group2, fac2, 1);
        TimeRoomSlot slot2 = new TimeRoomSlot(slotMon1, room1);

        String violation = checker.checkHardConstraintViolation(act2, slot2, List.of(existing));
        assertNotNull(violation);
        assertTrue(violation.contains("already occupied"));
    }

    @Test
    @DisplayName("Should detect student group double-booking conflict at same time slot")
    void testStudentGroupDoubleBookingViolation() {
        ConstraintChecker checker = new ConstraintChecker(Collections.emptySet());

        Activity act1 = new Activity(theoryCourse, group1, fac1, 1);
        TimeRoomSlot slot1 = new TimeRoomSlot(slotMon1, room1);
        ScheduleAssignment existing = new ScheduleAssignment(act1, slot1);

        // Different faculty fac2, different room lab1, SAME student group group1, same time slot
        Course course2 = new Course("IT802", "Security", dept, 1, 3, fac2, RoomType.LABORATORY, 2, 60);
        course2.setId(800L);
        Activity act2 = new Activity(course2, group1, fac2, 1);
        TimeRoomSlot slot2 = new TimeRoomSlot(slotMon1, lab1);

        String violation = checker.checkHardConstraintViolation(act2, slot2, List.of(existing));
        assertNotNull(violation);
        assertTrue(violation.contains("already attending"));
    }

    @Test
    @DisplayName("Should accept valid assignment when all constraints are satisfied")
    void testValidAssignment() {
        ConstraintChecker checker = new ConstraintChecker(Collections.emptySet());
        Activity act = new Activity(theoryCourse, group1, fac1, 1);
        TimeRoomSlot slot = new TimeRoomSlot(slotMon1, room1);

        String violation = checker.checkHardConstraintViolation(act, slot, Collections.emptyList());
        assertNull(violation);
        assertTrue(checker.isFeasible(act, slot, Collections.emptyList()));
    }
}
