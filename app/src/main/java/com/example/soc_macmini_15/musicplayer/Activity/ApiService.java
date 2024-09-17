package com.example.soc_macmini_15.musicplayer.Activity;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    @GET("v1/search?")
    Call<JsonObject> getTracks(@Header("Authorization") String accessToken, @Query("query") String query, @Query("type") String type,
                               @Query("offset") String offset, @Query("limit") String limit);
    @FormUrlEncoded
    @POST("api/token")
    Call<JsonObject> getToken(@Field("grant_type") String grantType,
                              @Field("client_id") String clientId, @Field("client_secret") String clientSecret);
}
