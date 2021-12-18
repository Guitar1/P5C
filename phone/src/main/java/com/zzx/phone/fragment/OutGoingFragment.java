package com.zzx.phone.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.zzx.phone.R;

/**@author Tomy
 * Created by Tomy on 2014/6/25.
 */
public class OutGoingFragment extends Fragment implements View.OnClickListener {
    /*private ViewPagerAdapter mAdapter = null;

    public OutGoingFragment(ViewPagerAdapter adapter) {
        mAdapter = adapter;
    }*/
    private Button mBtnReplace = null;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.container, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mBtnReplace = (Button) view.findViewById(R.id.btn_replace);
        mBtnReplace.setOnClickListener(this);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onClick(View v) {

    }
}
