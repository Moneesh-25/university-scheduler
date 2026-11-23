package com.university.scheduler.repository;

import com.university.scheduler.entity.TimetableEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TimetableEntryRepository extends JpaRepository<TimetableEntry, Long> {

    List<TimetableEntry> findByScheduleBatchId(String scheduleBatchId);

    List<TimetableEntry> findByFacultyId(Long facultyId);

    List<TimetableEntry> findByClassroomId(Long classroomId);

    List<TimetableEntry> findByStudentGroupId(Long studentGroupId);

    @Query("SELECT t FROM TimetableEntry t WHERE t.course.semester = :semester")
    List<TimetableEntry> findBySemester(@Param("semester") Integer semester);

    @Query("SELECT t FROM TimetableEntry t WHERE t.course.department.id = :departmentId")
    List<TimetableEntry> findByDepartmentId(@Param("departmentId") Long departmentId);

    @Query("SELECT t FROM TimetableEntry t WHERE t.timeSlot.id = :timeSlotId AND t.faculty.id = :facultyId")
    List<TimetableEntry> findSlotFacultyConflicts(@Param("timeSlotId") Long timeSlotId, @Param("facultyId") Long facultyId);

    @Query("SELECT t FROM TimetableEntry t WHERE t.timeSlot.id = :timeSlotId AND t.classroom.id = :classroomId")
    List<TimetableEntry> findSlotClassroomConflicts(@Param("timeSlotId") Long timeSlotId, @Param("classroomId") Long classroomId);

    @Query("SELECT t FROM TimetableEntry t WHERE t.timeSlot.id = :timeSlotId AND t.studentGroup.id = :studentGroupId")
    List<TimetableEntry> findSlotGroupConflicts(@Param("timeSlotId") Long timeSlotId, @Param("studentGroupId") Long studentGroupId);

    void deleteByScheduleBatchId(String scheduleBatchId);
}
