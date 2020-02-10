package com.twoid.proserv_mobile.model;

import java.util.Arrays;

public class UsersData {
    public byte[] getTemplate() {
        return template;
    }

    public void setTemplate(byte[] template) {
        this.template = template;
    }

    byte [] template;
    String userName;

    public UsersData( byte[] data,int len) {
        this.template = Arrays.copyOf(data,len);
    }
}
