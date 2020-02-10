package com.twoid.proserv_mobile.model;

import com.orm.SugarRecord;

public class FacilitatorStructure extends SugarRecord {

    private String Name;
    private String Surname;
    private WSQFingerprints WSQFingerprints;

    public FacilitatorStructure(){}




    public String setName(String name) {
        Name = name;
        return name;
    }

    public String setSurname(String surname) {
        Surname = surname;
        return surname;
    }

    public void setWSQFingerprints(WSQFingerprints WSQFingerprints) {
        this.WSQFingerprints = WSQFingerprints;
    }


    public String getName() {
        return Name;
    }

    public String getSurname() {
        return Surname;
    }


    public WSQFingerprints getWSQFingerprints() {
        return WSQFingerprints;
    }






}
