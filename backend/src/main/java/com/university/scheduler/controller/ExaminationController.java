package com.university.scheduler.controller;

import com.university.scheduler.dto.ApiResponse;
import com.university.scheduler.dto.ExaminationDto;
import com.university.scheduler.service.ExaminationService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/examinations")
public class ExaminationController {

    private final ExaminationService examinationService;

    public ExaminationController(ExaminationService examinationService) {
        this.examinationService = examinationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ExaminationDto>>> getAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Integer semester,
            @RequestParam(required = false) Long studentGroupId,
            @RequestParam(required = false) Long invigilatorId) {

        List<ExaminationDto> list;
        if (date != null) {
            list = examinationService.getByDate(date);
        } else if (semester != null) {
            list = examinationService.getBySemester(semester);
        } else if (studentGroupId != null) {
            list = examinationService.getByStudentGroup(studentGroupId);
        } else if (invigilatorId != null) {
            list = examinationService.getByInvigilator(invigilatorId);
        } else {
            list = examinationService.getAllExaminations();
        }

        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExaminationDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(examinationService.getExaminationById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ExaminationDto>> create(@Valid @RequestBody ExaminationDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Examination scheduled successfully", examinationService.createExamination(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExaminationDto>> update(@PathVariable Long id, @Valid @RequestBody ExaminationDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Examination updated successfully", examinationService.updateExamination(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        examinationService.deleteExamination(id);
        return ResponseEntity.ok(ApiResponse.success("Examination deleted successfully", null));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearAll() {
        examinationService.clearAllExaminations();
        return ResponseEntity.ok(ApiResponse.success("All examinations cleared", null));
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<List<ExaminationDto>>> generateExamSchedule(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(defaultValue = "1") int minDaysSpacing,
            @RequestParam(defaultValue = "true") boolean clearExisting) {
        List<ExaminationDto> scheduled = examinationService.generateExamSchedule(startDate, minDaysSpacing, clearExisting);
        return ResponseEntity.ok(ApiResponse.success(
                String.format("Generated %d conflict-free examinations", scheduled.size()),
                scheduled
        ));
    }
}
