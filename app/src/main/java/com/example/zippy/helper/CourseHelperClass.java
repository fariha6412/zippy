package com.example.zippy.helper;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CourseHelperClass {
    String courseTitle, coursePassCode, courseCredit, year, instructoruid;
    List<String> studentsuid;

    public CourseHelperClass(){
        throw new UnsupportedOperationException("Empty constructor is not supported.");
    }

    public CourseHelperClass(String courseTitle, String coursePassCode, String courseCredit, String year, String instructoruid) {
        this.courseTitle = courseTitle;
        this.coursePassCode = coursePassCode;
        this.courseCredit = courseCredit;
        this.year = year;
        this.instructoruid = instructoruid;
        studentsuid = new ArrayList<String>();
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

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getInstructoruid() {
        return instructoruid;
    }

    public void setInstructoruid(String instructoruid) {
        this.instructoruid = instructoruid;
    }

    public List<String> getStudentsuid() {
        return studentsuid;
    }

    public void setStudentsuid(List<String> studentsuid) {
        this.studentsuid = studentsuid;
    }

    @Override
    public @NotNull String toString() {
        return "CourseHelperClass{" +
                "courseTitle='" + courseTitle + '\'' +
                ", coursePassCode='" + coursePassCode + '\'' +
                ", courseCredit='" + courseCredit + '\'' +
                ", year='" + year + '\'' +
                ", instructoruid='" + instructoruid + '\'' +
                ", studentsuid=" + studentsuid +
                '}';
    }
}
