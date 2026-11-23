package com.university.scheduler.controller;

import com.university.scheduler.dto.ApiResponse;
import com.university.scheduler.dto.TimetableEntryDto;
import com.university.scheduler.service.TimetableService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timetable")
public class TimetableController {

    private final TimetableService timetableService;

    public TimetableController(TimetableService timetableService) {
        this.timetableService = timetableService;
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<List<TimetableEntryDto>>> generateTimetable(
            @RequestParam(defaultValue = "BACKTRACKING") String algorithm,
            @RequestParam(defaultValue = "true") boolean clearExisting) {
        List<TimetableEntryDto> entries = timetableService.generateTimetable(algorithm, clearExisting);
        return ResponseEntity.ok(ApiResponse.success(
                String.format("Timetable successfully generated using %s (%d sessions scheduled)", algorithm, entries.size()),
                entries
        ));
    }

    @PostMapping("/undo")
    public ResponseEntity<ApiResponse<Boolean>> undoLastSchedule() {
        boolean undone = timetableService.undoLastSchedule();
        if (undone) {
            return ResponseEntity.ok(ApiResponse.success("Last generated schedule batch rolled back successfully", true));
        } else {
            return ResponseEntity.ok(ApiResponse.success("No previous schedule batch to undo in stack", false));
        }
    }

    @GetMapping("/entries")
    public ResponseEntity<ApiResponse<List<TimetableEntryDto>>> getEntries(
            @RequestParam(required = false) Long studentGroupId,
            @RequestParam(required = false) Long facultyId,
            @RequestParam(required = false) Long classroomId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Integer semester) {

        List<TimetableEntryDto> list;
        if (studentGroupId != null) {
            list = timetableService.getEntriesByStudentGroup(studentGroupId);
        } else if (facultyId != null) {
            list = timetableService.getEntriesByFaculty(facultyId);
        } else if (classroomId != null) {
            list = timetableService.getEntriesByClassroom(classroomId);
        } else if (departmentId != null) {
            list = timetableService.getEntriesByDepartment(departmentId);
        } else if (semester != null) {
            list = timetableService.getEntriesBySemester(semester);
        } else {
            list = timetableService.getAllEntries();
        }

        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @DeleteMapping("/entries")
    public ResponseEntity<ApiResponse<Void>> clearEntries() {
        timetableService.clearAllEntries();
        return ResponseEntity.ok(ApiResponse.success("All timetable entries cleared", null));
    }

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv() {
        String csvData = timetableService.exportScheduleAsCsv();
        byte[] bytes = csvData.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"university_timetable.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }
}
