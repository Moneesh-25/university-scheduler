package com.university.scheduler.repository;

import com.university.scheduler.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Long> {
    Optional<Faculty> findByFacultyCode(String facultyCode);
    Optional<Faculty> findByEmail(String email);
    boolean existsByFacultyCode(String facultyCode);
    boolean existsByEmail(String email);
    List<Faculty> findByDepartmentId(Long departmentId);
}
