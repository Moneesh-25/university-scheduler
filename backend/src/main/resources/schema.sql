-- University Course Timetabling and Examination Scheduler DDL Schema
-- Compatible with MySQL 8.0+

CREATE DATABASE IF NOT EXISTS university_scheduler CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE university_scheduler;

-- 1. Departments
CREATE TABLE IF NOT EXISTS departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Classrooms & Laboratories
CREATE TABLE IF NOT EXISTS classrooms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_number VARCHAR(30) NOT NULL UNIQUE,
    building VARCHAR(100) NOT NULL,
    capacity INT NOT NULL,
    room_type VARCHAR(20) NOT NULL,
    is_available BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Faculty
CREATE TABLE IF NOT EXISTS faculty (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    faculty_code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    department_id BIGINT NOT NULL,
    specialization VARCHAR(150),
    max_weekly_hours INT DEFAULT 18,
    CONSTRAINT fk_faculty_department FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Student Groups
CREATE TABLE IF NOT EXISTS student_groups (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_code VARCHAR(30) NOT NULL UNIQUE,
    group_name VARCHAR(100) NOT NULL,
    department_id BIGINT NOT NULL,
    semester INT NOT NULL,
    student_count INT NOT NULL,
    CONSTRAINT fk_group_department FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Time Slots
CREATE TABLE IF NOT EXISTS time_slots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    day_of_week VARCHAR(15) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    slot_order INT,
    is_break BOOLEAN NOT NULL DEFAULT FALSE,
    is_lunch BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE KEY uq_slot (day_of_week, start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Faculty Availability Constraints
CREATE TABLE IF NOT EXISTS faculty_availabilities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    faculty_id BIGINT NOT NULL,
    time_slot_id BIGINT NOT NULL,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE KEY uq_faculty_slot (faculty_id, time_slot_id),
    CONSTRAINT fk_fa_faculty FOREIGN KEY (faculty_id) REFERENCES faculty(id) ON DELETE CASCADE,
    CONSTRAINT fk_fa_timeslot FOREIGN KEY (time_slot_id) REFERENCES time_slots(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. Courses
CREATE TABLE IF NOT EXISTS courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_code VARCHAR(30) NOT NULL UNIQUE,
    course_name VARCHAR(120) NOT NULL,
    department_id BIGINT NOT NULL,
    semester INT NOT NULL,
    credits INT NOT NULL,
    faculty_id BIGINT,
    required_room_type VARCHAR(20) NOT NULL DEFAULT 'CLASSROOM',
    weekly_sessions INT NOT NULL DEFAULT 3,
    session_duration_minutes INT NOT NULL DEFAULT 60,
    CONSTRAINT fk_course_department FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE CASCADE,
    CONSTRAINT fk_course_faculty FOREIGN KEY (faculty_id) REFERENCES faculty(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. Course to Student Group Enrollment
CREATE TABLE IF NOT EXISTS course_student_groups (
    course_id BIGINT NOT NULL,
    student_group_id BIGINT NOT NULL,
    PRIMARY KEY (course_id, student_group_id),
    CONSTRAINT fk_csg_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT fk_csg_group FOREIGN KEY (student_group_id) REFERENCES student_groups(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. Timetable Entries
CREATE TABLE IF NOT EXISTS timetable_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    schedule_batch_id VARCHAR(50) NOT NULL,
    course_id BIGINT NOT NULL,
    faculty_id BIGINT NOT NULL,
    classroom_id BIGINT NOT NULL,
    student_group_id BIGINT NOT NULL,
    time_slot_id BIGINT NOT NULL,
    algorithm_used VARCHAR(30) DEFAULT 'BACKTRACKING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_batch (schedule_batch_id),
    INDEX idx_slot_faculty (time_slot_id, faculty_id),
    INDEX idx_slot_room (time_slot_id, classroom_id),
    INDEX idx_slot_group (time_slot_id, student_group_id),
    CONSTRAINT fk_te_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT fk_te_faculty FOREIGN KEY (faculty_id) REFERENCES faculty(id) ON DELETE CASCADE,
    CONSTRAINT fk_te_room FOREIGN KEY (classroom_id) REFERENCES classrooms(id) ON DELETE CASCADE,
    CONSTRAINT fk_te_group FOREIGN KEY (student_group_id) REFERENCES student_groups(id) ON DELETE CASCADE,
    CONSTRAINT fk_te_timeslot FOREIGN KEY (time_slot_id) REFERENCES time_slots(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10. Examinations
CREATE TABLE IF NOT EXISTS examinations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    student_group_id BIGINT NOT NULL,
    exam_date DATE NOT NULL,
    time_slot_id BIGINT NOT NULL,
    classroom_id BIGINT NOT NULL,
    invigilator_id BIGINT NOT NULL,
    duration_minutes INT NOT NULL DEFAULT 180,
    semester INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    INDEX idx_exam_date_slot (exam_date, time_slot_id),
    INDEX idx_exam_invigilator (invigilator_id),
    INDEX idx_exam_room (classroom_id),
    CONSTRAINT fk_ex_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT fk_ex_group FOREIGN KEY (student_group_id) REFERENCES student_groups(id) ON DELETE CASCADE,
    CONSTRAINT fk_ex_timeslot FOREIGN KEY (time_slot_id) REFERENCES time_slots(id) ON DELETE CASCADE,
    CONSTRAINT fk_ex_room FOREIGN KEY (classroom_id) REFERENCES classrooms(id) ON DELETE CASCADE,
    CONSTRAINT fk_ex_invigilator FOREIGN KEY (invigilator_id) REFERENCES faculty(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 11. Scheduling Jobs
CREATE TABLE IF NOT EXISTS scheduling_jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id VARCHAR(50) NOT NULL UNIQUE,
    job_type VARCHAR(30) NOT NULL,
    algorithm VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'QUEUED',
    scheduled_activities INT DEFAULT 0,
    unscheduled_activities INT DEFAULT 0,
    conflicts_count INT DEFAULT 0,
    backtracks_count INT DEFAULT 0,
    execution_time_ms BIGINT DEFAULT 0,
    soft_constraint_score DOUBLE DEFAULT 0.0,
    metrics_json TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
