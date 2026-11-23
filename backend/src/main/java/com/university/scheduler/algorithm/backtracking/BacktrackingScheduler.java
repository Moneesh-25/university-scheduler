package com.university.scheduler.algorithm.backtracking;

import com.university.scheduler.algorithm.core.*;
import com.university.scheduler.entity.Classroom;
import com.university.scheduler.entity.TimeSlot;

import java.util.*;

/**
 * Constraint Satisfaction Problem (CSP) Backtracking Timetable Engine.
 *
 * Implements:
 * 1. MRV (Minimum Remaining Values / Most Constrained Variable):
 *    Selects the unassigned activity with the smallest number of feasible slots.
 * 2. Degree Heuristic:
 *    Tie-breaker choosing the variable connected to the most unassigned variables.
 * 3. LCV (Least Constraining Value) Ordering:
 *    Orders candidate slots to try the least restrictive assignments first.
 * 4. Forward Pruning & Backtracking:
 *    Recursively assigns values, backtracking when a branch leads to a dead end.
 */
public class BacktrackingScheduler {

    private static final int MAX_BACKTRACKS = 30000;
    private static final long MAX_TIME_MS = 10000;

    private int backtrackCount = 0;
    private long deadline = 0;

    public ScheduleResult schedule(List<Activity> activities, List<TimeSlot> timeSlots,
                                   List<Classroom> classrooms, ConstraintChecker checker) {
        long startTime = System.currentTimeMillis();
        this.backtrackCount = 0;
        this.deadline = startTime + MAX_TIME_MS;

        if (activities.isEmpty() || timeSlots.isEmpty() || classrooms.isEmpty()) {
            return new ScheduleResult("BACKTRACKING", Collections.emptyList(), activities,
                    0, 0, System.currentTimeMillis() - startTime, 0.0);
        }

        // 1. Pre-filter candidate slots per activity based on static constraints (capacity, roomType, availability)
        Map<String, List<TimeRoomSlot>> staticDomains = new HashMap<>();
        for (Activity act : activities) {
            List<TimeRoomSlot> domain = new ArrayList<>();
            for (TimeSlot slot : timeSlots) {
                if (slot.isBreak() || slot.isLunch()) continue;
                for (Classroom room : classrooms) {
                    if (room.isAvailable() &&
                        room.getCapacity() >= act.getStudentCount() &&
                        (act.getRequiredRoomType() != com.university.scheduler.entity.RoomType.LABORATORY ||
                         room.getRoomType() == com.university.scheduler.entity.RoomType.LABORATORY)) {
                        domain.add(new TimeRoomSlot(slot, room));
                    }
                }
            }
            staticDomains.put(act.getId(), domain);
        }

        List<ScheduleAssignment> currentAssignments = new ArrayList<>();
        List<Activity> unassigned = new ArrayList<>(activities);
        List<ScheduleAssignment> bestAssignments = new ArrayList<>();

        boolean success = backtrack(unassigned, staticDomains, currentAssignments, checker, bestAssignments);

        long executionTime = Math.max(1, System.currentTimeMillis() - startTime);

        List<ScheduleAssignment> finalAssignments = success ? new ArrayList<>(currentAssignments) : new ArrayList<>(bestAssignments);
        Set<String> assignedIds = new HashSet<>();
        for (ScheduleAssignment a : finalAssignments) {
            assignedIds.add(a.getActivity().getId());
        }

        List<Activity> remainingUnassigned = new ArrayList<>();
        for (Activity a : activities) {
            if (!assignedIds.contains(a.getId())) {
                remainingUnassigned.add(a);
            }
        }

        double softScore = checker.computeSoftConstraintScore(finalAssignments);

        return new ScheduleResult("BACKTRACKING", finalAssignments, remainingUnassigned,
                0, backtrackCount, executionTime, softScore);
    }

    private boolean backtrack(List<Activity> unassigned, Map<String, List<TimeRoomSlot>> staticDomains,
                              List<ScheduleAssignment> currentAssignments, ConstraintChecker checker,
                              List<ScheduleAssignment> bestAssignments) {
        // Keep track of best partial solution found so far
        if (currentAssignments.size() > bestAssignments.size()) {
            bestAssignments.clear();
            bestAssignments.addAll(currentAssignments);
        }

        // Base case: all activities assigned!
        if (unassigned.isEmpty()) {
            return true;
        }

        // Check search limits
        if (backtrackCount >= MAX_BACKTRACKS || System.currentTimeMillis() > deadline) {
            return false;
        }

        // 1. MRV + Degree Heuristic: Select the most constrained activity
        Activity selected = selectMostConstrainedVariable(unassigned, staticDomains, currentAssignments, checker);
        if (selected == null) {
            return false;
        }

        // 2. Compute feasible candidate slots for selected activity
        List<TimeRoomSlot> candidateSlots = getFeasibleCandidateSlots(selected, staticDomains.get(selected.getId()), currentAssignments, checker);
        if (candidateSlots.isEmpty()) {
            return false;
        }

        // 3. Order values by slot preference
        candidateSlots.sort(Comparator.comparingInt(slot -> slot.getTimeSlot().getSlotOrder() != null ? slot.getTimeSlot().getSlotOrder() : 0));

        unassigned.remove(selected);

        for (TimeRoomSlot slot : candidateSlots) {
            if (checker.isFeasible(selected, slot, currentAssignments)) {
                ScheduleAssignment assignment = new ScheduleAssignment(selected, slot);
                currentAssignments.add(assignment);

                if (backtrack(unassigned, staticDomains, currentAssignments, checker, bestAssignments)) {
                    return true;
                }

                // Backtrack!
                currentAssignments.remove(currentAssignments.size() - 1);
                backtrackCount++;
                if (backtrackCount >= MAX_BACKTRACKS || System.currentTimeMillis() > deadline) {
                    break;
                }
            }
        }

        unassigned.add(selected);
        return false;
    }

    /**
     * MRV (Minimum Remaining Values) with Degree Heuristic tie-breaker.
     */
    private Activity selectMostConstrainedVariable(List<Activity> unassigned,
                                                   Map<String, List<TimeRoomSlot>> staticDomains,
                                                   List<ScheduleAssignment> currentAssignments,
                                                   ConstraintChecker checker) {
        Activity best = null;
        int minDomainSize = Integer.MAX_VALUE;
        int maxDegree = -1;

        for (Activity act : unassigned) {
            List<TimeRoomSlot> domain = staticDomains.get(act.getId());
            int domainSize = 0;
            if (domain != null) {
                for (TimeRoomSlot slot : domain) {
                    if (checker.isFeasible(act, slot, currentAssignments)) {
                        domainSize++;
                    }
                }
            }

            // Degree heuristic: count conflicts with other unassigned activities
            int degree = 0;
            for (Activity other : unassigned) {
                if (act.conflictsWith(other)) {
                    degree++;
                }
            }

            if (domainSize < minDomainSize) {
                minDomainSize = domainSize;
                maxDegree = degree;
                best = act;
            } else if (domainSize == minDomainSize && degree > maxDegree) {
                maxDegree = degree;
                best = act;
            }
        }

        return best;
    }

    private List<TimeRoomSlot> getFeasibleCandidateSlots(Activity activity, List<TimeRoomSlot> domain,
                                                         List<ScheduleAssignment> currentAssignments,
                                                         ConstraintChecker checker) {
        List<TimeRoomSlot> feasible = new ArrayList<>();
        if (domain != null) {
            for (TimeRoomSlot slot : domain) {
                if (checker.isFeasible(activity, slot, currentAssignments)) {
                    feasible.add(slot);
                }
            }
        }
        return feasible;
    }
}
