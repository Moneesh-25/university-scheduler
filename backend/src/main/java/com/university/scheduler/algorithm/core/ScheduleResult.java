package com.university.scheduler.algorithm.core;

import java.util.Collections;
import java.util.List;

public class ScheduleResult {
    private final String algorithmName;
    private final List<ScheduleAssignment> assignments;
    private final List<Activity> unassignedActivities;
    private final int conflictsCount;
    private final int backtracksCount;
    private final long executionTimeMs;
    private final double softConstraintScore;

    public ScheduleResult(String algorithmName, List<ScheduleAssignment> assignments,
                          List<Activity> unassignedActivities, int conflictsCount,
                          int backtracksCount, long executionTimeMs, double softConstraintScore) {
        this.algorithmName = algorithmName;
        this.assignments = assignments != null ? assignments : Collections.emptyList();
        this.unassignedActivities = unassignedActivities != null ? unassignedActivities : Collections.emptyList();
        this.conflictsCount = conflictsCount;
        this.backtracksCount = backtracksCount;
        this.executionTimeMs = executionTimeMs;
        this.softConstraintScore = softConstraintScore;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public List<ScheduleAssignment> getAssignments() {
        return assignments;
    }

    public List<Activity> getUnassignedActivities() {
        return unassignedActivities;
    }

    public int getConflictsCount() {
        return conflictsCount;
    }

    public int getBacktracksCount() {
        return backtracksCount;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public double getSoftConstraintScore() {
        return softConstraintScore;
    }

    public int getTotalActivities() {
        return assignments.size() + unassignedActivities.size();
    }

    public int getScheduledCount() {
        return assignments.size();
    }
}
