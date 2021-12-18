package com.zzx.police.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zzx.police.R;
import com.zzx.police.adapter.GridViewPagerAdapter;
import com.zzx.police.data.Values;
import com.zzx.police.utils.EventBusUtils;

/**
 * @author Tomy
 *         Created by Tomy on 2014/7/2.
 *         dsj背景分页
 */
public class LauncherFragment extends Fragment implements ViewPager.OnPageChangeListener {
    private PagerAdapter mLauncherAdapter = null;
    private ImageView mIvLeft = null;
    private ImageView mIvRight = null;
    private TextView mTvUserName;

    @Override
    public void onAttach(Activity activity) {
        mLauncherAdapter = new GridViewPagerAdapter(activity);
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_launcher, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ViewPager mViewPager = (ViewPager) view.findViewById(R.id.view_pager);
        mIvLeft = (ImageView) view.findViewById(R.id.iv_left);
        mIvRight = (ImageView) view.findViewById(R.id.iv_right);
//        mTvUserName= (TextView) view.findViewById(R.id.tv_username);
        if (mLauncherAdapter.getCount() <= 1) {
            view.findViewById(R.id.point).setVisibility(View.GONE);
        }
        mViewPager.setAdapter(mLauncherAdapter);
        mViewPager.setOnPageChangeListener(this);
        EventBusUtils.registerEvent(Values.BUS_EVENT_LAUNCHER_FRAGMENT, this);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    /**
     * 分页滑动小图标
     * @param position
     */
    @Override
    public void onPageSelected(int position) {
        switch (position) {
            //0和1表示页数  0第一页  1第二页
            case 0:
                mIvLeft.setBackgroundResource(R.drawable.point_selected);
                mIvRight.setBackgroundResource(R.drawable.point_unselected);
                break;
            case 1:
                mIvLeft.setBackgroundResource(R.drawable.point_unselected);
                mIvRight.setBackgroundResource(R.drawable.point_selected);
                break;
        }
    }

    public void onEventMainThread(Integer what) {
        switch (what) {
            case Values.LAUNCHER_FRAGMENT:
//                if(Values.isUserAdmin){
//                    mTvUserName.setText(Values.UserNameNow);
//                }else {
//                    mTvUserName.setText("Administrator");
//                }
                Log.d("zzxzdm", "Update_username");
                break;

        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBusUtils.unregisterEvent(Values.BUS_EVENT_LAUNCHER_FRAGMENT, this);
    }
}
