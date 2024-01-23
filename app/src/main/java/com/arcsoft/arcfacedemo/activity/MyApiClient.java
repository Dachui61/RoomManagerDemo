package com.arcsoft.arcfacedemo.activity;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MyApiClient {
    private OkHttpClient client;

    public MyApiClient() {
        this.client = new OkHttpClient();
    }

    public String fetchDataFromServer(String apiUrl) {
        Request request = new Request.Builder()
                .url(apiUrl)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                // Handle error
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
