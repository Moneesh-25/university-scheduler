package com.university.scheduler.service;

import com.university.scheduler.algorithm.exam.ExamSchedulerEngine;
import com.university.scheduler.dto.ExaminationDto;
import com.university.scheduler.entity.*;
import com.university.scheduler.exception.ResourceNotFoundException;
import com.university.scheduler.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExaminationService {

    private final ExaminationRepository examinationRepository;
    private final CourseRepository courseRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final ClassroomRepository classroomRepository;
    private final FacultyRepository facultyRepository;
    private final TimeSlotRepository timeSlotRepository;

    public ExaminationService(ExaminationRepository examinationRepository,
                              CourseRepository courseRepository,
                              StudentGroupRepository studentGroupRepository,
                              ClassroomRepository classroomRepository,
                              FacultyRepository facultyRepository,
                              TimeSlotRepository timeSlotRepository) {
        this.examinationRepository = examinationRepository;
        this.courseRepository = courseRepository;
        this.studentGroupRepository = studentGroupRepository;
        this.classroomRepository = classroomRepository;
        this.facultyRepository = facultyRepository;
        this.timeSlotRepository = timeSlotRepository;
    }

    public List<ExaminationDto> getAllExaminations() {
        return examinationRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ExaminationDto getExaminationById(Long id) {
        Examination exam = examinationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Examination not found with id: " + id));
        return toDto(exam);
    }

    public ExaminationDto createExamination(ExaminationDto dto) {
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + dto.getCourseId()));
        StudentGroup group = studentGroupRepository.findById(dto.getStudentGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Student group not found with id: " + dto.getStudentGroupId()));
        Classroom room = classroomRepository.findById(dto.getClassroomId())
                .orElseThrow(() -> new ResourceNotFoundException("Classroom not found with id: " + dto.getClassroomId()));
        Faculty invigilator = facultyRepository.findById(dto.getInvigilatorId())
                .orElseThrow(() -> new ResourceNotFoundException("Invigilator not found with id: " + dto.getInvigilatorId()));
        TimeSlot slot = timeSlotRepository.findById(dto.getTimeSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("TimeSlot not found with id: " + dto.getTimeSlotId()));

        Examination exam = new Examination(
                course,
                group,
                dto.getExamDate(),
                slot,
                room,
                invigilator,
                dto.getDurationMinutes(),
                course.getSemester()
        );

        return toDto(examinationRepository.save(exam));
    }

    public ExaminationDto updateExamination(Long id, ExaminationDto dto) {
        Examination exam = examinationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Examination not found with id: " + id));

        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + dto.getCourseId()));
        StudentGroup group = studentGroupRepository.findById(dto.getStudentGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Student group not found with id: " + dto.getStudentGroupId()));
        Classroom room = classroomRepository.findById(dto.getClassroomId())
                .orElseThrow(() -> new ResourceNotFoundException("Classroom not found with id: " + dto.getClassroomId()));
        Faculty invigilator = facultyRepository.findById(dto.getInvigilatorId())
                .orElseThrow(() -> new ResourceNotFoundException("Invigilator not found with id: " + dto.getInvigilatorId()));
        TimeSlot slot = timeSlotRepository.findById(dto.getTimeSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("TimeSlot not found with id: " + dto.getTimeSlotId()));

        exam.setCourse(course);
        exam.setStudentGroup(group);
        exam.setClassroom(room);
        exam.setInvigilator(invigilator);
        exam.setTimeSlot(slot);
        exam.setExamDate(dto.getExamDate());
        exam.setDurationMinutes(dto.getDurationMinutes());
        exam.setStatus(dto.getStatus());

        return toDto(examinationRepository.save(exam));
    }

    public void deleteExamination(Long id) {
        if (!examinationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Examination not found with id: " + id);
        }
        examinationRepository.deleteById(id);
    }

    public void clearAllExaminations() {
        examinationRepository.deleteAll();
    }

    /**
     * Automatically generates a conflict-free examination schedule for all courses.
     */
    public List<ExaminationDto> generateExamSchedule(LocalDate startDate, int minDaysSpacing, boolean clearExisting) {
        if (clearExisting) {
            examinationRepository.deleteAll();
        }

        List<Course> courses = courseRepository.findAll();
        List<Classroom> classrooms = classroomRepository.findByAvailableTrue();
        List<Faculty> facultyList = facultyRepository.findAll();
        List<TimeSlot> slots = timeSlotRepository.findByIsBreakFalseAndIsLunchFalseOrderByDayOfWeekAscSlotOrderAsc();

        if (courses.isEmpty() || classrooms.isEmpty() || facultyList.isEmpty() || slots.isEmpty()) {
            throw new IllegalArgumentException("Cannot generate exams: Missing courses, classrooms, faculty, or slots");
        }

        ExamSchedulerEngine engine = new ExamSchedulerEngine();
        List<ExamSchedulerEngine.ExamPlan> plans = engine.generateExamSchedule(
                courses, classrooms, facultyList, slots, startDate, minDaysSpacing
        );

        List<Examination> exams = new ArrayList<>();
        for (ExamSchedulerEngine.ExamPlan plan : plans) {
            Examination exam = new Examination(
                    plan.getCourse(),
                    plan.getStudentGroup(),
                    plan.getDate(),
                    plan.getTimeSlot(),
                    plan.getClassroom(),
                    plan.getInvigilator(),
                    180, // 3 hours standard
                    plan.getCourse().getSemester()
            );
            exams.add(exam);
        }

        List<Examination> saved = examinationRepository.saveAll(exams);
        return saved.stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<ExaminationDto> getByDate(LocalDate date) {
        return examinationRepository.findByExamDate(date).stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<ExaminationDto> getBySemester(Integer semester) {
        return examinationRepository.findBySemester(semester).stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<ExaminationDto> getByStudentGroup(Long groupId) {
        return examinationRepository.findByStudentGroupId(groupId).stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<ExaminationDto> getByInvigilator(Long invigilatorId) {
        return examinationRepository.findByInvigilatorId(invigilatorId).stream().map(this::toDto).collect(Collectors.toList());
    }

    public ExaminationDto toDto(Examination entity) {
        if (entity == null) return null;
        ExaminationDto dto = new ExaminationDto();
        dto.setId(entity.getId());
        dto.setExamDate(entity.getExamDate());
        dto.setDurationMinutes(entity.getDurationMinutes());
        dto.setSemester(entity.getSemester());
        dto.setStatus(entity.getStatus());

        if (entity.getCourse() != null) {
            dto.setCourseId(entity.getCourse().getId());
            dto.setCourseCode(entity.getCourse().getCourseCode());
            dto.setCourseName(entity.getCourse().getCourseName());
        }

        if (entity.getStudentGroup() != null) {
            dto.setStudentGroupId(entity.getStudentGroup().getId());
            dto.setGroupCode(entity.getStudentGroup().getGroupCode());
            dto.setGroupName(entity.getStudentGroup().getGroupName());
            dto.setStudentCount(entity.getStudentGroup().getStudentCount());
        }

        if (entity.getClassroom() != null) {
            dto.setClassroomId(entity.getClassroom().getId());
            dto.setRoomNumber(entity.getClassroom().getRoomNumber());
            dto.setBuilding(entity.getClassroom().getBuilding());
            dto.setClassroomCapacity(entity.getClassroom().getCapacity());
        }

        if (entity.getInvigilator() != null) {
            dto.setInvigilatorId(entity.getInvigilator().getId());
            dto.setInvigilatorCode(entity.getInvigilator().getFacultyCode());
            dto.setInvigilatorName(entity.getInvigilator().getName());
        }

        if (entity.getTimeSlot() != null) {
            dto.setTimeSlotId(entity.getTimeSlot().getId());
            dto.setDayOfWeek(entity.getTimeSlot().getDayOfWeek());
            dto.setStartTime(entity.getTimeSlot().getStartTime());
            dto.setEndTime(entity.getTimeSlot().getEndTime());
        }

        return dto;
    }
}
