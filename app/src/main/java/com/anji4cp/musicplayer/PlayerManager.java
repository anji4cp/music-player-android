package com.anji4cp.musicplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlayerManager {

    private static PlayerManager instance;

    private MediaPlayer mediaPlayer;
    private Context context;

    private List<Song> songList = new ArrayList<>();
    private int currentIndex = -1;
    private Song currentSong;

    // =========================
    // PLAY MODE
    // =========================
    public static final int PLAY_MODE_NORMAL = 0;
    public static final int PLAY_MODE_SHUFFLE = 1;
    public static final int PLAY_MODE_REPEAT_ONE = 2;
    private int playMode = PLAY_MODE_NORMAL;
    private boolean restoredOnce = false;


    // =========================
    // PERSIST STATE
    // =========================
    private static final String PREF_PLAYER = "player_state";
    private static final String KEY_LAST_INDEX = "last_index";
    private static final String KEY_LAST_POSITION = "last_position";
    private static final String KEY_PLAY_MODE = "play_mode";

    private SharedPreferences prefs;

    // =========================
    // EQUALIZER
    // =========================
    private android.media.audiofx.Equalizer equalizer;
    private short[] bandLevels;
    private short minEQ;
    private short maxEQ;
    private int currentPreset = -1;

    private static final int NOTIFICATION_ID = 1;

    private PlayerManager(Context context) {
        this.context = context.getApplicationContext();
        NotificationUtils.createChannel(this.context);

        mediaPlayer = new MediaPlayer();

        prefs = this.context.getSharedPreferences(
                PREF_PLAYER,
                Context.MODE_PRIVATE
        );

        playMode = prefs.getInt(KEY_PLAY_MODE, PLAY_MODE_NORMAL);
    }

    public static PlayerManager getInstance(Context context) {
        if (instance == null) {
            instance = new PlayerManager(context);
        }
        return instance;
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

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public boolean isPlayerReady() {
        return mediaPlayer != null
                && currentSong != null
                && mediaPlayer.getAudioSessionId() != -1;
    }

    // =========================
    // PLAY MODE
    // =========================
    public void setPlayMode(int mode) {
        playMode = mode;
        prefs.edit().putInt(KEY_PLAY_MODE, playMode).apply();
    }

    public int getPlayMode() {
        return playMode;
    }

    // =========================
    // PLAY CONTROL (OVERLOAD)
    // =========================
    public void playSong(int index) {
        playSong(index, false);
    }

    public void playSong(int index, boolean restoring) {
        if (index < 0 || index >= songList.size()) return;

        currentIndex = index;
        currentSong = songList.get(index);

        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(currentSong.getPath());
            mediaPlayer.prepare();

            if (!restoring) {
                mediaPlayer.start();
            }

            prefs.edit()
                    .putInt(KEY_LAST_INDEX, currentIndex)
                    .apply();

            releaseEqualizer();
            initEqualizer();

            mediaPlayer.setOnCompletionListener(mp -> handleSongCompletion());

            showNotification(
                    currentSong.getTitle(),
                    currentSong.getArtist(),
                    !restoring
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void togglePlayPause() {
        if (currentSong == null) return;

        if (mediaPlayer.isPlaying()) {
            saveCurrentPosition();
            mediaPlayer.pause();
            showNotification(currentSong.getTitle(), currentSong.getArtist(), false);
        } else {
            mediaPlayer.start();
            showNotification(currentSong.getTitle(), currentSong.getArtist(), true);
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

    private void playRandom() {
        if (songList.isEmpty()) return;

        int random;
        do {
            random = new Random().nextInt(songList.size());
        } while (random == currentIndex && songList.size() > 1);

        playSong(random);
    }

    private void handleSongCompletion() {
        if (playMode == PLAY_MODE_REPEAT_ONE) {
            playSong(currentIndex);
        } else if (playMode == PLAY_MODE_SHUFFLE) {
            playRandom();
        } else {
            next();
        }
    }

    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
    }

    // =========================
    // RESTORE LAST PLAYBACK
    // =========================
    public void restoreLastPlayback() {

        if (restoredOnce) return;
        restoredOnce = true;

        int index = prefs.getInt(KEY_LAST_INDEX, -1);
        int position = prefs.getInt(KEY_LAST_POSITION, 0);
        playMode = prefs.getInt(KEY_PLAY_MODE, PLAY_MODE_NORMAL);

        if (index >= 0 && index < songList.size()) {
            playSong(index, true);
            mediaPlayer.seekTo(position);
        }
    }

    public boolean hasRestored() {
        return currentSong != null;
    }


    public void saveCurrentPosition() {
        if (mediaPlayer != null) {
            prefs.edit()
                    .putInt(KEY_LAST_POSITION, mediaPlayer.getCurrentPosition())
                    .apply();
        }
    }

    // =========================
    // EQUALIZER
    // =========================
    public void initEqualizer() {
        if (mediaPlayer == null || equalizer != null) return;

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

    public void applyPreset(short preset) {
        if (equalizer == null) return;

        equalizer.usePreset(preset);
        currentPreset = preset;

        for (short i = 0; i < bandLevels.length; i++) {
            bandLevels[i] = equalizer.getBandLevel(i);
        }
    }

    public short getMinEQ() { return minEQ; }
    public short getMaxEQ() { return maxEQ; }

    public void setBandLevel(short band, short level) {
        if (equalizer == null) return;
        equalizer.setBandLevel(band, level);
        bandLevels[band] = level;
    }

    public short getBandLevel(short band) {
        return bandLevels != null ? bandLevels[band] : 0;
    }

    private void releaseEqualizer() {
        if (equalizer != null) {
            equalizer.release();
            equalizer = null;
        }
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
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setOngoing(isPlaying)
                        .addAction(android.R.drawable.ic_media_previous, "Prev", prevPending)
                        .addAction(playPauseIcon, "Play", playPausePending)
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

    public void stopPlaybackAndNotification() {

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            saveCurrentPosition();
            mediaPlayer.pause();
        }

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (manager != null) {
            manager.cancel(NOTIFICATION_ID);
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
