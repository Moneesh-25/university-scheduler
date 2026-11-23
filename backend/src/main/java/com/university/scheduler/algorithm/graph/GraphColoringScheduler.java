package com.university.scheduler.algorithm.graph;

import com.university.scheduler.algorithm.core.*;
import com.university.scheduler.entity.Classroom;
import com.university.scheduler.entity.TimeSlot;

import java.util.*;

/**
 * Graph-Coloring-Based Course Timetabling Engine.
 *
 * Concepts:
 * - Vertices: Discrete course lecture/lab sessions (Activity).
 * - Edges: Mutual exclusion conflicts (shared faculty or shared student group).
 * - Colors: Feasible (TimeSlot, Classroom) pairs.
 * - Heuristic: Welsh-Powell (order vertices by descending conflict degree).
 *
 * Time Complexity:
 * - Graph construction: O(|V|^2)
 * - Welsh-Powell sorting: O(|V| log |V|)
 * - Coloring iteration: O(|V| * |Colors|)
 */
public class GraphColoringScheduler {

    public ScheduleResult schedule(List<Activity> activities, List<TimeSlot> timeSlots,
                                   List<Classroom> classrooms, ConstraintChecker checker) {
        long startTime = System.currentTimeMillis();

        if (activities.isEmpty() || timeSlots.isEmpty() || classrooms.isEmpty()) {
            return new ScheduleResult("GRAPH_COLORING", Collections.emptyList(), activities,
                    0, 0, System.currentTimeMillis() - startTime, 0.0);
        }

        // 1. Build conflict graph and compute vertex degrees
        Map<Activity, Set<Activity>> adjacencyList = new HashMap<>();
        for (Activity a1 : activities) {
            adjacencyList.put(a1, new HashSet<>());
            for (Activity a2 : activities) {
                if (!a1.equals(a2) && a1.conflictsWith(a2)) {
                    adjacencyList.get(a1).add(a2);
                }
            }
        }

        // 2. Order vertices by Welsh-Powell heuristic: descending degree of conflicts
        List<Activity> sortedActivities = new ArrayList<>(activities);
        sortedActivities.sort((a1, a2) -> Integer.compare(
                adjacencyList.get(a2).size(),
                adjacencyList.get(a1).size()
        ));

        // 3. Generate candidate color domains (TimeRoomSlot)
        List<TimeRoomSlot> allSlots = new ArrayList<>();
        for (TimeSlot slot : timeSlots) {
            if (slot.isBreak() || slot.isLunch()) continue;
            for (Classroom room : classrooms) {
                if (room.isAvailable()) {
                    allSlots.add(new TimeRoomSlot(slot, room));
                }
            }
        }

        List<ScheduleAssignment> assignments = new ArrayList<>();
        List<Activity> unassigned = new ArrayList<>();

        // 4. Greedy Graph Coloring
        for (Activity activity : sortedActivities) {
            boolean assigned = false;

            for (TimeRoomSlot candidateSlot : allSlots) {
                if (checker.isFeasible(activity, candidateSlot, assignments)) {
                    assignments.add(new ScheduleAssignment(activity, candidateSlot));
                    assigned = true;
                    break;
                }
            }

            if (!assigned) {
                unassigned.add(activity);
            }
        }

        long executionTime = Math.max(1, System.currentTimeMillis() - startTime);
        double softScore = checker.computeSoftConstraintScore(assignments);

        return new ScheduleResult("GRAPH_COLORING", assignments, unassigned,
                0, 0, executionTime, softScore);
    }
}
