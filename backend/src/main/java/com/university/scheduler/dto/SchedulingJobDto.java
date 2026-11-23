package com.university.scheduler.dto;

import java.time.LocalDateTime;

public class SchedulingJobDto {
    private Long id;
    private String jobId;
    private String jobType;
    private String algorithm;
    private String status;
    private Integer scheduledActivities;
    private Integer unscheduledActivities;
    private Integer conflictsCount;
    private Integer backtracksCount;
    private Long executionTimeMs;
    private Double softConstraintScore;
    private String metricsJson;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public SchedulingJobDto() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getScheduledActivities() {
        return scheduledActivities;
    }

    public void setScheduledActivities(Integer scheduledActivities) {
        this.scheduledActivities = scheduledActivities;
    }

    public Integer getUnscheduledActivities() {
        return unscheduledActivities;
    }

    public void setUnscheduledActivities(Integer unscheduledActivities) {
        this.unscheduledActivities = unscheduledActivities;
    }

    public Integer getConflictsCount() {
        return conflictsCount;
    }

    public void setConflictsCount(Integer conflictsCount) {
        this.conflictsCount = conflictsCount;
    }

    public Integer getBacktracksCount() {
        return backtracksCount;
    }

    public void setBacktracksCount(Integer backtracksCount) {
        this.backtracksCount = backtracksCount;
    }

    public Long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(Long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public Double getSoftConstraintScore() {
        return softConstraintScore;
    }

    public void setSoftConstraintScore(Double softConstraintScore) {
        this.softConstraintScore = softConstraintScore;
    }

    public String getMetricsJson() {
        return metricsJson;
    }

    public void setMetricsJson(String metricsJson) {
        this.metricsJson = metricsJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
