package com.zzx.soundrecord.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaActionSound;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.zzx.soundrecord.R;
import com.zzx.soundrecord.util.SystemUtil;
import com.zzx.soundrecord.values.Values;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class RecordService extends Service implements Handler.Callback, MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener {
    private static final String TAG = "RecordService";
    private static final int INIT_RECORD_TIME = 0x03;
    private static final int SPACE = 0X06;
    private boolean maskImportant = false;//是否长按
    public static boolean isRecording = false;
    private File saveDir = null;
    private SimpleDateFormat fileNameFormat = null;
    private SimpleDateFormat dirFormat = null;
    private String fileName = null;
    private String dateDir = null;
    private MediaRecorder mMediaRecorder = null;
    private MediaActionSound mediaActionSound = null;
    private RecordBroadCastReceiver recordBroadCastReceiver = null;
    private IntentFilter intentFilter = null;
    private String deletFileName = null;
    private File deleteDir = null;
    private File destDir = null;
    private String policeNumber = null;
    private String deviceNum = null;
    private String deviceNumber = null;
    private String mSoundFilePath = null;
    private Handler handler = null;
    private UpdateTimeUIRunnable updateTimeUIRunnable = null;
    private int hour1 = 0;
    private int hour2 = 0;
    private int minite1 = 0;
    private int minite2 = 0;
    private int second1 = 0;
    private int second2 = 0;
    private int BASE = 1;
    private  File soundFile;
    private static final String ZZXIsJieXinVersion = "ZZXIsJieXinVersion";
    private SoundPool mBurstSound;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(this);
        fileNameFormat = new SimpleDateFormat(Values.FILE_NAME_DATE_FORMATE);
        dirFormat = new SimpleDateFormat("yyyyMMdd");
        saveDir = new File(Values.SOUND_SAVE_PATH);
        recordBroadCastReceiver = new RecordBroadCastReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction("startRecord");
        intentFilter.addAction("pauseRecord");
        intentFilter.addAction("stopRecord");
        intentFilter.addAction("cancelRecord");
        intentFilter.addAction("initUI");
        intentFilter.addAction("sdcardState");
        intentFilter.addAction("zzx_action_record_Video");
        intentFilter.addAction(Intent.ACTION_SHUTDOWN);
        updateTimeUIRunnable = new UpdateTimeUIRunnable();
        handler.postDelayed(updateTimeUIRunnable, 1000);
        this.registerReceiver(recordBroadCastReceiver, intentFilter);
        mediaActionSound = new MediaActionSound();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        maskImportant = intent.getBooleanExtra("zzx_state", false);
        if (!isRecording) {
            if (!isP5XJNKYD(getApplicationContext())){
                playSatrtSound();
                startVibator();
            }
            recordStart();
        } else {
            if (!isP5XJNKYD(getApplicationContext())){
                playStopSound();
                startVibator();
            }
            recordStop();
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isRecording){
            recordStop();
        }
        if (recordBroadCastReceiver!=null)
        unregisterReceiver(recordBroadCastReceiver);
        handler.sendEmptyMessageDelayed(INIT_RECORD_TIME, 1000);
    }

    boolean isRemovable;
    boolean isCheck;

    //震动
    private void startVibator() {
        Vibrator vib = (Vibrator) getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(500);
    }

    private void recordStart() {
        if (isJieXinVersion(getApplicationContext())){
            Settings.System.putInt(getContentResolver(),"ZZXIsCheckExternalStorage",0);
        }
        if (Settings.System.getInt(getContentResolver(), "ZZXIsCheckExternalStorage", 1) == 1) {
            if (Settings.System.getInt(getContentResolver(), "ZZXisTFVersion", 1) == 1) {
                isRemovable = Environment.isExternalStorageRemovable();
            } else {
                isRemovable = true;
            }
            isCheck = isRemovable && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        } else {
            isCheck = true;
        }
        if (isCheck) {
            Log.e(TAG, "record start");
            isRecording = true;
            fileName = fileNameFormat.format(new Date(System.currentTimeMillis()));
            if (maskImportant){
                fileName = fileName + Values.MASK;
                maskImportant = false;
            }
            dateDir = dirFormat.format(new Date(System.currentTimeMillis()));
            destDir = new File(saveDir.getPath() + "/" + dateDir);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            policeNumber =getPoliceNumber();
            deviceNum = getDeviceNum();
            deviceNumber = getDeviceNumber();
            String case_number = Settings.System.getString(getContentResolver(),"case_number");
            if (isJieXinVersion(getApplicationContext())){
                mSoundFilePath = deviceNum +"_"+policeNumber+"_"+deviceNumber+
                        "_"+fileName+"_"+"start"+".mp3";
            }else if (is4GCSDJW(getApplicationContext()) || is4GCSY(getApplicationContext())){
                mSoundFilePath = fileName+"_"+"start"+".mp3";
            }else {
                if (!TextUtils.isEmpty(case_number)){
                    mSoundFilePath = case_number+"_"+policeNumber
                            + Values.FILE_FRONT + fileName  +"_"+"start" + ".mp3";
                }else {
                    mSoundFilePath = policeNumber
                            + Values.FILE_FRONT + fileName  +"_"+"start" + ".mp3";
                }

            }
            soundFile = new File(destDir, mSoundFilePath);
            if (!soundFile.exists()) {
                try {
                    soundFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            startMediaRecorder(soundFile);
//            readsize();
            updateUI();
            sendSoundRecordNotify(true);
            Log.e(TAG, "start    mSoundFilePath: " +soundFile.getPath());
        } else {
            Toast.makeText(this, getResources().getString(R.string.no_sdcard),
                    Toast.LENGTH_LONG).show();
            return;
        }

    }

    private void sendSoundRecordNotify(boolean isRecording) {
        Intent intent = new Intent();
        intent.setAction("zzx_action_record_mic");
        intent.putExtra("record_state", isRecording);
        this.sendBroadcast(intent);
    }

    //开始录制
    public void startMediaRecorder(File file) {
        if (mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
        }
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);  //音频输入源
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);   //设置输出格式
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);   //设置编码格式
        mMediaRecorder.setMaxDuration(30 * 60 * 1000);
        mMediaRecorder.setOnInfoListener(this);
        mMediaRecorder.setOnErrorListener(this);
        mMediaRecorder.setOutputFile(file.getAbsolutePath());
        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();  //开始录制
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readsize() {
        if (mMediaRecorder != null) {
            double ratio = (double) mMediaRecorder.getMaxAmplitude() / BASE;
            double db = 0;// 分贝
            if (ratio > 1)
                db = 20 * Math.log10(ratio);
            Log.d(TAG, "分贝值：" + db);
            handler.sendEmptyMessageAtTime(SPACE, 100);
        }
    }

    //停止录制，资源释放
    private void stopMediaRecorder() {
        String newFilepath = null;
        File newFile = null;
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
            if (maskImportant){

                if (!fileName.endsWith(Values.MASK)){
                    fileName = fileName +Values.MASK;
                }
                if (isJieXinVersion(getApplicationContext())){
                    newFile = new File(destDir,deviceNum
                            +"_"+ policeNumber +"_"+deviceNumber+"_"+ fileName + ".mp3");
                }else if (is4GCSDJW(getApplicationContext())|| is4GCSY(getApplicationContext())){
                    newFile = new File(destDir, fileName + ".mp3");
                }else {
                    newFile = new File(destDir, policeNumber
                            + Values.FILE_FRONT + fileName + ".mp3");
                }
                maskImportant = false;
            }
           if (mSoundFilePath.indexOf("start")!=-1){
               newFilepath = mSoundFilePath.replace("_" + "start", "");
               newFile = new File(destDir,newFilepath);
           }
            soundFile.renameTo(newFile);
            Log.e(TAG, "stop    mSoundFilePath: " +newFile.getAbsolutePath());
            sendBrodcast(newFile.getAbsolutePath());
            sendBroadcastSP("record_audio",newFile.getAbsolutePath());
        }
    }
    private void sendBroadcastSP(String action,String path){
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra("file_path",path);
        sendBroadcast(intent);
    }
    private void sendBrodcast(String path){
        Intent intent = new Intent();
        intent.setAction("action_sound");
        intent.putExtra("path",path);
        sendBroadcast(intent);
    }
    private void recordStop() {
        Log.e(TAG, "Record stop");
        isRecording = false;
        stopMediaRecorder();
        updateUI();
        initTime();
        sendSoundRecordNotify(false);
        Settings.System.putString(getContentResolver(),"case_number","");
//        stopSelf();
    }

    private void cancelRecord() {
        Log.e(TAG, "cancel stop");
        deletFileName = fileName;
        deleteDir = destDir;
        stopMediaRecorder();
        deletFile(deletFileName);
        isRecording = false;
        initTime();
        updateUI();
        sendSoundRecordNotify(false);
        // handler.sendEmptyMessageDelayed(DELETE_FILE, 1000);
        stopSelf();
    }

    //删除文件
    private void deletFile(String path) {
        final File file = new File(path);
        if (file.exists()) {
            new Thread() {
                @Override
                public void run() {
                    file.delete();
                }
            }.start();
        }
    }


    //暂停录制
    private void pauseMediaRecorder() {
        Log.e(TAG, "pauseMediaRecorder: 0000000");
        if (mMediaRecorder != null) {
            Log.e(TAG, "pauseMediaRecorder: 1111111" );
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.) {
//                Log.e(TAG, "pauseMediaRecorder: 222222" );
//                mMediaRecorder.pause();
//            }
        }
    }

    private void updateUI() {
        updateFubctionBtn();
    }

    private void updateRecordingTime(String recordTime) {
        Intent intent = new Intent();
        intent.setAction("updateRecordingTime");
        intent.putExtra("recordTime", recordTime);
        this.sendBroadcast(intent);
    }

    private void updateFubctionBtn() {
        Intent intent = new Intent();
        intent.setAction("updateFunctionButton");
        if (isRecording) {
            intent.putExtra("recordingState", 1);
        } else {
            intent.putExtra("recordingState", 0);
        }
        intent.putExtra("fileName", fileName);
        this.sendBroadcast(intent);
    }


    private void sendWarData(ArrayList<Short> buf) {
        Intent intent = new Intent();
        intent.setAction("updateAudioWarv");
        intent.putExtra("warData", buf);
        this.sendBroadcast(intent);
    }

    private void recordPause() {
        isRecording = false;
        updateUI();
    }

    private void playStopSound() {
        Log.e(TAG,isPlaySound(this)+"");
        if (isPlaySound(this)){
            stopSound();
        }else {
            mBurstSound = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
            mBurstSound.load(this, R.raw.stop_mic, 1);
            mBurstSound.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                @Override
                public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                    soundPool.play(sampleId, 1.0f, 1.0f, 0, 0, 1.0f);
                }
            });
        }

    }

    //声音
    private void playSatrtSound() {
        Log.e(TAG,isPlaySound(this)+"");
        if (isPlaySound(this)){
            startSound();
        }else {
            mBurstSound = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
            mBurstSound.load(this, R.raw.start_mic, 1);
            mBurstSound.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                @Override
                public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                    soundPool.play(sampleId, 1.0f, 1.0f, 0, 0, 1.0f);
                }
            });
        }

    }
    private void startSound(){
        new Thread() {
            public void run() {
                mediaActionSound.play(MediaActionSound.START_VIDEO_RECORDING);
            }
        }.start();
    }
    private void stopSound(){
        new Thread() {
            public void run() {
                mediaActionSound.play(MediaActionSound.STOP_VIDEO_RECORDING);
            }
        }.start();
    }

    private void initTime() {
        hour1 = 0;
        hour2 = 0;
        minite1 = 0;
        minite2 = 0;
        second1 = 0;
        second2 = 0;
    }

    private void updateTimeUI() {
        if (isRecording) {
            second2++;
            if (second2 == 10) {
                second1++;
                second2 = 0;
            }
            if (second1 == 6) {
                minite2++;
                second1 = 0;
            }
            if (minite2 == 10) {
                minite1++;
                minite2 = 0;
            }
            if (minite1 == 6) {
                hour2++;
                minite1 = 0;
            }
            if (hour2 == 10) {
                hour1++;
                hour2 = 0;
            }
        }
        String recordTime = "" + hour1 + hour2 + ":" + minite1 + minite2 + ":"
                + second1 + second2;
        Settings.System.putString(getContentResolver(),"recordTime",recordTime);
        updateRecordingTime(recordTime);
    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {

    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        switch (what){
            case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
            case MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED:
                mMediaRecorder.setOnInfoListener(null);
                recordSection();
                break;
        }
    }

    private void recordSection() {
        recordStop();
        recordStart();
    }
    private class UpdateTimeUIRunnable implements Runnable {

        @Override
        public void run() {
            updateTimeUI();
            handler.postDelayed(this, 1000);
        }

    }


    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case INIT_RECORD_TIME:
                handler.removeCallbacks(updateTimeUIRunnable);
                break;
            case SPACE:
                readsize();
                break;
        }
        return false;
    }

    private class RecordBroadCastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("startRecord")) {
                if (!isP5XJNKYD(getApplicationContext())){
                    playSatrtSound();
                }
                recordStart();
            } else if (action.equals("pauseRecord")) {
                if (!isP5XJNKYD(getApplicationContext())){
                    playStopSound();
                }
                recordPause();
                pauseMediaRecorder();
            } else if (action.equals("stopRecord")) {
                if (!isP5XJNKYD(getApplicationContext())){
                    playStopSound();
                }
                recordStop();
            } else if (action.equals("cancelRecord")) {
                cancelRecord();
            } else if (action.equals("initUI")) {
                updateUI();
            } else if (action.equals("sdcardState")) {
                if (!isRecording) {
                    return;
                }
                int sdLeftMemory = intent.getIntExtra("state", -1);
                if (sdLeftMemory == -1) {
                    Toast.makeText(RecordService.this,
                            getResources().getString(R.string.no_sdcard),
                            Toast.LENGTH_LONG).show();
                    recordStop();
                    return;
                }
                if (sdLeftMemory <= 1) {
                    Toast.makeText(RecordService.this,
                            getResources().getString(R.string.no_left_memory),
                            Toast.LENGTH_LONG).show();
                    recordStop();
                }
            } else if (action.equals(Intent.ACTION_SHUTDOWN)) {
                playStopSound();
                recordStop();
            }
        }
    }
    private String getPoliceNumber() {
        String policeNumber = null;
        policeNumber = Settings.System.getString(getContentResolver(),
                Values.POLICE_NUMBER);
        if (isJieXinVersion(getApplicationContext())){
            if (policeNumber == null || policeNumber.equals("")) {
                policeNumber = Values.DEFAULT_NUMBER_8;
            }
            if (policeNumber ==Values.DEFAULT_NUMBER ||policeNumber.equals(Values.DEFAULT_NUMBER)){
                policeNumber = Values.DEFAULT_NUMBER_8;
            }
        }else {
            if (policeNumber == null || policeNumber.equals("")) {
                policeNumber = Values.DEFAULT_NUMBER;
            }
        }
        return policeNumber;
    }

    private String getDeviceNum(){
        String deviceNum = null;
        deviceNum = Settings.System.getString(getContentResolver(),
                Values.DEVICE_NUMBER);
        if (isJieXinVersion(getApplicationContext())){
            if (deviceNum == null || deviceNum.equals("")){
                deviceNum = Values.DEVICE_DEFAULT_NUM_4;
            }
            if (deviceNum == Values.DEVICE_DEFAULT_NUM || deviceNum.equals(Values.DEVICE_DEFAULT_NUM)){
                deviceNum = Values.DEVICE_DEFAULT_NUM_4;
            }

        }else {
            if (deviceNum == null || deviceNum.equals("")) {
                deviceNum = Values.DEVICE_DEFAULT_NUM;
            }
        }
        return deviceNum;
    }
    private String getDeviceNumber(){
        String deviceNumber = null;
        deviceNumber = Settings.System.getString(getContentResolver(),
                Values.DEVICE_NUM);
        if (deviceNumber == null || deviceNumber.equals(""))
            deviceNumber = Values.DEVICE_DEFAULT_NUM_4;
        return deviceNumber;
    }
    private static boolean isJieXinVersion(Context context) {
        return Settings.System.getInt(context.getContentResolver(), ZZXIsJieXinVersion, 0) == 1;
    }
    private static boolean isP5XJNKYD(Context context){
        return Settings.System.getInt(context.getContentResolver(), "ZZXIsHideLcons", 0) == 1;
    }
    private static boolean is4GCSDJW(Context context){
        return Settings.System.getInt(context.getContentResolver(), "4GC_SDJW", 0) == 1;
    }
    private static boolean is4GCSY(Context context){
        return Settings.System.getInt(context.getContentResolver(), "4GC_SY", 0) == 1;
    }
    private static boolean is4GAHNDW(Context context){
        return Settings.System.getInt(context.getContentResolver(), "4GA_HNDW", 0) == 1;
    }
    public static boolean is4GACDHN(Context context){
        return Settings.System.getInt(context.getContentResolver(),"4GA_CDHN",0)==1;
    }
    public static boolean isP5XCDHN(Context context){
        return Settings.System.getInt(context.getContentResolver(),"P5X_CDHN",0)==1;
    }
    public static boolean isPlaySound(Context context){
        return Settings.System.getInt(context.getContentResolver(),"isplaysound",0)==1;
    }
}
