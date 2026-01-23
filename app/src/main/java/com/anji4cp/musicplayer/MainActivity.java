package com.anji4cp.musicplayer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    // Mini player
    private LinearLayout miniPlayer;
    private TextView tvMiniTitle;
    private ImageButton btnMiniPlayPause;
    private SeekBar miniSeekBar;

    private PlayerManager playerManager;
    private Handler handler = new Handler();

    // Track tab aktif (untuk animasi arah)
    private int currentTabId = R.id.navigation_home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        miniPlayer = findViewById(R.id.miniPlayer);
        tvMiniTitle = findViewById(R.id.tvMiniSongTitle);
        btnMiniPlayPause = findViewById(R.id.btnMiniPlayPause);
        miniSeekBar = findViewById(R.id.miniSeekBar);

        playerManager = PlayerManager.getInstance(this);

        requestNotificationPermission();
        setupMiniPlayer();

        // Fragment awal
        loadFragment(new HomeFragment(), false);

        bottomNavigation.setOnItemSelectedListener(item -> {

            if (item.getItemId() == currentTabId) return true;

            Fragment fragment;
            boolean forward;

            if (item.getItemId() == R.id.navigation_home) {
                fragment = new HomeFragment();
                forward = false;
            } else if (item.getItemId() == R.id.navigation_library) {
                fragment = new LibraryFragment();
                forward = true;
            } else if (item.getItemId() == R.id.navigation_equalizer) {
                fragment = new EqualizerFragment();
                forward = true;
            } else {
                return false;
            }

            currentTabId = item.getItemId();
            loadFragment(fragment, forward);
            return true;
        });
    }

    // =========================
    // PERMISSION
    // =========================
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        200
                );
            }
        }
    }

    // =========================
    // LOAD FRAGMENT (SATU-SATUNYA PINTU)
    // =========================
    private void loadFragment(Fragment fragment, boolean forward) {

        // Home → sembunyikan mini player
        if (fragment instanceof HomeFragment) {
            miniPlayer.setVisibility(View.GONE);
        } else {
            if (playerManager.getCurrentSong() != null) {
                miniPlayer.setVisibility(View.VISIBLE);
                updateMiniUI();
            } else {
                miniPlayer.setVisibility(View.GONE);
            }
        }

        if (forward) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left
                    )
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
        } else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_in_left,
                            R.anim.slide_out_right
                    )
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
        }
    }

    public void onSongsReady() {

        playerManager.restoreLastPlayback();

        // Kalau Home sedang aktif → refresh
        Fragment f = getSupportFragmentManager()
                .findFragmentById(R.id.fragmentContainer);

        if (f instanceof HomeFragment) {
            ((HomeFragment) f).refreshUI();
        }
    }


    // =========================
    // MINI PLAYER
    // =========================
    private void setupMiniPlayer() {

        btnMiniPlayPause.setOnClickListener(v -> {
            playerManager.togglePlayPause();
            updateMiniPlayButton();
        });

        miniSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(
                    SeekBar seekBar,
                    int progress,
                    boolean fromUser) {

                if (!fromUser) return;
                if (!playerManager.isPlayerReady()) return;

                playerManager.seekTo(progress);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // TAP MINI PLAYER → PINDAH KE HOME
        miniPlayer.setOnClickListener(v ->
                bottomNavigation.setSelectedItemId(R.id.navigation_home)
        );
    }

    private void updateMiniUI() {

        Song song = playerManager.getCurrentSong();

        if (song == null) {
            tvMiniTitle.setText("");
            miniSeekBar.setProgress(0);
            return;
        }

        tvMiniTitle.setText(song.getTitle() + " - " + song.getArtist());
        miniSeekBar.setMax(playerManager.getDuration());

        updateMiniPlayButton();
        updateMiniSeekBar();
    }

    private void updateMiniPlayButton() {
        btnMiniPlayPause.setImageResource(
                playerManager.isPlaying()
                        ? R.drawable.ic_pause
                        : R.drawable.ic_play
        );
    }


    private void updateMiniSeekBar() {
        miniSeekBar.setProgress(playerManager.getCurrentPosition());

        if (playerManager.isPlaying()) {
            handler.postDelayed(this::updateMiniSeekBar, 1000);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (playerManager.getCurrentSong() != null) {
            updateMiniUI();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (playerManager.isPlayerReady()) {
            playerManager.saveCurrentPosition();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // ❌ JANGAN stop MediaPlayer
        // ✔ BIARKAN PLAYER STATE TERSIMPAN
    }

}
