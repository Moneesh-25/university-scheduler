package com.university.scheduler.repository;

import com.university.scheduler.entity.SchedulingJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SchedulingJobRepository extends JpaRepository<SchedulingJob, Long> {
    Optional<SchedulingJob> findByJobId(String jobId);
    List<SchedulingJob> findTop10ByOrderByCreatedAtDesc();
}
