package com.zzx.police.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.EditText;

import com.zzx.police.MainActivity;
import com.zzx.police.R;
import com.zzx.police.ZZXConfig;
import com.zzx.police.data.Values;
import com.zzx.police.utils.TTSToast;


public class PasswordBaseActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "password";
    private EditText mEtPassword;
    private String mPassword;
    private String AdminPassword;
    private String SuperPassword;
    private String mClassname;
    private String mPackagename;
    private Integer mExtraValues;
    private String mExtraKey;
    public static final String CLASS_NAME_FINISH_ACTIVITY = "finish_activity_class_name";
    public static final String PACKAGE_NAME_FINISH_ACTIVITY = "finish_activity_package_name";
    public static final String EXTRA_VALUES_FINISH_ACTIVITY = "finish_activity_extra_values";
    public static final String EXTRA_KEY_FINISH_ACTIVITY = "finish_activity_extra_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_password);
        mEtPassword = (EditText) findViewById(R.id.et_password);
        findViewById(R.id.btn_positive).setOnClickListener(this);
        mClassname = getIntent().getExtras().getString(CLASS_NAME_FINISH_ACTIVITY);
        mPackagename = getIntent().getExtras().getString(PACKAGE_NAME_FINISH_ACTIVITY);
        mExtraValues = getIntent().getExtras().getInt(EXTRA_VALUES_FINISH_ACTIVITY);
        mExtraKey = getIntent().getExtras().getString(EXTRA_KEY_FINISH_ACTIVITY);
    }

    private void getDefultPsw() {
        AdminPassword = checkNull(Settings.System.getString(getContentResolver(), Values.ADMIN_PSW), Values.DEVICE_DEFAULT_PASSWORD);
        SuperPassword = checkNull(Settings.System.getString(getContentResolver(), Values.SUPER_PSW), Values.DEVICE_H9_DEFAULT_SUPER_PASSWORD);
    }

    @Override
    public void onClick(View v) {
        mPassword = mEtPassword.getText().toString().trim();
        switch (v.getId()) {
            case R.id.btn_positive:
                checkPassword();
                break;
        }
    }

    private void checkPassword() {
        if (mPassword == null || mPassword.equals("")) {
            TTSToast.showToast(this, R.string.null_password);
            return;
        }
        getDefultPsw();
        if (ZZXConfig.IsBrazilVerion) {
            if (mPassword.equals(AdminPassword)) {
                Finish();
            } else if (mPassword.equals(SuperPassword) || mPassword.equals(Values.DEVICE_DEFAULT_PASSWORD_BRAZIL)) {
                Finish();
            } else {
                mEtPassword.setText("");
                TTSToast.showToast(this, R.string.wrong_password);
            }
        } else {
            if (mPassword.equals(AdminPassword)) {
                Finish();
            } else if (mPassword.equals(SuperPassword) || mPassword.equals(Values.DEVICE_H9_DEFAULT_SUPER_PASSWORD)) {
                Finish();
            } else {
                mEtPassword.setText("");
                TTSToast.showToast(this, R.string.wrong_password);
            }
        }

    }


    private void Finish() {
        try {
            Intent intent = new Intent();
            if (mClassname != null && mPackagename != null) {
                intent.setClassName(mPackagename, mClassname);
                if(mExtraKey!=null){
                    intent.putExtra(mExtraKey,mExtraValues);
                }
            } else {
                intent.setClass(this, MainActivity.class);
            }

            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
//            Toast.makeText(getApplicationContext(),"")
        }

        finish();
    }

    private String checkNull(String psw, String defult) {
        if (psw == null || psw.equals("")) {
            return defult;
        } else return psw;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
