package com.anji4cp.musicplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;

public class PlayerManager {

    private static PlayerManager instance;

    private MediaPlayer mediaPlayer;
    private Context context;

    private List<Song> songList = new ArrayList<>();
    private int currentIndex = -1;
    private Song currentSong;

    private static final int NOTIFICATION_ID = 1;

    // =========================
    // EQUALIZER STATE (GLOBAL)
    // =========================
    private android.media.audiofx.Equalizer equalizer;
    private short[] bandLevels;
    private short minEQ;
    private short maxEQ;
    private int currentPreset = -1;

    private PlayerManager(Context context) {
        this.context = context.getApplicationContext();
        NotificationUtils.createChannel(this.context);
        mediaPlayer = new MediaPlayer();
    }

    public static PlayerManager getInstance(Context context) {
        if (instance == null) {
            instance = new PlayerManager(context);
        }
        return instance;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    // =========================
    // BASIC INFO
    // =========================
    public void setSongList(List<Song> songs) {
        this.songList = songs;
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public int getDuration() {
        return mediaPlayer != null ? mediaPlayer.getDuration() : 0;
    }

    public int getCurrentPosition() {
        return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
    }

    // =========================
    // EQUALIZER (INIT SEKALI)
    // =========================
    public void initEqualizer() {

        if (mediaPlayer == null) return;

        if (equalizer != null) return;

        equalizer = new android.media.audiofx.Equalizer(
                0,
                mediaPlayer.getAudioSessionId()
        );
        equalizer.setEnabled(true);

        minEQ = equalizer.getBandLevelRange()[0];
        maxEQ = equalizer.getBandLevelRange()[1];

        short bands = equalizer.getNumberOfBands();
        bandLevels = new short[bands];

        for (short i = 0; i < bands; i++) {
            bandLevels[i] = equalizer.getBandLevel(i);
        }
    }

    public short getMinEQ() {
        return minEQ;
    }

    public short getMaxEQ() {
        return maxEQ;
    }

    public void setBandLevel(short band, short level) {

        if (equalizer == null || bandLevels == null) return;

        equalizer.setBandLevel(band, level);
        bandLevels[band] = level;
    }

    public short getBandLevel(short band) {
        return bandLevels != null ? bandLevels[band] : 0;
    }

    public void applyPreset(short preset) {

        if (equalizer == null) return;

        equalizer.usePreset(preset);
        currentPreset = preset;

        for (short i = 0; i < bandLevels.length; i++) {
            bandLevels[i] = equalizer.getBandLevel(i);
        }
    }

    public int getCurrentPreset() {
        return currentPreset;
    }

    // =========================
    // PLAY CONTROL
    // =========================
    public void playSong(int index) {
        if (index < 0 || index >= songList.size()) return;

        currentIndex = index;
        currentSong = songList.get(index);

        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(currentSong.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();

            // 🔒 PASTIKAN AUDIO SESSION SUDAH VALID
            releaseEqualizer();
            initEqualizer();


            showNotification(
                    currentSong.getTitle(),
                    currentSong.getArtist(),
                    true
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void togglePlayPause() {
        if (currentSong == null) return;

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            showNotification(
                    currentSong.getTitle(),
                    currentSong.getArtist(),
                    false
            );
        } else {
            mediaPlayer.start();
            showNotification(
                    currentSong.getTitle(),
                    currentSong.getArtist(),
                    true
            );
        }
    }

    public void next() {
        if (songList.isEmpty()) return;
        playSong((currentIndex + 1) % songList.size());
    }

    public void previous() {
        if (songList.isEmpty()) return;
        playSong((currentIndex - 1 + songList.size()) % songList.size());
    }

    public boolean isPlayerReady() {
        return mediaPlayer != null
                && currentSong != null
                && mediaPlayer.getAudioSessionId() != -1;
    }

    // =========================
    // NOTIFICATION
    // =========================
    private void showNotification(String title, String artist, boolean isPlaying) {

        Intent openIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                0,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent prevIntent = new Intent(context, MusicActionReceiver.class);
        prevIntent.setAction(MusicActionReceiver.ACTION_PREVIOUS);
        PendingIntent prevPending = PendingIntent.getBroadcast(
                context, 1, prevIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent playPauseIntent = new Intent(context, MusicActionReceiver.class);
        playPauseIntent.setAction(MusicActionReceiver.ACTION_PLAY_PAUSE);
        PendingIntent playPausePending = PendingIntent.getBroadcast(
                context, 2, playPauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent nextIntent = new Intent(context, MusicActionReceiver.class);
        nextIntent.setAction(MusicActionReceiver.ACTION_NEXT);
        PendingIntent nextPending = PendingIntent.getBroadcast(
                context, 3, nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        int playPauseIcon = isPlaying
                ? android.R.drawable.ic_media_pause
                : android.R.drawable.ic_media_play;

        Notification notification =
                new NotificationCompat.Builder(context, NotificationUtils.CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_media_play)
                        .setContentTitle(title)
                        .setContentText(artist)
                        .setContentIntent(contentIntent)
                        .setOngoing(isPlaying)
                        .addAction(android.R.drawable.ic_media_previous, "Prev", prevPending)
                        .addAction(playPauseIcon, "PlayPause", playPausePending)
                        .addAction(android.R.drawable.ic_media_next, "Next", nextPending)
                        .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                                .setShowActionsInCompactView(0, 1, 2))
                        .build();

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (manager != null) {
            manager.notify(NOTIFICATION_ID, notification);
        }
    }

    // =========================
    // CLEANUP
    // =========================
    private void releaseEqualizer() {
        if (equalizer != null) {
            equalizer.release();
            equalizer = null;
        }
    }

    public void release() {
        releaseEqualizer();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}