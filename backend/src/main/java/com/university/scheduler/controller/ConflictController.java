package com.university.scheduler.controller;

import com.university.scheduler.dto.ApiResponse;
import com.university.scheduler.dto.ConflictReportDto;
import com.university.scheduler.service.ConflictDetectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/conflicts")
public class ConflictController {

    private final ConflictDetectionService conflictDetectionService;

    public ConflictController(ConflictDetectionService conflictDetectionService) {
        this.conflictDetectionService = conflictDetectionService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ConflictReportDto>>> scanConflicts() {
        List<ConflictReportDto> conflicts = conflictDetectionService.scanConflicts();
        String msg = conflicts.isEmpty()
                ? "Schedule is completely conflict-free. All hard and soft constraints satisfied."
                : String.format("Detected %d scheduling conflicts requiring attention.", conflicts.size());
        return ResponseEntity.ok(ApiResponse.success(msg, conflicts));
    }
}
