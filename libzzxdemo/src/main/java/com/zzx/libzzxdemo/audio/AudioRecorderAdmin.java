package com.zzx.libzzxdemo.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.util.Arrays;

class AudioRecorderAdmin implements AudioInterface.Recorder {

    private int mRate;
    private AudioRecord mAudioRecorder;
    private volatile byte[] mByte;
    private int mMinSize;

    AudioRecorderAdmin(int sampleRate) {
        this.mRate = sampleRate;
    }

    @Override
    public void start() {
        mMinSize = AudioRecord.getMinBufferSize(mRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        mAudioRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, mRate,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, mMinSize);
        mAudioRecorder.startRecording();
        mByte = new byte[mMinSize];
    }

    @Override
    public void release() {
        if (mAudioRecorder != null && mAudioRecorder.getState() == AudioRecord.STATE_INITIALIZED) {
            mAudioRecorder.stop();
            mAudioRecorder.release();
        }
        mAudioRecorder = null;
    }

    @Override
    public byte[] recorder() {
        int res = mAudioRecorder.read(mByte, 0, mByte.length);
        if (res < 0) {
            switch (res) {
                case AudioRecord.ERROR_INVALID_OPERATION:
                    break;
                case AudioRecord.ERROR_BAD_VALUE:
                    break;
//                case AudioRecord.ERROR_DEAD_OBJECT:
//                    break;
                case AudioRecord.ERROR:
                    break;
                default:
            }
        } else {
            if (res == mMinSize) {
                return mByte;
            } else {
                return Arrays.copyOfRange(mByte, 0, res);
            }

        }
        return new byte[0];
    }
}
