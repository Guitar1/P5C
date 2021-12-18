package com.zzx.police.utils;

import android.media.MediaRecorder;

import java.io.IOException;

/**@author Tomy
 * Created by Tomy on 2014-11-14.
 */
public class AudioSaver {
    private MediaRecorder mRecorder;
    public final static int SAMPLE_RATE_DEFAULT = 44100;
    public final static int CHANNEL_COUNT_DEFAULT   = 2;
    public final static boolean IS_16_PCM_DEFAULT    = true;
    private int mSampleRate     = 0;
    private int mChannelCount   = 0;
    private boolean mIs16PCM    = true;
    private int mState = STATE_IDLE;
    public static final int STATE_IDLE      = 0;
    public static final int STATE_RECORD    = 1;
    public static final int STATE_PAUSE     = 2;
    public static final int STATE_INIT      = 3;

    public AudioSaver() {
        this(SAMPLE_RATE_DEFAULT, CHANNEL_COUNT_DEFAULT, IS_16_PCM_DEFAULT);
    }

    public AudioSaver(int sampleRate, int channelCount, boolean is16PCM) {
        mRecorder = new MediaRecorder();
        mSampleRate = sampleRate;
        mChannelCount   = channelCount;
        mIs16PCM    = is16PCM;
    }

    public void initRecorder() {
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
        }
        if (mState == STATE_IDLE) {
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mRecorder.setAudioChannels(mChannelCount);
            mRecorder.setAudioSamplingRate(mSampleRate);
            mRecorder.setAudioEncodingBitRate(mSampleRate * mChannelCount * (mIs16PCM ? 2 : 1));
        }
        mState = STATE_INIT;
    }

    public void startRecord(String filePath) {
        initRecorder();
        if (mRecorder != null) {
            try {
                mRecorder.setOutputFile(filePath);
                mRecorder.prepare();
                mRecorder.start();
                mState = STATE_RECORD;
            } catch (IOException e) {
                e.printStackTrace();
                releaseRecorder();
            }
        }
    }

    public void pauseRecord() {
        mState = STATE_PAUSE;
//        mRecorder.setParametersExtra("media-param-pause=1");
    }

    public void stopRecord() {
        mState = STATE_IDLE;
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.reset();
        }
    }

    private void releaseRecorder() {
        mState = STATE_IDLE;
        try {
            if (mRecorder != null) {
                mRecorder.reset();
                mRecorder.release();
                mRecorder = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
