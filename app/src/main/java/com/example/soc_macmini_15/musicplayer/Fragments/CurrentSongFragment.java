package com.example.soc_macmini_15.musicplayer.Fragments;


import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
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

import com.example.soc_macmini_15.musicplayer.Adapter.SongAdapter;
import com.example.soc_macmini_15.musicplayer.Model.SongsList;
import com.example.soc_macmini_15.musicplayer.R;

import java.util.ArrayList;

public class CurrentSongFragment extends ListFragment {


    private static ContentResolver contentResolver1;

    public ArrayList<SongsList> songsList;

    public static ArrayList<SongsList> newList;

    private ListView listView;

    private CurrentSongFragment.createDataParse createDataParse;
    private ContentResolver contentResolver;

    public static Fragment getInstance(int position, ContentResolver mcontentResolver, ArrayList<SongsList> searchResultList) {
        // Store key pair data
        Bundle bundle = new Bundle();
        bundle.putInt("pos", position);
        CurrentSongFragment tabFragment = new CurrentSongFragment();
        tabFragment.setArguments(bundle);
        contentResolver1 = mcontentResolver;
        newList = searchResultList;
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
        createDataParse = (createDataParse) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        listView = view.findViewById(R.id.list_playlist);
        contentResolver = contentResolver1;
        setContent();
    }

    /**
     * Setting the content in the listView and sending the data to the Activity
     */
    public void setContent() {
//        boolean searchedList = true;
//        songsList = new ArrayList<>();
//        newList = new ArrayList<>();
//        getMusic();

        SongAdapter adapter = new SongAdapter(getContext(), newList);
//        if (!createDataParse.queryText().equals("")) {
//            adapter = onQueryTextChange();
        adapter.notifyDataSetChanged();
//            searchedList = true;
//        } else {
//            searchedList = false;
//        }
        createDataParse.getLength(newList.size());
        listView.setAdapter(adapter);

//        final boolean finalSearchedList = searchedList;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Toast.makeText(getContext(), "You clicked :\n" + songsList.get(position), Toast.LENGTH_SHORT).show();
//                if (!finalSearchedList) {
//                    createDataParse.onDataPass(songsList.get(position).getTitle(), songsList.get(position).getPath());
//                    createDataParse.fullSongList(songsList, position);
//                } else {
                createDataParse.onDataPass(newList.get(position).getTitle(), newList.get(position).getPath());
                createDataParse.fullSongList(songsList, position);
//                }
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


    public void getMusic() {

        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//        Uri songUri = MediaStore.Audio.Media.getContentUriForPath("/storage/emulated/0/Music");
        // point to a single row in result fetched by the query
        Cursor songCursor = contentResolver.query(songUri, null, null, null, null);
        // songCursor.moveToFirst() : move cursor to first row
        if (songCursor != null && songCursor.moveToFirst()) {
            int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int songPath = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA);

            do {
                songsList.add(new SongsList(songCursor.getString(songTitle), songCursor.getString(songArtist), songCursor.getString(songPath)));
            } while (songCursor.moveToNext());
            songCursor.close();
        }
    }

    public SongAdapter onQueryTextChange() {
        String text = createDataParse.queryText();
        for (SongsList songs : songsList) {
            String title = songs.getTitle().toLowerCase();
            if (title.contains(text)) {
                newList.add(songs);
            }
        }
        return new SongAdapter(getContext(), newList);

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
                        createDataParse.currentSong(songsList.get(position));
                        setContent();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public interface createDataParse {
        public void onDataPass(String name, String path);

        public void fullSongList(ArrayList<SongsList> songList, int position);

        public String queryText();

        public void currentSong(SongsList songsList);

        public void getLength(int length);
        public SongsList getSong();

        public boolean getPlaylistFlag();
    }


}
