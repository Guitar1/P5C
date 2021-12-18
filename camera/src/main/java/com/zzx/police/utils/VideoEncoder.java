package com.zzx.police.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.zzx.police.data.BaseCmd;
import com.zzx.police.data.DataHeader;
import com.zzx.police.data.Values;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**OutputStream为编码后输出.
 * @see #initVideoEncoder(int, int, int, int, int) 为初始化编码器.
 * @see #encode(byte[]) 为传入与初始化中分辨率相同的一帧数据,编完会自动通过OutputStream输出.
 * @see #release() 释放相关资源.
 *
 * @author Tomy
 * Created by Tomy on 2014/6/10.
 */
public class VideoEncoder {
    private static final String ENCODER_VIDEO_AVC   = "video/avc";
    private static final String TAG = "VideoEncoder: ";
    private MediaCodec mCodec = null;
    private ByteBuffer[] mInputBuffer   = null;
    private ByteBuffer[] mOutputBuffer  = null;
    private OutputStream mOutputStream = null;
    private int mWidth;
    private int mHeight;
    private int mColorFormat = 0;

    private int mBitRate;
    private int mFrameRate;
    private int mKeyRate;
    private ContentResolver mContext;
    private DataHeader mHeader;
    public VideoEncoder(Context context, OutputStream outputStream, int width, int height) {
        Values.LOG_I(TAG, "outputStream = " + outputStream);
        mHeader = new DataHeader();
        mHeader.mFrameType = 0xdc;
        mOutputStream = outputStream;
        mContext = context.getContentResolver();
        getParameters();
        initVideoEncoder(width, height, mBitRate, mFrameRate, mKeyRate);
    }

    private void getParameters() {
        mBitRate = Settings.System.getInt(mContext, MediaFormat.KEY_BIT_RATE, 500000);
        mFrameRate = Settings.System.getInt(mContext, MediaFormat.KEY_FRAME_RATE, 10);
        mKeyRate = Settings.System.getInt(mContext, MediaFormat.KEY_I_FRAME_INTERVAL, 1);
    }

