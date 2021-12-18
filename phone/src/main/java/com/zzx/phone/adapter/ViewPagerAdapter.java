package com.zzx.phone.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.zzx.phone.R;

import java.util.ArrayList;
import java.util.List;

/**@author Tomy
 * Created by Tomy on 2014/6/24.
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = "ViewPagerAdapter";
    private Context mContext = null;
    private List<String> mList = null;
    private List<String> mOriginalList = null;
    private FragmentManager mFm = null;
    public ViewPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mFm = fm;
        mContext = context;
        mList = new ArrayList<>();
        mOriginalList = new ArrayList<>();
    }

    public void replaceFragment(String fragmentClass, int position) {
        mFm.beginTransaction().remove(mFm.findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + position)).commit();
        mList.remove(position);
        addItem(fragmentClass, position);
        notifyDataSetChanged();
    }

    public void addItem(String fragmentName) {
        mList.add(fragmentName);
        mOriginalList.add(fragmentName);
    }

    public void addItem(String fragmentClass, int position) {
        mList.add(position, fragmentClass);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return super.getPageTitle(position);
    }

    @Override
    public Fragment getItem(int position) {
        return Fragment.instantiate(mContext, mList.get(position));
    }

    @Override
    public int getItemPosition(Object object) {
//        if (object instanceof ReplaceFragment)
            return POSITION_NONE;
//        return POSITION_UNCHANGED;
    }

    public boolean onBackPressed(int currentItem) {
        if (mList.get(currentItem).equals(mOriginalList.get(currentItem))) {
            return false;
        }
        replaceFragment(mOriginalList.get(currentItem), currentItem);
        return true;
    }
}
