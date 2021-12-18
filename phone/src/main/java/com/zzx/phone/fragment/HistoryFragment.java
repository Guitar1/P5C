package com.zzx.phone.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.zzx.phone.MainActivity;
import com.zzx.phone.R;

/**@author Tomy
 * Created by Tomy on 2014/6/25.
 */
public class HistoryFragment extends Fragment implements View.OnClickListener {
    private Button mBtnReplace = null;
    private View mContainer = null;
    private Context mContext = null;
    private View mRootView = null;

    public HistoryFragment() {

    }

    @Override
    public void onAttach(Activity activity) {
        mContext = activity;
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContainer = container;
        Log.i("tomy", "container.id = " + container.getId());

        mRootView = inflater.inflate(R.layout.container, container, false);
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mBtnReplace = (Button) view.findViewById(R.id.btn_replace);
        mBtnReplace.setOnClickListener(this);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        Log.w("onClick", "mBtnReplace");
        /*FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.addToBackStack(null);
        ft.replace(R.id.container_layout, Fragment.instantiate(mContext, ReplaceFragment.class.getName()));
        ft.commit();*/
        /*FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(mContainer.getId(), Fragment.instantiate(mContext, ReplaceFragment.class.getName()), "android:switcher:" + mContainer.getId() + ":" + 1).addToBackStack(null);
//        ft.detach(this);
//        ft.add(mContainer.getId(), Fragment.instantiate(mContext, ReplaceFragment.class.getName()), "android:switcher:" + mContainer.getId() + ":" + 1);
        ft.addToBackStack(null);
        ft.commit();*/
        MainActivity.mPagerAdapter.replaceFragment(ReplaceFragment.class.getName(), 1);
//        mAdapter.notifyDataSetChanged();
    }
}
