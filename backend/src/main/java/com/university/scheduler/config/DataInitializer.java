package com.university.scheduler.config;

import com.university.scheduler.entity.*;
import com.university.scheduler.repository.*;
import com.university.scheduler.service.TimeSlotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final DepartmentRepository departmentRepository;
    private final FacultyRepository facultyRepository;
    private final ClassroomRepository classroomRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final CourseRepository courseRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final FacultyAvailabilityRepository facultyAvailabilityRepository;
    private final TimeSlotService timeSlotService;

    @org.springframework.beans.factory.annotation.Value("${app.data.seed-demo:true}")
    private boolean seedDemoData;

    public DataInitializer(DepartmentRepository departmentRepository,
                           FacultyRepository facultyRepository,
                           ClassroomRepository classroomRepository,
                           StudentGroupRepository studentGroupRepository,
                           CourseRepository courseRepository,
                           TimeSlotRepository timeSlotRepository,
                           FacultyAvailabilityRepository facultyAvailabilityRepository,
                           TimeSlotService timeSlotService) {
        this.departmentRepository = departmentRepository;
        this.facultyRepository = facultyRepository;
        this.classroomRepository = classroomRepository;
        this.studentGroupRepository = studentGroupRepository;
        this.courseRepository = courseRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.facultyAvailabilityRepository = facultyAvailabilityRepository;
        this.timeSlotService = timeSlotService;
    }

    @Override
    public void run(String... args) {
        if (!seedDemoData) {
            log.info("Demo data seeding is disabled (app.data.seed-demo=false). Starting in blank template mode.");
            if (timeSlotRepository.count() == 0) {
                timeSlotService.generateDefaultAcademicSlots(List.of(
                        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
                ));
                log.info("Initialized standard 45-slot academic weekly grid template.");
            }
            return;
        }

        if (departmentRepository.count() > 0) {
            log.info("Database already contains data. Skipping initial seeding.");
            return;
        }

        log.info("Seeding academic dataset for University Timetabling & Examination Scheduler...");

        // 1. Departments
        Department cse = departmentRepository.save(new Department("CSE", "Computer Science & Engineering"));
        Department it = departmentRepository.save(new Department("IT", "Information Technology"));
        Department ece = departmentRepository.save(new Department("ECE", "Electronics & Communication Engineering"));

        // 2. Classrooms & Laboratories
        Classroom lh101 = classroomRepository.save(new Classroom("LH-101", "Main Academic Block", 65, RoomType.CLASSROOM, true));
        Classroom lh102 = classroomRepository.save(new Classroom("LH-102", "Main Academic Block", 65, RoomType.CLASSROOM, true));
        Classroom lh201 = classroomRepository.save(new Classroom("LH-201", "Science Block", 50, RoomType.CLASSROOM, true));
        Classroom lh202 = classroomRepository.save(new Classroom("LH-202", "Science Block", 45, RoomType.CLASSROOM, true));
        Classroom lab301 = classroomRepository.save(new Classroom("LAB-301", "IT Computing Center", 60, RoomType.LABORATORY, true));
        Classroom lab302 = classroomRepository.save(new Classroom("LAB-302", "Software Systems Lab", 60, RoomType.LABORATORY, true));
        Classroom lab303 = classroomRepository.save(new Classroom("LAB-303", "Hardware & Networks Lab", 35, RoomType.LABORATORY, true));

        // 3. Faculty Members
        Faculty f1 = facultyRepository.save(new Faculty("FAC-001", "Dr. A. Ramanathan", "ramanathan@university.edu", it, "Distributed Systems & Cloud Computing", 16));
        Faculty f2 = facultyRepository.save(new Faculty("FAC-002", "Dr. Priya Sundaram", "priya.s@university.edu", it, "Algorithm Design & Data Structures", 18));
        Faculty f3 = facultyRepository.save(new Faculty("FAC-003", "Prof. K. Venkatesh", "venkatesh.k@university.edu", cse, "Artificial Intelligence & Machine Learning", 16));
        Faculty f4 = facultyRepository.save(new Faculty("FAC-004", "Dr. Meenakshi Iyer", "meenakshi@university.edu", cse, "Database Engineering & Big Data", 18));
        Faculty f5 = facultyRepository.save(new Faculty("FAC-005", "Prof. Rajesh Sharma", "sharma.r@university.edu", it, "Network Security & Cryptography", 16));
        Faculty f6 = facultyRepository.save(new Faculty("FAC-006", "Dr. Shalini Verma", "shalini.v@university.edu", cse, "Operating Systems & Concurrency", 18));
        Faculty f7 = facultyRepository.save(new Faculty("FAC-007", "Prof. Arvind Menon", "arvind.m@university.edu", ece, "Embedded Systems & IoT", 16));
        Faculty f8 = facultyRepository.save(new Faculty("FAC-008", "Dr. Sangeetha Nair", "sangeetha.n@university.edu", it, "Software Engineering & Architecture", 18));

        // 4. Student Groups
        StudentGroup gMTechIT = studentGroupRepository.save(new StudentGroup("MTECH-IT-Y1", "M.Tech Information Technology - Year 1", it, 1, 28));
        StudentGroup gMTechCSE = studentGroupRepository.save(new StudentGroup("MTECH-CSE-Y1", "M.Tech Computer Science - Year 1", cse, 1, 32));
        StudentGroup gBTechIT = studentGroupRepository.save(new StudentGroup("BTECH-IT-Y3", "B.Tech IT - Year 3", it, 5, 48));
        StudentGroup gBTechCSE = studentGroupRepository.save(new StudentGroup("BTECH-CSE-Y3", "B.Tech CSE - Year 3", cse, 5, 55));

        // 5. Generate Academic Time Slots
        timeSlotService.generateDefaultAcademicSlots(List.of(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
        ));

        // 6. Courses with Classroom / Lab requirements and session counts
        Course c1 = new Course("IT701", "Advanced Algorithms & Complexity", it, 1, 4, f2, RoomType.CLASSROOM, 3, 55);
        c1.getStudentGroups().add(gMTechIT);
        courseRepository.save(c1);

        Course c2 = new Course("IT702", "Cloud Computing & Distributed Systems", it, 1, 4, f1, RoomType.CLASSROOM, 3, 55);
        c2.getStudentGroups().add(gMTechIT);
        courseRepository.save(c2);

        Course c3 = new Course("IT703P", "Distributed Systems Laboratory", it, 1, 2, f1, RoomType.LABORATORY, 2, 110);
        c3.getStudentGroups().add(gMTechIT);
        courseRepository.save(c3);

        Course c4 = new Course("IT704", "Information Security & Cryptography", it, 1, 3, f5, RoomType.CLASSROOM, 3, 55);
        c4.getStudentGroups().add(gMTechIT);
        courseRepository.save(c4);

        Course c5 = new Course("CS701", "Advanced Machine Learning", cse, 1, 4, f3, RoomType.CLASSROOM, 3, 55);
        c5.getStudentGroups().add(gMTechCSE);
        courseRepository.save(c5);

        Course c6 = new Course("CS702P", "Machine Learning & Deep Learning Lab", cse, 1, 2, f3, RoomType.LABORATORY, 2, 110);
        c6.getStudentGroups().add(gMTechCSE);
        courseRepository.save(c6);

        Course c7 = new Course("CS703", "Big Data Analytics & Storage Systems", cse, 1, 4, f4, RoomType.CLASSROOM, 3, 55);
        c7.getStudentGroups().add(gMTechCSE);
        courseRepository.save(c7);

        Course c8 = new Course("CS704", "Advanced Operating Systems & Concurrency", cse, 1, 3, f6, RoomType.CLASSROOM, 3, 55);
        c8.getStudentGroups().add(gMTechCSE);
        courseRepository.save(c8);

        Course c9 = new Course("IT501", "Database Management Systems", it, 5, 4, f4, RoomType.CLASSROOM, 3, 55);
        c9.getStudentGroups().add(gBTechIT);
        courseRepository.save(c9);

        Course c10 = new Course("IT502P", "Database Management Systems Lab", it, 5, 2, f4, RoomType.LABORATORY, 2, 110);
        c10.getStudentGroups().add(gBTechIT);
        courseRepository.save(c10);

        Course c11 = new Course("CS501", "Computer Networks & Protocols", cse, 5, 4, f5, RoomType.CLASSROOM, 3, 55);
        c11.getStudentGroups().add(gBTechCSE);
        courseRepository.save(c11);

        Course c12 = new Course("CS502P", "Computer Networks & Security Lab", cse, 5, 2, f5, RoomType.LABORATORY, 2, 110);
        c12.getStudentGroups().add(gBTechCSE);
        courseRepository.save(c12);

        // 7. Configure realistic Faculty Availability constraint
        // E.g. Dr. Ramanathan is unavailable on Friday afternoons (slot order 8 & 9)
        List<TimeSlot> friAfternoonSlots = timeSlotRepository.findAll().stream()
                .filter(s -> s.getDayOfWeek() == DayOfWeek.FRIDAY && s.getSlotOrder() != null && s.getSlotOrder() >= 8)
                .toList();
        for (TimeSlot ts : friAfternoonSlots) {
            facultyAvailabilityRepository.save(new FacultyAvailability(f1, ts, false));
        }

        log.info("Academic sample dataset successfully populated: 3 Departments, 8 Faculty, 7 Classrooms, 4 Student Groups, 12 Courses, 45 Time Slots.");
    }
}
