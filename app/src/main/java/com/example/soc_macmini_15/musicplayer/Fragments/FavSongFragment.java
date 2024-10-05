package com.example.soc_macmini_15.musicplayer.Fragments;


import android.content.Context;
import android.content.DialogInterface;
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
import com.example.soc_macmini_15.musicplayer.DB.FavoritesOperations;
import com.example.soc_macmini_15.musicplayer.Model.Music;
import com.example.soc_macmini_15.musicplayer.R;
import com.example.soc_macmini_15.musicplayer.Activity.CommonResourceInterface;

import java.util.ArrayList;

public class FavSongFragment extends ListFragment {

    private FavoritesOperations favoritesOperations;


    public ArrayList<Music> favouriteMusicList;
    public ArrayList<Music> filteredFavouriteMusicList;

    private ListView listView;

    private CommonResourceInterface CommonResourceInterface;
    private boolean searchedFilter = false;

    public static Fragment getInstance(int position) {
        Bundle bundle = new Bundle();
        bundle.putInt("pos", position);
        FavSongFragment tabFragment = new FavSongFragment();
        tabFragment.setArguments(bundle);
        return tabFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        CommonResourceInterface = (CommonResourceInterface) context;
        favoritesOperations = new FavoritesOperations(context);
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
        searchedFilter = false;
        favouriteMusicList = new ArrayList<>();
        filteredFavouriteMusicList = new ArrayList<>();
        favouriteMusicList = favoritesOperations.getAllFavorites();
        MusicAdapter adapter = new MusicAdapter(getContext(), favouriteMusicList);
        if (!CommonResourceInterface.queryTextToLowerCase().equals("")) {
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
                    CommonResourceInterface.pickMusicAndPlay(favouriteMusicList.get(position).getTitle(), favouriteMusicList.get(position).getPath(), favouriteMusicList.get(position).getFav());
                    CommonResourceInterface.fullSongList(favouriteMusicList, position);
                } else {
                    CommonResourceInterface.pickMusicAndPlay(filteredFavouriteMusicList.get(position).getTitle(), filteredFavouriteMusicList.get(position).getPath(), favouriteMusicList.get(position).getFav());
                    CommonResourceInterface.fullSongList(favouriteMusicList, position);
                }
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                deleteOption(position);
                return true;
            }
        });
    }

    private void deleteOption(int position) {
        if (searchedFilter) {
            showDialog(filteredFavouriteMusicList.get(position).getPath(), position);
        } else {
            showDialog(favouriteMusicList.get(position).getPath(), position);
        }
    }

    public MusicAdapter setFilteredOfflineMusicList() {
        String text = CommonResourceInterface.queryTextToLowerCase();
        for (Music music : favouriteMusicList) {
            String title = music.getTitle().toLowerCase();
            if (title.contains(text)) {
                filteredFavouriteMusicList.add(music);
            }
        }
        return new MusicAdapter(getContext(), filteredFavouriteMusicList);

    }

    private void showDialog(final String index, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.delete))
                .setMessage(getString(R.string.delete_text))
                .setCancelable(true)
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        favoritesOperations.removeSong(index);
                        if (searchedFilter) {
                            CommonResourceInterface.fullSongList(filteredFavouriteMusicList, position);
                        } else {
                            CommonResourceInterface.fullSongList(favouriteMusicList, position);
                        }
                        setSongsInListView();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}
