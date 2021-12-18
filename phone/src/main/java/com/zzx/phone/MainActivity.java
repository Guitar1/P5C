package com.zzx.phone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.zzx.phone.adapter.GridViewAdapter;
import com.zzx.phone.adapter.ViewPagerAdapter;
import com.zzx.phone.data.Values;


public class MainActivity extends FragmentActivity implements ViewPager.OnPageChangeListener, AdapterView.OnItemClickListener {
    private ViewPager mViewPager    = null;
    public static ViewPagerAdapter mPagerAdapter  = null;
    private ImageView mIvScroll = null;
    private GridView mGridView  = null;
    private GridViewAdapter mGridAdapter = null;
    private BroadcastReceiver mReceiver = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_pager_container);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Values.LOGI("Tomy", "action = " + intent.getAction());
            }
        };
        IntentFilter intentFilter = new IntentFilter(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        registerReceiver(mReceiver, intentFilter);
        /*mPagerAdapter   = new ViewPagerAdapter(getSupportFragmentManager(), this);
        mGridAdapter    = new GridViewAdapter(this);
        mGridAdapter.addItem(getString(R.string.tab_phone));
        mGridAdapter.addItem(getString(R.string.tab_call_history));
        mGridAdapter.addItem(getString(R.string.tab_sms));
        mGridAdapter.addItem(getString(R.string.tab_contact));
        mViewPager  = (ViewPager) findViewById(R.id.view_pager);
        mIvScroll = (ImageView) findViewById(R.id.iv_scroll);
        mGridView   = (GridView) findViewById(R.id.grid_view);
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
        mGridView.setSelector(R.drawable.bg_table);*/
        /*if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ViewPagerFragment(), ViewPagerFragment.class.getName())
                    .commit();
        }*/
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
//        if (!mPagerAdapter.onBackPressed(mViewPager.getCurrentItem()))
//            finish();
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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
}
