package com.zzx.police.utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

import java.io.File;
import java.io.IOException;

/**
 * Created by liyi on 2017/5/17 0017.
 */

public class AudioPlayerUtil{


    private MediaPlayer player;
    private Context mContext;
    private int mRawId;

    public AudioPlayerUtil(Context context, int rawId) {
        this.mContext = context;
        this.mRawId = rawId;
        this.player = new MediaPlayer();
        try {
            Uri uri = Uri.parse("android.resource://" + mContext.getPackageName() + "/" + rawId);
            player.setDataSource(mContext, uri);
            player.prepare();
            player.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public AudioPlayerUtil(File file) {
        this.player = new MediaPlayer();
        try {
            player.setDataSource(file.getPath());
            player.prepare();
            player.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
