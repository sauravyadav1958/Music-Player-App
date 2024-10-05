package com.example.soc_macmini_15.musicplayer.Activity;


import android.content.Context;
import android.util.Log;

import com.example.soc_macmini_15.musicplayer.DB.FavoritesOperations;
import com.example.soc_macmini_15.musicplayer.Fragments.CurrentSongFragment;
import com.example.soc_macmini_15.musicplayer.Model.Music;
import com.example.soc_macmini_15.musicplayer.R;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Optional;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MusicSearchApi {

    private Context context;
    private createDataParse createDataParse;
    private FavoritesOperations favoritesOperations;

    public MusicSearchApi(Context context) {
        this.context = context;
        // casting MainActivity context to interface.
        // This will help in accessing the functions of the interface implemented in MainActivity.
        createDataParse = (createDataParse) this.context;
        favoritesOperations = new FavoritesOperations(context);
    }

    public ArrayList<Music> searchResultList;


    protected void searchMusicAsPerQuery(String accessToken, String query) {

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        final String TAG = "MusicSearchApi";

        String type = context.getString(R.string.type);
        String offset = context.getString(R.string.offset);
        String limit = context.getString(R.string.limit);

        Call<JsonObject> trackJsonObject = apiService.getTracks(accessToken, query, type, offset, limit);
        trackJsonObject.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonArray tracks = response.body().getAsJsonObject("tracks").getAsJsonArray("items");
                    // Handle the response, e.g., update the UI
                    searchResultList = new ArrayList<>();
                    ArrayList<Music> favSongList = favoritesOperations.getAllFavorites();
                    for (JsonElement track : tracks) {
                        String name = track.getAsJsonObject().get("name").getAsString();
                        String id = track.getAsJsonObject().get("id").getAsString();
                        JsonElement url = track.getAsJsonObject().get("preview_url");
                        if (url.isJsonNull()) {
                            continue;
                        }
                        String preview_url = url.getAsString();
                        String fav = "0";
                        Optional<Music> favSong = favSongList.stream().filter(songsList -> songsList.getPath().equals(preview_url)).findFirst();
                        if (favSong.isPresent()) {
                            fav = "1";
                        }
                        searchResultList.add(new Music(name, id, preview_url, fav));
                    }
                    CurrentSongFragment.onlineSearchMusicList = searchResultList;
                    createDataParse.setPagerLayout(searchResultList);
                    createDataParse.setViewPager(1);

                } else {
                    // Handle the error
                    Log.e(TAG, "Request failed with code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                // Handle the failure
                Log.e(TAG, "API call failed: " + t.getMessage());
            }
        });
    }

    //TODO is this the only way to pass variables across the Activities
    public interface createDataParse {
        public void setPagerLayout(ArrayList<Music> searchResultList);

        public void setViewPager(int position);

    }


}
