package com.university.scheduler.repository;

import com.university.scheduler.entity.Examination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExaminationRepository extends JpaRepository<Examination, Long> {

    List<Examination> findByExamDate(LocalDate examDate);

    List<Examination> findBySemester(Integer semester);

    List<Examination> findByStudentGroupId(Long studentGroupId);

    List<Examination> findByInvigilatorId(Long invigilatorId);

    List<Examination> findByClassroomId(Long classroomId);

    @Query("SELECT e FROM Examination e WHERE e.examDate = :examDate AND e.timeSlot.id = :timeSlotId AND e.invigilator.id = :invigilatorId")
    List<Examination> findInvigilatorExamConflicts(@Param("examDate") LocalDate examDate,
                                                  @Param("timeSlotId") Long timeSlotId,
                                                  @Param("invigilatorId") Long invigilatorId);

    @Query("SELECT e FROM Examination e WHERE e.examDate = :examDate AND e.timeSlot.id = :timeSlotId AND e.studentGroup.id = :studentGroupId")
    List<Examination> findGroupExamConflicts(@Param("examDate") LocalDate examDate,
                                            @Param("timeSlotId") Long timeSlotId,
                                            @Param("studentGroupId") Long studentGroupId);

    @Query("SELECT e FROM Examination e WHERE e.examDate = :examDate AND e.timeSlot.id = :timeSlotId AND e.classroom.id = :classroomId")
    List<Examination> findRoomExamConflicts(@Param("examDate") LocalDate examDate,
                                           @Param("timeSlotId") Long timeSlotId,
                                           @Param("classroomId") Long classroomId);
}
