package com.arcsoft.arcfacedemo.util;

import com.arcsoft.arcfacedemo.API.ApiService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "https://your-api-base-url/";

    private static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public static ApiService getApiService() {
        return retrofit.create(ApiService.class);
    }
}
