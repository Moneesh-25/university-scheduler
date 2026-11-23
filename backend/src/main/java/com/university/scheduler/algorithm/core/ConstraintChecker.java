package com.university.scheduler.algorithm.core;

import com.university.scheduler.entity.Classroom;
import com.university.scheduler.entity.DayOfWeek;
import com.university.scheduler.entity.RoomType;
import com.university.scheduler.entity.TimeSlot;

import java.util.*;

/**
 * Validates Hard Constraints and computes Soft Constraint Quality Scores.
 */
public class ConstraintChecker {

    private final Set<String> facultyUnavailableSet; // "facultyId:timeSlotId"

    public ConstraintChecker(Set<String> facultyUnavailableSet) {
        this.facultyUnavailableSet = facultyUnavailableSet != null ? facultyUnavailableSet : Collections.emptySet();
    }

    /**
     * Checks all HARD constraints for placing candidate activity into candidate slot.
     * Returns null if valid; returns a descriptive error message if invalid.
     */
    public String checkHardConstraintViolation(Activity activity, TimeRoomSlot candidateSlot,
                                               List<ScheduleAssignment> currentAssignments) {
        TimeSlot slot = candidateSlot.getTimeSlot();
        Classroom room = candidateSlot.getClassroom();

        // 1. Break / Lunch hours
        if (slot.isBreak()) {
            return "Cannot schedule during break period: " + slot.getFormattedTime();
        }
        if (slot.isLunch()) {
            return "Cannot schedule during lunch period: " + slot.getFormattedTime();
        }

        // 2. Classroom availability
        if (!room.isAvailable()) {
            return "Classroom " + room.getRoomNumber() + " is marked unavailable";
        }

        // 3. Room Type compatibility
        if (activity.getRequiredRoomType() == RoomType.LABORATORY && room.getRoomType() != RoomType.LABORATORY) {
            return "Course " + activity.getCourse().getCourseCode() + " requires a LABORATORY, but " +
                    room.getRoomNumber() + " is a " + room.getRoomType();
        }

        // 4. Classroom Capacity
        if (room.getCapacity() < activity.getStudentCount()) {
            return "Classroom " + room.getRoomNumber() + " capacity (" + room.getCapacity() +
                    ") is insufficient for student group " + activity.getStudentGroup().getGroupCode() +
                    " (" + activity.getStudentCount() + " students)";
        }

        // 5. Faculty Availability
        if (activity.getFaculty() != null) {
            String key = activity.getFaculty().getId() + ":" + slot.getId();
            if (facultyUnavailableSet.contains(key)) {
                return "Faculty " + activity.getFaculty().getName() + " is not available at slot " + slot.getFormattedTime();
            }
        }

        // 6. Overlaps with existing assignments
        Long targetSlotId = slot.getId();
        Long targetRoomId = room.getId();
        Long facultyId = activity.getFaculty() != null ? activity.getFaculty().getId() : null;
        Long groupId = activity.getStudentGroup() != null ? activity.getStudentGroup().getId() : null;

        for (ScheduleAssignment existing : currentAssignments) {
            if (Objects.equals(existing.getTimeSlot().getId(), targetSlotId)) {
                // Room conflict
                if (Objects.equals(existing.getClassroom().getId(), targetRoomId)) {
                    return "Classroom " + room.getRoomNumber() + " is already occupied by " +
                            existing.getActivity().getCourse().getCourseCode() + " at " + slot.getFormattedTime();
                }
                // Faculty conflict
                if (facultyId != null && existing.getActivity().getFaculty() != null &&
                        Objects.equals(existing.getActivity().getFaculty().getId(), facultyId)) {
                    return "Faculty " + activity.getFaculty().getName() + " is already teaching " +
                            existing.getActivity().getCourse().getCourseCode() + " at " + slot.getFormattedTime();
                }
                // Student Group conflict
                if (groupId != null && existing.getActivity().getStudentGroup() != null &&
                        Objects.equals(existing.getActivity().getStudentGroup().getId(), groupId)) {
                    return "Student Group " + activity.getStudentGroup().getGroupCode() +
                            " is already attending " + existing.getActivity().getCourse().getCourseCode() +
                            " at " + slot.getFormattedTime();
                }
            }
        }

        return null; // Valid!
    }

    public boolean isFeasible(Activity activity, TimeRoomSlot candidateSlot, List<ScheduleAssignment> currentAssignments) {
        return checkHardConstraintViolation(activity, candidateSlot, currentAssignments) == null;
    }

    /**
     * Evaluates soft constraints and returns a quality score between 0.0 and 100.0.
     * Starts at 100.0 and deducts penalties:
     * - Gap penalties for student groups (idle waiting hours between classes on the same day).
     * - Consecutive class penalties (> 2 back-to-back classes).
     * - Workload variance for faculty across different days.
     */
    public double computeSoftConstraintScore(List<ScheduleAssignment> assignments) {
        if (assignments.isEmpty()) {
            return 0.0;
        }

        double penalty = 0.0;

        // Group assignments by StudentGroup and Day
        Map<String, List<Integer>> groupDaySlotOrders = new HashMap<>();
        Map<Long, Map<DayOfWeek, Integer>> facultyDailyLoad = new HashMap<>();

        for (ScheduleAssignment a : assignments) {
            String groupDayKey = a.getActivity().getStudentGroup().getId() + ":" + a.getTimeSlot().getDayOfWeek();
            groupDaySlotOrders.computeIfAbsent(groupDayKey, k -> new ArrayList<>())
                    .add(a.getTimeSlot().getSlotOrder() != null ? a.getTimeSlot().getSlotOrder() : 0);

            if (a.getActivity().getFaculty() != null) {
                Long facId = a.getActivity().getFaculty().getId();
                facultyDailyLoad.computeIfAbsent(facId, k -> new EnumMap<>(DayOfWeek.class))
                        .merge(a.getTimeSlot().getDayOfWeek(), 1, Integer::sum);
            }
        }

        // Penalize student group gaps and excessive consecutive classes
        for (List<Integer> orders : groupDaySlotOrders.values()) {
            if (orders.size() > 1) {
                Collections.sort(orders);
                int consecutive = 1;
                for (int i = 0; i < orders.size() - 1; i++) {
                    int diff = orders.get(i + 1) - orders.get(i);
                    if (diff == 1) {
                        consecutive++;
                        if (consecutive > 2) {
                            penalty += 3.0; // penalty for >2 consecutive classes without a break
                        }
                    } else if (diff > 1) {
                        consecutive = 1;
                        penalty += (diff - 1) * 2.5; // penalty for idle gaps between classes
                    }
                }
            }
        }

        // Penalize faculty workload imbalance (standard deviation of daily load)
        for (Map<DayOfWeek, Integer> dailyMap : facultyDailyLoad.values()) {
            if (dailyMap.size() > 1) {
                double avg = dailyMap.values().stream().mapToInt(Integer::intValue).average().orElse(0.0);
                double variance = 0.0;
                for (int count : dailyMap.values()) {
                    variance += Math.pow(count - avg, 2);
                }
                penalty += Math.sqrt(variance / dailyMap.size()) * 2.0;
            }
        }

        double score = Math.max(10.0, 100.0 - penalty);
        return Math.round(score * 100.0) / 100.0;
    }
}
