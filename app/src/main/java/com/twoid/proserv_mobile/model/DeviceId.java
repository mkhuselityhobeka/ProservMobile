package com.twoid.proserv_mobile.model;

import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeviceId implements Callback<DeviceId> {

    private String DeviceSN;

    public DeviceId(){

    }
    public DeviceId (String DeviceSN) {
     //   this.deviceSN = deviceSn;
        this.DeviceSN = DeviceSN;
    }


//    public String setDeviceSn(String deviceSN) {
//        this.deviceSN = deviceSN;
//        return deviceSN;
//    }

    @Override
    public String toString() {
        return "DeviceId{" +
                "DeviceSN='" + DeviceSN + '\'' +
                '}';
    }

  //  private String deviceSN;

    public String getDeviceSN() {
        return DeviceSN;
    }

    public String setDeviceSN(String DeviceSN) {
        this.DeviceSN = DeviceSN;
        return DeviceSN;
    }


    @Override
    public void onResponse(Call<DeviceId> call, Response<DeviceId> response) {
        if (response.isSuccessful()){

        }
    }

    @Override
    public void onFailure(Call<DeviceId> call, Throwable t) {

    }
}

