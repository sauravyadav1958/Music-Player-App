package com.example.soc_macmini_15.musicplayer.Fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
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
import com.example.soc_macmini_15.musicplayer.Model.Music;
import com.example.soc_macmini_15.musicplayer.R;
import com.example.soc_macmini_15.musicplayer.Activity.CommonResourceInterface;

import java.util.ArrayList;

public class CurrentSongFragment extends ListFragment {

    public static ArrayList<Music> onlineSearchMusicList;

    private ListView listView;

    private CommonResourceInterface CommonResourceInterface;

    public static Fragment getInstance(int position, ArrayList<Music> searchResultList) {
        // Store key pair data
        Bundle bundle = new Bundle();
        bundle.putInt("pos", position);
        CurrentSongFragment tabFragment = new CurrentSongFragment();
        tabFragment.setArguments(bundle);
        onlineSearchMusicList = searchResultList;
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        listView = view.findViewById(R.id.list_playlist);
        setSongsInListView();
    }

    /**
     * Setting the content in the listView and sending the data to the Activity
     */
    public void setSongsInListView() {

        MusicAdapter adapter = new MusicAdapter(getContext(), onlineSearchMusicList);
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CommonResourceInterface.pickMusicAndPlay(onlineSearchMusicList.get(position).getTitle(), onlineSearchMusicList.get(position).getPath(), onlineSearchMusicList.get(position).getFav());
                CommonResourceInterface.fullSongList(onlineSearchMusicList, position);
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
                                CommonResourceInterface.pickMusicAndPlay(onlineSearchMusicList.get(position).getTitle(),
                                        onlineSearchMusicList.get(position).getPath(), onlineSearchMusicList.get(position).getFav());
                                CommonResourceInterface.fullSongList(onlineSearchMusicList, position);
                            }
                        });
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }



}
