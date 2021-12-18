package com.zzx.libzzxdemo.audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

class AudioPlayerAdmin implements AudioInterface.Play {

    private AudioTrack mAudioTrack;
    private int mRate;

    public AudioPlayerAdmin(int simpleRate) {
        this.mRate = simpleRate;
    }

    @Override
    public void play(byte[] data) {
        if (data.length > 0) {
            mAudioTrack.write(data, 0, data.length);
        }
    }

    @Override
    public void start() {
        int bufferSize = AudioTrack.getMinBufferSize(mRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);
        mAudioTrack.play();
    }

    @Override
    public void release() {
        if (mAudioTrack != null) {
            mAudioTrack.flush();
            mAudioTrack.release();
            mAudioTrack = null;
        }
    }
}
