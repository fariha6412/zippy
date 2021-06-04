package com.example.zippy.helper;

import org.jetbrains.annotations.NotNull;

public class StudentHelperClass {

    String image, fullName, email, institution, registrationNo, password;

    public StudentHelperClass(){
        throw new UnsupportedOperationException("Empty constructor is not supported.");
    }

    public StudentHelperClass(String image, String fullName, String email, String institution, String registrationNo, String password) {
        this.image = image;
        this.fullName = fullName;
        this.email = email;
        this.institution = institution;
        this.registrationNo = registrationNo;
        this.password = password;
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

    public String getPassword() {
        return password;
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

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public @NotNull String toString() {
        return "StudentHelperClass{" +
                "image='" + image + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", institution='" + institution + '\'' +
                ", registrationNo='" + registrationNo + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
