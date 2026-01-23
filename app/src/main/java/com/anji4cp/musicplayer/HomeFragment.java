package com.anji4cp.musicplayer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {

    private ImageView imgAlbumArt;
    private TextView tvTitle, tvArtist;
    private SeekBar seekBar;

    private ImageButton btnPlayPause, btnNext, btnPrevious, btnPlayMode;

    private PlayerManager playerManager;
    private Handler handler = new Handler();

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // UI
        imgAlbumArt = view.findViewById(R.id.imgAlbumArt);
        tvTitle = view.findViewById(R.id.tvCurrentSongTitle);
        tvArtist = view.findViewById(R.id.tvCurrentArtist);
        seekBar = view.findViewById(R.id.seekBar);

        btnPlayPause = view.findViewById(R.id.btnPlayPause);
        btnNext = view.findViewById(R.id.btnNext);
        btnPrevious = view.findViewById(R.id.btnPrevious);
        btnPlayMode = view.findViewById(R.id.btnPlayMode);

        playerManager = PlayerManager.getInstance(requireContext());

        setupControls();
        updateUI();
        updatePlayModeUI();

        return view;
    }

    // =========================
    // CONTROLS
    // =========================
    private void setupControls() {

        btnPlayPause.setOnClickListener(v -> {
            playerManager.togglePlayPause();
            updatePlayButton();
        });

        btnNext.setOnClickListener(v -> {
            playerManager.next();
            updateUI();
        });

        btnPrevious.setOnClickListener(v -> {
            playerManager.previous();
            updateUI();
        });

        btnPlayMode.setOnClickListener(v -> {
            int mode = (playerManager.getPlayMode() + 1) % 3;
            playerManager.setPlayMode(mode);
            updatePlayModeUI();
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(
                    SeekBar seekBar,
                    int progress,
                    boolean fromUser) {

                if (!fromUser) return;
                if (!playerManager.isPlayerReady()) return;

                playerManager.getMediaPlayer().seekTo(progress);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    // =========================
    // UI UPDATE
    // =========================
    private void updateUI() {

        Song song = playerManager.getCurrentSong();

        if (song != null) {
            tvTitle.setText(song.getTitle());
            tvArtist.setText(song.getArtist());

            if (song.getAlbumArt() != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(
                        song.getAlbumArt(), 0, song.getAlbumArt().length);
                imgAlbumArt.setImageBitmap(bitmap);
            } else {
                imgAlbumArt.setImageResource(R.drawable.default_album_art);
            }

            seekBar.setMax(playerManager.getDuration());
            updateSeekBar();

        } else {
            tvTitle.setText("No song playing");
            tvArtist.setText("");
            imgAlbumArt.setImageResource(R.drawable.default_album_art);
            seekBar.setProgress(0);
        }

        updatePlayButton();
    }
    public void refreshUI() {
        updateUI();
    }
    private void updatePlayButton() {
        btnPlayPause.setImageResource(
                playerManager.isPlaying()
                        ? R.drawable.ic_pause
                        : R.drawable.ic_play
        );
    }

    private void updatePlayModeUI() {
        switch (playerManager.getPlayMode()) {
            case PlayerManager.PLAY_MODE_SHUFFLE:
                btnPlayMode.setImageResource(R.drawable.ic_play_mode_shuffle);
                break;
            case PlayerManager.PLAY_MODE_REPEAT_ONE:
                btnPlayMode.setImageResource(R.drawable.ic_play_mode_repeat_one);
                break;
            default:
                btnPlayMode.setImageResource(R.drawable.ic_repeat_all);
        }
    }

    // =========================
    // SEEK BAR LOOP
    // =========================
    private void updateSeekBar() {
        seekBar.setProgress(playerManager.getCurrentPosition());

        if (playerManager.isPlaying()) {
            handler.postDelayed(this::updateSeekBar, 1000);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
    }
}
