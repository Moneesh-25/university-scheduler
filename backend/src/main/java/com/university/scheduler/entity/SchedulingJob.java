package com.university.scheduler.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "scheduling_jobs")
public class SchedulingJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false, unique = true, length = 50)
    private String jobId;

    @Column(name = "job_type", nullable = false, length = 30)
    private String jobType;

    @Column(nullable = false, length = 30)
    private String algorithm;

    @Column(nullable = false, length = 20)
    private String status = "QUEUED";

    @Column(name = "scheduled_activities")
    private Integer scheduledActivities = 0;

    @Column(name = "unscheduled_activities")
    private Integer unscheduledActivities = 0;

    @Column(name = "conflicts_count")
    private Integer conflictsCount = 0;

    @Column(name = "backtracks_count")
    private Integer backtracksCount = 0;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs = 0L;

    @Column(name = "soft_constraint_score")
    private Double softConstraintScore = 0.0;

    @Column(name = "metrics_json", columnDefinition = "TEXT")
    private String metricsJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public SchedulingJob() {}

    public SchedulingJob(String jobId, String jobType, String algorithm) {
        this.jobId = jobId;
        this.jobType = jobType;
        this.algorithm = algorithm;
        this.status = "QUEUED";
        this.createdAt = LocalDateTime.now();
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SchedulingJob that)) return false;
        return Objects.equals(id, that.id) || Objects.equals(jobId, that.jobId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, jobId);
    }
}
