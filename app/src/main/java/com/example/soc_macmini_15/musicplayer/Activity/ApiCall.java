package com.example.soc_macmini_15.musicplayer.Activity;


import android.content.Context;
import android.os.AsyncTask;
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

public class ApiCall extends AsyncTask<String, Void, ArrayList<Music>> {

    private Context context;
    private createDataParse createDataParse;
    private FavoritesOperations favoritesOperations;

    public ApiCall(Context context) {
        this.context = context;
        createDataParse = (createDataParse) this.context;
        favoritesOperations = new FavoritesOperations(context);
    }


    public ArrayList<Music> searchResultList;

    private String query = "";

    private String accessToken = "";

    public ArrayList<Music> getSearchResultList() {
        return searchResultList;
    }

    public void setSearchResultList(ArrayList<Music> searchResultList) {
        this.searchResultList = searchResultList;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    protected ArrayList<Music> doInBackground(String... strings) {

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        // TODO make to token call only after token has expired
        ApiService apiServiceForToken = ApiClient.getAccessToken().create(ApiService.class);
        final String TAG = "MainActivity";

        String type = context.getString(R.string.type);
        String offset = context.getString(R.string.offset);
        String limit = context.getString(R.string.limit);

        String grantType = context.getString(R.string.grant_type);
        String clientId = context.getString(R.string.client_id);
        String clientSecret = context.getString(R.string.client_secret);

        Call<JsonObject> tokenJsonObject = apiServiceForToken.getToken(grantType, clientId, clientSecret);

        tokenJsonObject.enqueue(new Callback<JsonObject>() {

            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                accessToken = "Bearer " + response.body().get("access_token").getAsString();
                playTrack();
            }

            private void playTrack() {
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


//                            String preview_url = response.body().getAsJsonObject("tracks").getAsJsonArray("items")
//                                    .get(0).getAsJsonObject().get("preview_url").getAsString();
//                            MediaPlayer mediaPlayer = new MediaPlayer();
//                            try {
//                                mediaPlayer.setDataSource(preview_url);
//                                mediaPlayer.prepare();
//                            } catch (IOException e) {
//                                throw new RuntimeException(e);
//                            }
//
//                            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                                @Override
//                                public void onPrepared(MediaPlayer mp) {
//                                    mp.start(); // Start playback when prepared
//                                    try {
//                                        mp.start();
//                                        Thread.sleep(5000);
//                                        mp.stop();
//                                    } catch (InterruptedException e) {
//                                        throw new RuntimeException(e);
//                                    }
//                                }
//                            });
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

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });


        return searchResultList;
    }

    //TODO is this the only way to pass variables across the Activities
    public interface createDataParse {
        public void setPagerLayout(ArrayList<Music> searchResultList);

        public void setViewPager(int position);

    }


}
