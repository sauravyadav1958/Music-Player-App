package com.example.soc_macmini_15.musicplayer.Activity;

import android.content.ContentResolver;
import android.media.MediaPlayer;

import com.example.soc_macmini_15.musicplayer.Model.Music;

import java.util.ArrayList;

//TODO is this the only way to pass variables across the Activities
public interface CommonResourceInterface {
    public void fullSongList(ArrayList<Music> songList, int position);

    public String queryTextToLowerCase();

    public void pickMusicAndPlay(String name, String path, String fav);

    public void setCurrentSong(Music music);
    public ContentResolver getContentResolverMain();

    public Music getCurrentSong();

    public MediaPlayer getMediaPlayer();

    public Music getSong();

    public boolean getPlaylistFlag();
    public int getPosition();
    public void setPagerLayout(ArrayList<Music> searchResultList);

    public void setViewPager(int position);
}
