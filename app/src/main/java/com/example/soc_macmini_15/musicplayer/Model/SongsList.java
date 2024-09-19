package com.example.soc_macmini_15.musicplayer.Model;

public class SongsList {

    private String title;
    private String subTitle;
    private String path;
    private String fav;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public SongsList(String title, String subTitle, String path, String fav) {
        this.title = title;
        this.subTitle = subTitle;
        this.path = path;
        this.fav = fav;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public String getFav() {
        return fav;
    }

    public void setFav(String fav) {
        this.fav = fav;
    }

}
