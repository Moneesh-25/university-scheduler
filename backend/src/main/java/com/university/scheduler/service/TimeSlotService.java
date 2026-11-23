package com.university.scheduler.service;

import com.university.scheduler.dto.TimeSlotDto;
import com.university.scheduler.entity.DayOfWeek;
import com.university.scheduler.entity.TimeSlot;
import com.university.scheduler.exception.ResourceNotFoundException;
import com.university.scheduler.repository.TimeSlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;

    public TimeSlotService(TimeSlotRepository timeSlotRepository) {
        this.timeSlotRepository = timeSlotRepository;
    }

    public List<TimeSlotDto> getAllSlots() {
        return timeSlotRepository.findAllByOrderByDayOfWeekAscSlotOrderAsc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public TimeSlotDto getSlotById(Long id) {
        TimeSlot slot = timeSlotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TimeSlot not found with id: " + id));
        return toDto(slot);
    }

    public TimeSlotDto createSlot(TimeSlotDto dto) {
        TimeSlot slot = new TimeSlot(
                dto.getDayOfWeek(),
                dto.getStartTime(),
                dto.getEndTime(),
                dto.getSlotOrder(),
                dto.isBreak(),
                dto.isLunch()
        );
        return toDto(timeSlotRepository.save(slot));
    }

    public TimeSlotDto updateSlot(Long id, TimeSlotDto dto) {
        TimeSlot slot = timeSlotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TimeSlot not found with id: " + id));

        slot.setDayOfWeek(dto.getDayOfWeek());
        slot.setStartTime(dto.getStartTime());
        slot.setEndTime(dto.getEndTime());
        slot.setSlotOrder(dto.getSlotOrder());
        slot.setBreak(dto.isBreak());
        slot.setLunch(dto.isLunch());

        return toDto(timeSlotRepository.save(slot));
    }

    public void deleteSlot(Long id) {
        if (!timeSlotRepository.existsById(id)) {
            throw new ResourceNotFoundException("TimeSlot not found with id: " + id);
        }
        timeSlotRepository.deleteById(id);
    }

    /**
     * Generates standard academic timetable slots for specified days of the week.
     */
    public List<TimeSlotDto> generateDefaultAcademicSlots(List<DayOfWeek> days) {
        if (days == null || days.isEmpty()) {
            days = List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
        }

        List<TimeSlot> created = new ArrayList<>();
        for (DayOfWeek day : days) {
            // 9:00 - 9:55 (Slot 1)
            created.add(new TimeSlot(day, LocalTime.of(9, 0), LocalTime.of(9, 55), 1, false, false));
            // 9:55 - 10:50 (Slot 2)
            created.add(new TimeSlot(day, LocalTime.of(9, 55), LocalTime.of(10, 50), 2, false, false));
            // 10:50 - 11:10 (Tea Break)
            created.add(new TimeSlot(day, LocalTime.of(10, 50), LocalTime.of(11, 10), 3, true, false));
            // 11:10 - 12:05 (Slot 4)
            created.add(new TimeSlot(day, LocalTime.of(11, 10), LocalTime.of(12, 5), 4, false, false));
            // 12:05 - 13:00 (Slot 5)
            created.add(new TimeSlot(day, LocalTime.of(12, 5), LocalTime.of(13, 0), 5, false, false));
            // 13:00 - 13:50 (Lunch Break)
            created.add(new TimeSlot(day, LocalTime.of(13, 0), LocalTime.of(13, 50), 6, false, true));
            // 13:50 - 14:45 (Slot 7)
            created.add(new TimeSlot(day, LocalTime.of(13, 50), LocalTime.of(14, 45), 7, false, false));
            // 14:45 - 15:40 (Slot 8)
            created.add(new TimeSlot(day, LocalTime.of(14, 45), LocalTime.of(15, 40), 8, false, false));
            // 15:40 - 16:35 (Slot 9)
            created.add(new TimeSlot(day, LocalTime.of(15, 40), LocalTime.of(16, 35), 9, false, false));
        }

        timeSlotRepository.deleteAll();
        List<TimeSlot> saved = timeSlotRepository.saveAll(created);
        return saved.stream().map(this::toDto).collect(Collectors.toList());
    }

    public TimeSlotDto toDto(TimeSlot entity) {
        if (entity == null) return null;
        TimeSlotDto dto = new TimeSlotDto();
        dto.setId(entity.getId());
        dto.setDayOfWeek(entity.getDayOfWeek());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setSlotOrder(entity.getSlotOrder());
        dto.setBreak(entity.isBreak());
        dto.setLunch(entity.isLunch());
        dto.setFormattedLabel(entity.getDayOfWeek() + " " + entity.getStartTime() + " - " + entity.getEndTime());
        return dto;
    }
}
