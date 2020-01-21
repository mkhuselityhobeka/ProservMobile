package com.twoid.proserv_mobile.model;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.orm.SugarRecord;

@JsonDeserialize
public class UserRegistration extends SugarRecord {

    private String personName;
    private String personSurname;
    private String studentNumber;
    private String employeeNumber;
    private String syncStatus;
    private int id;


    public String setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
        return syncStatus;
    }

    public String getSyncStatus() {
        return syncStatus;
    }



    public  UserRegistration(){
        super();
    }

    public UserRegistration(String personName, String personSurname, String studentNumber, String employeeNumber,String syncStatus) {
        this.personName = personName;
        this.personSurname = personSurname;
        this.studentNumber = studentNumber;
        this.employeeNumber = employeeNumber;
        this.syncStatus = syncStatus;
    }

    public String setPersonName(String personName) {
        this.personName = personName;
        return personName;
    }

    public String setPersonSurname(String personSurname) {
        this.personSurname = personSurname;
        return personSurname;
    }

    public String setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
        return studentNumber;
    }

    public String setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
        return  employeeNumber;
    }



    public String getPersonName() {
        return personName;
    }

    public String getPersonSurname() {
        return personSurname;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    @Override
    public String toString() {
        return "UserRegistration{" +
                "personName='" + personName + '\'' +
                ", personSurname='" + personSurname + '\'' +
                ", studentNumber='" + studentNumber + '\'' +
                ", employeeNumber='" + employeeNumber + '\'' +
                ", syncStatus='" + syncStatus + '\'' +
                '}';
    }
}
