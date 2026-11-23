package com.university.scheduler.service;

import com.university.scheduler.dto.StudentGroupDto;
import com.university.scheduler.entity.Department;
import com.university.scheduler.entity.StudentGroup;
import com.university.scheduler.exception.ResourceNotFoundException;
import com.university.scheduler.repository.DepartmentRepository;
import com.university.scheduler.repository.StudentGroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudentGroupService {

    private final StudentGroupRepository studentGroupRepository;
    private final DepartmentRepository departmentRepository;

    public StudentGroupService(StudentGroupRepository studentGroupRepository,
                               DepartmentRepository departmentRepository) {
        this.studentGroupRepository = studentGroupRepository;
        this.departmentRepository = departmentRepository;
    }

    public List<StudentGroupDto> getAllGroups() {
        return studentGroupRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public StudentGroupDto getGroupById(Long id) {
        StudentGroup group = studentGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student group not found with id: " + id));
        return toDto(group);
    }

    public StudentGroupDto createGroup(StudentGroupDto dto) {
        if (studentGroupRepository.existsByGroupCode(dto.getGroupCode())) {
            throw new IllegalArgumentException("Student group code " + dto.getGroupCode() + " already exists");
        }
        Department dept = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + dto.getDepartmentId()));

        StudentGroup group = new StudentGroup(
                dto.getGroupCode(),
                dto.getGroupName(),
                dept,
                dto.getSemester(),
                dto.getStudentCount()
        );
        return toDto(studentGroupRepository.save(group));
    }

    public StudentGroupDto updateGroup(Long id, StudentGroupDto dto) {
        StudentGroup group = studentGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student group not found with id: " + id));

        if (!group.getGroupCode().equals(dto.getGroupCode()) && studentGroupRepository.existsByGroupCode(dto.getGroupCode())) {
            throw new IllegalArgumentException("Student group code " + dto.getGroupCode() + " already in use");
        }
        Department dept = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + dto.getDepartmentId()));

        group.setGroupCode(dto.getGroupCode());
        group.setGroupName(dto.getGroupName());
        group.setDepartment(dept);
        group.setSemester(dto.getSemester());
        group.setStudentCount(dto.getStudentCount());

        return toDto(studentGroupRepository.save(group));
    }

    public void deleteGroup(Long id) {
        if (!studentGroupRepository.existsById(id)) {
            throw new ResourceNotFoundException("Student group not found with id: " + id);
        }
        studentGroupRepository.deleteById(id);
    }

    public StudentGroupDto toDto(StudentGroup entity) {
        if (entity == null) return null;
        StudentGroupDto dto = new StudentGroupDto();
        dto.setId(entity.getId());
        dto.setGroupCode(entity.getGroupCode());
        dto.setGroupName(entity.getGroupName());
        dto.setSemester(entity.getSemester());
        dto.setStudentCount(entity.getStudentCount());
        if (entity.getDepartment() != null) {
            dto.setDepartmentId(entity.getDepartment().getId());
            dto.setDepartmentName(entity.getDepartment().getName());
            dto.setDepartmentCode(entity.getDepartment().getCode());
        }
        return dto;
    }
}
