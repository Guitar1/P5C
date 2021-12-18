package com.zzx.police.utils;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.zzx.police.ZZXConfig;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;


/**@author Tomy
 * Created by Tomy on 2014/8/11.
 */
public class AudioEncoder2 {
    private final String TAG = "AudioEncoder: ";
//    private MediaCodec mAudioCodec;
    private int mBufferMinSize = 0;
    private int mPackageLen = 0;
    private OutputStream mOutputStream = null;
    private AudioRecord mAudioRecorder;
    private Deque<byte[]> mAudioDeque;
    private int mTotalOffset = 0;
    private ByteBuffer mBuffer;
    private byte[] mHeader = null;
    private final boolean IS_PCM = false;
    private final int TOTAL_LEN = 1200;
    private byte[] buffer;

    public AudioEncoder2(int sampleRate, OutputStream outputStream) {
        mAudioDeque = new ArrayDeque<>(150);
        mOutputStream = outputStream;
        /*try {
            mOutputStream = new FileOutputStream("/sdcard/output.g726");
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        mBufferMinSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        mPackageLen = mBufferMinSize;
        mAudioRecorder  = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, sampleRate,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, mBufferMinSize);
        mAudioRecorder.startRecording();
        mPackageLen = TOTAL_LEN;
        buffer = new byte[mBufferMinSize];
        mBuffer = ByteBuffer.allocate(mPackageLen);
        initHeader();
        if (!ZZXConfig.isG726){
            initAudioCodec();
        }
    }

    private void initHeader() {
        mHeader = new byte[8];
        byte y;
        byte x = 0x30;
        if (IS_PCM) {
            y = 0x34;
        } else {
            y = 0x32;
        }
        byte w = 'w';
        byte b = 'b';
        short len = 168;
        short totalLen = (short) 1016;
        mHeader[0] = x;
        mHeader[1] = y;
        mHeader[2] = w;
        mHeader[3] = b;
        mHeader[4] = (byte) (len & 0xff);
        mHeader[5] = (byte) (len >> 8);
        mHeader[6] = (byte) (totalLen & 0xff);
        mHeader[7] = (byte) (totalLen >> 8);
    }

    public void startReadAudioData() {
        startReadAudioDataPCM();
    }

    public void startReadAudioDataPCM() {
        new Thread() {
            @Override
            public void run() {
                try {
                    while(true) {
                        if (mAudioRecorder == null) {
                            break;
                        }
                        int len = mAudioRecorder.read(buffer, 0, mBufferMinSize);
                        if (len > 0 && mAudioDeque != null && mAudioDeque.size() < 150) {
                            mAudioDeque.push(buffer);
                        }else{
                            if (mAudioDeque != null){
                                mAudioDeque.clear();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     *
     */
    AudioCodec audioCodec = null;
    private void initAudioCodec(){
        audioCodec = new AudioCodec(8000,true);
        audioCodec.getOutputStream(mOutputStream);
    }

    public void startReport() throws IOException {
        if (mAudioDeque != null && !mAudioDeque.isEmpty()) {
            if (ZZXConfig.isG726){
                encode726(mAudioDeque.pollLast());
            }else {
                audioCodec.encode(mAudioDeque.pollLast());
            }
        }
    }

    public void release() {
        if (mAudioRecorder != null) {
            mAudioRecorder.stop();
            mAudioRecorder.release();
            mAudioRecorder = null;
        }
        try {
            mOutputStream.flush();
            mOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mAudioDeque.clear();
        mAudioDeque = null;
        mOutputStream = null;
    }

    private byte[] mOutPut = new byte[1024 * 8];
    private void encode726(byte[] data) throws IOException {
        if (data != null && data.length > 0) {
            int len = g726_encode(data, data.length, mOutPut, mOutPut.length);
            if (len > 0) {
                encode(mOutPut, len);
            }
        }
    }

    private void encode(byte[] data, int len) throws IOException {
        int offset = 0;
        if (data != null) {
            do {
                int count = TOTAL_LEN - mTotalOffset <= len - offset ? TOTAL_LEN - mTotalOffset : len - offset;
                mBuffer.put(data, offset, count);
                offset += count;
                mTotalOffset += count;
                if (mTotalOffset >= TOTAL_LEN) {
                        mOutputStream.write(mBuffer.array());
                        mOutputStream.flush();
                        mBuffer.clear();
                        mTotalOffset = 0;
                }
            } while (offset < len);
        }
    }

    private native int g726_encode(byte[] input, int count, byte[] output, int outputSize);
    public static native int g726_decode (byte[] input, int count, byte[] output, int outputSize);
}
