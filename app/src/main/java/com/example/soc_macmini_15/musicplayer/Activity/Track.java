package com.example.soc_macmini_15.musicplayer.Activity;


public class Track {
    private String id;
    private String name;
    private String preview_url;

    public Track(String id, String name, String preview_url) {
        this.id = id;
        this.name = name;
        this.preview_url = preview_url;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPreview_url() {
        return preview_url;
    }

    public void setPreview_url(String preview_url) {
        this.preview_url = preview_url;
    }
}
