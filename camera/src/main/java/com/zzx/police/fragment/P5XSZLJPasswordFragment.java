package com.zzx.police.fragment;


import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.zzx.police.MainActivity;
import com.zzx.police.R;
import com.zzx.police.ZZXConfig;
import com.zzx.police.data.Values;
import com.zzx.police.manager.UsbMassManager;
import com.zzx.police.services.CameraService;
import com.zzx.police.utils.EventBusUtils;
import com.zzx.police.utils.TTSToast;

/**@author Tomy
 * Created by Tomy on 2014/10/27.
 */
public class P5XSZLJPasswordFragment extends Fragment implements View.OnClickListener {
    private Context mContext;
    private EditText mEtPassword;
    private EditText mEtNewPassword;
    private TextView mTvNewPassword;
    private TextView mTvForgetPassword;
    private TextView mTvPassword;
    private String mPassword;
    private String mNewPassword;
    private boolean mForgetMode = false;

    @Override
    public void onAttach(Activity activity) {
        mContext = activity;
        super.onAttach(activity);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_password, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mEtPassword = (EditText) view.findViewById(R.id.et_password);
        mEtNewPassword  = (EditText) view.findViewById(R.id.et_new_password);
        mTvNewPassword  = (TextView) view.findViewById(R.id.tv_new_password);
        mTvPassword     = (TextView) view.findViewById(R.id.tv_password);
        view.findViewById(R.id.btn_positive).setOnClickListener(this);
        view.findViewById(R.id.btn_skip).setOnClickListener(this);
        mTvForgetPassword = (TextView) view.findViewById(R.id.btn_forget_password);
        mTvForgetPassword.setOnClickListener(this);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        mPassword = mEtPassword.getText().toString();
        mNewPassword = mEtNewPassword.getText().toString();
        switch (v.getId()) {
            case R.id.btn_positive:
                checkPassword();
                break;
            case R.id.btn_skip:
                EventBusUtils.postEvent(Values.BUS_EVENT_MAIN_ACTIVITY, Values.REPLACE_LAUNCHER_FRAGMENT);
                break;
            case R.id.btn_forget_password:
                forgetPassword();
                break;
        }
    }

    private void forgetPassword() {
        mForgetMode = true;
        mEtNewPassword.setVisibility(View.VISIBLE);
        mTvNewPassword.setVisibility(View.VISIBLE);
        mTvForgetPassword.setVisibility(View.GONE);
        mTvPassword.setText(R.string.original_password);
    }

    private void checkPassword() {
        if (mPassword == null || mPassword.equals("")) {
            TTSToast.showToast(mContext, R.string.null_password);
            return;
        }
        String password = Settings.System.getString(mContext.getContentResolver(), Values.DEVICE_PASSWORD);
        if (password == null || password.equals("")) {
            if (ZZXConfig.isP5X_SZLJ){
                password = Values.DEVICE_P5X_SZLJ_DEFAULT_PASSWORD;
            }else {
                password = Values.DEVICE_DEFAULT_PASSWORD;
            }

        }
        if (mPassword.equals(password) || mPassword.equals(Values.DEVICE_DEFAULT_SUPER_PASSWORD)) {
            /*if (mForgetMode) {
                if (mNewPassword == null || mNewPassword.equals("")) {
                    TTSToast.showToast(mContext, R.string.null_new_password);
                    return;
                } else {
                    Settings.System.putString(mContext.getContentResolver(), Values.DEVICE_PASSWORD, mNewPassword);
                }
            }
            EventBusUtils.postEvent(Values.BUS_EVENT_MAIN_ACTIVITY, Values.REPLACE_LOGIN_FRAGMENT);*/
            startCameraService();
            getFragmentManager().beginTransaction().replace(R.id.container, Fragment.instantiate(mContext, LauncherFragment.class.getName())).commit();

        } else {
            mEtPassword.setText("");
            TTSToast.showToast(mContext, R.string.wrong_password);
        }
    }
    Intent mCameraIntent;
    private void startCameraService() {
        if (mCameraIntent==null)
        mCameraIntent = new Intent();
        mCameraIntent.setClass(mContext, CameraService.class);
        mCameraIntent.setAction(Values.ACTION_MAX_WINDOW);
        mCameraIntent.putExtra(Values.CAMERA_SERVICE_EXTRA_STRING, Values.EXTRA_RECORD);
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mContext.startService(mCameraIntent);
            }
        }.start();
    }

}