    public void release() {
        try {
            if (mCodec != null) {
                mCodec.stop();
                mCodec.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mOutputStream != null) {
            try {
                mOutputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mOutputStream = null;
        }
    }

    /**@param width 宽度像素点
     * @param height 高度像素点
     * @param bitRate 比特率 {@link MediaFormat#KEY_BIT_RATE}
     * @param frameRate {@link MediaFormat#KEY_FRAME_RATE}
     * @param frameInterval 关键帧间隔 {@link MediaFormat#KEY_I_FRAME_INTERVAL}
     * */
    public void initVideoEncoder(int width, int height, int bitRate, int frameRate, int frameInterval) {
        /*try {
            File file = new File("/sdcard/test.3gp");
            if (!file.exists()) {
                file.createNewFile();
            }
            mOutputStream = new FileOutputStream(file, false);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }*/
        mWidth = width;
        mHeight = height;
        try {
            mCodec = MediaCodec.createEncoderByType(ENCODER_VIDEO_AVC);
        } catch (Exception e) {
            e.printStackTrace();
        }
        MediaFormat format;
        format = MediaFormat.createVideoFormat(ENCODER_VIDEO_AVC, mWidth, mHeight);
        if (format == null) {
            return;
        }
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar);
//        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, frameInterval);
        try {
            MediaCodecInfo info = selectCodec(ENCODER_VIDEO_AVC);
            int[] type = info.getCapabilitiesForType(ENCODER_VIDEO_AVC).colorFormats;
            for (int one : type) {
                switch (one) {
                    case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
                    case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
                        Log.e("VideoEncoder", "Media type :" + Arrays.toString(type) + "  chooose :" + one + " support:" + SupportAvcCodec());
                        Log.e("VideoEncoder", "Media data :" + mBitRate + " " + mFrameRate + " " + mKeyRate);
                        mColorFormat = one;
                        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, one);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            mCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            Values.LOG_I(TAG, "colorFormat = YUV420Planar");
        } catch (Exception e) {
            e.printStackTrace();
//            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
//            mCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
//            LogUtils.Info(TAG, "colorFormat = YUV420SemiPlanar");
        }
        mCodec.start();
        mInputBuffer    = mCodec.getInputBuffers();
        mOutputBuffer   = mCodec.getOutputBuffers();
    }

    private void selectFormat(MediaCodecInfo codecInfo) {
        int colorFormat = 0;
        MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(ENCODER_VIDEO_AVC);
        for (int i = 0; i < capabilities.colorFormats.length; i++) {
            int format = capabilities.colorFormats[i];
            Values.LOG_W(TAG, "Using color format " + format);
        }
    }

    private static MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {
                continue;
             }
            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }

    //是否支持h264
    private static boolean SupportAvcCodec() {
        if (Build.VERSION.SDK_INT >= 18) {
            for (int j = MediaCodecList.getCodecCount() - 1; j >= 0; j--) {
                MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(j);

                String[] types = codecInfo.getSupportedTypes();
                for (int i = 0; i < types.length; i++) {
                    if (types[i].equalsIgnoreCase("video/avc")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    //时间戳
    private long computePresentationTime(long frameIndex) {
        return 132 + frameIndex * 1000000 / mFrameRate;
    }

    private ByteBuffer mOutBuffer;
    private byte[] mOutData;
    private long time = 1;

    public void encode(byte[] data) {
        int offset = 0;
        if (data == null || data.length == 0) {
            return;
        }
        switch (mColorFormat) {
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
                data = swapYV12toI420(data, mWidth, mHeight);
                break;
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
                data = swapYV12to420Sp(data, mWidth, mHeight);
                break;
            default:
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
            mCodec.queueInputBuffer(index, 0, count, computePresentationTime(time++), 0); //加入时间戳，否则某些手机无法编码
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outIndex = mCodec.dequeueOutputBuffer(bufferInfo, 0);
            /**内循环:判断{@link buffer}一次放入的数据是否已全部编解码完成.
             * 若{@link outIndex} < 0时则完成,反之循环继续取出数据.
             * */
//            boolean isFirst = true;
            while (outIndex >= 0) {
                if (mOutputStream == null) {
                    break;
                }
                mOutBuffer = mOutputBuffer[outIndex];
                mOutBuffer.position(bufferInfo.offset);
                mOutBuffer.limit(bufferInfo.offset + bufferInfo.size);
                try {
                    mOutData = new byte[bufferInfo.size];
                    mOutBuffer.get(mOutData);
                    if (mOutputStream != null) {
                        if (bufferInfo.offset > 0) {
                            mOutputStream.write(mOutData, bufferInfo.offset, bufferInfo.size - bufferInfo.offset);
                        } else {
                            mOutputStream.write(mOutData);
                        }
                        Value.LOG_E("video encoder","Send video data");
                        mOutputStream.flush();
                    }
                    mCodec.releaseOutputBuffer(outIndex, false);
                    outIndex = mCodec.dequeueOutputBuffer(bufferInfo, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        } while (offset < data.length);
    }

    private void encode_old(byte[] data) {
        if (data == null)
            return;
        data = swapYV12toI420(data, mWidth, mHeight);
//        byte[] out = new byte[data.length];
//        swapYV12toI420(data, out, 640, 480);
        /**获得可以传入多媒体数据的索引
         * */
        int inputBufferIndex = mCodec.dequeueInputBuffer(0);

        mInputBuffer    = mCodec.getInputBuffers();
        mOutputBuffer   = mCodec.getOutputBuffers();
        if (inputBufferIndex >= 0) {
            /**根据索引取出该Buffer,然后将数据放入.再调用 {@link android.media.MediaCodec#queueInputBuffer(int, int, int, long, int)}方法将该索引的buffer压入队列.
             * */
            ByteBuffer buffer = mInputBuffer[inputBufferIndex];
            buffer.clear();
            buffer.put(data);
            mCodec.queueInputBuffer(inputBufferIndex, 0, data.length, 0, 0);
        } else {
            return;
        }
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputIndex = mCodec.dequeueOutputBuffer(bufferInfo, 0);
        do {
            if (outputIndex >= 0) {
                ByteBuffer outBuffer = mOutputBuffer[outputIndex];
                try {
                    byte[] outData = new byte[bufferInfo.size];
                    outBuffer.get(outData);
                    if (bufferInfo.offset != 0) {
                        mOutputStream.write(outData, bufferInfo.offset, outData.length
                                - bufferInfo.offset);
                        /*mFos.write(mOutData, bufferInfo.offset, mOutData.length
                                - bufferInfo.offset);*/
                    } else {
                        mOutputStream.write(outData, 0, outData.length);
//                        mFos.write(mOutData, 0, mOutData.length);
                    }
//                    Values.LOG_I(TAG,  "size = " + mOutData.length);
//                    mFos.flush();
                    mOutputStream.flush();
                    mCodec.releaseOutputBuffer(outputIndex, false);
                    outputIndex = mCodec.dequeueOutputBuffer(bufferInfo,
                            0);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            } else if (outputIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                mOutputBuffer = mCodec.getOutputBuffers();
            } else if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                MediaFormat format = mCodec.getOutputFormat();
            }
        } while (outputIndex >= 0);
    }

    public byte[] swapYV12toI420(byte[] yv12bytes, int width, int height) {
        byte[] i420bytes = new byte[yv12bytes.length];
        System.arraycopy(yv12bytes, 0, i420bytes, 0, width * height);
        System.arraycopy(yv12bytes, width * height + (width / 2 * height / 2), i420bytes, width * height, width / 2 * height / 2);
        System.arraycopy(yv12bytes, width * height, i420bytes, width * height + (width / 2 * height / 2), width / 2 * height / 2);
        return i420bytes;
    }

    public byte[] swapYV12to420Sp(byte[] yv12bytes, int width, int height) {
        byte[] i420bytes = new byte[yv12bytes.length];
        System.arraycopy(yv12bytes, 0, i420bytes, 0, width * height);
        for (int i = width * height; i < yv12bytes.length; i++) {
            i420bytes[i + 1] = yv12bytes[(i - width * height) / 2 + width * height];
            i420bytes[i] = yv12bytes[(i - width * height) / 2 + width * height + width * height / 4];
            i++;
        }
        return i420bytes;
    }
}
