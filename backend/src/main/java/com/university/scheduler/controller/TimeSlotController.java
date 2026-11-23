package com.university.scheduler.controller;

import com.university.scheduler.dto.ApiResponse;
import com.university.scheduler.dto.TimeSlotDto;
import com.university.scheduler.entity.DayOfWeek;
import com.university.scheduler.service.TimeSlotService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/time-slots")
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    public TimeSlotController(TimeSlotService timeSlotService) {
        this.timeSlotService = timeSlotService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TimeSlotDto>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(timeSlotService.getAllSlots()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TimeSlotDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(timeSlotService.getSlotById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TimeSlotDto>> create(@Valid @RequestBody TimeSlotDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Time slot created successfully", timeSlotService.createSlot(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TimeSlotDto>> update(@PathVariable Long id, @Valid @RequestBody TimeSlotDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Time slot updated successfully", timeSlotService.updateSlot(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        timeSlotService.deleteSlot(id);
        return ResponseEntity.ok(ApiResponse.success("Time slot deleted successfully", null));
    }

    @PostMapping("/generate-academic")
    public ResponseEntity<ApiResponse<List<TimeSlotDto>>> generateDefaultSlots(
            @RequestBody(required = false) List<DayOfWeek> days) {
        return ResponseEntity.ok(ApiResponse.success(
                "Standard academic time slots generated",
                timeSlotService.generateDefaultAcademicSlots(days)
        ));
    }
}
