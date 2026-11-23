package com.university.scheduler.service;

import com.university.scheduler.dto.FacultyDto;
import com.university.scheduler.entity.Department;
import com.university.scheduler.entity.Faculty;
import com.university.scheduler.entity.FacultyAvailability;
import com.university.scheduler.entity.TimeSlot;
import com.university.scheduler.exception.ResourceNotFoundException;
import com.university.scheduler.repository.DepartmentRepository;
import com.university.scheduler.repository.FacultyAvailabilityRepository;
import com.university.scheduler.repository.FacultyRepository;
import com.university.scheduler.repository.TimeSlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FacultyService {

    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;
    private final FacultyAvailabilityRepository facultyAvailabilityRepository;
    private final TimeSlotRepository timeSlotRepository;

    public FacultyService(FacultyRepository facultyRepository,
                          DepartmentRepository departmentRepository,
                          FacultyAvailabilityRepository facultyAvailabilityRepository,
                          TimeSlotRepository timeSlotRepository) {
        this.facultyRepository = facultyRepository;
        this.departmentRepository = departmentRepository;
        this.facultyAvailabilityRepository = facultyAvailabilityRepository;
        this.timeSlotRepository = timeSlotRepository;
    }

    public List<FacultyDto> getAllFaculty() {
        return facultyRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public FacultyDto getFacultyById(Long id) {
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with id: " + id));
        return toDto(faculty);
    }

    public FacultyDto createFaculty(FacultyDto dto) {
        if (facultyRepository.existsByFacultyCode(dto.getFacultyCode())) {
            throw new IllegalArgumentException("Faculty ID " + dto.getFacultyCode() + " already exists");
        }
        if (facultyRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Faculty email " + dto.getEmail() + " already exists");
        }

        Department dept = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + dto.getDepartmentId()));

        Faculty faculty = new Faculty(
                dto.getFacultyCode(),
                dto.getName(),
                dto.getEmail(),
                dept,
                dto.getSpecialization(),
                dto.getMaxWeeklyHours()
        );
        Faculty saved = facultyRepository.save(faculty);

        // Save availability if specified
        updateAvailability(saved, dto.getAvailableSlotIds());

        return toDto(saved);
    }

    public FacultyDto updateFaculty(Long id, FacultyDto dto) {
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with id: " + id));

        if (!faculty.getFacultyCode().equals(dto.getFacultyCode()) && facultyRepository.existsByFacultyCode(dto.getFacultyCode())) {
            throw new IllegalArgumentException("Faculty ID " + dto.getFacultyCode() + " already in use");
        }
        if (!faculty.getEmail().equals(dto.getEmail()) && facultyRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Faculty email " + dto.getEmail() + " already in use");
        }

        Department dept = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + dto.getDepartmentId()));

        faculty.setFacultyCode(dto.getFacultyCode());
        faculty.setName(dto.getName());
        faculty.setEmail(dto.getEmail());
        faculty.setDepartment(dept);
        faculty.setSpecialization(dto.getSpecialization());
        faculty.setMaxWeeklyHours(dto.getMaxWeeklyHours());

        Faculty saved = facultyRepository.save(faculty);
        if (dto.getAvailableSlotIds() != null) {
            updateAvailability(saved, dto.getAvailableSlotIds());
        }

        return toDto(saved);
    }

    public void deleteFaculty(Long id) {
        if (!facultyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Faculty not found with id: " + id);
        }
        facultyAvailabilityRepository.deleteByFacultyId(id);
        facultyRepository.deleteById(id);
    }

    public void updateAvailability(Faculty faculty, List<Long> availableSlotIds) {
        facultyAvailabilityRepository.deleteByFacultyId(faculty.getId());
        if (availableSlotIds != null && !availableSlotIds.isEmpty()) {
            List<TimeSlot> slots = timeSlotRepository.findAllById(availableSlotIds);
            for (TimeSlot slot : slots) {
                facultyAvailabilityRepository.save(new FacultyAvailability(faculty, slot, true));
            }
        }
    }

    public FacultyDto toDto(Faculty entity) {
        if (entity == null) return null;
        FacultyDto dto = new FacultyDto();
        dto.setId(entity.getId());
        dto.setFacultyCode(entity.getFacultyCode());
        dto.setName(entity.getName());
        dto.setEmail(entity.getEmail());
        dto.setSpecialization(entity.getSpecialization());
        dto.setMaxWeeklyHours(entity.getMaxWeeklyHours());
        if (entity.getDepartment() != null) {
            dto.setDepartmentId(entity.getDepartment().getId());
            dto.setDepartmentName(entity.getDepartment().getName());
            dto.setDepartmentCode(entity.getDepartment().getCode());
        }
        List<Long> slotIds = facultyAvailabilityRepository.findByFacultyIdAndAvailableTrue(entity.getId())
                .stream().map(fa -> fa.getTimeSlot().getId()).collect(Collectors.toList());
        dto.setAvailableSlotIds(slotIds);
        return dto;
    }
}
