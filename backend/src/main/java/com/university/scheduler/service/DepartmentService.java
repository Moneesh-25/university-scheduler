package com.university.scheduler.service;

import com.university.scheduler.dto.DepartmentDto;
import com.university.scheduler.entity.Department;
import com.university.scheduler.exception.ResourceNotFoundException;
import com.university.scheduler.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public List<DepartmentDto> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public DepartmentDto getDepartmentById(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        return toDto(dept);
    }

    public DepartmentDto createDepartment(DepartmentDto dto) {
        if (departmentRepository.existsByCode(dto.getCode())) {
            throw new IllegalArgumentException("Department with code " + dto.getCode() + " already exists");
        }
        Department dept = new Department(dto.getCode(), dto.getName());
        return toDto(departmentRepository.save(dept));
    }

    public DepartmentDto updateDepartment(Long id, DepartmentDto dto) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        dept.setCode(dto.getCode());
        dept.setName(dto.getName());
        return toDto(departmentRepository.save(dept));
    }

    public void deleteDepartment(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Department not found with id: " + id);
        }
        departmentRepository.deleteById(id);
    }

    public DepartmentDto toDto(Department entity) {
        if (entity == null) return null;
        return new DepartmentDto(entity.getId(), entity.getCode(), entity.getName());
    }
}
