package com.twoid.proserv_mobile.model;



import com.orm.SugarRecord;

import java.io.Serializable;


public class LearnerStructure extends SugarRecord implements Serializable {



    private String Name;
    private String Surname;
    private String LearnerGuid;
    private String RSAID;
    private String course;
    private WSQFingerprints WSQFingerprintImages;

    public String getLearnerGuid() {
        return LearnerGuid;
    }

    public String setLearnerGuid(String LearnerGuid) {
        this.LearnerGuid = LearnerGuid;
        return LearnerGuid;
    }

    public  LearnerStructure(){
        super();
    }

    public LearnerStructure(String Name, String Surname, String learnerGuid,
                            String RSAID,String course,
                             WSQFingerprints WSQFingerprintImages) {
        this.Name = Name;
        this.Surname = Surname;
        this.RSAID = RSAID;


        this.LearnerGuid = learnerGuid;
        this.WSQFingerprintImages  = WSQFingerprintImages ;
        this.course = course;
    }

    public String setName(String Name) {
        this.Name = Name;
        return Name;
    }

    public String setSurname(String Surname) {
        this.Surname = Surname;
        return Surname;
    }

    public String setRSAID(String RSAID) {
        this.RSAID = RSAID;
        return RSAID;
    }

    public String setCourse(String course) {
        this.course = course;
        return  course;
    }



    public String getName() {
        return Name;
    }

    public String getSurname() {
        return Surname;
    }

    public String getRSAID() {
        return RSAID;
    }

    public WSQFingerprints getWSQFingerprintImages() {
        return WSQFingerprintImages;
    }

    public void setWSQFingerprintImages(WSQFingerprints WSQFingerprintImages) {
        this.WSQFingerprintImages = WSQFingerprintImages;
    }

    public String getCourse() {
        return course;
    }


    @Override
    public String toString() {
        return "LearnerStructure{" +
                "Name='" + Name + '\'' +
                ", Surname='" + Surname + '\'' +
                ", LearnerGuid='" + LearnerGuid + '\'' +
                ", RSAID='" + RSAID + '\'' +

                ", course='" + course + '\'' +
                ", WSQFingerprintImages =" + WSQFingerprintImages  +

                '}';
    }
}
