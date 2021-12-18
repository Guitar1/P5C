package com.zzx.police.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.RelativeLayout;

import com.zzx.police.data.Values;

/**@author Tomy
 * Created by Tomy on 2016/5/4.
 */
public class MainLayout extends RelativeLayout {
    private static final String TAG = "MainLayout";

    public MainLayout(Context context) {
        super(context);
    }

    public MainLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MainLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Values.LOG_W(TAG, "dispatchKeyEvent = " + event.getKeyCode());
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (mListener != null) {
                mListener.onBackPressed();
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private KeyBackPressedListener mListener;
    public void setOnKeyBackPressedListener(KeyBackPressedListener listener) {
        mListener = listener;
    }

    public interface KeyBackPressedListener {
        void onBackPressed();
    }
}
