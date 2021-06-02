package com.example.zippy;

public class StudentHelperClass {
    String fullName, email, institution, registrationNo, password;

    public StudentHelperClass(){}

    public StudentHelperClass(String fullName, String email, String institution, String registrationNo, String password) {
        this.fullName = fullName;
        this.email = email;
        this.institution = institution;
        this.registrationNo = registrationNo;
        this.password = password;
    }

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
}
