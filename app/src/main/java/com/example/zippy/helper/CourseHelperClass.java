package com.example.zippy.helper;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CourseHelperClass {
    String courseCode, courseTitle, coursePassCode, courseCredit, courseYear, instructoruid;
    Long noOfStudents;

    public CourseHelperClass(){
    }

    public CourseHelperClass(String courseCode, String courseTitle, String courseYear, String courseCredit, String coursePassCode, String instructoruid) {
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.coursePassCode = coursePassCode;
        this.courseCredit = courseCredit;
        this.courseYear = courseYear;
        this.instructoruid = instructoruid;
        noOfStudents = 0L;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public String getCoursePassCode() {
        return coursePassCode;
    }

    public void setCoursePassCode(String coursePassCode) {
        this.coursePassCode = coursePassCode;
    }

    public String getCourseCredit() {
        return courseCredit;
    }

    public void setCourseCredit(String courseCredit) {
        this.courseCredit = courseCredit;
    }

    public String getCourseYear() {
        return courseYear;
    }

    public void setCourseYear(String courseYear) {
        this.courseYear = courseYear;
    }

    public String getInstructoruid() {
        return instructoruid;
    }

    public void setInstructoruid(String instructoruid) {
        this.instructoruid = instructoruid;
    }

    public Long getNoOfStudents() {
        return noOfStudents;
    }

    public void setNoOfStudents(Long noOfStudents) {
        this.noOfStudents = noOfStudents;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    @Override
    public @NotNull String toString() {
        return "CourseHelperClass{" +
                "courseTitle='" + courseTitle + '\'' +
                ", coursePassCode='" + coursePassCode + '\'' +
                ", courseCredit='" + courseCredit + '\'' +
                ", courseYear='" + courseYear + '\'' +
                ", instructoruid='" + instructoruid + '\'' +
                ", noOfStudents=" + noOfStudents +
                '}';
    }
}
