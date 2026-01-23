package com.anji4cp.musicplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MusicActionReceiver extends BroadcastReceiver {

    public static final String ACTION_PLAY_PAUSE = "ACTION_PLAY_PAUSE";
    public static final String ACTION_NEXT = "ACTION_NEXT";
    public static final String ACTION_PREVIOUS = "ACTION_PREVIOUS";

    @Override
    public void onReceive(Context context, Intent intent) {

        PlayerManager playerManager = PlayerManager.getInstance(context);

        if (intent == null || intent.getAction() == null) return;

        switch (intent.getAction()) {
            case ACTION_PLAY_PAUSE:
                playerManager.togglePlayPause();
                break;

            case ACTION_NEXT:
                playerManager.next();
                break;

            case ACTION_PREVIOUS:
                playerManager.previous();
                break;
        }
    }
}
