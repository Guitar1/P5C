package com.zzx.police.view.ftpdown;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.zzx.police.R;
import com.zzx.police.adapter.FTPDownAdapter;
import com.zzx.police.data.FTPDownLoadInfo;

import java.util.LinkedHashMap;


/**
 * Created by wanderFly on 2017/11/28.
 * 显示FTP文件下载的详细列表
 */
public class FloatingWindowBig extends LinearLayout implements View.OnClickListener {
    private static final boolean DEBUG = true;
    private static final String TAG = "FloatingWindowBig";
    //记录大悬浮窗口的宽和高
    private int viewWidth;
    private int viewHeight;
    private FTPDownAdapter mAdapter;
    private Button btnClose;
    private Button btnMin;
    private TextView tvRate;//下载比例
    private ImageView ivFTPLogin;
    private ListView mListView;


    public interface OnBigWindowListener {
        void onBigWindowKeyBack();

        void onBigWindowButtonClick(View v);

    }

    private OnBigWindowListener mListener;

    public void setOnBigWindowListener(OnBigWindowListener listener) {
        this.mListener = listener;
    }


    public FloatingWindowBig(final Context context) {
        this(context, null);
    }

    public FloatingWindowBig(Context context, @Nullable LinkedHashMap<String, FTPDownLoadInfo> datas) {
        super(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        LinearLayout ly = (LinearLayout) inflater.inflate(R.layout.float_window_big, this);
        btnClose = (Button) ly.findViewById(R.id.btn_close);
        btnMin = (Button) ly.findViewById(R.id.btn_min);
        tvRate = (TextView) ly.findViewById(R.id.tv_rate);
        ivFTPLogin = (ImageView) ly.findViewById(R.id.iv_ftp_login);
        LinearLayout mBigWindow = (LinearLayout) ly.findViewById(R.id.big_layout);
        mListView = (ListView) ly.findViewById(R.id.list_ftp_down);
        viewHeight = mBigWindow.getLayoutParams().height;//这个线性布局没有设置具体长度，获取的值不正确，影响不大
        viewWidth = mBigWindow.getLayoutParams().width;
        btnClose.setOnClickListener(this);
        btnMin.setOnClickListener(this);
        mAdapter = new FTPDownAdapter(context, datas);
        mListView.setAdapter(mAdapter);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (mListener != null) {
                mListener.onBigWindowKeyBack();
            }
        }
        return super.dispatchKeyEvent(event);
    }


    @Override
    public void onClick(View v) {
        if (mListener != null) {
            mListener.onBigWindowButtonClick(v);
        }

    }

    private void d(String msg) {
        if (DEBUG) Log.d(TAG, "" + msg);
    }

    public int getViewWidth() {
        return viewWidth;
    }

    public int getViewHeight() {
        return viewHeight;
    }

    public FTPDownAdapter getAdapter() {
        return mAdapter;
    }

    public ListView getListView() {
        return mListView;
    }

    public void setButtonMinVis() {
        btnMin.setVisibility(VISIBLE);
        btnClose.setVisibility(GONE);
    }

    public void setButtonCloseVis() {
        btnMin.setVisibility(GONE);
        btnClose.setVisibility(VISIBLE);
    }

    public void setFTPLoginSuccess() {
        ivFTPLogin.setImageResource(R.drawable.login_success);
    }

    public void setFTPLoginFailed() {
        ivFTPLogin.setImageResource(R.drawable.login_failed);
    }

    public void setRate(String rate) {
        tvRate.setText(rate);
    }


}
