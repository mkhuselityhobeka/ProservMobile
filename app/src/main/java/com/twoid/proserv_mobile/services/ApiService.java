package com.twoid.proserv_mobile.services;

import com.twoid.proserv_mobile.model.UserRegistration;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("api/demographics/registration")
    Call<UserRegistration> saveDemographicsToServer(@Body UserRegistration userRegistration);
}
