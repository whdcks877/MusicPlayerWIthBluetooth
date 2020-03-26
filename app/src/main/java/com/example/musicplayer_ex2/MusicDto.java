package com.example.musicplayer_ex2;

import android.media.MediaPlayer;

import java.io.Serializable;

public class MusicDto implements Serializable {
    private String id;
    private String albumId;
    private String title;
    private String artist;

    public MusicDto(){
    }
    public MusicDto(String id, String albumId, String title, String artist) {
        this.id = id;
        this.albumId = albumId;
        this.title = title;
        this.artist = artist;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return "MusicDto{" +
                "id='" + id + '\'' +
                ", albumId='" + albumId + '\'' +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                '}';
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
}
