package com.university.scheduler.service;

import com.university.scheduler.dto.ConflictReportDto;
import com.university.scheduler.dto.DashboardStatsDto;
import com.university.scheduler.dto.SchedulingJobDto;
import com.university.scheduler.entity.SchedulingJob;
import com.university.scheduler.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;
    private final FacultyRepository facultyRepository;
    private final ClassroomRepository classroomRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final TimetableEntryRepository timetableEntryRepository;
    private final ExaminationRepository examinationRepository;
    private final SchedulingJobRepository schedulingJobRepository;
    private final ConflictDetectionService conflictDetectionService;

    public DashboardService(DepartmentRepository departmentRepository,
                            CourseRepository courseRepository,
                            FacultyRepository facultyRepository,
                            ClassroomRepository classroomRepository,
                            StudentGroupRepository studentGroupRepository,
                            TimetableEntryRepository timetableEntryRepository,
                            ExaminationRepository examinationRepository,
                            SchedulingJobRepository schedulingJobRepository,
                            ConflictDetectionService conflictDetectionService) {
        this.departmentRepository = departmentRepository;
        this.courseRepository = courseRepository;
        this.facultyRepository = facultyRepository;
        this.classroomRepository = classroomRepository;
        this.studentGroupRepository = studentGroupRepository;
        this.timetableEntryRepository = timetableEntryRepository;
        this.examinationRepository = examinationRepository;
        this.schedulingJobRepository = schedulingJobRepository;
        this.conflictDetectionService = conflictDetectionService;
    }

    public DashboardStatsDto getDashboardStats() {
        DashboardStatsDto stats = new DashboardStatsDto();
        stats.setTotalDepartments(departmentRepository.count());
        stats.setTotalCourses(courseRepository.count());
        stats.setTotalFaculty(facultyRepository.count());
        stats.setTotalClassrooms(classroomRepository.count());
        stats.setTotalStudentGroups(studentGroupRepository.count());
        stats.setTotalScheduledClasses(timetableEntryRepository.count());
        stats.setTotalScheduledExaminations(examinationRepository.count());

        List<ConflictReportDto> conflicts = conflictDetectionService.scanConflicts();
        stats.setSchedulingConflictsCount(conflicts.size());
        stats.setRecentConflicts(conflicts.stream().limit(5).collect(Collectors.toList()));

        List<SchedulingJob> recentJobs = schedulingJobRepository.findTop10ByOrderByCreatedAtDesc();
        stats.setRecentJobs(recentJobs.stream().map(this::toJobDto).collect(Collectors.toList()));

        return stats;
    }

    private SchedulingJobDto toJobDto(SchedulingJob entity) {
        if (entity == null) return null;
        SchedulingJobDto dto = new SchedulingJobDto();
        dto.setId(entity.getId());
        dto.setJobId(entity.getJobId());
        dto.setJobType(entity.getJobType());
        dto.setAlgorithm(entity.getAlgorithm());
        dto.setStatus(entity.getStatus());
        dto.setScheduledActivities(entity.getScheduledActivities());
        dto.setUnscheduledActivities(entity.getUnscheduledActivities());
        dto.setConflictsCount(entity.getConflictsCount());
        dto.setBacktracksCount(entity.getBacktracksCount());
        dto.setExecutionTimeMs(entity.getExecutionTimeMs());
        dto.setSoftConstraintScore(entity.getSoftConstraintScore());
        dto.setMetricsJson(entity.getMetricsJson());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setCompletedAt(entity.getCompletedAt());
        return dto;
    }
}
