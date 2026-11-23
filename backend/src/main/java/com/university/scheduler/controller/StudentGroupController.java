package com.university.scheduler.controller;

import com.university.scheduler.dto.ApiResponse;
import com.university.scheduler.dto.StudentGroupDto;
import com.university.scheduler.service.StudentGroupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student-groups")
public class StudentGroupController {

    private final StudentGroupService studentGroupService;

    public StudentGroupController(StudentGroupService studentGroupService) {
        this.studentGroupService = studentGroupService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StudentGroupDto>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(studentGroupService.getAllGroups()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentGroupDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(studentGroupService.getGroupById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StudentGroupDto>> create(@Valid @RequestBody StudentGroupDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Student group created successfully", studentGroupService.createGroup(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentGroupDto>> update(@PathVariable Long id, @Valid @RequestBody StudentGroupDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Student group updated successfully", studentGroupService.updateGroup(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        studentGroupService.deleteGroup(id);
        return ResponseEntity.ok(ApiResponse.success("Student group deleted successfully", null));
    }
}
