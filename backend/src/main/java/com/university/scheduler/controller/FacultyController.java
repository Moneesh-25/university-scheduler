package com.university.scheduler.controller;

import com.university.scheduler.dto.ApiResponse;
import com.university.scheduler.dto.FacultyDto;
import com.university.scheduler.service.FacultyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faculty")
public class FacultyController {

    private final FacultyService facultyService;

    public FacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FacultyDto>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(facultyService.getAllFaculty()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FacultyDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(facultyService.getFacultyById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FacultyDto>> create(@Valid @RequestBody FacultyDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Faculty created successfully", facultyService.createFaculty(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FacultyDto>> update(@PathVariable Long id, @Valid @RequestBody FacultyDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Faculty updated successfully", facultyService.updateFaculty(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        facultyService.deleteFaculty(id);
        return ResponseEntity.ok(ApiResponse.success("Faculty deleted successfully", null));
    }
}
