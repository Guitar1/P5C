package com.zzx.libzzxdemo;

import android.util.Log;

import com.zzx.libzzxdemo.audio.AudioTransManager;
import com.zzx.libzzxdemo.audio.AudioValue;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class AudioTransManagerTest {

    @Test
    public void oneceDemo() throws Exception {
        File file = new File("/sdcard/lzctest");
        final int[] send = {0};
        int receive = 0;
        final OutputStream out = new FileOutputStream(file);
        InputStream in = new FileInputStream(file);
        AudioTransManager am = new AudioTransManager(new AudioTransManager.SendAudio() {
            @Override
            public void sendAudioData(byte[] data) {
                if (data.length == 0) {
                    return;
                }
                send[0]++;
                Log.e("lzcdemo", "send " + data.length);
                try {
                    out.write(data);
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        am.setCodecType(AudioValue.CodecType.G726);
        am.startToSend();
        Thread.sleep(3 * 1000);
        Log.e("lzc", "file " + file.length());
        Log.e("lzc", "send times " + send[0]);
        am.endToSend();
        Thread.sleep(1 * 1000);
        am.startToSend();
        Thread.sleep(3 * 1000);
        Log.e("lzc", "file " + file.length());
        Log.e("lzc", "send times " + send[0]);
        am.endToSend();

//        Thread.sleep(1 * 1000);
//        am.startToReceive();
//        int len;
//        byte[] data = new byte[1024];
//        while ((len = in.read(data)) != -1) {
//            receive++;
////            Thread.sleep(20);
////            Log.e("lzc","receive " + len );
//            am.receiveAudioData(Arrays.copyOfRange(data, 0, len));
//        }
////        Log.e("lzc","receive times "+ receive);
//        Thread.sleep(7 * 1000);
//        am.endToReceive();
        Log.e("lzc", "end");
    }


}