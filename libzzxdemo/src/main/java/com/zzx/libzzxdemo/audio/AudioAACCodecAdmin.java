package com.zzx.libzzxdemo.audio;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;

class AudioAACCodecAdmin
        implements AudioInterface.Decode<Flowable<byte[]>>, AudioInterface.Encode<Flowable<byte[]>> {
    /**
     * audio/3gpp" - AMR narrowband audio //状态异常
     * audio/amr-wb" - AMR wideband audio //状态异常
     * audio/mpeg" - MPEG1/2 audio layer III //不支持
     * audio/mp4a-latm" - AAC audio (note, t //状态异常
     * audio/vorbis" - vorbis audio //状态异常
     * audio/g711-alaw" - G.711 alaw audio //不支持
     * audio/g711-mlaw" - G.711 ulaw audio //不支持
     */
    private final static String AAC = "audio/mp4a-latm";
    private static final byte[] DECODE_START = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x15, (byte) 0x90};
    private boolean mFirst = true;
    private MediaCodec mCodec;
    private int mRate;
    private boolean isEncoder;
    private codeDataCallback mCallback;

    private ByteBuffer[] mInputBuffer;
    private ByteBuffer[] mOutputBuffer;
    private MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();


    private interface codeDataCallback {
        void completeData(byte[] data);
    }

    AudioAACCodecAdmin(int sampleRate, boolean isEncoder) {
        this.isEncoder = isEncoder;
        this.mRate = sampleRate;
    }

    @Override
    public void start() {
        initMediaCodec(mRate);
    }

    private void initMediaCodec(int sampleRate) {
        try {
            if (isEncoder) {
                mCodec = MediaCodec.createEncoderByType(AAC);
            } else {
                mCodec = MediaCodec.createDecoderByType(AAC);
            }
            MediaFormat format = MediaFormat.createAudioFormat(AAC, sampleRate, 2);
            format.setInteger(MediaFormat.KEY_BIT_RATE, sampleRate * 2);
//          format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE,0);
//          format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectHE);
//          format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectMain);
            if (isEncoder) {
                mCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            } else {
                mCodec.configure(format, null, null, 0);
            }
            mCodec.start();
            mInputBuffer = mCodec.getInputBuffers();
            mOutputBuffer = mCodec.getOutputBuffers();
//            if (!isEncoder) {
//                mediacodec(DECODE_START, 0, DECODE_START.length);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Flowable<byte[]> encode(final byte[] data) {
        AudioTransManager.Log("lzcdemo", "code start");
        return Flowable.create(new FlowableOnSubscribe<byte[]>() {
            @Override
            public void subscribe(final FlowableEmitter<byte[]> e) throws Exception {
                mCallback = new codeDataCallback() {
                    @Override
                    public void completeData(byte[] data) {
                        e.onNext(data);
                    }
                };
                mediacodec(data);
                AudioTransManager.Log("lzcdemo", "code end");
                e.onComplete();
            }
        }, BackpressureStrategy.DROP);
    }

    @Override
    public Flowable<byte[]> decode(final byte[] data) {
        return Flowable.create(new FlowableOnSubscribe<byte[]>() {
            @Override
            public void subscribe(final FlowableEmitter<byte[]> e) throws Exception {
                mCallback = new codeDataCallback() {
                    @Override
                    public void completeData(byte[] data) {
                        e.onNext(data);
                    }
                };
                if (mFirst) {
                    mFirst = false;
                    mediacodec(DECODE_START);
                }
                if(data != null && data.length>0){
                    mediacodec(data);
                }
                e.onComplete();
            }
        }, BackpressureStrategy.BUFFER);
    }


    @Override
    public void release() {
        mFirst = true;
        try {
            if (mCodec != null) {
                mCodec.stop();
                mCodec.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mCodec = null;
    }

    //当data为null时，进行流结束操作
    private void mediacodec(byte[] data) {
        int start = 0;

        if (!isEncoder) {
            start += 7;
        }
        do {
            AudioTransManager.Log("lzcdemo", "code 1");
            int index = mCodec.dequeueInputBuffer(10);
            if (index < 0) {
                return;
            }
            ByteBuffer buffer = mInputBuffer[index];
            buffer.clear();
            if (data == null) {
                mCodec.queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            } else {
                int count = Math.min(data.length - start, buffer.remaining());
                buffer.put(data, start, count);
                start += count;
                mCodec.queueInputBuffer(index, 0, count, 0, 0);
            }
            int outIndex = mCodec.dequeueOutputBuffer(bufferInfo, 10);
            while (outIndex >= 0) {
                AudioTransManager.Log("lzcdemo", "code 2");
                ByteBuffer outBuffer;
                if (Build.VERSION.SDK_INT >= 21) {
                    outBuffer = mCodec.getOutputBuffer(outIndex);
                } else {
                    outBuffer = mOutputBuffer[outIndex];
                }
                if (outBuffer == null)
                    return;
                int outBitsSize = bufferInfo.size;
                outBuffer.position(bufferInfo.offset);
                outBuffer.limit(bufferInfo.offset + outBitsSize);
                try {
                    byte[] outData;
                    if (isEncoder) {
                        outData = new byte[outBitsSize + 7];
                        addADTStoPacket(outData, outBitsSize + 7);
                        outBuffer.get(outData, 7, outBitsSize);
                    } else {
                        outData = new byte[outBitsSize];
                        outBuffer.get(outData, 0, outBitsSize);
                    }
                    mCallback.completeData(outData);
                    mCodec.releaseOutputBuffer(outIndex, false);
                    outIndex = mCodec.dequeueOutputBuffer(bufferInfo, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        } while (start < (data == null ? 0 : data.length));
        AudioTransManager.Log("lzcdemo", "code 3");
    }

    private void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2; //(?)AAC LC
        //39=MediaCodecInfo.CodecProfileLevel.AACObjectELD;
        int freqIdx = 11; //8000Hz 11
        int chanCfg = 2; //CPE
        // fill in ADTS data
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }


    static class AACdataSplit
            implements AudioInterface.DataSplit, Runnable {

        private BlockingQueue<byte[]> myQueue;
        private BlockingQueue<byte[]> dataQueue;
        private byte[] lastData = new byte[0];
        private int start;

        private Thread thread;

        @Override
        public void start() {
            if (thread == null || !thread.isAlive()) {
                thread = new Thread(this);
                thread.start();
            }
        }

        @Override
        public BlockingQueue<byte[]> dataSplit(BlockingQueue<byte[]> queue) {
            this.dataQueue = queue;
            if (myQueue == null){
                this.myQueue = new ArrayBlockingQueue<>(queue.remainingCapacity());
            }
            return this.myQueue;
        }

        @Override
        public void run() {
            while (!thread.isInterrupted()) {
                try {
                    byte[] data = dataQueue.take();
                    findAACHead(data, 0, data.length);
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void findAACHead(byte[] data, int offset, int length) {
            byte[] playData = new byte[length + lastData.length];
            System.arraycopy(lastData, 0, playData, 0, lastData.length);
            System.arraycopy(data, offset, playData, lastData.length, length);
            try {
                start = 0;
                while (true) {
                    int aaclength = findAACLength(playData);
                    if (aaclength < 0 || aaclength > playData.length - start) {
                        lastData = Arrays.copyOfRange(playData, start, playData.length);
                        break;
                    }
                    myQueue.put(Arrays.copyOfRange(playData, start, start + aaclength));
                    start += aaclength;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        private int findAACLength(byte[] data) {
            if (start > data.length - 7) {
                return -1;
            }
            if (data[start] == (byte) 0xff && (byte) (data[start + 1] & 0xf0) == (byte) 0xf0) {
                return ((data[start + 5] & 0xff) >> 5) | ((data[start + 4] & 0xff) << 3);
            } else {
                start++;
                return findAACLength(data);
            }
        }

        @Override
        public void release() {
            thread.interrupt();
            myQueue.clear();
        }
    }


}
