package com.university.scheduler.repository;

import com.university.scheduler.entity.StudentGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentGroupRepository extends JpaRepository<StudentGroup, Long> {
    Optional<StudentGroup> findByGroupCode(String groupCode);
    boolean existsByGroupCode(String groupCode);
    List<StudentGroup> findByDepartmentId(Long departmentId);
    List<StudentGroup> findBySemester(Integer semester);
}
