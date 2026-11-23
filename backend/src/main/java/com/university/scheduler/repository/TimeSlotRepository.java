package com.university.scheduler.repository;

import com.university.scheduler.entity.DayOfWeek;
import com.university.scheduler.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    List<TimeSlot> findByDayOfWeekOrderBySlotOrderAsc(DayOfWeek dayOfWeek);
    List<TimeSlot> findByIsBreakFalseAndIsLunchFalseOrderByDayOfWeekAscSlotOrderAsc();
    List<TimeSlot> findAllByOrderByDayOfWeekAscSlotOrderAsc();
}
