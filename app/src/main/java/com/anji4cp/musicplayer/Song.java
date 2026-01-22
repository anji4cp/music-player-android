package com.anji4cp.musicplayer;

public class Song {

    private String title;
    private String artist;
    private String path;
    private long duration;
    private byte[] albumArt; // 👈 cover lagu

    public Song(String title, String artist, String path, long duration, byte[] albumArt) {
        this.title = title;
        this.artist = artist;
        this.path = path;
        this.duration = duration;
        this.albumArt = albumArt;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getPath() {
        return path;
    }

    public long getDuration() {
        return duration;
    }

    public byte[] getAlbumArt() {
        return albumArt;
    }
}
