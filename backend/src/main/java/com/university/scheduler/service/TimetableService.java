package com.university.scheduler.service;

import com.university.scheduler.algorithm.backtracking.BacktrackingScheduler;
import com.university.scheduler.algorithm.core.*;
import com.university.scheduler.algorithm.graph.GraphColoringScheduler;
import com.university.scheduler.concurrency.ConcurrentScheduleStack;
import com.university.scheduler.dto.TimetableEntryDto;
import com.university.scheduler.entity.*;
import com.university.scheduler.exception.SchedulingConflictException;
import com.university.scheduler.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class TimetableService {

    private final TimetableEntryRepository timetableEntryRepository;
    private final CourseRepository courseRepository;
    private final ClassroomRepository classroomRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final FacultyAvailabilityRepository facultyAvailabilityRepository;
    private final SchedulingJobRepository schedulingJobRepository;

    // Concurrent stack for Undo operations
    private final ConcurrentScheduleStack<String> scheduleHistoryStack = new ConcurrentScheduleStack<>();

    public TimetableService(TimetableEntryRepository timetableEntryRepository,
                            CourseRepository courseRepository,
                            ClassroomRepository classroomRepository,
                            TimeSlotRepository timeSlotRepository,
                            FacultyAvailabilityRepository facultyAvailabilityRepository,
                            SchedulingJobRepository schedulingJobRepository) {
        this.timetableEntryRepository = timetableEntryRepository;
        this.courseRepository = courseRepository;
        this.classroomRepository = classroomRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.facultyAvailabilityRepository = facultyAvailabilityRepository;
        this.schedulingJobRepository = schedulingJobRepository;
    }

    /**
     * Generates a conflict-free course timetable using either Graph Coloring or Backtracking.
     */
    public List<TimetableEntryDto> generateTimetable(String algorithmName, boolean clearExisting) {
        if (clearExisting) {
            timetableEntryRepository.deleteAll();
        }

        List<Course> courses = courseRepository.findAll();
        List<Classroom> classrooms = classroomRepository.findByAvailableTrue();
        List<TimeSlot> timeSlots = timeSlotRepository.findAllByOrderByDayOfWeekAscSlotOrderAsc();

        if (courses.isEmpty()) {
            throw new IllegalArgumentException("Cannot generate timetable: No courses available");
        }
        if (classrooms.isEmpty()) {
            throw new IllegalArgumentException("Cannot generate timetable: No available classrooms");
        }
        if (timeSlots.isEmpty()) {
            throw new IllegalArgumentException("Cannot generate timetable: No time slots configured");
        }

        // Build Discrete Activities
        List<Activity> activities = new ArrayList<>();
        for (Course course : courses) {
            for (StudentGroup group : course.getStudentGroups()) {
                int sessions = course.getWeeklySessions() != null ? course.getWeeklySessions() : 3;
                for (int i = 1; i <= sessions; i++) {
                    activities.add(new Activity(course, group, course.getAssignedFaculty(), i));
                }
            }
        }

        // Build Constraint Checker
        Set<String> unavailable = new HashSet<>();
        for (FacultyAvailability fa : facultyAvailabilityRepository.findAll()) {
            if (!fa.isAvailable()) {
                unavailable.add(fa.getFaculty().getId() + ":" + fa.getTimeSlot().getId());
            }
        }
        ConstraintChecker checker = new ConstraintChecker(unavailable);

        String batchId = "BATCH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        ScheduleResult result;

        if ("GRAPH_COLORING".equalsIgnoreCase(algorithmName)) {
            GraphColoringScheduler scheduler = new GraphColoringScheduler();
            result = scheduler.schedule(activities, timeSlots, classrooms, checker);
        } else {
            BacktrackingScheduler scheduler = new BacktrackingScheduler();
            result = scheduler.schedule(activities, timeSlots, classrooms, checker);
        }

        if (result.getAssignments().isEmpty()) {
            throw new SchedulingConflictException("Scheduling algorithm could not find any feasible assignments under current constraints.");
        }

        // Persist generated timetable entries
        List<TimetableEntry> entities = new ArrayList<>();
        for (ScheduleAssignment a : result.getAssignments()) {
            TimetableEntry entry = new TimetableEntry(
                    batchId,
                    a.getActivity().getCourse(),
                    a.getActivity().getFaculty(),
                    a.getClassroom(),
                    a.getActivity().getStudentGroup(),
                    a.getTimeSlot(),
                    result.getAlgorithmName()
            );
            entities.add(entry);
        }

        List<TimetableEntry> saved = timetableEntryRepository.saveAll(entities);

        // Record in Concurrent Stack for undo/rollback capability
        scheduleHistoryStack.push(batchId);

        // Record Scheduling Job in database
        SchedulingJob job = new SchedulingJob(batchId, "COURSE_TIMETABLE", result.getAlgorithmName());
        job.setStatus("COMPLETED");
        job.setScheduledActivities(result.getScheduledCount());
        job.setUnscheduledActivities(result.getUnassignedActivities().size());
        job.setConflictsCount(result.getConflictsCount());
        job.setBacktracksCount(result.getBacktracksCount());
        job.setExecutionTimeMs(result.getExecutionTimeMs());
        job.setSoftConstraintScore(result.getSoftConstraintScore());
        job.setCompletedAt(LocalDateTime.now());
        job.setMetricsJson(String.format("{\"total\": %d, \"scheduled\": %d, \"unscheduled\": %d, \"score\": %.2f}",
                result.getTotalActivities(), result.getScheduledCount(),
                result.getUnassignedActivities().size(), result.getSoftConstraintScore()));
        schedulingJobRepository.save(job);

        return saved.stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Undoes the most recent schedule generation using the ConcurrentStack.
     */
    public boolean undoLastSchedule() {
        if (scheduleHistoryStack.isEmpty()) {
            return false;
        }
        String lastBatchId = scheduleHistoryStack.pop();
        timetableEntryRepository.deleteByScheduleBatchId(lastBatchId);
        return true;
    }

    public List<TimetableEntryDto> getAllEntries() {
        return timetableEntryRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<TimetableEntryDto> getEntriesByStudentGroup(Long studentGroupId) {
        return timetableEntryRepository.findByStudentGroupId(studentGroupId).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    public List<TimetableEntryDto> getEntriesByFaculty(Long facultyId) {
        return timetableEntryRepository.findByFacultyId(facultyId).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    public List<TimetableEntryDto> getEntriesByClassroom(Long classroomId) {
        return timetableEntryRepository.findByClassroomId(classroomId).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    public List<TimetableEntryDto> getEntriesByDepartment(Long departmentId) {
        return timetableEntryRepository.findByDepartmentId(departmentId).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    public List<TimetableEntryDto> getEntriesBySemester(Integer semester) {
        return timetableEntryRepository.findBySemester(semester).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    public void clearAllEntries() {
        timetableEntryRepository.deleteAll();
        scheduleHistoryStack.clear();
    }

    public String exportScheduleAsCsv() {
        List<TimetableEntry> entries = timetableEntryRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("Batch ID,Day,Start Time,End Time,Course Code,Course Name,Faculty,Room,Room Type,Student Group\n");

        for (TimetableEntry e : entries) {
            sb.append(e.getScheduleBatchId()).append(",")
                    .append(e.getTimeSlot().getDayOfWeek()).append(",")
                    .append(e.getTimeSlot().getStartTime()).append(",")
                    .append(e.getTimeSlot().getEndTime()).append(",")
                    .append(e.getCourse().getCourseCode()).append(",")
                    .append("\"").append(e.getCourse().getCourseName()).append("\",")
                    .append(e.getFaculty() != null ? e.getFaculty().getName() : "Unassigned").append(",")
                    .append(e.getClassroom().getRoomNumber()).append(",")
                    .append(e.getClassroom().getRoomType()).append(",")
                    .append(e.getStudentGroup().getGroupName()).append("\n");
        }

        return sb.toString();
    }

    public TimetableEntryDto toDto(TimetableEntry entity) {
        if (entity == null) return null;
        TimetableEntryDto dto = new TimetableEntryDto();
        dto.setId(entity.getId());
        dto.setScheduleBatchId(entity.getScheduleBatchId());
        dto.setAlgorithmUsed(entity.getAlgorithmUsed());
        dto.setCreatedAt(entity.getCreatedAt());

        if (entity.getCourse() != null) {
            dto.setCourseId(entity.getCourse().getId());
            dto.setCourseCode(entity.getCourse().getCourseCode());
            dto.setCourseName(entity.getCourse().getCourseName());
            dto.setCredits(entity.getCourse().getCredits());
        }

        if (entity.getFaculty() != null) {
            dto.setFacultyId(entity.getFaculty().getId());
            dto.setFacultyCode(entity.getFaculty().getFacultyCode());
            dto.setFacultyName(entity.getFaculty().getName());
        }

        if (entity.getClassroom() != null) {
            dto.setClassroomId(entity.getClassroom().getId());
            dto.setRoomNumber(entity.getClassroom().getRoomNumber());
            dto.setBuilding(entity.getClassroom().getBuilding());
            dto.setRoomType(entity.getClassroom().getRoomType());
            dto.setClassroomCapacity(entity.getClassroom().getCapacity());
        }

        if (entity.getStudentGroup() != null) {
            dto.setStudentGroupId(entity.getStudentGroup().getId());
            dto.setGroupCode(entity.getStudentGroup().getGroupCode());
            dto.setGroupName(entity.getStudentGroup().getGroupName());
            dto.setStudentCount(entity.getStudentGroup().getStudentCount());
        }

        if (entity.getTimeSlot() != null) {
            dto.setTimeSlotId(entity.getTimeSlot().getId());
            dto.setDayOfWeek(entity.getTimeSlot().getDayOfWeek());
            dto.setStartTime(entity.getTimeSlot().getStartTime());
            dto.setEndTime(entity.getTimeSlot().getEndTime());
        }

        return dto;
    }
}
