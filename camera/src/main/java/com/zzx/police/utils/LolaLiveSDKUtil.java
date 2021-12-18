package com.zzx.police.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;


import com.lolaage.module_rtsp.AudioQuality;
import com.lolaage.module_rtsp.VideoQuality;
import com.lolaage.module_rtsp.codec.EncoderDebugger;
import com.lolaage.module_rtsp.live.LolaLiveListener;
import com.lolaage.module_rtsp.live.LolaLiveManager;
import com.lolaage.module_rtsp.live.SendCmdHelper;
import com.lolaage.module_rtsp.server.RtspServer;
import com.zzx.police.data.Values;
import com.zzx.police.services.CameraService;

import java.io.IOException;
import java.nio.ByteBuffer;


public class LolaLiveSDKUtil {
    private static final String TAG="LolaLiveSDKUtil";
    private Context mContext;
    public boolean isstreame;
    private ByteBuffer[] inputBuffersVideo;
    private ByteBuffer[] inputBuffersAudio;
    public boolean isLolaLive;
    private RtspServer rtspServer;
    private final String H264_VIDEO = "video/avc";
    private final String H264_AUDIO = "audio/mp4a-latm";

    private VideoQuality videoQuality = new VideoQuality(320, 240, 5, 320*240*3);
    private AudioQuality audioQuality = new AudioQuality(8000);
    private Handler myHandler = new Handler();
    private String imei = "";

    public LolaLiveSDKUtil(Context context,String imei){
        mContext = context;
        this.imei = imei;
        Log.d(TAG, "LolaLiveSDKUtil() called with: context = [" + context + "], imei = [" + imei + "]");
        initRtsp();
        LolaLiveManager.getInstance().setCtrlListener(new LolaLiveListener() {
            @Override
            public void netConnectSuccess() {
                Log.d(TAG, "网络连接成功netConnectSuccess() called");
                auth(5000);
            }

            @Override
            public void authDeviceSuccess() {
                Log.d(TAG, "设备验证成功authDeviceSuccess() called");
                isstreame = true;
                startRtsp();

            }
            @Override
            public void authDeviceFail() {
                Log.d(TAG, "设备验证失败authDeviceFail() called");
                auth(1000);

            }
            @Override
            public void deviceNoAuth() {
                Log.d(TAG, "设备未验证deviceNoAuth() called");
            }

            @Override
            public void startRecord(String s) {
                isLolaLive = true;
                Log.d(TAG, "开启录像startRecord() called with: s = [" + s + "]");
                Settings.System.putString(mContext.getContentResolver(),"LolaLive_Record",s);
                if (!CameraService.mIsRecording){
                    sendBroadcast(Values.ACTION_UEVENT_RECORD);
                }
            }

            @Override
            public void stopRecord(String s) {
                Log.d(TAG, "停止录像stopRecord() called with: s = [" + s + "]");
                isLolaLive = true;
                Settings.System.putString(mContext.getContentResolver(),"LolaLive_Record","");
                if (CameraService.mIsRecording){
                    sendBroadcast(Values.ACTION_UEVENT_RECORD);
                }
            }

            @Override
            public void playPicture(String s) {
                isLolaLive = true;
                Settings.System.putString(mContext.getContentResolver(),"LolaLive_Pic",s);
                Log.d(TAG, "拍照playPicture() called with: s = [" + s + "]");
                Intent intent = new Intent();
                intent.setAction(Values.ACTION_UEVENT_CAPTURE);
                intent.putExtra("zzx_state",2);
                context.sendBroadcast(intent);
            }

            @Override
            public void closeRedRay() {
                Log.d(TAG, "关闭红外closeRedRay() called");
                SystemUtil.writeToDevice(0,"/sys/devices/platform/zzx-misc/ir_led_stats");
                SystemUtil.writeToDevice(1,"/sys/devices/platform/zzx-misc/ir_cut_stats");
            }

            @Override
            public void openRedRay() {
                Log.d(TAG, "打开红外openRedRay() called");
                SystemUtil.writeToDevice(1,"/sys/devices/platform/zzx-misc/ir_led_stats");
                SystemUtil.writeToDevice(0,"/sys/devices/platform/zzx-misc/ir_cut_stats");
            }


        }).init(context,true);
    }

    private void sendBroadcast(String action){
        Intent intent = new Intent();
        intent.setAction(action);
        mContext.sendBroadcast(intent);
    }

