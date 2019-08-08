package com.angelzbg.communityforum.utils;

import android.content.Context;
import android.media.MediaPlayer;

import com.angelzbg.communityforum.R;

public final class SoundHelper {

    public static final int TYPE_MENU_SWITCH = 1;

    public static void playSound(final Context context, final int type) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int resId = -1;
                switch (type) {
                    case TYPE_MENU_SWITCH:
                        resId = R.raw.menu_switch;
                        break;
                }

                if (resId != -1) {
                    final MediaPlayer mediaPlayer = MediaPlayer.create(context, resId);
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mediaPlayer.stop();
                            mediaPlayer.release();
                        }
                    });
                    mediaPlayer.setLooping(false);
                    mediaPlayer.start();
                }
            }
        }).start();
    } // playSound()

} // SoundHelper{}