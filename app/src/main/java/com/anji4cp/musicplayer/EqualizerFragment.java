package com.anji4cp.musicplayer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class EqualizerFragment extends Fragment {

    private static final String PREF_NAME = "eq_settings";
    private static final String KEY_PRESET = "preset";
    private static final String KEY_BAND_PREFIX = "band_";
    private static final int PRESET_CUSTOM = 5;

    private PlayerManager playerManager;
    private SharedPreferences prefs;

    private SeekBar seekBass, seekMid, seekTreble;
    private Spinner spinnerPreset;

    private short minEQ;
    private short maxEQ;

    private boolean isRestoringState = false;

    private final String[] presets = {
            "Normal",
            "Bass Boost",
            "Pop",
            "Rock",
            "Vocal",
            "Custom"
    };

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_equalizer, container, false);

        // INIT
        playerManager = PlayerManager.getInstance(requireContext());
        prefs = requireContext().getSharedPreferences(PREF_NAME, 0);

        seekBass = view.findViewById(R.id.seekBass);
        seekMid = view.findViewById(R.id.seekMid);
        seekTreble = view.findViewById(R.id.seekTreble);
        spinnerPreset = view.findViewById(R.id.spinnerPreset);

        // INIT EQ (GLOBAL)
        playerManager.initEqualizer();

        minEQ = playerManager.getMinEQ();
        maxEQ = playerManager.getMaxEQ();

        boolean enabled = playerManager.isPlayerReady();
        seekBass.setEnabled(enabled);
        seekMid.setEnabled(enabled);
        seekTreble.setEnabled(enabled);
        spinnerPreset.setEnabled(enabled);

        setupPresetSpinner();
        setupSeekBars();
        restoreState();

        return view;
    }

    // =========================
    // SEEK BARS
    // =========================
    private void setupSeekBars() {
        setupSeekBar(seekBass, (short) 0);
        setupSeekBar(seekMid, (short) 1);
        setupSeekBar(seekTreble, (short) 2);
    }

    private void setupSeekBar(SeekBar seekBar, short band) {

        seekBar.setMax(maxEQ - minEQ);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(
                    SeekBar seekBar,
                    int progress,
                    boolean fromUser) {

                if (!fromUser) return;
                if (!playerManager.isPlayerReady()) return;

                short level = (short) (progress + minEQ);
                playerManager.setBandLevel(band, level);

                // AUTO → CUSTOM
                if (spinnerPreset.getSelectedItemPosition() != PRESET_CUSTOM) {
                    isRestoringState = true;
                    spinnerPreset.setSelection(PRESET_CUSTOM);
                    isRestoringState = false;

                    prefs.edit()
                            .putInt(KEY_PRESET, PRESET_CUSTOM)
                            .apply();
                }

                prefs.edit()
                        .putInt(KEY_BAND_PREFIX + band, level)
                        .apply();
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    // =========================
    // PRESET
    // =========================
    private void setupPresetSpinner() {

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                presets
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPreset.setAdapter(adapter);

        spinnerPreset.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        if (isRestoringState) return;

                        if (position != PRESET_CUSTOM) {
                            playerManager.applyPreset((short) position);
                        }

                        prefs.edit()
                                .putInt(KEY_PRESET, position)
                                .apply();

                        refreshSliders();
                    }

                    @Override public void onNothingSelected(
                            android.widget.AdapterView<?> parent) {}
                }
        );
    }

    private void refreshSliders() {
        updateSlider(seekBass, (short) 0);
        updateSlider(seekMid, (short) 1);
        updateSlider(seekTreble, (short) 2);
    }

    private void updateSlider(SeekBar seekBar, short band) {
        short level = playerManager.getBandLevel(band);
        seekBar.setProgress(level - minEQ);
    }

    // =========================
    // RESTORE
    // =========================
    private void restoreState() {

        isRestoringState = true;

        int preset = prefs.getInt(KEY_PRESET, PRESET_CUSTOM);
        spinnerPreset.setSelection(preset);

        refreshSliders();

        isRestoringState = false;
    }
}