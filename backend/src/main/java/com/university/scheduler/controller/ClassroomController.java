package com.university.scheduler.controller;

import com.university.scheduler.dto.ApiResponse;
import com.university.scheduler.dto.ClassroomDto;
import com.university.scheduler.entity.RoomType;
import com.university.scheduler.service.ClassroomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classrooms")
public class ClassroomController {

    private final ClassroomService classroomService;

    public ClassroomController(ClassroomService classroomService) {
        this.classroomService = classroomService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ClassroomDto>>> getAll(@RequestParam(required = false) RoomType type) {
        if (type != null) {
            return ResponseEntity.ok(ApiResponse.success(classroomService.getAvailableRoomsByType(type)));
        }
        return ResponseEntity.ok(ApiResponse.success(classroomService.getAllClassrooms()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClassroomDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(classroomService.getClassroomById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ClassroomDto>> create(@Valid @RequestBody ClassroomDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Classroom created successfully", classroomService.createClassroom(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClassroomDto>> update(@PathVariable Long id, @Valid @RequestBody ClassroomDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Classroom updated successfully", classroomService.updateClassroom(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        classroomService.deleteClassroom(id);
        return ResponseEntity.ok(ApiResponse.success("Classroom deleted successfully", null));
    }
}
