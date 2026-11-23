package com.university.scheduler.dto;

import java.util.Map;

public class AlgorithmBenchmarkResultDto {
    private String algorithmName;
    private long executionTimeMs;
    private int totalActivities;
    private int scheduledActivities;
    private int unscheduledActivities;
    private int conflictsCount;
    private int backtracksCount;
    private double softConstraintScore;
    private Map<String, Object> additionalMetrics;

    public AlgorithmBenchmarkResultDto() {}

    public AlgorithmBenchmarkResultDto(String algorithmName, long executionTimeMs, int totalActivities,
                                       int scheduledActivities, int unscheduledActivities, int conflictsCount,
                                       int backtracksCount, double softConstraintScore, Map<String, Object> additionalMetrics) {
        this.algorithmName = algorithmName;
        this.executionTimeMs = executionTimeMs;
        this.totalActivities = totalActivities;
        this.scheduledActivities = scheduledActivities;
        this.unscheduledActivities = unscheduledActivities;
        this.conflictsCount = conflictsCount;
        this.backtracksCount = backtracksCount;
        this.softConstraintScore = softConstraintScore;
        this.additionalMetrics = additionalMetrics;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public void setAlgorithmName(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public int getTotalActivities() {
        return totalActivities;
    }

    public void setTotalActivities(int totalActivities) {
        this.totalActivities = totalActivities;
    }

    public int getScheduledActivities() {
        return scheduledActivities;
    }

    public void setScheduledActivities(int scheduledActivities) {
        this.scheduledActivities = scheduledActivities;
    }

    public int getUnscheduledActivities() {
        return unscheduledActivities;
    }

    public void setUnscheduledActivities(int unscheduledActivities) {
        this.unscheduledActivities = unscheduledActivities;
    }

    public int getConflictsCount() {
        return conflictsCount;
    }

    public void setConflictsCount(int conflictsCount) {
        this.conflictsCount = conflictsCount;
    }

    public int getBacktracksCount() {
        return backtracksCount;
    }

    public void setBacktracksCount(int backtracksCount) {
        this.backtracksCount = backtracksCount;
    }

    public double getSoftConstraintScore() {
        return softConstraintScore;
    }

    public void setSoftConstraintScore(double softConstraintScore) {
        this.softConstraintScore = softConstraintScore;
    }

    public Map<String, Object> getAdditionalMetrics() {
        return additionalMetrics;
    }

    public void setAdditionalMetrics(Map<String, Object> additionalMetrics) {
        this.additionalMetrics = additionalMetrics;
    }
}
