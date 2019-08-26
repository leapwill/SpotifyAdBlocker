package com.cameron.spotifyadblocker;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashSet;

import static android.content.Context.MODE_PRIVATE;

class Util {

    private static Util singletonInstance;

    private Context context;
    private boolean muted;
    private int originalVolume;
    private int zeroVolume;
    private static String currentTitle;
    private HashSet<String> blocklist;

    private Util(Context context) {
        this.context = context;
        blocklist = new HashSet<String>();
        muted = false;
        originalVolume = 0;
        zeroVolume = 0;

        // Load blocklist
        Resources resources = context.getResources();
        InputStream inputStream = resources.openRawResource(R.raw.blocklist);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                blocklist.add(line);
            }
            SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.saved_filters), MODE_PRIVATE);
            blocklist.addAll((Collection<? extends String>) preferences.getAll().values());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static Util getInstance(Context context) {
        if (singletonInstance == null) {
            singletonInstance = new Util(context);
        }
        return singletonInstance;
    }

    void checkSpotifyNotification(String title) {
        currentTitle = title;
        if (title != null) {
            Log.d("DEBUG", title);
            boolean isAdPlaying = blocklist.contains(title);
            String s = isAdPlaying? "Ad playing" : "Ad not playing";
            Log.d("DEBUG", s);
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (isAdPlaying && !muted) {
                originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, zeroVolume, AudioManager.FLAG_SHOW_UI);
                muted = true;
            }
            else if (!isAdPlaying && muted) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, AudioManager.FLAG_SHOW_UI);
                muted = false;
            }
        }
    }

    String getCurrentTitle() {
        return currentTitle;
    }
}
