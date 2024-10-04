package com.example.soc_macmini_15.musicplayer.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.soc_macmini_15.musicplayer.Model.Music;

import java.util.ArrayList;

public class FavoritesOperations {

    public static final String TAG = "Favorites Database";

    SQLiteOpenHelper dbHandler;
    SQLiteDatabase database;

    private static final String[] allColumns = {
            FavoritesDBHandler.COLUMN_ID,
            FavoritesDBHandler.COLUMN_TITLE,
            FavoritesDBHandler.COLUMN_SUBTITLE,
            FavoritesDBHandler.COLUMN_FAV,
            FavoritesDBHandler.COLUMN_PATH
    };

    public FavoritesOperations(Context context) {
        dbHandler = new FavoritesDBHandler(context);
    }

    public void open() {
        Log.i(TAG, " Database Opened");
        database = dbHandler.getWritableDatabase();
    }

    public void close() {
        Log.i(TAG, "Database Closed");
        dbHandler.close();
    }

    public void addSongFav(Music music) {
        open();
        // ContentValues: for storing values that contentResolver can process.
        ContentValues values = new ContentValues();
        values.put(FavoritesDBHandler.COLUMN_TITLE, music.getTitle());
        values.put(FavoritesDBHandler.COLUMN_SUBTITLE, music.getSubTitle());
        values.put(FavoritesDBHandler.COLUMN_PATH, music.getPath());
        values.put(FavoritesDBHandler.COLUMN_FAV, music.getFav());

        database.insertWithOnConflict(FavoritesDBHandler.TABLE_SONGS, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        close();
    }

    public ArrayList<Music> getAllFavorites() {
        open();
        Cursor cursor = database.query(FavoritesDBHandler.TABLE_SONGS, allColumns,
                null, null, null, null, null);
        ArrayList<Music> favSongs = new ArrayList<>();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                Music music = new Music(cursor.getString(cursor.getColumnIndex(FavoritesDBHandler.COLUMN_TITLE))
                        , cursor.getString(cursor.getColumnIndex(FavoritesDBHandler.COLUMN_SUBTITLE))
                        , cursor.getString(cursor.getColumnIndex(FavoritesDBHandler.COLUMN_PATH))
                        , cursor.getString(cursor.getColumnIndex(FavoritesDBHandler.COLUMN_FAV)));
                favSongs.add(music);
            }
        }
        close();
        return favSongs;
    }

    public void removeSong(String songPath) {
        open();
        String whereClause =
                FavoritesDBHandler.COLUMN_PATH + "=?";
        String[] whereArgs = new String[]{songPath};

        database.delete(FavoritesDBHandler.TABLE_SONGS, whereClause, whereArgs);
        close();
    }

}






















