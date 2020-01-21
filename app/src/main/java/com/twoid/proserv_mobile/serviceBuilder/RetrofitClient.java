package com.twoid.proserv_mobile.serviceBuilder;


import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    //retrofit instance
    private static Retrofit retrofit = null;

    //retrofit builder method returns retrofit instance
    public static Retrofit getClient(String baseUrl){

        if (retrofit == null){
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }


}
