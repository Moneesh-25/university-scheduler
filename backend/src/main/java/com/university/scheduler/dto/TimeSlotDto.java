package com.university.scheduler.dto;

import com.university.scheduler.entity.DayOfWeek;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public class TimeSlotDto {
    private Long id;

    @NotNull
    private DayOfWeek dayOfWeek;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    private Integer slotOrder;
    private boolean isBreak = false;
    private boolean isLunch = false;
    private String formattedLabel;

    public TimeSlotDto() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Integer getSlotOrder() {
        return slotOrder;
    }

    public void setSlotOrder(Integer slotOrder) {
        this.slotOrder = slotOrder;
    }

    public boolean isBreak() {
        return isBreak;
    }

    public void setBreak(boolean aBreak) {
        isBreak = aBreak;
    }

    public boolean isLunch() {
        return isLunch;
    }

    public void setLunch(boolean lunch) {
        isLunch = lunch;
    }

    public String getFormattedLabel() {
        return (dayOfWeek != null ? dayOfWeek.name() : "") + " " + startTime + " - " + endTime;
    }

    public void setFormattedLabel(String formattedLabel) {
        this.formattedLabel = formattedLabel;
    }
}
