package com.example.soc_macmini_15.musicplayer.Adapter;

import android.content.ContentResolver;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.soc_macmini_15.musicplayer.Fragments.AllSongFragment;
import com.example.soc_macmini_15.musicplayer.Fragments.CurrentSongFragment;
import com.example.soc_macmini_15.musicplayer.Fragments.FavSongFragment;
import com.example.soc_macmini_15.musicplayer.Model.SongsList;

import java.util.ArrayList;
import java.util.List;

// FragmentPagerAdapter: flip left and right through pages of data (number of pages fixed)
// FragmentStatePagerAdapter: flip left and right through pages of data (number of pages are dynamic)
public class ViewPagerAdapter extends FragmentPagerAdapter {
    // Provides application access to data.
    private ContentResolver contentResolver;
    private String title[] = {"OFFLINE SONGS", "SEARCH SONGS", "FAVORITES"};
    ArrayList<SongsList> searchResultList;

    public ViewPagerAdapter(FragmentManager fm, ContentResolver contentResolver, ArrayList<SongsList> searchResultList) {
        super(fm);
        this.contentResolver = contentResolver;
        this.searchResultList = searchResultList;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return AllSongFragment.getInstance(position, contentResolver);
            case 1:
                return CurrentSongFragment.getInstance(position, contentResolver, searchResultList);
            case 2:
                return FavSongFragment.getInstance(position);
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return title.length;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return title[position];
    }
}
