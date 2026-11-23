package com.university.scheduler.algorithm.exam;

import com.university.scheduler.entity.*;

import java.time.LocalDate;
import java.util.*;

/**
 * Examination Scheduling Engine.
 *
 * Enforces Hard Constraints:
 * 1. No student group has multiple exams simultaneously on the same date and slot.
 * 2. Room capacity must comfortably accommodate the student group.
 * 3. An invigilator cannot supervise multiple exams simultaneously.
 * 4. Configurable spacing between examinations for the same student group.
 * 5. Fair invigilator workload distribution.
 */
public class ExamSchedulerEngine {

    public static class ExamPlan {
        private final Course course;
        private final StudentGroup studentGroup;
        private final LocalDate date;
        private final TimeSlot timeSlot;
        private final Classroom classroom;
        private final Faculty invigilator;

        public ExamPlan(Course course, StudentGroup studentGroup, LocalDate date,
                        TimeSlot timeSlot, Classroom classroom, Faculty invigilator) {
            this.course = course;
            this.studentGroup = studentGroup;
            this.date = date;
            this.timeSlot = timeSlot;
            this.classroom = classroom;
            this.invigilator = invigilator;
        }

        public Course getCourse() { return course; }
        public StudentGroup getStudentGroup() { return studentGroup; }
        public LocalDate getDate() { return date; }
        public TimeSlot getTimeSlot() { return timeSlot; }
        public Classroom getClassroom() { return classroom; }
        public Faculty getInvigilator() { return invigilator; }
    }

    public List<ExamPlan> generateExamSchedule(List<Course> courses, List<Classroom> classrooms,
                                               List<Faculty> facultyList, List<TimeSlot> availableSlots,
                                               LocalDate startDate, int minDaysSpacing) {
        List<ExamPlan> plans = new ArrayList<>();
        if (courses.isEmpty() || classrooms.isEmpty() || facultyList.isEmpty() || availableSlots.isEmpty()) {
            return plans;
        }

        // Track faculty invigilation counts for workload balancing
        Map<Long, Integer> invigilatorDutyCounts = new HashMap<>();
        for (Faculty f : facultyList) {
            invigilatorDutyCounts.put(f.getId(), 0);
        }

        // Track last exam date for each student group to enforce spacing
        Map<Long, LocalDate> groupLastExamDate = new HashMap<>();

        // Group courses by student groups
        Map<StudentGroup, List<Course>> groupCoursesMap = new LinkedHashMap<>();
        for (Course course : courses) {
            for (StudentGroup group : course.getStudentGroups()) {
                groupCoursesMap.computeIfAbsent(group, k -> new ArrayList<>()).add(course);
            }
        }

        LocalDate currentDate = startDate != null ? startDate : LocalDate.now().plusDays(7);

        for (Map.Entry<StudentGroup, List<Course>> entry : groupCoursesMap.entrySet()) {
            StudentGroup group = entry.getKey();
            List<Course> groupCourses = entry.getValue();

            LocalDate examDateForGroup = groupLastExamDate.getOrDefault(group.getId(), currentDate);

            for (Course course : groupCourses) {
                boolean scheduled = false;

                // Try finding a suitable date, slot, classroom, and invigilator
                for (int dayOffset = 0; dayOffset < 30 && !scheduled; dayOffset++) {
                    LocalDate trialDate = examDateForGroup.plusDays(dayOffset);

                    // Skip weekends if needed (e.g. Sunday)
                    if (trialDate.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
                        continue;
                    }

                    for (TimeSlot slot : availableSlots) {
                        if (slot.isBreak() || slot.isLunch()) continue;

                        // Check if group is already taking an exam at trialDate & slot
                        boolean groupBusy = plans.stream().anyMatch(p ->
                                p.getStudentGroup().getId().equals(group.getId()) &&
                                p.getDate().equals(trialDate) &&
                                p.getTimeSlot().getId().equals(slot.getId())
                        );
                        if (groupBusy) continue;

                        // Find eligible classroom with sufficient capacity
                        Classroom selectedRoom = classrooms.stream()
                                .filter(r -> r.isAvailable() && r.getCapacity() >= group.getStudentCount())
                                .filter(r -> plans.stream().noneMatch(p ->
                                        p.getDate().equals(trialDate) &&
                                        p.getTimeSlot().getId().equals(slot.getId()) &&
                                        p.getClassroom().getId().equals(r.getId())))
                                .findFirst()
                                .orElse(null);

                        if (selectedRoom == null) continue;

                        // Select available invigilator with lowest duty count (workload balancing)
                        Faculty selectedInvigilator = facultyList.stream()
                                .filter(f -> plans.stream().noneMatch(p ->
                                        p.getDate().equals(trialDate) &&
                                        p.getTimeSlot().getId().equals(slot.getId()) &&
                                        p.getInvigilator().getId().equals(f.getId())))
                                .min(Comparator.comparingInt(f -> invigilatorDutyCounts.getOrDefault(f.getId(), 0)))
                                .orElse(null);

                        if (selectedInvigilator == null) continue;

                        // Schedule this exam
                        plans.add(new ExamPlan(course, group, trialDate, slot, selectedRoom, selectedInvigilator));
                        invigilatorDutyCounts.merge(selectedInvigilator.getId(), 1, Integer::sum);
                        examDateForGroup = trialDate.plusDays(Math.max(1, minDaysSpacing));
                        groupLastExamDate.put(group.getId(), examDateForGroup);
                        scheduled = true;
                        break;
                    }
                }
            }
        }

        return plans;
    }
}
