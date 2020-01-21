package com.twoid.proserv_mobile.serviceBuilder

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object  ServiceBuilder{


    private const val URL = "http://169.254.208.79:8091/"
    private val logger = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    private  val okhttp = OkHttpClient.Builder().addInterceptor(logger)

    // create retrofit builder
    private  val retrofitBuilder = Retrofit.Builder().baseUrl(URL)
                                    .addConverterFactory(GsonConverterFactory.create())
                                    .client(okhttp.build())
  // retrofrit instance
    private val retrofit = retrofitBuilder.build()



    fun <T> buildService(serviceType: Class<T>): T {
        return retrofit.create(serviceType)
    }
}