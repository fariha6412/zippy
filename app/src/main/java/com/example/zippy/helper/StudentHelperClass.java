package com.example.zippy.helper;

import org.jetbrains.annotations.NotNull;

public class StudentHelperClass {

    String image;
    String fullName;
    String email;
    String institution;
    String registrationNo;
    Long noOfCourses;

    public StudentHelperClass(){
    }

    public StudentHelperClass(String image, String fullName, String email, String institution, String registrationNo) {
        this.image = image;
        this.fullName = fullName;
        this.email = email;
        this.institution = institution;
        this.registrationNo = registrationNo;
        this.noOfCourses = 0L;
    }
    public String getImage() { return image; }

    public void setImage(String image) { this.image = image; }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getInstitution() {
        return institution;
    }


    public String getRegistrationNo() {
        return registrationNo;
    }

    public void setRegistrationNo(String registrationNo) {
        this.registrationNo = registrationNo;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public Long getNoOfCourses() {
        return noOfCourses;
    }

    public void setNoOfCourses(Long noOfCourses) {
        this.noOfCourses = noOfCourses;
    }

    @Override
    public @NotNull String toString() {
        return "StudentHelperClass{" +
                "image='" + image + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", institution='" + institution + '\'' +
                ", registrationNo='" + registrationNo + '\'' +
                ", noOfCourses= " + noOfCourses +
                '}';
    }
    public @NotNull String showProfileDetails() {
        return  " Account Type:     Student\n" +
                " FullName:         " + fullName + '\n' +
                " Email Address: " + email + '\n' +
                " Institution:      " + institution + '\n' +
                " RegistrationNo:   " + registrationNo + '\n' +
                " NoOfCourses:      " + noOfCourses ;
    }
}
