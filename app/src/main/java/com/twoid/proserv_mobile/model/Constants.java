package com.twoid.proserv_mobile.model;

public class Constants {

    private static String SYNC_STATUS_OK = "1";
    private static String SYNC_STATUS_FAIL = "0";

    public static String getSyncStatusOk() {
        return SYNC_STATUS_OK;
    }

    public static void setSyncStatusOk(String syncStatusOk) {
        SYNC_STATUS_OK = syncStatusOk;
    }

    public static String getSyncStatusFail() {
        return SYNC_STATUS_FAIL;
    }

    public static void setSyncStatusFail(String syncStatusFail) {
        SYNC_STATUS_FAIL = syncStatusFail;
    }

    @Override
    public String toString() {
        return "Constants{}";
    }

    public Constants() {
    }
}
