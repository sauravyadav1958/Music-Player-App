package com.example.soc_macmini_15.musicplayer.Activity;

import android.content.Context;
import android.util.Log;

import com.example.soc_macmini_15.musicplayer.R;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccessTokenApi {
    private Context context;
    public AccessTokenApi(Context context) {
        this.context = context;
    }
    // TAG has className, helps in identifying class source of the log.
    final String TAG = "AccessTokenApi";

    ApiService apiServiceForToken = ApiClient.getAccessToken().create(ApiService.class);

    public void getAccessToken(final NetworkCallback<JsonObject> callback) {
        String grantType = context.getString(R.string.grant_type);
        String clientId = context.getString(R.string.client_id);
        String clientSecret = context.getString(R.string.client_secret);
        Call<JsonObject> tokenJsonObject = apiServiceForToken.getToken(grantType, clientId, clientSecret);
        tokenJsonObject.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage());
                callback.onError(new Exception(t));
            }
        });
    }

}
