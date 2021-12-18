package com.zzx.police.utils;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;

/**@author Tomy
 * Created by Tomy on 2014/11/11.
 */
public class AudioCodec {
    /**
     * audio/3gpp" - AMR narrowband audio //状态异常
     audio/amr-wb" - AMR wideband audio //状态异常
     audio/mpeg" - MPEG1/2 audio layer III //不支持
     audio/mp4a-latm" - AAC audio (note, t //状态异常
     audio/vorbis" - vorbis audio //状态异常
     audio/g711-alaw" - G.711 alaw audio //不支持
     audio/g711-mlaw" - G.711 ulaw audio //不支持
     * */
    public final static String AAC = "audio/mp4a-latm";
    private MediaCodec mCodec;
    private Deque<ByteBuffer> mDataDeque;
    private ByteBuffer[] mInputBuffer;
    private ByteBuffer[] mOutputBuffer;
    private FileInputStream outputStream1;
    private FileOutputStream outputStream2;
    private ByteBuffer outBuffer;
    private byte[] outData;
    private boolean isEncoder = true;

    private OutputStream outputStream;
    public void getOutputStream(OutputStream output){
        outputStream = output;
    }

    public AudioCodec(int sampleRate, boolean isEncoder) {
        this.isEncoder = isEncoder;
        mDataDeque = new ArrayDeque<>(150);
        initMediaCodec(sampleRate);
//        try {
//            outputStream1 = new FileInputStream("/sdcard/test.aac");
//            outputStream2 = new FileOutputStream("/sdcard/test0.aac");
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
    }

    public void release() {
        try {
            mCodec.stop();
            mCodec.release();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initMediaCodec(int sampleRate) {
        try {
//            mCodec = MediaCodec.createByCodecName("OMX.MTK.AUDIO.ENCODER.AAC");
            if (isEncoder) {
                mCodec = MediaCodec.createEncoderByType(AAC);
            } else {
                mCodec = MediaCodec.createDecoderByType(AAC);
            }
            MediaFormat format = MediaFormat.createAudioFormat(AAC, sampleRate, 2);
            format.setInteger(MediaFormat.KEY_BIT_RATE, sampleRate * 2);
//            format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectHE);
            if (isEncoder) {
                mCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            } else {
                mCodec.configure(format, null, null, 0);
            }
            mCodec.start();
            mInputBuffer = mCodec.getInputBuffers();
            mOutputBuffer = mCodec.getOutputBuffers();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void encode(byte[] data) {
        int offset = 0;
        if (data == null || data.length == 0) {
            return;
        }
        /**外循环:判断{@link data}数组是否已被全部编解码.
         * {@link offset} 每次追加放入buffer的长度,直到大于等于{@link data.length}之后完成编解码.
         * */
        do {
            int index = mCodec.dequeueInputBuffer(0);
            if (index < 0) {
                return;
            }
            ByteBuffer buffer = mInputBuffer[index];
            buffer.clear();
            int count = Math.min(data.length - offset, buffer.remaining());
            buffer.put(data, offset, count);
            offset += count;
            mCodec.queueInputBuffer(index, 0, count, 0, 0);

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outIndex = mCodec.dequeueOutputBuffer(bufferInfo, 0);
            /**内循环:判断{@link buffer}一次放入的数据是否已全部编解码完成.
             * 若{@link outIndex} < 0时则完成,反之循环继续取出数据.
             * */
            while (outIndex >= 0) {
                outBuffer = mOutputBuffer[outIndex];
                int outBitsSize = bufferInfo.size;
                int outPacketSize = outBitsSize + 7; // 7 is ADTS size
                outBuffer.position(bufferInfo.offset);
                outBuffer.limit(bufferInfo.offset + outBitsSize);
                try {
                    outData = new byte[outBitsSize];
                    outBuffer.get(outData);

                    byte[] buf = new byte[7];
                    addADTStoPacket(buf, outPacketSize);

                        outputStream.write(buf);
                        if (bufferInfo.offset > 0) {
                            outputStream.write(outData, bufferInfo.offset, outBitsSize - bufferInfo.offset);
//                        outputStream.getChannel().size();
                        } else {
                            outputStream.write(outData);
                        }
                        outputStream.flush();

//                        outputStream2.write(buf);
//                        if (bufferInfo.offset > 0) {
//                            outputStream2.write(outData, bufferInfo.offset, outBitsSize - bufferInfo.offset);
////                        outputStream.getChannel().size();
//                        } else {
//                            outputStream2.write(outData);
//                        }
//                        outputStream2.flush();


                    mCodec.releaseOutputBuffer(outIndex, false);
                    outIndex = mCodec.dequeueOutputBuffer(bufferInfo, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        } while (offset < data.length);
    }

    private void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2; //(?)AAC LC
        //39=MediaCodecInfo.CodecProfileLevel.AACObjectELD;
        int freqIdx = 11; //8000Hz 11
        int chanCfg = 2; //CPE
        // fill in ADTS data
        packet[0] = (byte)0xFF;
        packet[1] = (byte)0xF9;
        packet[2] = (byte)(((profile-1)<<6) + (freqIdx<<2) +(chanCfg>>2));
        packet[3] = (byte)(((chanCfg&3)<<6) + (packetLen>>11));
        packet[4] = (byte)((packetLen&0x7FF) >> 3);
        packet[5] = (byte)(((packetLen&7)<<5) + 0x1F);
        packet[6] = (byte)0xFC;
    }

}