    private void initRtsp(){
        if (rtspServer == null) {
            rtspServer = new RtspServer(mContext,videoQuality, audioQuality, null, 8554);
            initAudio();
            initVideo();

        }
    }
    private void startRtsp() {
        if (rtspServer == null){
            initRtsp();
        }
        rtspServer.startServer();

    }
    private void auth(long times){
        myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SendCmdHelper.getSendCmdHelper().authDevice(imei);
            }
        },times);
    }


    private OutThread videoOutputThread = null;

    private MediaCodec mMediaCodec;
    private EncoderDebugger debugger;
    public void initVideo() {
        Log.e(TAG, "Video encoded using the MediaCodec API with a buffer");
        try {
//            debugger = EncoderDebugger.debug(PreferenceManager.getDefaultSharedPreferences(mContext), videoQuality.width, videoQuality.height);
            final VideoQuality videoQuality = rtspServer.getVideoQuality();
            mMediaCodec = MediaCodec.createByCodecName(rtspServer.getDebugger().getEncoderName());
            MediaFormat mediaFormat = MediaFormat.createVideoFormat(H264_VIDEO, videoQuality.width, videoQuality.height);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, videoQuality.bitrate);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, videoQuality.framerate);
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, rtspServer.getDebugger().getEncoderColorFormat());
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
            mediaFormat.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR);
            mMediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            startVideo();
        } catch (Throwable e) {
            e.printStackTrace();
            Log.e(TAG, "initVideo"+e.getMessage());
        }
    }
    public void startVideo() {
        try {
            mMediaCodec.start();
            inputBuffersVideo = mMediaCodec.getInputBuffers();
            videoOutputThread = new OutThread(mMediaCodec, 0);
            videoOutputThread.setName("video_thread");
            videoOutputThread.start();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void stopVideo() {
        Log.d(TAG, "stopVideo() called");
        try {
            if (mMediaCodec!=null){
                Log.d(TAG, "mMediaCodec() called");
                mMediaCodec.stop();
                mMediaCodec.release();
            }
            if (videoOutputThread!=null){
                Log.d(TAG, "videoOutputThread() called");
                videoOutputThread.interrupt();
            }
            videoOutputThread = null;
            mMediaCodec = null;
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    long now = System.nanoTime() / 1000, oldnow = now, i = 0;

    public void sendData(byte[] data){
//        if (!isstreame) {
//            return;
//        }
        now = System.nanoTime() / 1000;
        try {
            int bufferIndex = mMediaCodec.dequeueInputBuffer(0);
            Log.e(TAG, "convert onPreviewFrame: " + bufferIndex);
            if (bufferIndex >= 0) {
                inputBuffersVideo[bufferIndex].clear();
                if (data == null)
                    Log.e(TAG, "Symptom of the \"Callback buffer was to small\" problem...");
                else {
                    Log.e(TAG, "convert onPreviewFrame");
                    rtspServer.getDebugger().getNV21Convertor().convert(data, inputBuffersVideo[bufferIndex]);
                }
                mMediaCodec.queueInputBuffer(bufferIndex, 0, inputBuffersVideo[bufferIndex].position(), now, 0);
            } else {
                Log.e(TAG, "No buffer available !");
            }

        } catch (Throwable throwable) {
            throwable.printStackTrace();
            initVideo();
        }
    }
    public void stop(){
        Log.d(TAG, "stop: ");
        isstreame = false;
        if (rtspServer!=null){
            rtspServer.stopServer();
            rtspServer=null;
        }
        stopVideo();
        stopAudio();

    }

    /**
     * *************************************************************************************************************************************************************
     * 音频数据采集测试
     * *************************************************************************************************************************************************************
     */
    private MediaCodec aMediaCodec;
    private AudioRecord mAudioRecord;
    private AudioInputThread audioInputThread;
    private OutThread audioOutputThead;
    private int bufferSize;

    public void initAudio() {
        try {
            final AudioQuality audioQuality = rtspServer.getAudioQuality();
            bufferSize = AudioRecord.getMinBufferSize(audioQuality.samplingRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) * 2;
            mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, audioQuality.samplingRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
            try {
                aMediaCodec = MediaCodec.createEncoderByType(H264_AUDIO);
            } catch (IOException e) {
//                Log.e(TAG, "Unable to instantiate a decoder, trying Google one...");
                try {
                    aMediaCodec = MediaCodec.createByCodecName("OMX.google.aac.encoder");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            MediaFormat format = new MediaFormat();
            format.setString(MediaFormat.KEY_MIME, H264_AUDIO);
            format.setInteger(MediaFormat.KEY_BIT_RATE, audioQuality.bitRate);
            format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
            format.setInteger(MediaFormat.KEY_SAMPLE_RATE, audioQuality.samplingRate);
            format.setInteger(MediaFormat.KEY_BIT_RATE, audioQuality.bitRate);
            format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, bufferSize);
            aMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            startAudio();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void startAudio() {
        try {
            mAudioRecord.startRecording();
            aMediaCodec.start();
            audioInputThread = new AudioInputThread();
            audioInputThread.start();
            audioOutputThead = new OutThread(aMediaCodec, 1);
            audioOutputThead.setName("audio_thread");
            audioOutputThead.start();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    public void stopAudio() {
        Log.d(TAG, "stopAudio() called");
        try {
            if (aMediaCodec!=null){
                aMediaCodec.stop();
                aMediaCodec.release();
            }
            if (mAudioRecord!=null){
                mAudioRecord.stop();
                mAudioRecord.release();
            }
            if (audioInputThread!=null && !audioOutputThead.isInterrupted()){
                audioInputThread.interrupt();
            }
            if (audioOutputThead!=null && !audioOutputThead.isInterrupted()){
                audioOutputThead.interrupt();
            }
            audioInputThread = null;
            audioOutputThead = null;
            aMediaCodec = null;
            mAudioRecord = null;
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private class AudioInputThread extends Thread {
        @Override
        public void run() {
            try {
                int len = 0, bufferIndex = 0;
                inputBuffersAudio = aMediaCodec.getInputBuffers();
                while (!Thread.interrupted()) {
//                    Log.e(TAG, "AudioInputThread run!");
                    bufferIndex = aMediaCodec.dequeueInputBuffer(0);
                    if (bufferIndex >= 0) {
                        inputBuffersAudio[bufferIndex].clear();
                        len = mAudioRecord.read(inputBuffersAudio[bufferIndex], bufferSize);
                        if (len == AudioRecord.ERROR_INVALID_OPERATION || len == AudioRecord.ERROR_BAD_VALUE) {
                            Log.e(TAG, "An error occured with the AudioRecord API !");
                        } else {
                            //Log.v(TAG,"Pushing raw audio to the decoder: len="+len+" bs: "+inputBuffers[bufferIndex].capacity());
                            long pts = System.nanoTime() / 1000;
                            aMediaCodec.queueInputBuffer(bufferIndex, 0, len, pts, 0);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                initAudio();
            }
        }
    }
    public class OutThread extends Thread {
        private MediaCodec mediaCodec;
        private int type;
        ByteBuffer[] outputBuffers;
        private long timeStamp;


        public OutThread(MediaCodec mediaCodec, int type ) {
            this.mediaCodec = mediaCodec;
            this.type = type;
            outputBuffers = mediaCodec.getOutputBuffers();
        }

        @SuppressLint("WrongConstant")
        @Override
        public void run() {
            try {
                MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
                while (!Thread.interrupted()  ) {
//                    if (!isstreame) {
//                        return;
//                    }
//                    Log.e(TAG, "OutThread getSteam");
                    if (mediaCodec!=null){
                        int mIndex = -1;
                        if (getName().equals("audio_thread")) {
                            mIndex = mediaCodec.dequeueOutputBuffer(mBufferInfo, 0);
                        } else {
                            mIndex = mediaCodec.dequeueOutputBuffer(mBufferInfo, 0);
                            if (System.currentTimeMillis() - timeStamp >= 1000) {
                                //1000毫秒后，强制输出关键帧
                                timeStamp = System.currentTimeMillis();
                                if (Build.VERSION.SDK_INT >= 23) {
                                    Bundle params = new Bundle();
                                    params.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0);
                                    mMediaCodec.setParameters(params);
                                }
                            }
                        }

                        ByteBuffer mBuffer ;
                        if (mIndex >= 0) {
                            mBuffer = outputBuffers[mIndex];
                            mBuffer.position(0);
                            int len = mBuffer.remaining();
                            Log.e(TAG, "len: "+len );
                            if (len > 0) {
                                if (type == 0) {
                                    rtspServer.sendVideo(mBuffer, mBufferInfo);
                                } else {
                                    rtspServer.sendAudio(mBuffer, mBufferInfo);
                                }
                                mediaCodec.releaseOutputBuffer(mIndex, false);
                            }
                        }
                    }
                }
                if (mediaCodec != null) {
                    mediaCodec.stop();
                    mediaCodec.release();
                    mediaCodec = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
