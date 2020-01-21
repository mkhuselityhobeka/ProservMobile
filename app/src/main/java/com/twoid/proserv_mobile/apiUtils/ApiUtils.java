package com.twoid.proserv_mobile.apiUtils;

import com.twoid.proserv_mobile.serviceBuilder.RetrofitClient;
import com.twoid.proserv_mobile.services.ApiService;

public class ApiUtils {

    //api url
    private static final String BASE_URL = "http://169.254.208.79:8091/" ;

    public ApiUtils(){ }


    public static ApiService getApiService(){

        return RetrofitClient.getClient(BASE_URL).create(ApiService.class);
    }

}
