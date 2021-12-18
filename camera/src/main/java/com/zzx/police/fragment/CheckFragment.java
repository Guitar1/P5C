package com.zzx.police.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zzx.police.R;
import com.zzx.police.utils.TTSToast;

/**@author Tomy
 * Created by Tomy on 2015/8/8.
 */
public class CheckFragment extends Fragment implements View.OnClickListener {
    private Context mContext;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_check_in, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btn_check_in).setOnClickListener(this);
        view.findViewById(R.id.btn_check_out).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_check_in:
                TTSToast.showToast(mContext, R.string.start_work);
                break;
            case R.id.btn_check_out:
                TTSToast.showToast(mContext, R.string.off_duty);
                break;
        }
    }
}
