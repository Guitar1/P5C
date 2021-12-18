package com.zzx.police.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.provider.Settings;

import com.zzx.police.data.Values;
import com.zzx.police.manager.UsbMassManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**@author Tomy
 * Created by Tomy on 2014/8/5.
 */
public class PcSocketUtils {
    private static final String WRITE_POLICE_NUMBER = "DoWritePoliceNumber";
    private static final String READ_POLICE_NUMBER = "DoReadPoliceNumber";

    private static final String READ_TIME   = "DoReadDevTime";
    private static final String UPDATE_TIME = "DoUpdateTime";
    private static final String OPEN_USB = "OpenUDisk";
    private static final String WRITE_DEV_NUMBER    = "DoWriteDevNumber";
    private static final String READ_DEV_NUMBER     = "DoReadDevNumber";
    private static final String WRITE_IP_ADDRESS     = "DoWriteIpAddress";
    private static final String READ_IP_ADDRESS     = "DoReadIpAddress";
    private static final String WRITE_IP_PORT     = "DoWriteIpPort";
    private static final String READ_IP_PORT     = "DoReadIpPort";
    private ServerSocket mServerSocket;
    private BufferedReader mReader;
    private OutputStreamWriter mWriter;
    private String msg = null;
    private UsbMassManager mUsbManager;
    private Context mContext;
    private static boolean isPcSocketCreated = false;
    private Intent mStorageIntent;
    private String mPoliceNum;
    private String mDeviceNum;
    private ContentResolver mResolver;

    public static boolean isPcSocketCreated() {
        return isPcSocketCreated;
    }

    public PcSocketUtils(Context context) {
        mContext = context;
        mResolver = mContext.getContentResolver();
        this.mUsbManager = new UsbMassManager(context);
        mStorageIntent = new Intent(Values.ACTION_STORAGE_STATE);
        mStorageIntent.putExtra("state", -1);
    }

