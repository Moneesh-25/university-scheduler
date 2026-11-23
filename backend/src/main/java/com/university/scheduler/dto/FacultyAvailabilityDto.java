package com.university.scheduler.dto;

public class FacultyAvailabilityDto {
    private Long id;
    private Long facultyId;
    private String facultyName;
    private Long timeSlotId;
    private String timeSlotLabel;
    private boolean available;

    public FacultyAvailabilityDto() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(Long facultyId) {
        this.facultyId = facultyId;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public Long getTimeSlotId() {
        return timeSlotId;
    }

    public void setTimeSlotId(Long timeSlotId) {
        this.timeSlotId = timeSlotId;
    }

    public String getTimeSlotLabel() {
        return timeSlotLabel;
    }

    public void setTimeSlotLabel(String timeSlotLabel) {
        this.timeSlotLabel = timeSlotLabel;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
