package com.zzx.police.utils;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;

import java.io.FileOutputStream;
import java.io.IOException;

/**@author Tomy
 * Created by Tomy on 2014/11/12.
 */
public class AudioRecorder {
    private AudioRecord mAudioRecord;
    private int mAudioSizeMin = 0;
    private DataCallback mDataCallback;
    private ReadDataTask mReadTask;
    private FileOutputStream mOutputStream;
    private int mTotalAudioSize = 0;
    private int mSampleRate = 0;
    private int mChannelCount = 0;
    private boolean mIs16PCM = false;

    public AudioRecorder(int sampleRate, int channelCount, boolean is16PCM) {
        mSampleRate = sampleRate;
        mChannelCount = channelCount;
        mIs16PCM = is16PCM;
        try {
            initAudioRecord();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startRecord() {
        try {
            mAudioRecord.startRecording();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mReadTask == null) {
            mReadTask = new ReadDataTask();
            mReadTask.execute();
        }
    }

    public void stopRecord() {
        if (mReadTask != null) {
            mReadTask.cancel(true);
            mReadTask = null;
        }
        mAudioRecord.stop();

        if (mOutputStream != null) {
            try {
                writeWaveFileHeader(mOutputStream, mTotalAudioSize, mTotalAudioSize + 36, mSampleRate, mChannelCount, mSampleRate * mChannelCount * (mIs16PCM ? 2 : 1));
                mOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mOutputStream = null;
        }
    }

    public void release() {
        mAudioRecord.release();
    }

    public void setDataCallback(DataCallback callback) {
        mDataCallback = callback;
    }

    public void setOutputFile(String path) {
        if (mOutputStream != null) {
            try {
                mOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mOutputStream = null;
        }
        try {
            mOutputStream = new FileOutputStream(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getMinDataSize() {
        return mAudioSizeMin;
    }

    private void initAudioRecord() {
        mAudioSizeMin = AudioRecord.getMinBufferSize(mSampleRate, getChannel(), getPCMBit());
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, mSampleRate, getChannel(), getPCMBit(), mAudioSizeMin);
    }

    private int getChannel() {
        return mChannelCount == 1 ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO;
    }

    private int getPCMBit() {
        return mIs16PCM ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT;
    }

    class ReadDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            while (!isCancelled()) {
                try {
                    if (mAudioRecord != null) {
                        byte[] mData = new byte[mAudioSizeMin];
                        int count = mAudioRecord.read(mData, 0, mAudioSizeMin);
                        if (count == AudioRecord.ERROR_BAD_VALUE || count == AudioRecord.ERROR_INVALID_OPERATION) {
                            break;
                        }

                        if (mOutputStream != null) {
                            mOutputStream.write(mData, 0, count);
                            mOutputStream.flush();
                            mTotalAudioSize += count;
                        }

                        if (mDataCallback != null) {
                            mDataCallback.onDataCallback(mData, count);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
            return null;
        }
    }

    public interface DataCallback {
        public void onDataCallback(byte[] data, int count);
    }

    private void writeWaveFileHeader(FileOutputStream out, long totalAudioLen,
                                     long totalDataLen, long longSampleRate, int channels, long byteRate)
            throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8); // block align
        header[33] = 0;
        header[34] = 16; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }
}
