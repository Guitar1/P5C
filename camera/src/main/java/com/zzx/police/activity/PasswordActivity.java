package com.zzx.police.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.zzx.police.R;
import com.zzx.police.data.Values;
import com.zzx.police.manager.RecordManager;
import com.zzx.police.utils.EventBusUtils;
import com.zzx.police.utils.PcSocketNewUtils;
import com.zzx.police.utils.TTSToast;

import java.util.List;

public class PasswordActivity extends Activity implements View.OnClickListener {

    private EditText mEtPassword, mEtUsername;
    private CheckBox mCbRemeberPsw;
    private String mPassword, mUsername;
    private boolean isUseradmin = false;
    private String UserPassword;
    private String AdminPassword;
    private String SuperPassword;
    private RecordManager mRecordManager;
    private static String LAST_USER = "PasswordActivityLastUser";
    private static String LAST_CHECK = "PasswordActivityLastCheck";
    private static String LAST_PSW = "PasswordActivityLastUserPSW";

    private boolean mIsChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);
        mEtPassword = (EditText) findViewById(R.id.et_password);
        mEtUsername = (EditText) findViewById(R.id.et_usermane);
        mCbRemeberPsw = (CheckBox) findViewById(R.id.cb_remeber);
        mCbRemeberPsw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mIsChecked = isChecked;
                try {
                    Settings.System.putInt(getContentResolver(), LAST_CHECK, isChecked == true ? 1 : 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        findViewById(R.id.btn_positive).setOnClickListener(this);
        EventBusUtils.registerEvent(Values.BUS_EVENT_FINISH_PASSWORD, this);
        try {
            Settings.System.putInt(getContentResolver(), "PasswordActivity", 1);//确定acitivity是否还活着
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (Settings.System.getString(getContentResolver(), LAST_USER) != null) {
            //   mEtUsername.setText(Settings.System.getString(getContentResolver(), LAST_USER));
            if (Settings.System.getString(getContentResolver(), LAST_USER) != " ") {
                mEtUsername.setText(Settings.System.getString(getContentResolver(), LAST_USER));
            } else {
                mEtUsername.clearFocus();
            }
        }
        mIsChecked = Settings.System.getInt(getContentResolver(), LAST_CHECK, 1) == 1;
        if (mIsChecked) {
            mEtPassword.setText(checkNull(Settings.System.getString(getContentResolver(), LAST_PSW), Values.DEVICE_DEFAULT_PASSWORD));
        }
        mCbRemeberPsw.setChecked(mIsChecked);
        if (PcSocketNewUtils.UserInfo.retrieveUsers(this).size() == 0) {
            initDefultUser();  //如果初始化没有数据的话，写入默认数据
        }
//        mRecordManager=new RecordManager(this);
//        startRecord();
    }

    private void getDefultPsw() {
        AdminPassword = checkNull(Settings.System.getString(getContentResolver(), Values.ADMIN_PSW), Values.DEVICE_DEFAULT_PASSWORD);
        SuperPassword = checkNull(Settings.System.getString(getContentResolver(), Values.SUPER_PSW), Values.DEVICE_H9_DEFAULT_SUPER_PASSWORD);
    }

    @Override
    public void onClick(View v) {
        mPassword = mEtPassword.getText().toString().trim();
        mUsername = mEtUsername.getText().toString().trim();
        switch (v.getId()) {
            case R.id.btn_positive:
                checkPassword();
                break;
        }
    }


    public void onEventMainThread(Integer what) {
        switch (what) {
            case Values.FINISH_PASSWORD:
                finish();
                Log.e("zzxzdm", "FINISH_PASSWORD");
                break;

        }
    }


    private void checkPassword() {
        if (mPassword == null || mPassword.equals("")) {
            TTSToast.showToast(this, R.string.null_password);
            return;
        }

        PcSocketNewUtils.UserInfo loginUser = getUserInfo();
        getDefultPsw();
        if (mPassword.equals(UserPassword)) {
            if (isUseradmin) {
                isUseradmin = false;
                Intent userintent = new Intent();
                userintent.setAction(Values.LOGCAT_USER_ADMIN);
                if (loginUser != null) {
                    userintent.putExtra(Values.EXTRA_USERNAME, loginUser.getName());
                    userintent.putExtra(Values.EXTRA_ID, loginUser.getNumber());
                }
                this.sendBroadcast(userintent);
                Values.isUserAdmin = true;
                Values.isRootAdmin = false;
                Values.UserNameNow = loginUser.getName();
                Values.UserNubNow = loginUser.getNumber();
                Values.UserIDNow = loginUser.getId();
                Settings.System.putString(getContentResolver(), LAST_USER, mUsername);
                if (mIsChecked) {
                    Settings.System.putString(getContentResolver(), LAST_PSW, mPassword);
                }
            }
            Finish();
        } else if (mPassword.equals(AdminPassword)) {
            Intent rootintent = new Intent();
            rootintent.setAction(Values.LOGCAT_ROOT_ADMIN);
            this.sendBroadcast(rootintent);
            Values.isUserAdmin = false;
            Values.isRootAdmin = true;
            if (mIsChecked) {
                try {
                    Settings.System.putString(getContentResolver(), LAST_PSW, mPassword);
                    Settings.System.putString(getContentResolver(), LAST_USER, " ");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            EventBusUtils.postEvent(Values.BUS_EVENT_MAIN_ACTIVITY, Values.MAIN_ONLY_USB);
            Finish();
        } else if (mPassword.equals(SuperPassword) || mPassword.equals(Values.DEVICE_H9_DEFAULT_SUPER_PASSWORD)) {
            Values.isUserAdmin = false;
            Values.isRootAdmin = true;
            Finish();
        } else {
            mEtPassword.setText("");
            TTSToast.showToast(this, R.string.wrong_password);
        }
    }

    @Nullable
    private PcSocketNewUtils.UserInfo getUserInfo() {
        List<PcSocketNewUtils.UserInfo> userInfoList;
        PcSocketNewUtils.UserInfo loginUser = null;
        userInfoList = PcSocketNewUtils.UserInfo.retrieveUsers(this);
        for (int i = 0; i < userInfoList.size(); i++) {
            String username = userInfoList.get(i).getName();
            String password = userInfoList.get(i).getPwd();
            String number = userInfoList.get(i).getNumber();
            if (mUsername.equals(username) || mUsername.equals(number)) {
                UserPassword = password;
                isUseradmin = true;
                loginUser = userInfoList.get(i);
            }
            Log.e("Password", userInfoList.get(i).toString());
        }

        return loginUser;
    }

    private void initDefultUser() {
        PcSocketNewUtils.UserInfo defultUser1 = new PcSocketNewUtils.UserInfo();
        PcSocketNewUtils.UserInfo defultUser2 = new PcSocketNewUtils.UserInfo();
        PcSocketNewUtils.UserInfo defultUser3 = new PcSocketNewUtils.UserInfo();
        PcSocketNewUtils.UserInfo defultUser4 = new PcSocketNewUtils.UserInfo();
        PcSocketNewUtils.UserInfo defultUser5 = new PcSocketNewUtils.UserInfo();

        initUser(defultUser1, "1", "ID1", "001", "123456");
        initUser(defultUser2, "2", "ID2", "002", "123456");
        initUser(defultUser3, "3", "ID3", "003", "123456");
        initUser(defultUser4, "4", "ID4", "004", "123456");
        initUser(defultUser5, "5", "ID5", "005", "123456");

        PcSocketNewUtils.UserInfo.createUser(defultUser1, this);
        PcSocketNewUtils.UserInfo.createUser(defultUser2, this);
        PcSocketNewUtils.UserInfo.createUser(defultUser3, this);
        PcSocketNewUtils.UserInfo.createUser(defultUser4, this);
        PcSocketNewUtils.UserInfo.createUser(defultUser5, this);
    }

     private void createUser( String Id, String Name, String Num, String pwd){
         PcSocketNewUtils.UserInfo defultUser = new PcSocketNewUtils.UserInfo();
         initUser(defultUser, Id, Name, Num, pwd);
         PcSocketNewUtils.UserInfo.createUser(defultUser, this);
     }

    private void initUser(PcSocketNewUtils.UserInfo defultUser1, String Id, String Name, String Num, String pwd) {
        defultUser1.setId(Id);
        defultUser1.setName(Name);
        defultUser1.setNumber(Num);
        defultUser1.setPwd(pwd);
    }

    private void Finish() {
        EventBusUtils.postEvent(Values.BUS_EVENT_SOCKET_CONNECT, Values.SERVICE_REGISTER_SUCCESS);
        EventBusUtils.postEvent(Values.BUS_EVENT_LAUNCHER_FRAGMENT, Values.LAUNCHER_FRAGMENT);
        finish();

    }

    private void startRecord() {
        if (mRecordManager.needBootRecord()) {
            Intent intent = new Intent();
            intent.setAction("zzx_action_record");
            this.sendBroadcast(intent);
        }
    }

    private String checkNull(String psw, String defult) {
        if (psw == null || psw.equals("")) {
            return defult;
        } else return psw;
    }

    @Override
    public void onBackPressed() {
        return;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBusUtils.unregisterEvent(Values.BUS_EVENT_FINISH_PASSWORD, this);
        try {
            Settings.System.putInt(getContentResolver(), "PasswordActivity", 0);//确定acitivity是否还活着
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
