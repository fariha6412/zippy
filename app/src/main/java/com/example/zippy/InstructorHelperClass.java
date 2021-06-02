package com.example.zippy;

public class InstructorHelperClass {
    String fullName, email, institution, employeeID, designation, password;

    public InstructorHelperClass(){}

    public InstructorHelperClass(String fullName, String email, String institution, String employeeID, String designation, String password) {
        this.fullName = fullName;
        this.email = email;
        this.institution = institution;
        this.employeeID = employeeID;
        this.designation = designation;
        this.password = password;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
