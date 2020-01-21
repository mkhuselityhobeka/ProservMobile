package com.twoid.proserv_mobile.services

import com.twoid.proserv_mobile.model.DemographicRegistration
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface DemographicRegistrationService{

    @POST("api/demographics/registration")
    fun addDemographicData(@Body demographicRegistration : DemographicRegistration) : Call<DemographicRegistration>
}