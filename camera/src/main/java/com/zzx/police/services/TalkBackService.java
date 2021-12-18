package com.zzx.police.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.zzx.police.utils.EventBusUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.List;

/**@author donke
 * Created by donke on 2016/6/14.
 */
public class TalkBackService extends Service {
    public static final String ACTION_CONNECT_SERVICE = "talkConnect";
    public static final String EXTRA_SERVICE_ADDRESS = "address";
    private static final String EVENT_TALK_BACK = "EventTalk";
    private Socket mTalkSocket;
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private boolean mSocketCreated = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return stub;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initEventBus();
    }

    private void initEventBus() {
        EventBusUtils.registerEvent(EVENT_TALK_BACK, this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action == null) {
            return START_NOT_STICKY;
        }
        switch (action) {
            case ACTION_CONNECT_SERVICE:
                String address = intent.getStringExtra(EXTRA_SERVICE_ADDRESS);
                String[] service = address.split(":");
                connectService(service[0], service[1]);
                break;
        }
        return START_NOT_STICKY;
    }

    private void connectService(final String host, final String port) {
        new Thread() {

            @Override
            public void run() {
                super.run();
                try {
                    SocketAddress address = new InetSocketAddress(host, Integer.parseInt(port));
                    mTalkSocket = new Socket();
                    mTalkSocket.bind(null);
                    mTalkSocket.setKeepAlive(true);
                    mTalkSocket.setSoTimeout(5 * 1000);
                    mTalkSocket.setReuseAddress(true);
                    mTalkSocket.connect(address);
                    mInputStream    = mTalkSocket.getInputStream();
                    mOutputStream   = mTalkSocket.getOutputStream();
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                mSocketCreated = true;
                readSocket();
            }
        }.start();
    }

    private void readSocket() {
        try {
            byte[] buffer = new byte[1024];
            int size;
            while ((size = mInputStream.read(buffer)) != -1) {
                String msg = new String(buffer, 0, size);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseEventBus();
    }

    private void releaseEventBus() {
        EventBusUtils.unregisterEvent(EVENT_TALK_BACK, this);
    }

    private ITalkBackService.Stub stub = new ITalkBackService.Stub() {
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public boolean requestTalkBack() throws RemoteException {
            return false;
        }

        @Override
        public List<String> getChannelList() throws RemoteException {
            return null;
        }

        @Override
        public boolean selectChannel(String channel) throws RemoteException {
            return false;
        }

        @Override
        public void startTalkBack() throws RemoteException {

        }

        @Override
        public void stopTalkBack() throws RemoteException {

        }
    };

}
