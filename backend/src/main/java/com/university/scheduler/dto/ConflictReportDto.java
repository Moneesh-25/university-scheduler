package com.university.scheduler.dto;

public class ConflictReportDto {
    private String conflictType;
    private String severity; // HARD or SOFT
    private String description;
    private String affectedEntity;
    private Long timeSlotId;
    private String timeSlotInfo;
    private String suggestedResolution;

    public ConflictReportDto() {}

    public ConflictReportDto(String conflictType, String severity, String description,
                             String affectedEntity, Long timeSlotId, String timeSlotInfo, String suggestedResolution) {
        this.conflictType = conflictType;
        this.severity = severity;
        this.description = description;
        this.affectedEntity = affectedEntity;
        this.timeSlotId = timeSlotId;
        this.timeSlotInfo = timeSlotInfo;
        this.suggestedResolution = suggestedResolution;
    }

    public String getConflictType() {
        return conflictType;
    }

    public void setConflictType(String conflictType) {
        this.conflictType = conflictType;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAffectedEntity() {
        return affectedEntity;
    }

    public void setAffectedEntity(String affectedEntity) {
        this.affectedEntity = affectedEntity;
    }

    public Long getTimeSlotId() {
        return timeSlotId;
    }

    public void setTimeSlotId(Long timeSlotId) {
        this.timeSlotId = timeSlotId;
    }

    public String getTimeSlotInfo() {
        return timeSlotInfo;
    }

    public void setTimeSlotInfo(String timeSlotInfo) {
        this.timeSlotInfo = timeSlotInfo;
    }

    public String getSuggestedResolution() {
        return suggestedResolution;
    }

    public void setSuggestedResolution(String suggestedResolution) {
        this.suggestedResolution = suggestedResolution;
    }
}
