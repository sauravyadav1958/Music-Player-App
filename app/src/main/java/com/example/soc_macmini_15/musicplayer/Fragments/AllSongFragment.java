package com.example.soc_macmini_15.musicplayer.Fragments;


import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;

import com.example.soc_macmini_15.musicplayer.Adapter.MusicAdapter;
import com.example.soc_macmini_15.musicplayer.DB.FavoritesOperations;
import com.example.soc_macmini_15.musicplayer.Model.Music;
import com.example.soc_macmini_15.musicplayer.R;
import com.example.soc_macmini_15.musicplayer.Activity.CommonResourceInterface;

import java.util.ArrayList;
import java.util.Optional;

public class AllSongFragment extends ListFragment {

    private FavoritesOperations favoritesOperations;

    public ArrayList<Music> offlineMusicList;
    public ArrayList<Music> filteredOfflineMusicList;

    private ListView listView;

    private CommonResourceInterface CommonResourceInterface;
    private ContentResolver contentResolver;
    private boolean searchedFilter = false;

    public static Fragment getInstance(int position) {
        // Store key pair data
        // pos is passed just in case we need the position information.
        Bundle bundle = new Bundle();
        bundle.putInt("pos", position);
        AllSongFragment tabFragment = new AllSongFragment();
        tabFragment.setArguments(bundle);
        return tabFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    // Context: It allows us to information about application/activity and access to it's resources.
    // Eg : Room-service person of a hotel.
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        CommonResourceInterface = (CommonResourceInterface) context;
        favoritesOperations = new FavoritesOperations(context);
    }
    // ViewGroup: contain other children views eg : TextView, etc.
    // subclasses of ViewGroup: LinearLayout, RelativeLayout, ConstraintLayout, FrameLayout, and RecyclerView.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // create a layout from an XML resource file into view object.
        return inflater.inflate(R.layout.fragment_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        listView = view.findViewById(R.id.list_playlist);
        contentResolver = CommonResourceInterface.getContentResolverMain();
        setSongsInListView();
    }

    /**
     * Setting the content in the listView and sending the data to the Activity
     */
    public void setSongsInListView() {
        searchedFilter = false;
        offlineMusicList = new ArrayList<>();
        filteredOfflineMusicList = new ArrayList<>();
        // populate songsList by getting all the audio file from local storage
        setOfflineMusicList();
        // adapter of offlineMusicList for listView
        MusicAdapter adapter = new MusicAdapter(getContext(), offlineMusicList);
        if (!CommonResourceInterface.queryTextToLowerCase().equals("")) {
            // populate newList
            adapter = setFilteredOfflineMusicList();
            adapter.notifyDataSetChanged();
            searchedFilter = true;
        } else {
            searchedFilter = false;
        }
        listView.setAdapter(adapter);

        final boolean finalSearchedList = searchedFilter;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!finalSearchedList) {
                    CommonResourceInterface.pickMusicAndPlay(offlineMusicList.get(position).getTitle(), offlineMusicList.get(position).getPath(), offlineMusicList.get(position).getFav());
                    CommonResourceInterface.setCurrentSong(offlineMusicList.get(position));
                    // we update musicList in MainActivity to get the musicList of current tab
                    // so that we can perform operations on the correct musicList.
                    CommonResourceInterface.fullSongList(offlineMusicList, position);
                } else {
                    CommonResourceInterface.pickMusicAndPlay(filteredOfflineMusicList.get(position).getTitle(), filteredOfflineMusicList.get(position).getPath(), filteredOfflineMusicList.get(position).getFav());
                    CommonResourceInterface.setCurrentSong(filteredOfflineMusicList.get(position));
                    CommonResourceInterface.fullSongList(filteredOfflineMusicList, position);
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showDialog(position);
                return true;
            }
        });
    }


    public void setOfflineMusicList() {

        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        // point to a single row in result fetched by the query
        Cursor musicCursor = contentResolver.query(musicUri, null, null, null, null);
        ArrayList<Music> favMusicList = favoritesOperations.getAllFavorites();
        // musicCursor.moveToFirst() : move cursor to first row
        if (musicCursor != null && musicCursor.moveToFirst()) {
            int musicTitle = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int musicArtist = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int musicPath = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int musicFav = musicCursor.getColumnIndex(MediaStore.Audio.Media.IS_FAVORITE);
            String fav = musicCursor.getString(musicFav);
            String path = musicCursor.getString(musicPath);
            Optional<Music> favMusic = favMusicList.stream().filter(music -> music.getPath().equals(path)).findFirst();
            if (favMusic.isPresent()) {
                fav = "1";
            }

            do {
                offlineMusicList.add(new Music(musicCursor.getString(musicTitle), musicCursor.getString(musicArtist), path, fav));
            } while (musicCursor.moveToNext());
            musicCursor.close();
        }
    }

    public MusicAdapter setFilteredOfflineMusicList() {
        String text = CommonResourceInterface.queryTextToLowerCase();
        for (Music music : offlineMusicList) {
            String title = music.getTitle().toLowerCase();
            if (title.contains(text)) {
                filteredOfflineMusicList.add(music);
            }
        }
        return new MusicAdapter(getContext(), filteredOfflineMusicList);

    }

    private void showDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(getString(R.string.play_next))
                .setCancelable(true)
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MediaPlayer mediaPlayer = CommonResourceInterface.getMediaPlayer();
                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                if (searchedFilter) {
                                    CommonResourceInterface.pickMusicAndPlay(filteredOfflineMusicList.get(position).getTitle(),
                                            filteredOfflineMusicList.get(position).getPath(), filteredOfflineMusicList.get(position).getFav());
                                    CommonResourceInterface.fullSongList(filteredOfflineMusicList, position);
                                } else {
                                    CommonResourceInterface.pickMusicAndPlay(offlineMusicList.get(position).getTitle(),
                                            offlineMusicList.get(position).getPath(), offlineMusicList.get(position).getFav());
                                    CommonResourceInterface.fullSongList(offlineMusicList, position);
                                }
                            }
                        });
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


}
