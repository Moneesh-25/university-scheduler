package com.university.scheduler.algorithm.comparison;

import com.university.scheduler.algorithm.backtracking.BacktrackingScheduler;
import com.university.scheduler.algorithm.core.Activity;
import com.university.scheduler.algorithm.core.ConstraintChecker;
import com.university.scheduler.algorithm.core.ScheduleResult;
import com.university.scheduler.algorithm.graph.GraphColoringScheduler;
import com.university.scheduler.dto.AlgorithmBenchmarkResultDto;
import com.university.scheduler.entity.*;
import com.university.scheduler.repository.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AlgorithmComparisonService {

    private final CourseRepository courseRepository;
    private final ClassroomRepository classroomRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final FacultyAvailabilityRepository facultyAvailabilityRepository;

    public AlgorithmComparisonService(CourseRepository courseRepository,
                                      ClassroomRepository classroomRepository,
                                      TimeSlotRepository timeSlotRepository,
                                      FacultyAvailabilityRepository facultyAvailabilityRepository) {
        this.courseRepository = courseRepository;
        this.classroomRepository = classroomRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.facultyAvailabilityRepository = facultyAvailabilityRepository;
    }

    public List<AlgorithmBenchmarkResultDto> runBenchmark() {
        List<Course> courses = courseRepository.findAll();
        List<Classroom> classrooms = classroomRepository.findByAvailableTrue();
        List<TimeSlot> timeSlots = timeSlotRepository.findAllByOrderByDayOfWeekAscSlotOrderAsc();

        // Build activities
        List<Activity> activities = new ArrayList<>();
        for (Course course : courses) {
            for (StudentGroup group : course.getStudentGroups()) {
                int sessions = course.getWeeklySessions() != null ? course.getWeeklySessions() : 3;
                for (int i = 1; i <= sessions; i++) {
                    activities.add(new Activity(course, group, course.getAssignedFaculty(), i));
                }
            }
        }

        // Build constraint checker
        Set<String> unavailable = new HashSet<>();
        for (FacultyAvailability fa : facultyAvailabilityRepository.findAll()) {
            if (!fa.isAvailable()) {
                unavailable.add(fa.getFaculty().getId() + ":" + fa.getTimeSlot().getId());
            }
        }
        ConstraintChecker checker = new ConstraintChecker(unavailable);

        // 1. Run Graph Coloring
        GraphColoringScheduler graphScheduler = new GraphColoringScheduler();
        ScheduleResult graphResult = graphScheduler.schedule(
                new ArrayList<>(activities),
                new ArrayList<>(timeSlots),
                new ArrayList<>(classrooms),
                checker
        );

        // 2. Run Backtracking CSP with MRV, Degree, LCV
        BacktrackingScheduler backtrackingScheduler = new BacktrackingScheduler();
        ScheduleResult backtrackResult = backtrackingScheduler.schedule(
                new ArrayList<>(activities),
                new ArrayList<>(timeSlots),
                new ArrayList<>(classrooms),
                checker
        );

        Map<String, Object> graphMeta = new HashMap<>();
        graphMeta.put("algorithmType", "Greedy Welsh-Powell Graph Coloring");
        graphMeta.put("heuristic", "Largest Degree First (Vertex Degree Ordering)");
        graphMeta.put("completeness", "Incomplete (Polynomial-time approximation)");

        Map<String, Object> backtrackMeta = new HashMap<>();
        backtrackMeta.put("algorithmType", "Backtracking Constraint Satisfaction (CSP)");
        backtrackMeta.put("heuristics", "MRV (Minimum Remaining Values) + Degree Heuristic + LCV");
        backtrackMeta.put("completeness", "Systematic Exact Search with Pruning");

        List<AlgorithmBenchmarkResultDto> comparison = new ArrayList<>();
        comparison.add(new AlgorithmBenchmarkResultDto(
                "Greedy Graph Coloring",
                graphResult.getExecutionTimeMs(),
                activities.size(),
                graphResult.getScheduledCount(),
                graphResult.getUnassignedActivities().size(),
                graphResult.getConflictsCount(),
                graphResult.getBacktracksCount(),
                graphResult.getSoftConstraintScore(),
                graphMeta
        ));

        comparison.add(new AlgorithmBenchmarkResultDto(
                "Backtracking with MRV & LCV",
                backtrackResult.getExecutionTimeMs(),
                activities.size(),
                backtrackResult.getScheduledCount(),
                backtrackResult.getUnassignedActivities().size(),
                backtrackResult.getConflictsCount(),
                backtrackResult.getBacktracksCount(),
                backtrackResult.getSoftConstraintScore(),
                backtrackMeta
        ));

        return comparison;
    }
}
