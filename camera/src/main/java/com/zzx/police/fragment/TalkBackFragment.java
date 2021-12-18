package com.zzx.police.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.zzx.police.R;
import com.zzx.police.services.ITalkBackService;
import com.zzx.police.services.TalkBackService;

/**@author Tomy
 * Created by Tomy on 2016/6/15.
 */
public class TalkBackFragment extends Fragment implements View.OnLongClickListener, View.OnClickListener {
    private Button mBtnTalkBack;
    private ITalkBackService mService;
    private Context mContext;
    private TalkBackConnection mServiceConnection;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_talk_back, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindService();
        mBtnTalkBack = (Button) view.findViewById(R.id.btn_talk_back);
        mBtnTalkBack.setOnClickListener(this);
        mBtnTalkBack.setOnLongClickListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbindService();
    }

    private void bindService() {
        Intent intent = new Intent(mContext, TalkBackService.class);
        if (mServiceConnection == null) {
            mServiceConnection = new TalkBackConnection();
        }
        mContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindService() {
        mContext.unbindService(mServiceConnection);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_talk_back:
                try {
                    mService.requestTalkBack();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    class TalkBackConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ITalkBackService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    }
}
