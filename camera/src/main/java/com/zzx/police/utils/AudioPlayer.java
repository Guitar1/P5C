package com.zzx.police.utils;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;
/**@author Tomy
 * Created by Tomy on 2014/9/19.
 */
public class AudioPlayer {
    private AudioTrack mAudioTrack;
    private Deque<byte[]> mDataList = new ArrayDeque<>(150);
    private boolean isPlaying = false;
    private byte[] result = new byte[1024 * 8];
    private Deque<byte[]> mResultList = new ArrayDeque<>(150);
    private byte[] data = null;
    private ByteBuffer mBuffer;
    private int mTotalOffset = 0;
    private int BUFFER_SIZE = 8912;
    private PlayTask mPlayTask;
//    private FileInputStream inputStream;

    public AudioPlayer() {
        int bufferSize = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        mAudioTrack = new AudioTrack(AudioManager.STREAM_SYSTEM, 8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);
        mAudioTrack.play();
        /*try {
            inputStream = new FileInputStream("/sdcard/out.g726");
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    public void play() {
        if (isPlaying) {
            return;
        }
        isPlaying = true;
        new Thread() {
            @Override
            public void run() {
                while (isPlaying) {
                    startDecode();
                }
            }
        }.start();
        new Thread() {
            @Override
            public void run() {
                while (isPlaying) {
                    if (!mResultList.isEmpty()) {
                        byte[] data = mResultList.pollLast();
                        mAudioTrack.write(data, 0, data.length);
                    }
                }
            }
        }.start();
    }

    public void insertData(byte[] data) {
        if (mDataList != null && mDataList.size() < 150)
            mDataList.push(data);
    }

    public void startDecode() {
        if (mDataList == null || mDataList.isEmpty())
            return;
        data = mDataList.pollLast();
        if (data != null && data.length > 0) {
            int len = AudioEncoder2.g726_decode(data, data.length, result, result.length);
            int offset = 0;
            /*if (len > 0)
                mAudioTrack.write(result, 0, len);*/
            do {
                if (mTotalOffset == 0) {
                    //若总索引为0即读头时,读取第6,7位得到音频数据长度,然后buffer的索引+8跳过头.
                    initReadCount();
                }
                if (BUFFER_SIZE > mTotalOffset) {
                    /**单个音频帧数据未填满时,若该buffer所剩长度小于音频帧所剩长度,则读取buffer的所有所剩长度数据,反之则只读取音频帧所剩长度.
                     * 读取完后追加buffer的索引及音频帧的索引.
                     * */
                    int count = (BUFFER_SIZE - mTotalOffset) >= (len - offset) ? len - offset : BUFFER_SIZE - mTotalOffset;
                    mBuffer.put(result, offset, count);
                    offset += count;
                    mTotalOffset += count;
                    if (mTotalOffset >= BUFFER_SIZE) {
                        //当音频帧索引大于等于音频帧总长,将索引置0,然后下一次就是读取音频头.
                        mTotalOffset = 0;
                    }
                }
            } while (offset < len);//若该buffer的索引已大于等于buffer长度则读取下一个buffer.
        }
    }

    private void initReadCount() {
        if (mBuffer != null) {
            mResultList.push(mBuffer.array());
            mBuffer.clear();
        } else {
            mBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        }
    }

    public void playFile() {
        isPlaying = true;
        int len = 0;
        byte[] data = new byte[1024];
        /*try {
            while ((len = inputStream.read(data)) > 0) {
                play(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    public void play(byte[] data) {
        int len = AudioEncoder2.g726_decode(data, data.length, result, result.length);
        if (len > 0) {
            mAudioTrack.write(result, 0, len);
        }
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void stop() {
        if (!isPlaying) {
            return;
        }
        isPlaying = false;
    }

    public void release() {
        stop();
        if (mAudioTrack != null) {
            mAudioTrack.flush();
            mAudioTrack.release();
            mAudioTrack = null;
        }
    }

    class PlayTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            while (true) {
                boolean isCancel = isCancelled();
                if (isCancel)
                    break;
                if (!mResultList.isEmpty()) {
                    byte[] data = mResultList.pollLast();
                    mAudioTrack.write(data, 0, data.length);
                }
            }
            return null;
        }
    }
}
