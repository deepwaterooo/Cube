package dev.cube;

import android.content.res.Resources;
import android.media.MediaPlayer;

public class GLMusic {
    public static MediaPlayer[] mp = new MediaPlayer[13];

    public static void load(Resources resources) {
        mp[0] = MediaPlayer.create(resources, R.raw.theme);
        mp[1] = MediaPlayer.create(resources, R.raw.e1);
        mp[2] = MediaPlayer.create(resources, R.raw.e2);
        mp[3] = MediaPlayer.create(resources, R.raw.e3);
        mp[4] = MediaPlayer.create(resources, R.raw.e4);
        mp[5] = MediaPlayer.create(resources, R.raw.e5);
        mp[6] = MediaPlayer.create(resources, R.raw.e6);
        mp[7] = MediaPlayer.create(resources, R.raw.e7);
        mp[8] = MediaPlayer.create(resources, R.raw.e8);
        mp[9] = MediaPlayer.create(resources, R.raw.e9);
        mp[10] = MediaPlayer.create(resources, R.raw.e10);
        mp[11] = MediaPlayer.create(resources, R.raw.e11);
        mp[12] = MediaPlayer.create(resources, R.raw.e12);
    }  
}
