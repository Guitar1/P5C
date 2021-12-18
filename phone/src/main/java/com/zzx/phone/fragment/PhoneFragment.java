package com.zzx.phone.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.zzx.phone.R;
import com.zzx.phone.manager.PhoneManager;

/**@author Tomy
 * Created by Tomy on 2014/6/24.
 */
public class PhoneFragment extends Fragment implements View.OnClickListener {
    private Button mBtnAnswer   = null;
    private Button mBtnHungup   = null;
    private Activity mContext   = null;

    @Override
    public void onAttach(Activity activity) {
        mContext = activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mContext = null;
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        /*mBtnAnswer  = (Button) view.findViewById(R.id.btn_answer);
        mBtnHungup  = (Button) view.findViewById(R.id.btn_hangup);
        mBtnAnswer.setOnClickListener(this);
        mBtnHungup.setOnClickListener(this);*/
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_answer:
                new PhoneManager(mContext).doAnswerRingingCall();
                break;
            case R.id.btn_hangup:
                break;
        }
    }
}
