package com.zzx.police.utils;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;

import com.zzx.police.MainActivity;
import com.zzx.police.R;
import com.zzx.police.ZZXConfig;
import com.zzx.police.data.Values;
import com.zzx.police.manager.UsbMassManager;
import com.zzx.police.services.CameraService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.zzx.police.MainActivity.CUT_VIDEO_RECORD_TIME;
import static com.zzx.police.data.Values.ADMIN_PSW;
import static com.zzx.police.data.Values.IsOpenDialer;
import static com.zzx.police.data.Values.LOOP_RECORD;
import static com.zzx.police.data.Values.SERIAL_NUMBER;
import static com.zzx.police.data.Values.SUPER_PSW;
import static com.zzx.police.data.Values.USER_PSW;

/**
 * Created by lzc on 2016/11/28 0028.
 */

public class PcSocketNewUtils extends BroadcastReceiver {

    private static final String TAG = "PcSocketNewUtils";
    static final boolean Debug = true;
    /**
     * 设备识别码,5位
     */
//    private final String IDCode = "byand";
    /**
     * 产品序号，7位
     */
//    private final String Serial = "0000000";

    private final String BroadAction = "com.zzx.sdk";
    private final String BroadExtra = "port";
    private static boolean isPcSocketCreated = false;

    private Context mContext;
    private ServerSocket mServerSocket;
    private Socket clientSocket;
    private BufferedReader mReader;
    private OutputStreamWriter mWriter;


    //    private static PcSocketNewUtils newUtils = null;
//
//    public static PcSocketNewUtils getInstance(Context context){
//        if (newUtils == null){
//            newUtils = new PcSocketNewUtils(context);
//        }
//        return newUtils;
//    }
    public void Close() {
        Close(false);
    }

    public PcSocketNewUtils(Context context) {
        Value.LOG_E(TAG, "new function");
        mContext = context;

        Start();

    }

    private void Start() {
        synchronized (this) {
            if (isPcSocketCreated) {
                return;
            }
            Value.LOG_E(TAG, "start broadcast register");
            IntentFilter intent = new IntentFilter();
            intent.addAction(BroadAction);
            mContext.registerReceiver(this, intent);
        }
    }

    public static boolean isPcSocketCreated() {
        return isPcSocketCreated;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Value.LOG_E(TAG, "receive broadcas:" + intent.toString());
        if (intent.getAction().equals(BroadAction)) {
            StartSocket(intent.getIntExtra(BroadExtra, -1));
        }
    }

