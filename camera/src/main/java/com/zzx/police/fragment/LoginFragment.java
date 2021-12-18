package com.zzx.police.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.zzx.police.MainActivity;
import com.zzx.police.R;
import com.zzx.police.data.IntentInfo;
import com.zzx.police.data.Values;
import com.zzx.police.utils.DeviceUtils;
import com.zzx.police.utils.EventBusUtils;
import com.zzx.police.utils.TTSToast;

/**@author Tomy
 * Created by Tomy on 2014/6/19.
 */
public class LoginFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "LauncherFragment: ";
    private Activity mContext   = null;
    private EditText mEtPolice = null;
    private EditText mEtDevice = null;
    private Button mBtnLogin    = null;
//    public static ProtocolUtils mProtocolUtils = null;
    private ProgressDialog mDialog = null;
    private Handler mHandler = null;
    private Button mBtnLauncher = null;

    @Override
    public void onAttach(Activity activity) {
        Values.LOG_I(TAG, "onAttach()");
        mContext = activity;
        mHandler = new Handler();
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.login_container, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mEtPolice   = (EditText) view.findViewById(R.id.et_police_number);
        mEtDevice   = (EditText) view.findViewById(R.id.et_device_num);
        initInfo();
        mBtnLogin   = (Button) view.findViewById(R.id.btn_positive);
        mBtnLauncher = (Button) view.findViewById(R.id.btn_skip);
        mBtnLauncher.setOnClickListener(this);
        mBtnLogin.setOnClickListener(this);
        super.onViewCreated(view, savedInstanceState);
    }

    private void initInfo() {
        String policeNum = DeviceUtils.getPoliceNum(mContext);
        String deviceNum = DeviceUtils.getDeviceNum(mContext);
        if (!policeNum.equals(Values.POLICE_DEFAULT_NUM)) {
            mEtPolice.setHint(policeNum);
        }
        if (!deviceNum.equals(Values.DEVICE_DEFAULT_NUM)) {
            mEtDevice.setHint(deviceNum);
        }
    }

    @Override
    public void onDetach() {
        Values.LOG_I(TAG, "onDetach()");
        mContext = null;
        super.onDetach();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_positive:
                login();
                break;
            case R.id.btn_skip:
                goLauncher();
                break;
        }
    }

    private void goLauncher() {
        getFragmentManager().beginTransaction().replace(R.id.container, Fragment.instantiate(mContext, LauncherFragment.class.getName())).commit();
//        getFragmentManager().beginTransaction().replace(R.id.container, Fragment.instantiate(mContext, PictureModeSettingFragment.class.getName())).addToBackStack(null).commit();
    }

    private void login() {
        String policeNumber = mEtPolice.getText().toString();
        if (!policeNumber.matches("[0-9]{6}")) {
            TTSToast.showToast(mContext, R.string.wrong_police_number, true);
            return;
        }
        String deviceNumber = mEtDevice.getText().toString();
        if (!deviceNumber.matches("[0-9]{5}")) {
            TTSToast.showToast(mContext, R.string.wrong_device_number, true);
            return;
        }
        if (!DeviceUtils.checkHasService(mContext)) {
            TTSToast.showToast(mContext, R.string.service_not_set, false, Toast.LENGTH_LONG);
        }
        IntentInfo intentInfo = new IntentInfo(Values.ACTION_LOGIN_STATE, 0);
        EventBusUtils.postEvent(Values.BUS_EVENT_SEND_SYSTEM_BROADCAST, intentInfo);
        DeviceUtils.writePoliceNum(mContext, policeNumber);
        DeviceUtils.writeDeviceNum(mContext, deviceNumber);
        getFragmentManager().beginTransaction().replace(R.id.container, Fragment.instantiate(mContext, LauncherFragment.class.getName())).commit();
        EventBusUtils.postEvent(Values.BUS_EVENT_RESET_SOCKET, Values.SOCKET_RECONNECTION);
//        showDialog();
    }

    private void showDialog() {
        if (mDialog == null) {
            createDialog();
        }
        mDialog.show();
    }

    private void createDialog() {
        mDialog = new ProgressDialog(mContext, R.style.CustomDialogTheme);
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setMessage(getString(R.string.registering));
        mDialog.setCanceledOnTouchOutside(false);
    }

    public void onEventBackgroundThread() {
        Values.LOG_I(TAG, "onEvenBackgroundThread()");
    }

    public void onEvent() {

    }

    public void onEventAsync() {

    }

    public void onEventMainThread(Integer what) {
        Values.LOG_I(TAG, "onEventMainThread().what = " + what);
        switch (what) {
            case Values.SERVICE_REGISTER_SUCCESS:
                TTSToast.showToast(mContext, R.string.register_success);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mDialog != null)
                            mDialog.dismiss();
                        FragmentManager manager = getFragmentManager();
                        FragmentTransaction transaction = manager.beginTransaction();
                        transaction.replace(R.id.container, Fragment.instantiate(mContext, LauncherFragment.class.getName())).commit();
                    }
                }, 300);
                break;
            case Values.SERVICE_REGISTER_FAILED:
                TTSToast.showToast(mContext, R.string.register_failed);
                MainActivity.mProtocolUtils = null;
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mDialog.dismiss();
                    }
                }, 300);
                break;
        }
    }
}
