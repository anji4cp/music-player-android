package com.anji4cp.musicplayer;

import android.content.Context;
import android.media.MediaPlayer;

import java.io.IOException;
import java.util.List;

public class PlayerManager {

    private static PlayerManager instance;
    private MediaPlayer mediaPlayer;
    private List<Song> songList;
    private int currentIndex = -1;

    private PlayerManager(Context context) {
        mediaPlayer = new MediaPlayer();
    }

    public static PlayerManager getInstance(Context context) {
        if (instance == null) {
            instance = new PlayerManager(context.getApplicationContext());
        }
        return instance;
    }

    public void setSongList(List<Song> songs) {
        this.songList = songs;
    }

    public void playSong(int index) throws IOException {
        if (songList == null || index < 0 || index >= songList.size()) return;

        currentIndex = index;
        Song song = songList.get(index);

        mediaPlayer.reset();
        mediaPlayer.setDataSource(song.getPath());
        mediaPlayer.prepare();
        mediaPlayer.start();
    }

    // ▶️⏸
    public void togglePlayPause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.start();
        }
    }

    // ⏭ NEXT
    public void next() throws IOException {
        if (songList == null || songList.isEmpty()) return;
        currentIndex = (currentIndex + 1) % songList.size();
        playSong(currentIndex);
    }

    // ⏮ PREVIOUS
    public void previous() throws IOException {
        if (songList == null || songList.isEmpty()) return;
        currentIndex = (currentIndex - 1 + songList.size()) % songList.size();
        playSong(currentIndex);
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public Song getCurrentSong() {
        if (songList == null || currentIndex < 0) return null;
        return songList.get(currentIndex);
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }
}
