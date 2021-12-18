package com.zzx.phone.fragment;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.zzx.phone.R;
import com.zzx.phone.adapter.ViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**@author Tomy
 * Created by Tomy on 2014/6/24.
 */
public class ViewPagerFragment extends Fragment implements ViewPager.OnPageChangeListener, AdapterView.OnItemClickListener {
    private ViewPager mViewPager    = null;
    private ViewPagerAdapter mPagerAdapter  = null;
    private FragmentActivity mContext = null;
    private ImageView mIvScroll = null;
    private GridView mGridView  = null;
    private GridViewAdapter mGridAdapter = null;

    public void notifyDataSetChanged() {
        if (mPagerAdapter != null) {
            mPagerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        mContext = (FragmentActivity) activity;
        mPagerAdapter   = new ViewPagerAdapter(getChildFragmentManager(), mContext);
        mGridAdapter    = new GridViewAdapter();
        mGridAdapter.addItem(getString(R.string.tab_phone));
        mGridAdapter.addItem(getString(R.string.tab_call_history));
        mGridAdapter.addItem(getString(R.string.tab_sms));
        mGridAdapter.addItem(getString(R.string.tab_contact));
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_pager_container, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mViewPager  = (ViewPager) view.findViewById(R.id.view_pager);
        mIvScroll = (ImageView) view.findViewById(R.id.iv_scroll);
        mGridView   = (GridView) view.findViewById(R.id.grid_view);
        Log.i("tomy", "mViewPager.id = " + mViewPager.getId());

        mPagerAdapter.addItem(OutGoingFragment.class.getName());
        mPagerAdapter.addItem(HistoryFragment.class.getName());
        mPagerAdapter.addItem(SMSFragment.class.getName());
        mPagerAdapter.addItem(ContactFragment.class.getName());
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOnPageChangeListener(this);
        mGridView.setAdapter(mGridAdapter);
        mGridView.setOnItemClickListener(this);
        mGridView.setHorizontalSpacing(0);
        mGridView.setSelector(R.drawable.bg_table);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mViewPager.setCurrentItem(position, true);
    }

    public class GridViewAdapter extends BaseAdapter {
        List<String> mList  = null;
        class ViewHolder {
            View view = null;
            TextView tvTag = null;
        }
        public GridViewAdapter() {
            this.mList = new ArrayList<String>(4);
        }

        public void addItem(String tag) {
            mList.add(tag);
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public String getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                convertView = mContext.getLayoutInflater().inflate(R.layout.table_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.view = convertView.findViewById(R.id.view);
                viewHolder.tvTag = (TextView) convertView.findViewById(R.id.tv_tag);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            if (position == 0) {
                viewHolder.view.setVisibility(View.GONE);
            }
            viewHolder.tvTag.setText(getItem(position));
            return convertView;
        }
    }
}
