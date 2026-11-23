package com.university.scheduler.algorithm.core;

import com.university.scheduler.entity.Course;
import com.university.scheduler.entity.Faculty;
import com.university.scheduler.entity.RoomType;
import com.university.scheduler.entity.StudentGroup;

import java.util.Objects;

/**
 * Represents a discrete schedulable course lecture or laboratory session.
 * For example, if a Course has 3 weekly sessions for a Student Group,
 * it yields 3 Activity instances: (Course, Group, Faculty, sessionIndex=1..3).
 */
public class Activity {
    private final String id;
    private final Course course;
    private final StudentGroup studentGroup;
    private final Faculty faculty;
    private final int sessionIndex;
    private final RoomType requiredRoomType;
    private final int studentCount;

    public Activity(Course course, StudentGroup studentGroup, Faculty faculty, int sessionIndex) {
        this.course = course;
        this.studentGroup = studentGroup;
        this.faculty = faculty;
        this.sessionIndex = sessionIndex;
        this.requiredRoomType = course.getRequiredRoomType();
        this.studentCount = studentGroup.getStudentCount();
        this.id = course.getCourseCode() + "-" + studentGroup.getGroupCode() + "-S" + sessionIndex;
    }

    public String getId() {
        return id;
    }

    public Course getCourse() {
        return course;
    }

    public StudentGroup getStudentGroup() {
        return studentGroup;
    }

    public Faculty getFaculty() {
        return faculty;
    }

    public int getSessionIndex() {
        return sessionIndex;
    }

    public RoomType getRequiredRoomType() {
        return requiredRoomType;
    }

    public int getStudentCount() {
        return studentCount;
    }

    /**
     * Two activities conflict if they share the same Faculty or the same Student Group,
     * meaning they cannot occur simultaneously.
     */
    public boolean conflictsWith(Activity other) {
        if (other == null || this.equals(other)) return false;
        boolean sameFaculty = (this.faculty != null && other.faculty != null &&
                Objects.equals(this.faculty.getId(), other.faculty.getId()));
        boolean sameGroup = (this.studentGroup != null && other.studentGroup != null &&
                Objects.equals(this.studentGroup.getId(), other.studentGroup.getId()));
        return sameFaculty || sameGroup;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Activity activity)) return false;
        return Objects.equals(id, activity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return id + " (" + course.getCourseName() + ", " + faculty.getName() + ")";
    }
}
