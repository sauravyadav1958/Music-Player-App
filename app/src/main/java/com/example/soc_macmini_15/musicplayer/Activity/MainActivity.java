package com.example.soc_macmini_15.musicplayer.Activity;


import android.Manifest;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.example.soc_macmini_15.musicplayer.Adapter.ViewPagerAdapter;
import com.example.soc_macmini_15.musicplayer.DB.FavoritesOperations;
import com.example.soc_macmini_15.musicplayer.Fragments.AllSongFragment;
import com.example.soc_macmini_15.musicplayer.Fragments.CurrentSongFragment;
import com.example.soc_macmini_15.musicplayer.Fragments.FavSongFragment;
import com.example.soc_macmini_15.musicplayer.Model.Music;
import com.example.soc_macmini_15.musicplayer.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AllSongFragment.createDataParse, FavSongFragment.createDataParsed, CurrentSongFragment.createDataParse, MusicSearchApi.createDataParse {

    private Menu menu;

    private ImageButton imgBtnPlayPause, imgbtnReplay, imgBtnPrev, imgBtnNext, imgBtnPlaylist;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private SeekBar seekbarController;
    private DrawerLayout mDrawerLayout;
    private TextView tvCurrentTime, tvTotalTime;
    private String accessToken = "";

    private ArrayList<Music> musicList;
    private int currentPosition;
    private String searchText = "";
    private Music currSong;
    private Date tokenExpiryDate;

    private boolean musicSelectedFlag = false, repeatFlag = false, playlistFlag = false, favFlag = true;
    private final int MY_PERMISSION_REQUEST = 100;

    MediaPlayer mediaPlayer;
    // send and process message/runnable to the thread's messageQueue.
    Handler handler;
    // interface that represents a task that can be executed by a thread in background.
    Runnable runnable;
    private ActivityResultLauncher<Intent> storageActivityResultLauncher;
//    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // result after granting permission to access storage for android 11 and above.
        storageActivityResultLauncherResult();

//        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//        StrictMode.setThreadPolicy(policy);


//        permissionLauncher = registerForActivityResult(
//                new ActivityResultContracts.RequestPermission(),
//                new ActivityResultCallback<Boolean>() {
//                    @Override
//                    public void onActivityResult(Boolean isGranted) {
//                        if (isGranted) {
//                            // Permission granted, start the foreground service
//                            startForegroundService();
//                        } else {
//                            // Permission denied, handle the case
//                            Toast.makeText(MainActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });

        init();
        if (checkStoragePermissions()) {
            setPagerLayout(new ArrayList<>());
        } else {
            requestPermission();
        }


    }

    private void storageActivityResultLauncherResult() {
        storageActivityResultLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        new ActivityResultCallback<ActivityResult>() {
                            //
                            @Override
                            public void onActivityResult(ActivityResult o) {
                                if (checkStoragePermissions()) {
                                    Toast.makeText(MainActivity.this, "Storage Permissions Granted", Toast.LENGTH_SHORT).show();
                                    setPagerLayout(new ArrayList<>());
                                } else {
                                    Toast.makeText(MainActivity.this, "Storage Permissions Denied", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
    }

//    private void startForegroundService() {
//        Intent serviceIntent = new Intent(this, MyForegroundService.class);
//        ContextCompat.startForegroundService(this, serviceIntent);
//    }

    /**
     * Initialising the views
     */

    private void init() {
        imgBtnPrev = findViewById(R.id.img_btn_previous);
        imgBtnNext = findViewById(R.id.img_btn_next);
        imgbtnReplay = findViewById(R.id.img_btn_replay);
        imgBtnPlaylist = findViewById(R.id.img_btn_playlist);
        imgBtnPlaylist.setVisibility(View.INVISIBLE);

        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvTotalTime = findViewById(R.id.tv_total_time);
        FloatingActionButton refreshSongs = findViewById(R.id.btn_refresh);
        seekbarController = findViewById(R.id.seekbar_controller);
        viewPager = findViewById(R.id.songs_viewpager);
        NavigationView navigationView = findViewById(R.id.nav_view);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        // commented actionBarDrawerToggle since we are manually opening/closing drawer hence this is not required
//        // ActionBarDrawerToggle set the toggle(open/close switch(Hamburger <-> <-- )) for opening/closing drawer
//        // R.string.nav_open/R.string.nav_close for action accessibility purpose
//        actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.nav_open, R.string.nav_close);
//        // listen for toggle(open/close switch(Hamburger <-> <-- )) state changes.
//        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);
//        // update the state of the toggle(open/close switch(Hamburger <-> <-- )) with the drawer
//        actionBarDrawerToggle.syncState();
        Toolbar toolbar = findViewById(R.id.toolbar);
        // to make the Navigation drawer icon always appear on the action bar
        toolbar.setTitleTextColor(getResources().getColor(R.color.text_color));
        // set toolBar as ActionBar
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        // showing the back button in action bar
        actionBar.setDisplayHomeAsUpEnabled(true);
        // set home button icon, replacing the back button.
        actionBar.setHomeAsUpIndicator(R.drawable.menu_icon);

        imgBtnPlayPause = findViewById(R.id.img_btn_play);

        handler = new Handler();
        mediaPlayer = new MediaPlayer();

        imgBtnNext.setOnClickListener(this);
        imgBtnPrev.setOnClickListener(this);
        imgbtnReplay.setOnClickListener(this);
        refreshSongs.setOnClickListener(this);
        imgBtnPlayPause.setOnClickListener(this);
        imgBtnPlaylist.setOnClickListener(this);


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                item.setChecked(true);
                mDrawerLayout.closeDrawers();
                switch (item.getItemId()) {
                    case R.id.nav_about:
                        about();
                        break;
                }
                return true;
            }
        });
    }


    private boolean isAndroid_11_OrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R;
    }

    private boolean checkStoragePermissions() {

        if (isAndroid_11_OrAbove()) {
            //Android is 11 (R) or above
            return Environment.isExternalStorageManager();

        } else {
            //Below android 11
            int write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

            return read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED;
        }
    }

    /**
     * Function to ask user to grant the permission.
     */

    private void requestPermission() {
        if (isAndroid_11_OrAbove()) {
            //Android is 11 (R) or above
            try {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                intent.setData(uri);
                storageActivityResultLauncher.launch(intent);
//                ActivityCompat.requestPermissions(MainActivity.this,
//                        new String[]{Manifest.permission.FOREGROUND_SERVICE_SPECIAL_USE}, MY_PERMISSION_REQUEST);
//                // Request the permission at runtime
//                permissionLauncher.launch(Manifest.permission.FOREGROUND_SERVICE_SPECIAL_USE);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                storageActivityResultLauncher.launch(intent);
            }
        } else {
            //Below android 11
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            // To check whether Rationale UI can be shown or not.
            // Rationale UI shows why a certain permission is required.
            // Use this if the person denies permission at first time.
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            } else {
                if (!checkStoragePermissions()) {
                    // Shows a message
                    Snackbar snackbar = Snackbar.make(mDrawerLayout, "Provide the Storage Permission", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        }
    }


    /**
     * Checking if the permission is granted or not
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                        setPagerLayout(new ArrayList<>());
                    } else {
                        Snackbar snackbar = Snackbar.make(mDrawerLayout, "Provide the Storage Permission", Snackbar.LENGTH_LONG);
                        snackbar.show();
                        finish();
                    }
                }
        }
    }

    /**
     * Setting up the tab layout with the viewpager in it.
     */

    public void setPagerLayout(ArrayList<Music> searchResultList) {
        // contentResolver is present in Activity class. We can do getActivity().getContentResolver() in Fragments.
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), getContentResolver(), searchResultList);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tabLayout = findViewById(R.id.tabs);
        // spread the tab heading across the screen
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                if (tab.getPosition() == 0 || tab.getPosition() == 1) {
                    imgBtnPlaylist.setVisibility(View.INVISIBLE);
                } else {
                    imgBtnPlaylist.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    public void setViewPager(int position) {
        viewPager.setCurrentItem(position);
    }

    /**
     * Function to show the dialog for about us.
     */
    private void about() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.about))
                .setMessage(getString(R.string.about_text))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        // inflate(create) : create a menu from an XML resource file into a Menu object
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
        MusicSearchApi musicSearchApi = new MusicSearchApi(this);
        AccessTokenApi accessTokenApi = new AccessTokenApi(this);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Storing token expiryDate in code only as of now.
                int currentItem = viewPager.getCurrentItem();
                if (currentItem == 1) {
                    if (tokenExpiryDate == null || tokenExpiryDate.before(new Date())) {
                        accessTokenApi.getAccessToken(new NetworkCallback<JsonObject>() {
                            @Override
                            public void onSuccess(JsonObject result) {
                                tokenExpiryDate = new Date(System.currentTimeMillis() + 60000 * 59);
                                accessToken = "Bearer " + result.get("access_token").getAsString();
                                musicSearchApi.searchMusicAsPerQuery(accessToken, query);
                            }

                            @Override
                            public void onError(Exception e) {
                                // Handle the error
                                Log.e("RetrofitError", "Error: " + e.getMessage());
                            }
                        });
                    } else {
                        musicSearchApi.searchMusicAsPerQuery(accessToken, query);
                    }
                    return true;
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchText = newText;
                queryText();
                int currentItem = viewPager.getCurrentItem();
                setPagerLayout(new ArrayList<>());
                if (currentItem == 1) {
                    setViewPager(1);
                } else if (currentItem == 2) {
                    setViewPager(2);
                }
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    // called when Toolbar/Menu Icons are clicked.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // checks if the clicked item corresponds to the ActionBarDrawerToggle
        // (i.e., if the hamburger/arrow icon was clicked and open/close the drawer depending on it's current state.
        // and returns true for confirmation.

        // This is not required since there are other icons as well which has diff functionality which we are handling below.
        // We could have used this if only opening/closing of drawer was there.

//        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
//            return true;
//        }
        switch (item.getItemId()) {
            // home is the hamburger Icon
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.menu_search:
                Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_favorites:
                if (musicSelectedFlag)
                    if (mediaPlayer != null) {
                        if (!favFlag) {
                            Toast.makeText(this, "Added to Favorites", Toast.LENGTH_SHORT).show();
                            item.setIcon(R.drawable.ic_favorite_filled);
                            musicList.get(currentPosition).setFav("1");
                            Music favList = new Music(musicList.get(currentPosition).getTitle(),
                                    musicList.get(currentPosition).getSubTitle(), musicList.get(currentPosition).getPath(),
                                    musicList.get(currentPosition).getFav());
                            FavoritesOperations favoritesOperations = new FavoritesOperations(this);
                            favoritesOperations.addSongFav(favList);
                            int currentItem = viewPager.getCurrentItem();
                            setPagerLayout(new ArrayList<>());
                            setViewPager(currentItem);
                            favFlag = true;
                        } else {
                            item.setIcon(R.drawable.favorite_icon);
                            musicList.get(currentPosition).setFav("0");
                            String songPath = musicList.get(currentPosition).getPath();
                            FavoritesOperations favoritesOperations = new FavoritesOperations(this);
                            favoritesOperations.removeSong(songPath);
                            int currentItem = viewPager.getCurrentItem();
                            setPagerLayout(new ArrayList<>());
                            setViewPager(currentItem);
                            favFlag = false;
                        }
                    }
                return true;
        }

        return super.onOptionsItemSelected(item);

    }


    /**
     * Function to handle the click events.
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_btn_play:
                if (musicSelectedFlag) {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        imgBtnPlayPause.setImageResource(R.drawable.play_icon);
                    } else if (!mediaPlayer.isPlaying()) {
                        mediaPlayer.start();
                        imgBtnPlayPause.setImageResource(R.drawable.pause_icon);
                        playCycle();
                    }
                } else {
                    Toast.makeText(this, "Select the Song ..", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_refresh:
                Toast.makeText(this, "Refreshing", Toast.LENGTH_SHORT).show();
                int currentItem = viewPager.getCurrentItem();
                setPagerLayout(new ArrayList<>());
                setViewPager(currentItem);
                break;
            case R.id.img_btn_replay:

                if (repeatFlag) {
                    Toast.makeText(this, "Replaying Removed..", Toast.LENGTH_SHORT).show();
                    mediaPlayer.setLooping(false);
                    Icon icon = Icon.createWithResource(getApplicationContext(), R.drawable.undo_icon);
                    imgbtnReplay.setImageIcon(icon);
                    repeatFlag = false;
                } else {
                    Toast.makeText(this, "Replaying Added..", Toast.LENGTH_SHORT).show();
                    mediaPlayer.setLooping(true);
                    Icon icon = Icon.createWithResource(getApplicationContext(), R.drawable.undo_icon_dark);
                    imgbtnReplay.setImageIcon(icon);
                    repeatFlag = true;
                }
                break;
            case R.id.img_btn_previous:
                if (musicSelectedFlag) {
                    if (mediaPlayer.getCurrentPosition() > 10) {
                        if (currentPosition - 1 > -1) {
                            pickMusicAndPlay(musicList.get(currentPosition - 1).getTitle(), musicList.get(currentPosition - 1).getPath(), musicList.get(currentPosition - 1).getFav());
                            currentPosition = currentPosition - 1;
                        } else {
                            pickMusicAndPlay(musicList.get(currentPosition).getTitle(), musicList.get(currentPosition).getPath(), musicList.get(currentPosition).getFav());
                        }
                    } else {
                        pickMusicAndPlay(musicList.get(currentPosition).getTitle(), musicList.get(currentPosition).getPath(), musicList.get(currentPosition).getFav());
                    }
                } else {
                    Toast.makeText(this, "Select a Song . .", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.img_btn_next:
                if (musicSelectedFlag) {
                    if (currentPosition + 1 < musicList.size()) {
                        pickMusicAndPlay(musicList.get(currentPosition + 1).getTitle(), musicList.get(currentPosition + 1).getPath(), musicList.get(currentPosition + 1).getFav());
                        currentPosition += 1;
                    } else {
                        Toast.makeText(this, "Playlist Ended", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Select the Song ..", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.img_btn_playlist:
                if (!playlistFlag) {
                    playlistFlag = true;
                    Icon icon = Icon.createWithResource(getApplicationContext(), R.drawable.baseline_playlist_play_dark);
                    imgBtnPlaylist.setImageIcon(icon);
                    Toast.makeText(this, "Loop Added", Toast.LENGTH_SHORT).show();
                } else {
                    playlistFlag = false;
                    Icon icon = Icon.createWithResource(getApplicationContext(), R.drawable.baseline_playlist_play);
                    imgBtnPlaylist.setImageIcon(icon);
                    Toast.makeText(this, "Loop Removed", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * Function to attach the song to the music player
     *
     * @param name
     * @param path
     */

    public void pickMusicAndPlay(String name, String path, String fav) {
        imgBtnPlayPause.setImageResource(R.drawable.play_icon);
        // change the title in actionBar
        setTitle(name);
        if (fav.equals("0")) {
            menu.getItem(1).setIcon(R.drawable.favorite_icon);
            favFlag = false;
        } else {
            menu.getItem(1).setIcon(R.drawable.ic_favorite_filled);
            favFlag = true;
        }


        try {
            boolean looping = mediaPlayer.isLooping();


            mediaPlayer.reset();
            mediaPlayer.setDataSource(path);
            mediaPlayer.setLooping(looping);
            Icon icon;
            if (looping) {
                icon = Icon.createWithResource(getApplicationContext(), R.drawable.undo_icon_dark);
            } else {
                icon = Icon.createWithResource(getApplicationContext(), R.drawable.undo_icon);
            }
            imgbtnReplay.setImageIcon(icon);
            if (playlistFlag) {
                icon = Icon.createWithResource(getApplicationContext(), R.drawable.baseline_playlist_play_dark);
            } else {
                icon = Icon.createWithResource(getApplicationContext(), R.drawable.baseline_playlist_play);
            }
            imgBtnPlaylist.setImageIcon(icon);
            mediaPlayer.prepare();
            playAndTrackMusic();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                imgBtnPlayPause.setImageResource(R.drawable.play_icon);
                Music currentMusic = musicList.get(currentPosition);
                if (playlistFlag && currentMusic.getFav().equals("1")) {
                    if (currentPosition + 1 < musicList.size()) {
                        pickMusicAndPlay(musicList.get(currentPosition + 1).getTitle(), musicList.get(currentPosition + 1).getPath(), musicList.get(currentPosition + 1).getFav());
                        currentPosition += 1;
                    } else {
                        Toast.makeText(MainActivity.this, "PlayList Ended", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    /**
     * Function to set the controls according to the song
     */

    private void playAndTrackMusic() {
        seekbarController.setMax(mediaPlayer.getDuration());
        mediaPlayer.start();
        playCycle();
        musicSelectedFlag = true;
        if (mediaPlayer.isPlaying()) {
            imgBtnPlayPause.setImageResource(R.drawable.pause_icon);
            tvTotalTime.setText(getTimeFormatted(mediaPlayer.getDuration()));
        }

        seekbarController.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                    tvCurrentTime.setText(getTimeFormatted(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    /**
     * Function to play the song using a thread
     */
    private void playCycle() {
        try {
            seekbarController.setProgress(mediaPlayer.getCurrentPosition());
            tvCurrentTime.setText(getTimeFormatted(mediaPlayer.getCurrentPosition()));
            if (mediaPlayer.isPlaying()) {
                // creating task to run in background thread.
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        playCycle();

                    }
                };
                // run the runnable in background thread after a delay of 100 milliseconds.
                handler.postDelayed(runnable, 100);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getTimeFormatted(long milliSeconds) {
        String finalTimerString = "";
        String secondsString;

        //Converting total duration into time
        int hours = (int) (milliSeconds / 3600000);
        int minutes = (int) (milliSeconds % 3600000) / 60000;
        int seconds = (int) ((milliSeconds % 3600000) % 60000 / 1000);

        // Adding hours if any
        if (hours > 0)
            finalTimerString = hours + ":";

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10)
            secondsString = "0" + seconds;
        else
            secondsString = "" + seconds;

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // Return timer String;
        return finalTimerString;
    }

    @Override
    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    @Override
    public void fullSongList(ArrayList<Music> musicList, int position) {
        this.musicList = musicList;
        this.currentPosition = position;
    }

    @Override
    public String queryText() {
        return searchText.toLowerCase();
    }

    @Override
    public ContentResolver getContentResolverMain() {
        return getContentResolver();
    }

    @Override
    public Music getSong() {
        currentPosition = -1;
        return currSong;
    }

    @Override
    public Music getCurrentSong() {
        return currSong;
    }

    @Override
    public boolean getPlaylistFlag() {
        return playlistFlag;
    }

    @Override
    public void currentSong(Music music) {
        this.currSong = music;
    }

    @Override
    public int getPosition() {
        return currentPosition;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
        // empty the messageQueue having pending posts of runnable
        handler.removeCallbacks(runnable);
    }
}
