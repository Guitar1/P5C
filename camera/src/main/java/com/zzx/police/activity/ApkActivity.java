package com.zzx.police.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.zzx.police.R;
import com.zzx.police.adapter.ApkViewPagerAdapter;
import com.zzx.police.data.ApkInfo;
import com.zzx.police.data.Values;
import com.zzx.police.utils.EventBusUtils;
import com.zzx.police.view.ConfirmDialog;
import com.zzx.police.view.PagePointLayout;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ApkActivity extends Activity implements AdapterView.OnItemClickListener, ViewPager.OnPageChangeListener, AdapterView.OnItemLongClickListener, View.OnClickListener {

    public static final String NAV_APK_SELECTED = "navApk";
    private ApkViewPagerAdapter mAdapter;
    private PackageManager mManager;
    private List<ApkInfo> mList;
    private PagePointLayout mPoint;
    private int mPosition = 0;
    private Context mContext;
    private ConfirmDialog mDialog;
    private int mIndex = 0;
    private UninstallReceiver mReceiver;
    private ViewPager mViewPager;
    private int mCurrent = 0;
    public static int APP_NUM = 9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apk);
        mPoint = (PagePointLayout) findViewById(R.id.page_point);
        mViewPager = (ViewPager) findViewById(R.id.apk_view_pager);
        mViewPager.setOnPageChangeListener(this);


        mContext = this;
        mList = new ArrayList<>();
        initDialog();
        mManager = getPackageManager();
        mAdapter = new ApkViewPagerAdapter(this);
        mAdapter.setOnItemClickListener(this);
        mAdapter.setOnItemLongClickListener(this);
        EventBusUtils.registerEvent(Values.ACTION_APK, this);
        EventBusUtils.postEvent(Values.ACTION_APK, Values.SEARCH_APK_START);
        initUninstallReceiver();
    }

    @Override
    protected void onDestroy() {
        EventBusUtils.unregisterEvent(Values.ACTION_APK, this);
        mPoint = null;
        mViewPager = null;
        mContext.unregisterReceiver(mReceiver);
        mContext = null;
        super.onDestroy();
    }

    private void initUninstallReceiver() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");
        mReceiver = new UninstallReceiver();
        mContext.registerReceiver(mReceiver, filter);
    }

    private void initDialog() {
        mDialog = new ConfirmDialog(mContext);
        mDialog.setMessage(R.string.uninstall_apk_sure);
        mDialog.setPositiveListener(this);
        mDialog.setNegativeListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                uninstallApk();
//                uninstallPackage();
                break;
            case R.id.btn_cancel:
                break;
        }
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }

    }
    /* 卸载apk */
    public void uninstallApk() {
        ApkInfo info = mList.get(mIndex);
        String packageName = info.mPackageName;
        Uri uri = Uri.parse("package:" + packageName);
        Intent intent = new Intent(Intent.ACTION_DELETE, uri);
        if (packageName.contains(Values.PACKAGE_NAME_ID_CARD) || packageName.contains(Values.PACKAGE_NAME_UVC)||
            packageName.contains(Values.PACKAGE_NAME_TALK_BAD)){
            Toast.makeText(this,"此应用不允许卸载",Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(intent);
    }
    private void uninstallPackage() {
        ApkInfo info = mList.get(mIndex);
        String packageName = info.mPackageName;
        try {
            Class packageClass = mManager.getClass();
            Method[] methods = packageClass.getMethods();
            for (Method method : methods) {
                if (method.getName().equals("deletePackage")) {
                    method.setAccessible(true);
                    method.invoke(mManager, packageName, null, 0x00000002);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int index = mPosition * APP_NUM + position;
        ApkInfo info = mList.get(index);
        Log.e("zzx", info.toString());
        Intent mintent = new Intent();
        mintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mintent.setClassName(info.mPackageName, info.mActivityName);
        startActivity(mintent);
        //      EventBusUtils.postEvent(NAV_APK_SELECTED, info);
        //   getFragmentManager().popBackStackImmediate();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        mCurrent = position;
        mIndex = mPosition * 12 + position;
        uninstallApk();
//        if (!mDialog.isShowing())
//            mDialog.show();
        return true;
    }

    void onEventBackgroundThread(Integer event) {
        switch (event) {
            case Values.SEARCH_APK_START:
                searchApk();
                break;
        }
    }

    void onEventMainThread(Integer event) {
        switch (event) {
            case Values.SEARCH_APK_FINISHED:
                Log.e("zzx", mList.toString() + "================count=" + mList.size());
                mViewPager.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
                mPoint.setPagePoint(mAdapter.getCount());
                break;
        }
    }

    private boolean checkMAPPackage(String packageName) {
        String MAP_PACKAGE_NAME = "";
        boolean ischeck = false;
        if ((packageName.equals(Values.PACKAGE_NAME_MAP_BD) || packageName.equals(Values.PACKAGE_NAME_MAP_GOOGLE))) {
            Locale locale = mContext.getResources().getConfiguration().locale;
            if (locale.getLanguage().equalsIgnoreCase(Locale.CHINA.getLanguage())) {
                MAP_PACKAGE_NAME = Values.PACKAGE_NAME_MAP_BD;
            } else {
                MAP_PACKAGE_NAME = Values.PACKAGE_NAME_MAP_GOOGLE;
            }
            ischeck = !packageName.equals(MAP_PACKAGE_NAME);
        }
        return ischeck;

    }

    private boolean checkExcludePackage(String packageName) {
        if (
            //  packageName.equals(Values.PACKAGE_NAME_DX_CALL) ||
                packageName.equals(Values.PACKAGE_NAME_XUNFEI)
                        || packageName.contains("com.zzx.police")
                        || packageName.contains("com.mst.v2")
                        || packageName.contains(Values.PACKAGE_NAME_SOUND_RECORD)
                        || packageName.contains(Values.PACKAGE_NAME_DX_CALL)
                        || packageName.contains(Values.PACKAGE_NAME_GOODGLE)
                        || packageName.contains(Values.PACKAGE_NAME_GOODGLE_PINYIN)
                        || packageName.contains(Values.PACKAGE_NAME_VIDEO_ASSIST)
                        || packageName.contains(Values.PACKAGE_NAME_UNION_BROAD)
                        || packageName.contains(Values.PACKAGE_NAME_TRANSLATION)
                ) {
            return true;
        }
        return false;
    }

    private void searchApk() { //除了对讲和地图，其他安装的应用都会显示
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> list = mManager.queryIntentActivities(intent, PackageManager.GET_ACTIVITIES);
        for (ResolveInfo info : list) {
            ActivityInfo activityInfo = info.activityInfo;
            if ((activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0 && !activityInfo.packageName.equals(Values.PACKAGE_NAME_TALK_BACK_RONG_HE)
                    && !activityInfo.packageName.equals("com.zzx.locations") && !activityInfo.packageName.contains(Values.PACKAGE_NAME_ID_CARD) &&
                    !activityInfo.packageName.equals(Values.PACKAGE_NAME_HGSPPT)
                    ) {
                continue;
            }
            if (checkMAPPackage(activityInfo.packageName)) {
                continue;
            }

            if (checkExcludePackage(activityInfo.packageName)) {
                continue;
            }
            ApkInfo apkInfo = new ApkInfo();
            apkInfo.mPackageName = activityInfo.packageName;
            apkInfo.mActivityName = activityInfo.name;
            apkInfo.mApkName = String.valueOf(info.loadLabel(mManager));
            mAdapter.addIcon(info.loadIcon(mManager));
            mAdapter.addName(apkInfo.mApkName);
            mList.add(apkInfo);
        }
        mAdapter.addFinish();
        EventBusUtils.postEvent(Values.ACTION_APK, Values.SEARCH_APK_FINISHED);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mPoint.setPageIndex(position);
        mPosition = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }


    private class UninstallReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String pkgName = intent.getDataString();
                if (pkgName.contains(mList.get(mIndex).mPackageName)) {
                    mList.remove(mIndex);
                    mAdapter.removeItem(mPosition, mCurrent);
                    mAdapter.notifyDataSetChanged();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
