-- University Course Timetabling and Examination Scheduler Realistic Academic Seed Data
USE university_scheduler;

-- Departments
INSERT IGNORE INTO departments (id, code, name) VALUES
(1, 'CSE', 'Computer Science & Engineering'),
(2, 'IT', 'Information Technology'),
(3, 'ECE', 'Electronics & Communication Engineering');

-- Classrooms & Labs
INSERT IGNORE INTO classrooms (id, room_number, building, capacity, room_type, is_available) VALUES
(1, 'LH-101', 'Main Academic Block', 65, 'CLASSROOM', TRUE),
(2, 'LH-102', 'Main Academic Block', 65, 'CLASSROOM', TRUE),
(3, 'LH-201', 'Science Block', 50, 'CLASSROOM', TRUE),
(4, 'LH-202', 'Science Block', 45, 'CLASSROOM', TRUE),
(5, 'LAB-301', 'IT Computing Center', 60, 'LABORATORY', TRUE),
(6, 'LAB-302', 'Software Systems Lab', 60, 'LABORATORY', TRUE),
(7, 'LAB-303', 'Hardware & Networks Lab', 35, 'LABORATORY', TRUE);

-- Faculty Members
INSERT IGNORE INTO faculty (id, faculty_code, name, email, department_id, specialization, max_weekly_hours) VALUES
(1, 'FAC-001', 'Dr. A. Ramanathan', 'ramanathan@university.edu', 2, 'Distributed Systems & Cloud Computing', 16),
(2, 'FAC-002', 'Dr. Priya Sundaram', 'priya.s@university.edu', 2, 'Algorithm Design & Data Structures', 18),
(3, 'FAC-003', 'Prof. K. Venkatesh', 'venkatesh.k@university.edu', 1, 'Artificial Intelligence & Machine Learning', 16),
(4, 'FAC-004', 'Dr. Meenakshi Iyer', 'meenakshi@university.edu', 1, 'Database Engineering & Big Data', 18),
(5, 'FAC-005', 'Prof. Rajesh Sharma', 'sharma.r@university.edu', 2, 'Network Security & Cryptography', 16),
(6, 'FAC-006', 'Dr. Shalini Verma', 'shalini.v@university.edu', 1, 'Operating Systems & Concurrency', 18),
(7, 'FAC-007', 'Prof. Arvind Menon', 'arvind.m@university.edu', 3, 'Embedded Systems & IoT', 16),
(8, 'FAC-008', 'Dr. Sangeetha Nair', 'sangeetha.n@university.edu', 2, 'Software Engineering & Architecture', 18);

-- Student Groups
INSERT IGNORE INTO student_groups (id, group_code, group_name, department_id, semester, student_count) VALUES
(1, 'MTECH-IT-Y1', 'M.Tech Information Technology - Year 1', 2, 1, 28),
(2, 'MTECH-CSE-Y1', 'M.Tech Computer Science - Year 1', 1, 1, 32),
(3, 'BTECH-IT-Y3', 'B.Tech IT - Year 3', 2, 5, 48),
(4, 'BTECH-CSE-Y3', 'B.Tech CSE - Year 3', 1, 5, 55);
