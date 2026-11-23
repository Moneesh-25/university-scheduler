package com.university.scheduler.service;

import com.university.scheduler.dto.ConflictReportDto;
import com.university.scheduler.entity.*;
import com.university.scheduler.repository.FacultyAvailabilityRepository;
import com.university.scheduler.repository.TimetableEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class ConflictDetectionService {

    private final TimetableEntryRepository timetableEntryRepository;
    private final FacultyAvailabilityRepository facultyAvailabilityRepository;

    public ConflictDetectionService(TimetableEntryRepository timetableEntryRepository,
                                    FacultyAvailabilityRepository facultyAvailabilityRepository) {
        this.timetableEntryRepository = timetableEntryRepository;
        this.facultyAvailabilityRepository = facultyAvailabilityRepository;
    }

    public List<ConflictReportDto> scanConflicts() {
        List<TimetableEntry> entries = timetableEntryRepository.findAll();
        List<ConflictReportDto> reports = new ArrayList<>();

        if (entries.isEmpty()) {
            return reports;
        }

        // Cache faculty unavailable slots
        Set<String> unavailableSlots = new HashSet<>();
        for (FacultyAvailability fa : facultyAvailabilityRepository.findAll()) {
            if (!fa.isAvailable()) {
                unavailableSlots.add(fa.getFaculty().getId() + ":" + fa.getTimeSlot().getId());
            }
        }

        // 1. Check pairwise Hard Conflicts
        for (int i = 0; i < entries.size(); i++) {
            TimetableEntry e1 = entries.get(i);
            TimeSlot s1 = e1.getTimeSlot();
            Classroom r1 = e1.getClassroom();
            Faculty f1 = e1.getFaculty();
            StudentGroup g1 = e1.getStudentGroup();
            Course c1 = e1.getCourse();

            // Room Type constraint
            if (c1.getRequiredRoomType() == RoomType.LABORATORY && r1.getRoomType() != RoomType.LABORATORY) {
                reports.add(new ConflictReportDto(
                        "ROOM_TYPE_MISMATCH",
                        "HARD",
                        String.format("Course %s requires LABORATORY but is assigned to CLASSROOM %s", c1.getCourseCode(), r1.getRoomNumber()),
                        "Course: " + c1.getCourseCode() + ", Room: " + r1.getRoomNumber(),
                        s1.getId(),
                        s1.getDayOfWeek() + " " + s1.getStartTime(),
                        "Reassign this course session to an available laboratory room."
                ));
            }

            // Room Capacity constraint
            if (r1.getCapacity() < g1.getStudentCount()) {
                reports.add(new ConflictReportDto(
                        "CAPACITY_DEFICIT",
                        "HARD",
                        String.format("Room %s capacity (%d) is smaller than Student Group %s (%d students)",
                                r1.getRoomNumber(), r1.getCapacity(), g1.getGroupCode(), g1.getStudentCount()),
                        "Room: " + r1.getRoomNumber() + ", Group: " + g1.getGroupCode(),
                        s1.getId(),
                        s1.getDayOfWeek() + " " + s1.getStartTime(),
                        "Move the class to a larger lecture hall with capacity >= " + g1.getStudentCount()
                ));
            }

            // Faculty Availability constraint
            if (f1 != null && unavailableSlots.contains(f1.getId() + ":" + s1.getId())) {
                reports.add(new ConflictReportDto(
                        "FACULTY_UNAVAILABLE",
                        "HARD",
                        String.format("Faculty %s has marked time slot %s as unavailable", f1.getName(), s1.getFormattedTime()),
                        "Faculty: " + f1.getName(),
                        s1.getId(),
                        s1.getDayOfWeek() + " " + s1.getStartTime(),
                        "Move this session to an alternate time slot when " + f1.getName() + " is available."
                ));
            }

            // Pairwise collisions at the same time slot
            for (int j = i + 1; j < entries.size(); j++) {
                TimetableEntry e2 = entries.get(j);
                if (Objects.equals(e1.getTimeSlot().getId(), e2.getTimeSlot().getId())) {
                    // Classroom collision
                    if (Objects.equals(e1.getClassroom().getId(), e2.getClassroom().getId())) {
                        reports.add(new ConflictReportDto(
                                "CLASSROOM_COLLISION",
                                "HARD",
                                String.format("Classroom %s is double-booked by %s and %s",
                                        r1.getRoomNumber(), c1.getCourseCode(), e2.getCourse().getCourseCode()),
                                "Room: " + r1.getRoomNumber(),
                                s1.getId(),
                                s1.getDayOfWeek() + " " + s1.getStartTime(),
                                "Reassign one of the courses to a different available room at this hour."
                        ));
                    }

                    // Faculty collision
                    if (f1 != null && e2.getFaculty() != null && Objects.equals(f1.getId(), e2.getFaculty().getId())) {
                        reports.add(new ConflictReportDto(
                                "FACULTY_COLLISION",
                                "HARD",
                                String.format("Faculty %s is assigned to teach two classes simultaneously: %s and %s",
                                        f1.getName(), c1.getCourseCode(), e2.getCourse().getCourseCode()),
                                "Faculty: " + f1.getName(),
                                s1.getId(),
                                s1.getDayOfWeek() + " " + s1.getStartTime(),
                                "Move one of the conflicting course sessions to a different time slot."
                        ));
                    }

                    // Student group collision
                    if (Objects.equals(g1.getId(), e2.getStudentGroup().getId())) {
                        reports.add(new ConflictReportDto(
                                "STUDENT_GROUP_COLLISION",
                                "HARD",
                                String.format("Student Group %s has overlapping classes: %s and %s",
                                        g1.getGroupCode(), c1.getCourseCode(), e2.getCourse().getCourseCode()),
                                "Group: " + g1.getGroupCode(),
                                s1.getId(),
                                s1.getDayOfWeek() + " " + s1.getStartTime(),
                                "Reschedule one course session so students do not have conflicting classes."
                        ));
                    }
                }
            }
        }

        // 2. Soft constraint checks: consecutive classes & idle gaps
        Map<String, List<Integer>> groupDaySlots = new HashMap<>();
        for (TimetableEntry e : entries) {
            String key = e.getStudentGroup().getGroupCode() + ":" + e.getTimeSlot().getDayOfWeek();
            int order = e.getTimeSlot().getSlotOrder() != null ? e.getTimeSlot().getSlotOrder() : 0;
            groupDaySlots.computeIfAbsent(key, k -> new ArrayList<>()).add(order);
        }

        for (Map.Entry<String, List<Integer>> item : groupDaySlots.entrySet()) {
            List<Integer> slots = item.getValue();
            if (slots.size() > 2) {
                Collections.sort(slots);
                int consecutive = 1;
                for (int idx = 0; idx < slots.size() - 1; idx++) {
                    if (slots.get(idx + 1) - slots.get(idx) == 1) {
                        consecutive++;
                        if (consecutive > 3) {
                            reports.add(new ConflictReportDto(
                                    "CONSECUTIVE_CLASSES_FATIGUE",
                                    "SOFT",
                                    String.format("Group %s has %d consecutive classes on %s without an intervening break",
                                            item.getKey().split(":")[0], consecutive, item.getKey().split(":")[1]),
                                    "Student Group: " + item.getKey().split(":")[0],
                                    null,
                                    item.getKey().split(":")[1],
                                    "Redistribute one lecture to another day or create a rest break in between."
                            ));
                            break;
                        }
                    } else {
                        consecutive = 1;
                    }
                }
            }
        }

        return reports;
    }
}