    public void startSocket() {
        new Thread() {
            @Override
            public void run() {
                try {
                    mServerSocket = new ServerSocket();
                    SocketAddress localAddress = new InetSocketAddress(5910);
                    mServerSocket.bind(localAddress);
                    while (true) {
                        if (mReader != null) {
                            mReader.close();
                            mReader = null;
                        }
                        Socket clientSocket = mServerSocket.accept();
                        mReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        mWriter = new OutputStreamWriter(clientSocket.getOutputStream());
                        isPcSocketCreated = true;
                        while (((msg = mReader.readLine()) != null)) {
                            parserMsg(msg);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (mReader != null) {
                            mReader.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        mWriter.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        mServerSocket.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    isPcSocketCreated = false;
                }
                super.run();
            }
        }.start();
    }

    private void parserMsg(String msg) {
        String[] tmp = msg.split(" ");
        String cmd = tmp[0];
        switch (cmd) {
            case WRITE_DEV_NUMBER:
                writeDeviceNum(tmp[1]);
                break;
            case READ_DEV_NUMBER:
//                String deviceNum = PreferenceManager.getDefaultSharedPreferences(mContext).getString(Values.DEVICE_NUMBER, "");
                String deviceNum = DeviceUtils.getDeviceNum(mContext);
                if (!deviceNum.equals(Values.DEVICE_DEFAULT_NUM))
                    replyMsg(deviceNum);
                else {
                    replyMsg("");
                }
                break;
            case UPDATE_TIME:
                setSystemTime(tmp[1], tmp[2]);
                break;
            case READ_TIME:
                replyTime();
                break;
            case OPEN_USB:
                openUsb();
                break;
            case READ_POLICE_NUMBER:
//                String policeNum = PreferenceManager.getDefaultSharedPreferences(mContext).getString(Values.POLICE_NUMBER, "");
                String policeNum = DeviceUtils.getPoliceNum(mContext);
                if (!policeNum.equals(Values.POLICE_DEFAULT_NUM)) {
                    replyMsg(policeNum);
                } else {
                    replyMsg("");
                }
                break;
            case WRITE_POLICE_NUMBER:
                writePoliceNum(tmp[1]);
                break;
            case WRITE_IP_ADDRESS:
                writeSettings(Values.SERVICE_IP_ADDRESS, tmp[1]);
                break;
            case WRITE_IP_PORT:
                try {
                    int port = Integer.parseInt(tmp[1]);
                    writeSettings(Values.SERVICE_IP_PORT, port + "");
                } catch (Exception e) {
                    replyFailed();
                    return;
                }
                break;
            case READ_IP_ADDRESS:
                readSettings(Values.SERVICE_IP_ADDRESS);
                break;
            case READ_IP_PORT:
                readSettings(Values.SERVICE_IP_PORT);
                break;
        }
    }

    private void writeSettings(String key, String value) {
        if (Settings.System.putString(mResolver, key, value)) {
            replySuccess();
        } else {
            replyFailed();
        }
    }

    private void readSettings(String key) {
        String value = Settings.System.getString(mResolver, key);
        if (value == null || value.equals("")) {
            replyMsg("");
        } else {
            replyMsg(value);
        }
    }

    private void replyTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss", Locale.getDefault());
        replyMsg(format.format(System.currentTimeMillis()));
    }

    private void writePoliceNum(String policeNumber) {
        if (!checkPoliceNum(policeNumber)) {
            replyFailed();
            return;
        }
//        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putString(Values.POLICE_NUMBER, policeNumber).commit();
        Settings.System.putString(mResolver, Values.POLICE_NUMBER, policeNumber);
        mPoliceNum = policeNumber;
        EventBusUtils.postEvent(Values.BUS_EVENT_RESET_SOCKET, Values.SOCKET_RECONNECTION);
        replySuccess();
        checkWriteNumber();
    }

    private boolean checkPoliceNum(String policeNumber) {
        return policeNumber.matches("[0-9]{6}");
    }

    private void openUsb() {
        EventBusUtils.postEvent(Values.BUS_EVENT_CAMERA, Values.STORAGE_NOT_ENOUGH);
        sendBroadcast();
        deleteAndroidDir();
        mUsbManager.enableUsbMass();
        replySuccess();
    }

    private void deleteAndroidDir() {
        /*File file = new File("/storage/sdcard0/Android");
        deleteFile(file);
        file = new File("/storage/sdcard1/Android");
        deleteFile(file);*/
        deleteDir("/storage/sdcard1/Android");
        deleteDir("/storage/sdcard0/Android");
    }

    private void deleteDir(String dir) {
        File file = new File(dir);
        if (file.exists() && file.isDirectory()) {
            try {
                Runtime runtime = Runtime.getRuntime();
                Process process = runtime.exec("rm -rf " + dir);
                process.waitFor();
                process.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void deleteFile(File file) {
        if (!file.exists())
            return;
        if (file.isFile()) {
            file.delete();
        } else if (file.isDirectory()) {
            File[] fileList = file.listFiles();
            if (fileList == null) {
                file.delete();
                return;
            }
            if (fileList.length == 0) {
                file.delete();
                return;
            }
            for (File file1 : fileList) {
                deleteFile(file1);
            }
            file.delete();
        }
    }

    private void sendBroadcast() {
        mContext.sendBroadcast(mStorageIntent);
    }

    private void setSystemTime(String date, String time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd_kk:mm:ss", Locale.getDefault());
        try {
            SystemClock.setCurrentTimeMillis(format.parse(date + "_" + time).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            replyFailed();
            return;
        }
        replySuccess();
    }

    private void writeDeviceNum(String num) {
        if (!checkDeviceNum(num)) {
            replyFailed();
            return;
        }
        try {
            Settings.System.putString(mResolver, Values.DEVICE_NUMBER, num.substring(num.length() - 5));
        } catch (Exception e) {
            e.printStackTrace();
        }
        mDeviceNum = num;
        replySuccess();
        checkWriteNumber();
    }

    private void checkWriteNumber() {
        if (mDeviceNum != null && mPoliceNum != null) {
            writeInfo(mDeviceNum + " " + mPoliceNum, Values.PATH_POLICE_NUMBER);
        }
    }

    private boolean writeInfo(String num, String path) {
        boolean success = false;
        File file = new File(path);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            byte[] buffer = num.getBytes();
            outputStream.write(buffer, 0, buffer.length);
            outputStream.flush();
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
            replyFailed();
            success = false;
        } finally {
            try {
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return success;
    }

    private void replyMsg(String reply) {
        try {
            mWriter.write("Success " + reply + "\n");
            mWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void replyFailed() {
        try {
            mWriter.write("Failed\n");
            mWriter.flush();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private void replySuccess() {
        try {
            mWriter.write("Success\n");
            mWriter.flush();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private boolean checkDeviceNum(String num) {
        try {
            String[] numbers = num.split("-");
            //if (numbers[0].equals("DSJ") && numbers[1].matches("([A-Za-z][0-9])") && numbers[2].matches("[A-Za-z0-9]{5}")) {
            if (numbers[0].equals("DSJ") && numbers[2].matches("[A-Za-z0-9]{5}")) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

}
