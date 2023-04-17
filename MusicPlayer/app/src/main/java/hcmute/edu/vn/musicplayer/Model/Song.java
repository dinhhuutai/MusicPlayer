package hcmute.edu.vn.musicplayer.Model;

import android.net.Uri;

public class Song {
    private String id;
    private String path;
    private String title;
    private String album;
    private String artist;
    private String duration;

    public Song() {
    }

    public Song(String id, String path, String title, String album, String artist, String duration) {
        this.id = id;
        this.path = path;
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.duration = duration;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
