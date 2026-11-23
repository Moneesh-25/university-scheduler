package com.university.scheduler.dto;

import java.util.List;

public class DashboardStatsDto {
    private long totalDepartments;
    private long totalCourses;
    private long totalFaculty;
    private long totalClassrooms;
    private long totalStudentGroups;
    private long totalScheduledClasses;
    private long totalScheduledExaminations;
    private int schedulingConflictsCount;
    private List<SchedulingJobDto> recentJobs;
    private List<ConflictReportDto> recentConflicts;

    public DashboardStatsDto() {}

    public long getTotalDepartments() {
        return totalDepartments;
    }

    public void setTotalDepartments(long totalDepartments) {
        this.totalDepartments = totalDepartments;
    }

    public long getTotalCourses() {
        return totalCourses;
    }

    public void setTotalCourses(long totalCourses) {
        this.totalCourses = totalCourses;
    }

    public long getTotalFaculty() {
        return totalFaculty;
    }

    public void setTotalFaculty(long totalFaculty) {
        this.totalFaculty = totalFaculty;
    }

    public long getTotalClassrooms() {
        return totalClassrooms;
    }

    public void setTotalClassrooms(long totalClassrooms) {
        this.totalClassrooms = totalClassrooms;
    }

    public long getTotalStudentGroups() {
        return totalStudentGroups;
    }

    public void setTotalStudentGroups(long totalStudentGroups) {
        this.totalStudentGroups = totalStudentGroups;
    }

    public long getTotalScheduledClasses() {
        return totalScheduledClasses;
    }

    public void setTotalScheduledClasses(long totalScheduledClasses) {
        this.totalScheduledClasses = totalScheduledClasses;
    }

    public long getTotalScheduledExaminations() {
        return totalScheduledExaminations;
    }

    public void setTotalScheduledExaminations(long totalScheduledExaminations) {
        this.totalScheduledExaminations = totalScheduledExaminations;
    }

    public int getSchedulingConflictsCount() {
        return schedulingConflictsCount;
    }

    public void setSchedulingConflictsCount(int schedulingConflictsCount) {
        this.schedulingConflictsCount = schedulingConflictsCount;
    }

    public List<SchedulingJobDto> getRecentJobs() {
        return recentJobs;
    }

    public void setRecentJobs(List<SchedulingJobDto> recentJobs) {
        this.recentJobs = recentJobs;
    }

    public List<ConflictReportDto> getRecentConflicts() {
        return recentConflicts;
    }

    public void setRecentConflicts(List<ConflictReportDto> recentConflicts) {
        this.recentConflicts = recentConflicts;
    }
}
