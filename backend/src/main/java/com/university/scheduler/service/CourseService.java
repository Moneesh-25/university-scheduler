package com.university.scheduler.service;

import com.university.scheduler.dto.CourseDto;
import com.university.scheduler.entity.*;
import com.university.scheduler.exception.ResourceNotFoundException;
import com.university.scheduler.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;
    private final FacultyRepository facultyRepository;
    private final StudentGroupRepository studentGroupRepository;

    public CourseService(CourseRepository courseRepository,
                         DepartmentRepository departmentRepository,
                         FacultyRepository facultyRepository,
                         StudentGroupRepository studentGroupRepository) {
        this.courseRepository = courseRepository;
        this.departmentRepository = departmentRepository;
        this.facultyRepository = facultyRepository;
        this.studentGroupRepository = studentGroupRepository;
    }

    public List<CourseDto> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public CourseDto getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        return toDto(course);
    }

    public CourseDto createCourse(CourseDto dto) {
        if (courseRepository.existsByCourseCode(dto.getCourseCode())) {
            throw new IllegalArgumentException("Course code " + dto.getCourseCode() + " already exists");
        }

        Department dept = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + dto.getDepartmentId()));

        Faculty faculty = null;
        if (dto.getAssignedFacultyId() != null) {
            faculty = facultyRepository.findById(dto.getAssignedFacultyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with id: " + dto.getAssignedFacultyId()));
        }

        Course course = new Course(
                dto.getCourseCode(),
                dto.getCourseName(),
                dept,
                dto.getSemester(),
                dto.getCredits(),
                faculty,
                dto.getRequiredRoomType() != null ? dto.getRequiredRoomType() : RoomType.CLASSROOM,
                dto.getWeeklySessions() != null ? dto.getWeeklySessions() : 3,
                dto.getSessionDurationMinutes() != null ? dto.getSessionDurationMinutes() : 60
        );

        if (dto.getStudentGroupIds() != null && !dto.getStudentGroupIds().isEmpty()) {
            Set<StudentGroup> groups = new HashSet<>(studentGroupRepository.findAllById(dto.getStudentGroupIds()));
            course.setStudentGroups(groups);
        }

        return toDto(courseRepository.save(course));
    }

    public CourseDto updateCourse(Long id, CourseDto dto) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        if (!course.getCourseCode().equals(dto.getCourseCode()) && courseRepository.existsByCourseCode(dto.getCourseCode())) {
            throw new IllegalArgumentException("Course code " + dto.getCourseCode() + " already in use");
        }

        Department dept = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + dto.getDepartmentId()));

        Faculty faculty = null;
        if (dto.getAssignedFacultyId() != null) {
            faculty = facultyRepository.findById(dto.getAssignedFacultyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with id: " + dto.getAssignedFacultyId()));
        }

        course.setCourseCode(dto.getCourseCode());
        course.setCourseName(dto.getCourseName());
        course.setDepartment(dept);
        course.setSemester(dto.getSemester());
        course.setCredits(dto.getCredits());
        course.setAssignedFaculty(faculty);
        course.setRequiredRoomType(dto.getRequiredRoomType() != null ? dto.getRequiredRoomType() : RoomType.CLASSROOM);
        course.setWeeklySessions(dto.getWeeklySessions());
        course.setSessionDurationMinutes(dto.getSessionDurationMinutes());

        if (dto.getStudentGroupIds() != null) {
            Set<StudentGroup> groups = new HashSet<>(studentGroupRepository.findAllById(dto.getStudentGroupIds()));
            course.setStudentGroups(groups);
        }

        return toDto(courseRepository.save(course));
    }

    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Course not found with id: " + id);
        }
        courseRepository.deleteById(id);
    }

    public CourseDto toDto(Course entity) {
        if (entity == null) return null;
        CourseDto dto = new CourseDto();
        dto.setId(entity.getId());
        dto.setCourseCode(entity.getCourseCode());
        dto.setCourseName(entity.getCourseName());
        dto.setSemester(entity.getSemester());
        dto.setCredits(entity.getCredits());
        dto.setRequiredRoomType(entity.getRequiredRoomType());
        dto.setWeeklySessions(entity.getWeeklySessions());
        dto.setSessionDurationMinutes(entity.getSessionDurationMinutes());

        if (entity.getDepartment() != null) {
            dto.setDepartmentId(entity.getDepartment().getId());
            dto.setDepartmentName(entity.getDepartment().getName());
            dto.setDepartmentCode(entity.getDepartment().getCode());
        }

        if (entity.getAssignedFaculty() != null) {
            dto.setAssignedFacultyId(entity.getAssignedFaculty().getId());
            dto.setAssignedFacultyName(entity.getAssignedFaculty().getName());
        }

        if (entity.getStudentGroups() != null) {
            dto.setStudentGroupIds(entity.getStudentGroups().stream().map(StudentGroup::getId).collect(Collectors.toList()));
            dto.setStudentGroupNames(entity.getStudentGroups().stream().map(StudentGroup::getGroupName).collect(Collectors.toList()));
        }
        return dto;
    }
}
