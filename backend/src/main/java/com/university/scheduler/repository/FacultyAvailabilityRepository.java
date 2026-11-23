package com.university.scheduler.repository;

import com.university.scheduler.entity.FacultyAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FacultyAvailabilityRepository extends JpaRepository<FacultyAvailability, Long> {
    List<FacultyAvailability> findByFacultyId(Long facultyId);
    List<FacultyAvailability> findByFacultyIdAndAvailableTrue(Long facultyId);
    Optional<FacultyAvailability> findByFacultyIdAndTimeSlotId(Long facultyId, Long timeSlotId);
    void deleteByFacultyId(Long facultyId);
}
