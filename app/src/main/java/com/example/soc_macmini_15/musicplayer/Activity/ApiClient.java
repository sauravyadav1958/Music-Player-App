package com.example.soc_macmini_15.musicplayer.Activity;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "https://api.spotify.com/"; // Base URL of the API

    private static final String BASE_URL_FOR_TOKEN = "https://accounts.spotify.com/";
    private static Retrofit retrofit = null;
    private static Retrofit retrofitToken = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
    public static Retrofit getAccessToken() {
        if (retrofitToken == null) {
            retrofitToken = new Retrofit.Builder()
                    .baseUrl(BASE_URL_FOR_TOKEN)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitToken;
    }
}
