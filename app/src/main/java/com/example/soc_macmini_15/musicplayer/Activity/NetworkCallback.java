package com.example.soc_macmini_15.musicplayer.Activity;

public interface NetworkCallback<T> {
    void onSuccess(T result);     // Called when the response is successful
    void onError(Exception e);    // Called when there is an error
}
