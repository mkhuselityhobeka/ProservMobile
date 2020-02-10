package com.twoid.proserv_mobile.services;

import com.twoid.proserv_mobile.model.DeviceId;
import com.twoid.proserv_mobile.model.LearnerStructure;
import com.twoid.proserv_mobile.model.WSQFingerprints;


import org.json.simple.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {

    @Multipart
    @POST("ProServeService.svc/saveLearner")
    Call<LearnerStructure> saveDemographicsToServer(
            @Part("DeviceSN") DeviceId DeviceSN,
            @Part("Learner") LearnerStructure Learner
            );

    @POST("ProServeService.svc/TestDeviceSN")
    Call<DeviceId>deviceSN(@Body DeviceId deviceSN);
}
