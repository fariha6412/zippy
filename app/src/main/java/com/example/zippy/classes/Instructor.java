package com.example.zippy.classes;

import org.jetbrains.annotations.NotNull;

public class Instructor {
    String image, fullName, email, institution, employeeID, designation;
    Long noOfCourses;

    public Instructor(){
    }

    public Instructor(String image, String fullName, String email, String institution, String employeeID, String designation) {
        this.image = image;
        this.fullName = fullName;
        this.email = email;
        this.institution = institution;
        this.employeeID = employeeID;
        this.designation = designation;
        this.noOfCourses = 0L;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public String getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(String employeeID) {
        this.employeeID = employeeID;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public Long getNoOfCourses() {
        return noOfCourses;
    }

    public void setNoOfCourses(Long noOfCourses) {
        this.noOfCourses = noOfCourses;
    }

    @Override
    public @NotNull String toString() {
        return "Instructor{" +
                "image='" + image + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", institution='" + institution + '\'' +
                ", employeeID='" + employeeID + '\'' +
                ", designation='" + designation + '\'' +
                ", noOfCourses=" + noOfCourses +
                '}';
    }
}