    private void StartSocket(final int port) {
        if (port <= 0) {
            return;
        }
        if (isPcSocketCreated) {
            Value.LOG_E(TAG, "close last Server");
            Close(true);
        }
        Value.LOG_E(TAG, "start Server:" + port);
        new Thread() {
            @Override
            public void run() {
                try {
                    mServerSocket = new ServerSocket();
                    SocketAddress localAddress = new InetSocketAddress(port);
                    mServerSocket.bind(localAddress);
                    while (true) {
                        if (mReader != null) {
                            mReader.close();
                            mReader = null;
                        }
                        isPcSocketCreated = true;
                        clientSocket = mServerSocket.accept();
                        mReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        mWriter = new OutputStreamWriter(clientSocket.getOutputStream());
                        String msg;
                        int index;
                        while (((msg = mReader.readLine()) != null) && isPcSocketCreated) {
                            index = msg.indexOf("{");
                            Value.LOG_E(TAG, "read:" + msg + " " + index);
                            parserMsg(msg.substring(index < 0 ? 0 : index));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (isPcSocketCreated)
                        Close(true);
                }
                super.run();
            }
        }.start();
    }

    private void Close(boolean start) {
        Value.LOG_E(TAG, "close all");
        isPcSocketCreated = false;
        Value.LOG_E(TAG, "close client");
        try {
            if (clientSocket != null) {
                clientSocket.close();
                clientSocket = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Value.LOG_E(TAG, "close reader");
        try {
            if (mReader != null) {
                mReader.close();
                mReader = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Value.LOG_E(TAG, "close writer");
        try {
            if (mWriter != null) {
                mWriter.close();
                mWriter = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Value.LOG_E(TAG, "close server");
        try {
            if (mServerSocket != null) {
                mServerSocket.close();
                mServerSocket = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Value.LOG_E(TAG, "close unregister");
        try {
            mContext.unregisterReceiver(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (start) {
            Value.LOG_E(TAG, "close start");
            Start();
        }
//        newUtils = null;
    }

    private void parserMsg(String msg) {
        Value.LOG_E(TAG, "receive:" + msg + " " + msg.indexOf("{"));
        try {
            JSONObject jsonObject = new JSONObject(msg);
            JSONObject headJson = jsonObject.getJSONObject(Value.Head);
            String idstr = headJson.getString(Value.FunctionID);
            int id = Integer.parseInt(idstr);
            JsonZZX replyJson = new JsonZZX();
            Value.LOG_E(TAG, "switch:" + id + " " + idstr);
            switch (id) {
                case Value.InitDevice:
                    GetIDCode(replyJson);
                    break;
                case Value.GetIDCode:
                    GetIDCode(replyJson);
                    break;
                case Value.GetZFYInfo:
                    GetZFYInfo(jsonObject.getJSONObject(Value.Body), replyJson);
                    break;
                case Value.WriteZFYInfo:
                    WriteZFYInfo(jsonObject.getJSONObject(Value.Body), replyJson);
                    break;
                case Value.SyncDevTime:
                    SyncDevTime(jsonObject.getJSONObject(Value.Body), replyJson);
                    break;
                case Value.SetMSDC:
                    SetMSDC(jsonObject.getJSONObject(Value.Body), replyJson);
                    break;
                case Value.ReadDeviceResolution:
                    ReadDeviceResolution(jsonObject.getJSONObject(Value.Body), replyJson);
                    break;
                case Value.ReadDeviceBatteryDumpEnergy:
                    ReadDeviceBatteryDumpEnergy(jsonObject.getJSONObject(Value.Body), replyJson);
                    break;
                case Value.GPSSET:
                    GPSSet(jsonObject.getJSONObject(Value.Body), replyJson);
                    break;
                case Value.RetrieveContact:
                    RetrieveContact(jsonObject.getJSONObject(Value.Body), replyJson);
                    break;
                case Value.CreateContact:
                    CreateContact(jsonObject.getJSONObject(Value.Body), replyJson);
                    break;
                case Value.UpdateContact:
                    UpdateContact(jsonObject.getJSONObject(Value.Body), replyJson);
                    break;
                case Value.DeleteContact:
                    DeleteContact(jsonObject.getJSONObject(Value.Body), replyJson);
                    break;
                case Value.LoopVideo:
                    LoopVideo(jsonObject.getJSONObject(Value.Body), replyJson);
                    break;
                case Value.WriteSerial:
                    WirteSerialNum(jsonObject.getJSONObject(Value.Body), replyJson);
                    break;
                case Value.GetDevTime:
                    GetDevTime(jsonObject.getJSONObject(Value.Body), replyJson);
                    break;
                case Value.SendPassword:
                    SendPwd(jsonObject.getJSONObject(Value.Body), replyJson);
                    break;
                case Value.GetDiskStorage:
                    GetDiskStorage(jsonObject.getJSONObject(Value.Body), replyJson);
                    break;
                case Value.GetGPSStatus:
                    GetGPSStatus(jsonObject.getJSONObject(Value.Body), replyJson);
                    break;
                case Value.GetLoopStatus:
                    GetLoopStatus(jsonObject.getJSONObject(Value.Body), replyJson);
                    break;
                case Value.LoginStatus:
                    DoLogin(jsonObject.getJSONObject(Value.Body), replyJson);
                    break;
                case Value.GetSerial:
                    GetSerial(jsonObject.getJSONObject(Value.Body), replyJson);
                    break;
                case Value.GetDialStatus:
                    GetDialStatus(jsonObject.getJSONObject(Value.Body), replyJson);
                    break;
                case Value.SetDialStatus:
                    SetDialStatus(jsonObject.getJSONObject(Value.Body), replyJson);
                    break;
                case Value.RetrieveUsers:
                    RetrieveUsers(jsonObject.getJSONObject(Value.Body), replyJson);
                    break;
                case Value.CreateUser:
                    CreateUser(jsonObject.getJSONObject(Value.Body), replyJson);
                    break;
                case Value.UpdateUser:
                    UpdateUser(jsonObject.getJSONObject(Value.Body), replyJson);
                    break;
                case Value.DeleteUser:
                    DeleteUser(jsonObject.getJSONObject(Value.Body), replyJson);
                    break;
                case Value.FindLoginUser:
                    FindLoginUser(jsonObject.getJSONObject(Value.Body), replyJson);
                    break;
                default:
                    return;
            }
            replyJson.AddHead(id);
            replyMsg(replyJson.getJson());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void replyMsg(JSONObject jsonObject) {
        if (mWriter == null) {
            return;
        }
        try {
            Value.LOG_E(TAG, "reply:" + jsonObject.toString());
            mWriter.write(jsonObject.toString() + "\r\n");
            mWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 核查管理员密码
     */
    private boolean CheckPassWord(String pass, JsonZZX jsonZZX) {
        String password = Settings.System.getString(mContext.getContentResolver(), Values.ADMIN_PSW);
        if (password == null || password.equals("")) {
            password = Values.DEVICE_DEFAULT_PASSWORD;
        }
        Value.LOG_E(TAG, "password:" + password);
        boolean ispass = password.equals(pass) || Values.DEVICE_H9_DEFAULT_SUPER_PASSWORD.equals(pass);
        jsonZZX.AddBody(Value.PassWord, ispass);
        return ispass;
    }

    /**
     * 获取生产厂代码及产品型号代码
     */
    private void GetIDCode(JsonZZX jsonZZX) {
        String IDcodeNum = DeviceUtils.getIDCodeName(mContext);
        jsonZZX.AddBody(Value.IDCode, IDcodeNum);
    }

    /**
     * 获取记录仪信息
     */
    private void GetZFYInfo(JSONObject jsonObject, JsonZZX jsonZZX) {
        try {
            if (CheckPassWord(jsonObject.getString(Value.PassWord), jsonZZX)) {
                String SerialNum = DeviceUtils.getSerialName(mContext);
                String policeNum = DeviceUtils.getPoliceNum(mContext);
                String userNum = DeviceUtils.getUserNum(mContext);
                String modelNum = DeviceUtils.getModelName(mContext);
                String deviceNum = DeviceUtils.getDeviceNum(mContext).substring(DeviceUtils.getDeviceNum(mContext).length()-5,DeviceUtils.getDeviceNum(mContext).length());
                jsonZZX.AddBody(Value.PoliceNo, policeNum);
                String policeName = null;
                String gourpNo = null;
                String groupName = null;

                policeName = Settings.System.getString(mContext.getContentResolver(), Values.POLICE_NAME);
                gourpNo = Settings.System.getString(mContext.getContentResolver(), Values.GOURP_NO);
                groupName = Settings.System.getString(mContext.getContentResolver(), Values.GROUP_NAME);
                if (ZZXConfig.isP5X_SZLJ){
                    jsonZZX.AddBody(Value.Serial, deviceNum);
                    jsonZZX.AddBody(Value.PoliceName,CheckNUllSetDefault(userNum, "00000000000"));
                    jsonZZX.AddBody(Value.GroupNo, CheckNUllSetDefault(modelNum, "00000"));
                }else {
                    jsonZZX.AddBody(Value.Serial, SerialNum);
                    jsonZZX.AddBody(Value.PoliceName,CheckNUllSetDefault(policeName, "000000"));
                    jsonZZX.AddBody(Value.GroupNo, CheckNUllSetDefault(gourpNo, "000000"));
                }
                jsonZZX.AddBody(Value.GroupName, CheckNUllSetDefault(groupName, ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String CheckNUllSetDefault(String values, String defalut) {
        if (values == null) {
            return defalut;
        } else {
            return values;
        }
    }

    /**
     * 写入记录仪信息
     */
    private void WriteZFYInfo(JSONObject jsonObject, JsonZZX jsonZZX) {
        try {
            if (CheckPassWord(jsonObject.getString(Value.PassWord), jsonZZX)) {
                Settings.System.putString(mContext.getContentResolver(), Values.POLICE_NUMBER, jsonObject.getString(Value.PoliceNo));
                Settings.System.putString(mContext.getContentResolver(), ConfigXMLParser.CFG_SETTING_SERIAL, jsonObject.getString(Value.Serial));
                String policeName = jsonObject.getString(Value.PoliceName);
                String policeNum = jsonObject.getString(Value.PoliceNo);
                String serial = jsonObject.getString(Value.Serial);
                String gourpNo = jsonObject.getString(Value.GroupNo);
                String groupName = jsonObject.getString(Value.GroupName);
                Settings.System.putString(mContext.getContentResolver(),Values.POLICE_NUMBER,policeNum);
                if (ZZXConfig.isP5X_SZLJ){
                    Settings.System.putString(mContext.getContentResolver(),Values.USER_NUMBER,policeName);
                    Settings.System.putString(mContext.getContentResolver(),Values.DEVICE_NUMBER,serial);
                    Settings.System.putString(mContext.getContentResolver(),Values.MODEL_NAME,gourpNo);
                }else {
                    Settings.System.putString(mContext.getContentResolver(), Values.POLICE_NAME, policeName);
                    Settings.System.putString(mContext.getContentResolver(), Values.GOURP_NO, gourpNo);
                }
                Settings.System.putString(mContext.getContentResolver(), Values.GROUP_NAME, groupName);
                EventBusUtils.postEvent(Values.BUS_EVENT_SOCKET_CONNECT, Values.SERVICE_REGISTER_SUCCESS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 同步执法记录仪时间
     */
    private void SyncDevTime(JSONObject jsonObject, JsonZZX jsonZZX) {
        try {
            if (CheckPassWord(jsonObject.getString(Value.PassWord), jsonZZX)) {
                String date = jsonObject.getString(Value.CurrentTime);
                Value.LOG_E(TAG, "current time" + date);
                SystemClock.setCurrentTimeMillis(Long.parseLong(date));
                jsonZZX.AddBody(Value.ResultReply, true);
            }
        } catch (Exception e) {
            jsonZZX.AddBody(Value.ResultReply, false);
            e.printStackTrace();
        }
    }

    /**
     * 设置为移动磁盘模式
     */
    private void SetMSDC(JSONObject jsonObject, JsonZZX jsonZZX) {
        try {
            if (CheckPassWord(jsonObject.getString(Value.PassWord), jsonZZX)) {
                if (CameraService.mRecordTime <= CUT_VIDEO_RECORD_TIME && Settings.System.getInt(mContext.getContentResolver(), Values.VIDEO_CUT, 0) == 0) {
                    //      MainActivity.isStorageNotEnought = false;
                    EventBusUtils.postEvent(Values.BUS_EVENT_CAMERA, Values.STORAGE_NOT_ENOUGH);
                    Intent mStorageIntent = new Intent(Values.ACTION_STORAGE_STATE);
                    mStorageIntent.putExtra("state", -1);
                    mContext.sendBroadcast(mStorageIntent);
                    UsbMassManager massManager = new UsbMassManager(mContext);
                    massManager.enableUsbMass();
                    jsonZZX.AddBody(Value.ResultReply, true);
                } else {
                    jsonZZX.AddBody(Value.ResultReply, false);
                }
            }
        } catch (Exception e) {
            jsonZZX.AddBody(Value.ResultReply, false);
            e.printStackTrace();
        }
    }

    /**
     * 读取当前录像分辨率
     */
    private void ReadDeviceResolution(JSONObject jsonObject, JsonZZX jsonZZX) {
        try {
            if (CheckPassWord(jsonObject.getString(Value.PassWord), jsonZZX)) {
                String ratio = RemotePreferences.getRemotePreferences(mContext)
                        .getString(mContext.getResources().getString(R.string.key_ratio_record), Values.RATIO_RECORD_DEFAULT_VALUE);
                String[] ratios = ratio.split("x");
                jsonZZX.AddBody(Value.VideoWidth, Integer.parseInt(ratios[0]))
                        .AddBody(Value.VideoHeight, Integer.parseInt(ratios[1]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取电量
     */
    private void ReadDeviceBatteryDumpEnergy(JSONObject jsonObject, JsonZZX jsonZZX) {
        try {
            if (CheckPassWord(jsonObject.getString(Value.PassWord), jsonZZX)) {
                jsonZZX.AddBody(Value.Battery, MainActivity.battery);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 开关GPS
     */
    private void GPSSet(JSONObject jsonObject, JsonZZX jsonZZX) {
        try {
            if (CheckPassWord(jsonObject.getString(Value.PassWord), jsonZZX)) {
                if (jsonObject.getString(Value.GPS).equals("1")) {
                    openGPSSettings();
                } else {
                    closeGPSSetting();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean getGPsState() {
        return Settings.Secure.isLocationProviderEnabled(
                mContext.getContentResolver(), LocationManager.GPS_PROVIDER);
    }

    private void openGPSSettings() {
        Value.LOG_E(TAG, "open gps");
        try {
            Settings.Secure.setLocationProviderEnabled(
                    mContext.getContentResolver(), LocationManager.GPS_PROVIDER, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeGPSSetting() {
        Value.LOG_E(TAG, "close gps");
        try {
            Settings.Secure.setLocationProviderEnabled(
                    mContext.getContentResolver(), LocationManager.GPS_PROVIDER, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询联系人
     */

    private void RetrieveContact(JSONObject jsonObject, JsonZZX jsonZZX) {
        try {
            if (CheckPassWord(jsonObject.getString(Value.PassWord), jsonZZX)) {
                JSONArray array = new JSONArray();
                ContactsController controller = new ContactsController();
                List<ContactsController.ContactInfo> list = controller.query(mContext);
                for (ContactsController.ContactInfo l : list) {
                    Value.ContactInfo info = new Value.ContactInfo();
                    info.id = l.getContactId();
                    info.name = l.getContactName();
                    info.number = l.getContactPhone();
                    JSONObject object = new JSONObject();
                    object.put(Value.ConId, info.id);
                    object.put(Value.ConName, info.name);
                    object.put(Value.ConNumber, info.number);
                    array.put(object);
                }

                jsonZZX.AddBody(Value.Contact, array);
                jsonZZX.AddBody(Value.ResultReply, true);
            }
        } catch (Exception e) {
            jsonZZX.AddBody(Value.ResultReply, false);
            e.printStackTrace();
        }
    }


    /**
     * 新增联系人
     */
    private void CreateContact(JSONObject jsonObject, JsonZZX jsonZZX) {
        try {
            if (CheckPassWord(jsonObject.getString(Value.PassWord), jsonZZX)) {
                Value.ContactInfo info = new Value.ContactInfo();
                info.name = jsonObject.getString(Value.ConName);
                info.number = jsonObject.getString(Value.ConNumber);
                ContactsController controller = new ContactsController();
                ContactsController.ContactInfo l = new ContactsController.ContactInfo();
                l.setContactName(info.name);
                l.setContactPhone(info.number);
                if (controller.insert(l, mContext)) {
                    String id = l.getContactId();
                    jsonZZX.AddBody(Value.ConId, id);
                    jsonZZX.AddBody(Value.ResultReply, true);
                } else {
                    jsonZZX.AddBody(Value.ResultReply, false);
                }
            }
        } catch (Exception e) {
            jsonZZX.AddBody(Value.ResultReply, false);
            e.printStackTrace();
        }
    }


    /**
     * 更新联系人
     */
    private void UpdateContact(JSONObject jsonObject, JsonZZX jsonZZX) {
        try {
            if (CheckPassWord(jsonObject.getString(Value.PassWord), jsonZZX)) {
                Value.ContactInfo info = new Value.ContactInfo();
                info.name = jsonObject.getString(Value.ConName);
                info.number = jsonObject.getString(Value.ConNumber);
                info.id = jsonObject.getString(Value.ConId);
                ContactsController controller = new ContactsController();
                ContactsController.ContactInfo l = new ContactsController.ContactInfo();
                l.setContactName(info.name);
                l.setContactPhone(info.number);
                l.setContactId(info.id);
                if (controller.update(l, mContext)) {
                    jsonZZX.AddBody(Value.ResultReply, true);
                } else {
                    jsonZZX.AddBody(Value.ResultReply, false);
                }
            }
        } catch (Exception e) {
            jsonZZX.AddBody(Value.ResultReply, false);
            e.printStackTrace();
        }
    }


    /**
     * 删除联系人
     */
    private void DeleteContact(JSONObject jsonObject, JsonZZX jsonZZX) {
        try {
            if (CheckPassWord(jsonObject.getString(Value.PassWord), jsonZZX)) {
                String id = jsonObject.getString(Value.ConId);
                ContactsController controller = new ContactsController();
                ContactsController.ContactInfo l = new ContactsController.ContactInfo();
                l.setContactId(id);
                if (controller.delete(l, mContext)) {
                    jsonZZX.AddBody(Value.ResultReply, true);
                } else {
                    jsonZZX.AddBody(Value.ResultReply, false);
                }
            }
        } catch (Exception e) {
            jsonZZX.AddBody(Value.ResultReply, false);
            e.printStackTrace();
        }
    }


    /**
     * 设置循环录像
     */
    private void LoopVideo(JSONObject jsonObject, JsonZZX jsonZZX) {
        try {
            if (CheckPassWord(jsonObject.getString(Value.PassWord), jsonZZX)) {
                if (jsonObject.getString(Value.Loop).equals("1")) {
                    Settings.System.putInt(mContext.getContentResolver(), LOOP_RECORD, 1);
                } else {
                    Settings.System.putInt(mContext.getContentResolver(), LOOP_RECORD, 0);
                }
                jsonZZX.AddBody(Value.ResultReply, true);
            }
        } catch (Exception e) {
            jsonZZX.AddBody(Value.ResultReply, false);
            e.printStackTrace();
        }

    }

    /**
     * 写入串号SerialNumber
     */
    private void WirteSerialNum(JSONObject jsonObject, JsonZZX jsonZZX) {
        try {
            if (CheckPassWord(jsonObject.getString(Value.PassWord), jsonZZX)) {
                String serial = jsonObject.getString(Value.SerialNum);
                Settings.System.putString(mContext.getContentResolver(), SERIAL_NUMBER, serial);
                jsonZZX.AddBody(Value.ResultReply, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            jsonZZX.AddBody(Value.ResultReply, false);
        }
    }

    /**
     * 读取当前时间
     */
    private void GetDevTime(JSONObject jsonObject, JsonZZX jsonZZX) {
        try {
            if (CheckPassWord(jsonObject.getString(Value.PassWord), jsonZZX)) {
                jsonZZX.AddBody(Value.CurrentTime, System.currentTimeMillis() / 1000);
                jsonZZX.AddBody(Value.ResultReply, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            jsonZZX.AddBody(Value.ResultReply, false);
        }
    }

    /**
     * 设置密码
     */
    private void SendPwd(JSONObject jsonObject, JsonZZX jsonZZX) {
        try {
            if (CheckPassWord(jsonObject.getString(Value.PassWord), jsonZZX)) {
                String superPwd = jsonObject.getString(Value.SuperPwd);
                String adminPwd = jsonObject.getString(Value.AdminPwd);
                String userPwd = jsonObject.getString(Value.UserPwd);
                Settings.System.putString(mContext.getContentResolver(), USER_PSW, userPwd);
                Settings.System.putString(mContext.getContentResolver(), ADMIN_PSW, adminPwd);
                Settings.System.putString(mContext.getContentResolver(), SUPER_PSW, superPwd);
                jsonZZX.AddBody(Value.ResultReply, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            jsonZZX.AddBody(Value.ResultReply, false);
        }
    }

    /**
     * 获取存储信息
     */
    private void GetDiskStorage(JSONObject jsonObject, JsonZZX jsonZZX) {
        try {
            if (CheckPassWord(jsonObject.getString(Value.PassWord), jsonZZX)) {
                long freeStorage;
                long totalStorage;
                String sdcardDir = Environment
                        .getExternalStorageDirectory().getAbsolutePath();
                StatFs sf = new StatFs(sdcardDir);
                long bSize = sf.getBlockSize();
                long bCount = sf.getBlockCount();
                long bfree = sf.getFreeBlocks();
                //  long availBlocks = bCount - bfree;
                freeStorage = bSize * bfree;
                totalStorage = bSize * bCount;
                freeStorage = freeStorage / 1024 / 1024;
                totalStorage = totalStorage / 1024 / 1024;
                jsonZZX.AddBody(Value.StorageFree, freeStorage);
                jsonZZX.AddBody(Value.StorageTotal, totalStorage);
//                jsonZZX.AddBody(Value.Storage, (freeStorage + "M/" + totalStorage + "M"));
                jsonZZX.AddBody(Value.ResultReply, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            jsonZZX.AddBody(Value.ResultReply, false);
        }
    }

    /**
     * 获取Gps是否开启
     */
    private void GetGPSStatus(JSONObject jsonObject, JsonZZX jsonZZX) {
        try {
            if (CheckPassWord(jsonObject.getString(Value.PassWord), jsonZZX)) {
                boolean isOn = false;
                isOn = Settings.Secure.isLocationProviderEnabled(
                        mContext.getContentResolver(), LocationManager.GPS_PROVIDER);
                jsonZZX.AddBody(Value.IsOn, isOn ? "1" : "0");
                jsonZZX.AddBody(Value.ResultReply, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            jsonZZX.AddBody(Value.ResultReply, false);
        }
    }


    /**
     * 获取vidoe loop是否开启
     */
    private void GetLoopStatus(JSONObject jsonObject, JsonZZX jsonZZX) {
        try {
            if (CheckPassWord(jsonObject.getString(Value.PassWord), jsonZZX)) {
                jsonZZX.AddBody(Value.IsOn, Settings.System.getInt(mContext.getContentResolver(), LOOP_RECORD, 0));
                jsonZZX.AddBody(Value.ResultReply, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            jsonZZX.AddBody(Value.ResultReply, false);
        }
    }


    /**
     * login
     */
    private void DoLogin(JSONObject jsonObject, JsonZZX jsonZZX) {
        try {
            if (CheckPassWord(jsonObject.getString(Value.PassWord), jsonZZX)) {
                String pwd = jsonObject.getString(Value.LoginPwd);
                EventBusUtils.postEvent(Values.BUS_EVENT_FINISH_PASSWORD, Values.FINISH_PASSWORD);
                jsonZZX.AddBody(Value.ResultReply, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            jsonZZX.AddBody(Value.ResultReply, false);
        }
    }

    /**
     * 读取串号
     */
    private void GetSerial(JSONObject jsonObject, JsonZZX jsonZZX) {
        try {
            if (CheckPassWord(jsonObject.getString(Value.PassWord), jsonZZX)) {
                String serial;
                serial = Settings.System.getString(mContext.getContentResolver(), SERIAL_NUMBER);
                jsonZZX.AddBody(Value.SerialNum, serial);
                jsonZZX.AddBody(Value.ResultReply, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            jsonZZX.AddBody(Value.ResultReply, false);
        }
    }

    /**
     * 读取拨号功能开关
     */
    private void GetDialStatus(JSONObject jsonObject, JsonZZX jsonZZX) {
        try {
            if (CheckPassWord(jsonObject.getString(Value.PassWord), jsonZZX)) {
                Boolean isOn;
                String values;
                if (Settings.System.getString(mContext.getContentResolver(), IsOpenDialer) != null) {
                    values = Settings.System.getString(mContext.getContentResolver(), IsOpenDialer);
                } else {
                    values = "1";
                }
                isOn = values.equals("1");
                jsonZZX.AddBody(Value.IsOn, isOn ? "1" : "0");
                jsonZZX.AddBody(Value.ResultReply, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            jsonZZX.AddBody(Value.ResultReply, false);
        }
    }


    /**
     * 写入拨号功能开关
     */
    private void SetDialStatus(JSONObject jsonObject, JsonZZX jsonZZX) {
        try {
            if (CheckPassWord(jsonObject.getString(Value.PassWord), jsonZZX)) {
                String values;
                if (jsonObject.getString(Value.GPS) != null) {
                    values = jsonObject.getString(Value.GPS);
                } else {
                    values = "1";
                }
                Settings.System.putString(mContext.getContentResolver(), IsOpenDialer, values);
                jsonZZX.AddBody(Value.ResultReply, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            jsonZZX.AddBody(Value.ResultReply, false);
        }
    }

    /**
     * 查询Users
     */

    private void RetrieveUsers(JSONObject jsonObject, JsonZZX jsonZZX) {
        try {
            if (CheckPassWord(jsonObject.getString(Value.PassWord), jsonZZX)) {
                JSONArray array = new JSONArray();
                List<UserInfo> list = UserInfo.retrieveUsers(mContext);
                for (UserInfo l : list) {
                    JSONObject object = new JSONObject();
                    object.put(Value.UsersId, l.getId());
                    object.put(Value.UsersName, l.getName());
                    object.put(Value.UsersPwd, l.getPwd());
                    object.put(Value.UsersNum, l.getNumber());
                    array.put(object);
                }
                jsonZZX.AddBody(Value.Users, array);
                jsonZZX.AddBody(Value.ResultReply, true);
            }
        } catch (Exception e) {
            jsonZZX.AddBody(Value.ResultReply, false);
            e.printStackTrace();
        }
    }


    /**
     * 新增User
     */
    private void CreateUser(JSONObject jsonObject, JsonZZX jsonZZX) {
        try {
            if (CheckPassWord(jsonObject.getString(Value.PassWord), jsonZZX)) {
                UserInfo info = new UserInfo();
                info.setName(jsonObject.getString(Value.UsersName));
                info.setPwd(jsonObject.getString(Value.UsersPwd));
                info.setNumber(jsonObject.getString(Value.UsersNum));
                if (UserInfo.createUser(info, mContext)) {
                    String id = info.getId();
                    jsonZZX.AddBody(Value.UsersId, id);
                    jsonZZX.AddBody(Value.ResultReply, true);
                } else {
                    jsonZZX.AddBody(Value.ResultReply, false);
                }
            }
        } catch (Exception e) {
            jsonZZX.AddBody(Value.ResultReply, false);
            e.printStackTrace();
        }
    }


    /**
     * 更新User
     */
    private void UpdateUser(JSONObject jsonObject, JsonZZX jsonZZX) {
        try {
            if (CheckPassWord(jsonObject.getString(Value.PassWord), jsonZZX)) {

                UserInfo info = new UserInfo();
                info.setName(jsonObject.getString(Value.UsersName));
                info.setPwd(jsonObject.getString(Value.UsersPwd));
                info.setNumber(jsonObject.getString(Value.UsersNum));
                info.setId(jsonObject.getString(Value.UsersId));
                if (UserInfo.updateUser(info, mContext)) {
                    jsonZZX.AddBody(Value.ResultReply, true);
                } else {
                    jsonZZX.AddBody(Value.ResultReply, false);
                }
            }
        } catch (Exception e) {
            jsonZZX.AddBody(Value.ResultReply, false);
            e.printStackTrace();
        }
    }


    /**
     * 删除User
     */
    private void DeleteUser(JSONObject jsonObject, JsonZZX jsonZZX) {
        try {
            if (CheckPassWord(jsonObject.getString(Value.PassWord), jsonZZX)) {
                String id = jsonObject.getString(Value.UsersId);
                if (UserInfo.deleteUser(id, mContext)) {
                    jsonZZX.AddBody(Value.ResultReply, true);
                } else {
                    jsonZZX.AddBody(Value.ResultReply, false);
                }
            }
        } catch (Exception e) {
            jsonZZX.AddBody(Value.ResultReply, false);
            e.printStackTrace();
        }
    }

    /**
     * 查询登录id
     */

    private void FindLoginUser(JSONObject jsonObject, JsonZZX jsonZZX) {
        try {
            if (CheckPassWord(jsonObject.getString(Value.PassWord), jsonZZX)) {
                String id = "";
                if (Values.isUserAdmin) {
                    id = Values.UserIDNow;
                } else {
                    id = "-1";
                }
                jsonZZX.AddBody(Value.LoginID, id);
            }
        } catch (Exception e) {
            jsonZZX.AddBody(Value.ResultReply, false);
            e.printStackTrace();
        }
    }

    /**
     * 使用者信息
     * 最多5个  id 用来唯一标识
     * pwd 用户密码  name 用户姓名   number 用户编号
     */

    public static class UserInfo {

        private String id = "";

        private String pwd = "";

        private String name = "";

        private String number = "";

        @Override
        public String toString() {
            return "UserInfo{" +
                    "id='" + id + '\'' +
                    ", pwd='" + pwd + '\'' +
                    ", name='" + name + '\'' +
                    ", number='" + number + '\'' +
                    '}';
        }

        public String getPwd() {
            return pwd;
        }

        public void setPwd(String pwd) {
            this.pwd = pwd;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        private static final String SHARED = "com.zzx.setting.user";
        private static final String USER = "users";
        private static final String SPLIT = "-";
        private static final String SPLIT2 = "--";

        public static List<UserInfo> retrieveUsers(Context context) {
            SharedPreferences preferences = context.getSharedPreferences(SHARED, Context.MODE_PRIVATE);
            String s = preferences.getString(USER, "");
            Value.LOG_E(TAG, "user list " + s);
            List<UserInfo> list = new ArrayList<>();
            for (String ss : s.split(SPLIT2)) {
                String[] u = ss.split(SPLIT);
                if (u.length != 4) {
                    continue;
                }
                UserInfo userInfo = new UserInfo();
                userInfo.setId(u[0]);
                userInfo.setName(u[1]);
                userInfo.setPwd(u[2]);
                userInfo.setNumber(u[3]);
                list.add(userInfo);
            }
            return list;
        }

        public static boolean createUser(UserInfo info, Context context) {
            SharedPreferences preferences = context.getSharedPreferences(SHARED, Context.MODE_PRIVATE);
            String s = preferences.getString(USER, "");
            String uu = UUID.randomUUID().toString();
            info.setId(uu.replace(SPLIT, "").replace(SPLIT2, ""));
            StringBuilder builder = new StringBuilder();
            builder.append(info.getId());
            builder.append(SPLIT);
            builder.append(info.getName());
            builder.append(SPLIT);
            builder.append(info.getPwd());
            builder.append(SPLIT);
            builder.append(info.getNumber());
            if (s.equals("")) {
                s = builder.toString();
            } else {
                s = s + SPLIT2 + builder.toString();
            }
            preferences.edit().putString(USER, s).apply();
            return true;
        }

        public static boolean updateUser(UserInfo info, Context context) {
            SharedPreferences preferences = context.getSharedPreferences(SHARED, Context.MODE_PRIVATE);
            String s = preferences.getString(USER, "");
            StringBuilder builder = new StringBuilder();
            for (String ss : s.split(SPLIT2)) {
                if (builder.length() > 0) {
                    builder.append(SPLIT2);
                }
                String[] u = ss.split(SPLIT);
                if (u.length != 4) {
                    continue;
                }
                String id = u[0];
                if (id.equals(info.getId())) {
                    builder.append(info.getId());
                    builder.append(SPLIT);
                    builder.append(info.getName());
                    builder.append(SPLIT);
                    builder.append(info.getPwd());
                    builder.append(SPLIT);
                    builder.append(info.getNumber());
                } else {
                    builder.append(ss);
                }
            }
            s = builder.toString();
            preferences.edit().putString(USER, s).apply();
            return true;
        }

        public static boolean deleteUser(String did, Context context) {
            SharedPreferences preferences = context.getSharedPreferences(SHARED, Context.MODE_PRIVATE);
            String s = preferences.getString(USER, "");
            StringBuilder builder = new StringBuilder();
            for (String ss : s.split(SPLIT2)) {
                if (builder.length() > 0) {
                    builder.append(SPLIT2);
                }
                String[] u = ss.split(SPLIT);
                if (u.length != 4) {
                    continue;
                }
                String id = u[0];
                if (!id.equals(did)) {
                    builder.append(ss);
                }
            }
            s = builder.toString();
            preferences.edit().putString(USER, s).apply();
            return true;
        }
    }
}

class JsonZZX {

    private JSONObject head = new JSONObject();
    private JSONObject body = new JSONObject();

    JsonZZX AddHead(int id) {
        try {
            head.put(Value.Version, "1.0");
            head.put(Value.FunctionID, id);
            head.put(Value.Device, "ZZX2080");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    JsonZZX AddBody(String name, Object value) {
        try {
            body.put(name, value == null ? "" : value.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    JsonZZX AddBody(String name, JSONArray value) {
        try {
            body.put(name, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    JSONObject getJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Value.Head, head);
            jsonObject.put(Value.Body, body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}

class Value {
    static final String Head = "header";
    static final String Body = "body";

    static final String Version = "version";
    static final String Device = "device";

    static final String FunctionID = "id";

    static final int InitDevice = 1;
    static final int GetIDCode = 2;
    static final int GetZFYInfo = 3;
    static final int WriteZFYInfo = 4;
    static final int SyncDevTime = 5;
    static final int SetMSDC = 6;
    static final int ReadDeviceResolution = 7;
    static final int ReadDeviceBatteryDumpEnergy = 8;
    static final int GPSSET = 9;
    static final int RetrieveContact = 10;
    static final int CreateContact = 11;
    static final int UpdateContact = 12;
    static final int DeleteContact = 13;
    static final int LoopVideo = 14;
    static final int WriteSerial = 15;
    static final int GetDevTime = 16;
    static final int SendPassword = 17;
    static final int GetDiskStorage = 18;
    static final int GetGPSStatus = 19;
    static final int GetLoopStatus = 20;
    static final int LoginStatus = 21;
    static final int GetSerial = 22;
    static final int GetDialStatus = 23;
    static final int SetDialStatus = 24;
    static final int RetrieveUsers = 25;
    static final int CreateUser = 26;
    static final int UpdateUser = 27;
    static final int DeleteUser = 28;
    static final int FindLoginUser = 29;

    static final String PassWord = "password";
    static final String IDCode = "IDCode";
    static final String Serial = "serial";
    static final String PoliceNo = "policeNo";  //policeNo
    static final String PoliceName = "policeName";
    static final String GroupNo = "groupNo";  //单位编号
    static final String GroupName = "groupName";
    static final String CurrentTime = "Time";
    static final String ResultReply = "Result";
    static final String VideoWidth = "VideoWidth";
    static final String VideoHeight = "VideoHeight";
    static final String Battery = "battery";
    static final String GPS = "GPS";
    static final String Contact = "contacts";
    static final String ConId = "cId";
    static final String ConName = "name";
    static final String ConNumber = "number";
    static final String Loop = "isLoop";
    static final String SerialNum = "serialNum";
    static final String SuperPwd = "superPwd";
    static final String AdminPwd = "adminPwd";
    static final String UserPwd = "userPwd";
    static final String Storage = "storage";
    static final String StorageFree = "free";
    static final String StorageTotal = "total";
    static final String IsOn = "isOn";
    static final String LoginPwd = "loginPwd";
    static final String Users = "users";
    static final String UsersId = "id";
    static final String UsersName = "name";
    static final String UsersPwd = "pwd";
    static final String UsersNum = "number";
    static final String LoginID = "id";

    static void LOG_E(String TAG, String value) {
        if (PcSocketNewUtils.Debug) {
            Log.e(TAG, value);
        }
    }

    static class ContactInfo {
        String id;
        String name;
        String number;
    }


}


/**
 * http://blog.csdn.net/wwj_748/article/details/19628755/
 */
class ContactsController {


    public List<ContactInfo> query(Context context) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cursor == null) {
            return new ArrayList<>(0);
        }
        List<ContactInfo> list = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            Cursor rawCursor = resolver.query(ContactsContract.RawContacts.CONTENT_URI, null,
                    ContactsContract.RawContacts.CONTACT_ID + " =? ", new String[]{contactId}, null);
            if (rawCursor == null) {
                continue;
            }
            ContactInfo info = new ContactInfo();
            if (rawCursor.moveToNext()) {
                String rawContactId = rawCursor.getString(rawCursor.getColumnIndex(ContactsContract.RawContacts._ID));
                info.setContactId(rawContactId);
                rawCursor.close();
                Cursor dataCursor = resolver.query(ContactsContract.Data.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID + " =? ", new String[]{rawContactId}, null);
                if (dataCursor != null) {
                    while (dataCursor.moveToNext()) {
                        String type = dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.Data.MIMETYPE));
                        if (ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE.equals(type)) {
                            String dataName = dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME));
                            info.setContactName(dataName);
                        } else if (ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE.equals(type)) {
                            String dataPhone = dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            info.setContactPhone(dataPhone);
                        }
                    }
                    dataCursor.close();
                }
            } else {
                rawCursor.close();
                continue;
            }
            list.add(info);
        }
        cursor.close();
        return list;
    }

    public boolean insert(ContactInfo info, Context context) {
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.clear();
        Uri rawContactUri = resolver.insert(ContactsContract.RawContacts.CONTENT_URI, values);
        long rawContactId = ContentUris.parseId(rawContactUri);
        if (!"".equals(info.getContactName())) {
            values.clear();
            values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
            values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
            values.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, info.getContactName());
            resolver.insert(ContactsContract.Data.CONTENT_URI, values);
        }
        if (!"".equals(info.getContactPhone())) {
            values.clear();
            values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
            values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
            values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, info.getContactPhone());
            resolver.insert(ContactsContract.Data.CONTENT_URI, values);
        }
        info.setContactId(rawContactId + "");
        return true;
    }

    public boolean update(ContactInfo info, Context context) {
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        int update = 0;
        if (!"".equals(info.getContactName())) {
            values.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, info.getContactName());
            String whereName = ContactsContract.Data.RAW_CONTACT_ID + "=? AND "
                    + ContactsContract.Data.MIMETYPE + "=?";
            int upName = resolver.update(ContactsContract.Data.CONTENT_URI, values, whereName
                    , new String[]{info.getContactId(), ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE});
            update += upName;
        }
        if (!"".equals(info.getContactPhone())) {
            values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, info.getContactPhone());
            String wherePhone = ContactsContract.Data.RAW_CONTACT_ID + "=? AND "
                    + ContactsContract.Data.MIMETYPE + "=?";
            int upPhone = resolver.update(ContactsContract.Data.CONTENT_URI, values, wherePhone
                    , new String[]{info.getContactId(), ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE});
            update += upPhone;
        }
        return update != 0;
    }

    public boolean delete(ContactInfo info, Context context) {
        int delete = context.getContentResolver()
                .delete(ContentUris.withAppendedId(ContactsContract.RawContacts.CONTENT_URI,
                        Long.parseLong(info.getContactId()))
                        , null, null
                );
        return delete != 0;
    }


    /**
     * contactId    raw_contacts 表中的 RawContactID
     * contactName  raw_contacts 表中的 display
     * contactPhone data 表中的 data1
     */
    public static class ContactInfo {
        private String contactId = "";
        private String contactName = "";
        private String contactPhone = "";

        public ContactInfo() {

        }

        public ContactInfo(String id, String name, String num) {
            setContactId(id);
            setContactName(name);
            setContactPhone(num);
        }

        public String getContactId() {
            return contactId;
        }

        public void setContactId(String contactId) {
            this.contactId = contactId;
        }

        public String getContactName() {
            return contactName;
        }

        public void setContactName(String contactName) {
            this.contactName = contactName;
        }

        public String getContactPhone() {
            return contactPhone;
        }

        public void setContactPhone(String contactPhone) {
            this.contactPhone = contactPhone;
        }
    }


}

