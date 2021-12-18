package com.zzx.police.view.ftpdown;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.zzx.police.R;
import com.zzx.police.data.FTPDownLoadInfo;
import com.zzx.police.utils.FTPDownUtil;

import java.lang.reflect.Field;

import static com.zzx.police.R.id.percent;

/**
 * Created by wanderFly on 2017/11/28.
 * 显示FTP当前文件的下载百分比
 */
public class FloatingWindowSmall extends LinearLayout implements View.OnTouchListener {
    //private static final String TAG = "FloatingWindowSmall";
    private static final String TAG = "FTPDownService";
    private static final boolean DEBUG = false;

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private Button mPercent;
    //当前手指位置在屏幕上的横纵坐标
    private float xInScreen;
    private float yInScreen;
    //记录手指按下时在屏幕上的横纵坐标(手指按下时可以在屏幕上滑动等，所以当前手指的位置可能和按下时的位置发生变化)
    private float xDownInScreen;
    private float yDownInScreen;
    //记录手指按下时在小悬浮窗View上横纵坐标的相对位置
    private float xInView;
    private float yInView;
    //是否需要将表示百分比的按钮背景设置为按下状态(通过xml的selector方式时有时图片不会变成按下状态且暂未解决,所以改用这个方法)
    private boolean mIsNeedUpdateImage = true;
    //小悬浮窗高度和宽度
    public int viewWidth;
    public int viewHeight;
    //记录系统状态栏的高度
    private int statusBarHeight;

    public interface OnSmallWindowClickListener {
        void onSmallWindowClick();
    }

    private OnSmallWindowClickListener mListener;

    public void setOnSmallWindowLister(OnSmallWindowClickListener listener) {
        mListener = listener;
    }


    /**
     * 在构造函数中初始化数据
     **/
    public FloatingWindowSmall(Context context) {
        super(context);
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater inflater = LayoutInflater.from(context);
        LinearLayout ly = (LinearLayout) inflater.inflate(R.layout.float_window_small, this);
        LinearLayout smallWindow = (LinearLayout) ly.findViewById(R.id.small_layout);
        /**
         * 1.本应用中即使不获取正确的长宽值，影响也不大 (所以你可以忽略下面的注释) \-(0.0)-/
         * 2.在代码前面，可以看到通过布局加载器在{@link FloatingWindowSmall}线性布局中，
         *   加载了一个变量名为{@link ly}的线性布局.
         * 3.{@link FloatingWindowSmall} 线性布局在没有调用{@link #onMeasure(int, int)}之前，
         *   得到的{@link #getWidth()}{@link #getHeight()}宽和高是为0的.
         * 4.这里为了能在{@link #onMeasure(int, int)} 方法之前，获取到{@link FloatingWindowSmall}的宽和高，
         *   做了三件事:
         *          a.将加载进来的布局宽和高视为{@link FloatingWindowSmall}的宽和高(因为该布局中只加载了{@link ly}布局)
         *          b.通过findViewById获取到线性布局的实例{@link smallWindow},
         *            然后获取到smallWindow.getLayoutParams()对象.(ly.getLayoutParams()会为null)
         *          c.{@link smallWindow}对应的线性布局给了具体的长丶宽(单位:dp)值.(如果用wrap_content获取到的长宽为0)
         * */
        viewHeight = smallWindow.getLayoutParams().height;
        viewWidth = smallWindow.getLayoutParams().width;
        mPercent = (Button) ly.findViewById(percent);
        mPercent.setOnTouchListener(this);

        d(""
                + "FloatingWindowSmall: "
                + "\n" + "view.width :" + viewWidth
                + "\n" + "view.height:" + viewHeight
                + "\n" + "width      :" + getWidth()
                + "\n" + "height     :" + getHeight()
                + "\n" + "layout     :" + ly.getLayoutParams()
        );
    }


    private void d(String msg) {
        if (DEBUG) Log.d(TAG, "" + msg);
    }

    private void e(String msg) {
        if (DEBUG) Log.e(TAG, "" + msg);
    }

    /**
     * 设置mLayoutParams 参数
     *
     * @param layoutParams 布局参数
     */
    public void setParams(WindowManager.LayoutParams layoutParams) {
        this.mLayoutParams = layoutParams;
    }

    /**
     * 设置文件下载的百分比
     *
     * @param info 文件下载对象
     */
    public void setPercent(FTPDownLoadInfo info) {
        //String content = "文件\n下发" + percent + "%";
        mPercent.setText(getStatusContent(info));
    }
    public void setUpload(String progress){
        mPercent.setText(progress);
    }

    private String getStatusContent(FTPDownLoadInfo info) {
        String content;
        switch (info.getStatus()) {
            case FTPDownUtil.DOWN_STATUS_DEFAULT:
                content = "等待";
                break;
            case FTPDownUtil.DOWN_STATUS_READY:
                content = "准备";
                break;
            case FTPDownUtil.DOWN_STATUS_START:
                content = "开始";
                break;
            case FTPDownUtil.DOWN_STATUS_TRANSFERRED:
                content = (int) (info.getProgress() * 100) + "%";
                break;
            case FTPDownUtil.DOWN_STATUS_COMPLETED:
                content = "完成";
                break;
            case FTPDownUtil.DOWN_STATUS_ABORTED:
                content = "中止";
                break;
            case FTPDownUtil.DOWN_STATUS_FAILED:
                content = "失败";
                break;
            case FTPDownUtil.DOWN_STATUS_NETWORK_IS_UNAVAILABLE:
                content = "无网";
                break;
            case FTPDownUtil.DOWN_STATUS_LOGIN_FAILED:
                content = "登陆失败";
                break;
            case FTPDownUtil.DOWN_STATUS_LOGIN_SUCCESS:
                content = "登陆成功";
                break;
            case FTPDownUtil.DOWN_STATUS_FILE_NO_FIND:
                content = "没有文件";
                break;
            case FTPDownUtil.DOWN_STATUS_ERROR:
                content = "错误";
                break;
            default:
                return "";
        }
        return content;
    }


    /**
     * 获取小窗口的高度
     */
    public int getViewHeight() {
        d("getViewHeight: " + viewHeight);
        return viewHeight;
    }


    /**
     * 获取小窗口的宽
     */
    public int getViewWidth() {
        d("getViewWidth: " + viewWidth);
        return viewWidth;
    }


    /**
     * 更新小悬浮窗在屏幕中的位置
     */
    private void updateViewPosition() {
        mLayoutParams.x = (int) (xInScreen - xInView);
        mLayoutParams.y = (int) (yInScreen - yInView);
        mWindowManager.updateViewLayout(this, mLayoutParams);
    }

    /**
     * 通过反射机制，获取状态栏高度
     **/
    private float getStatusBarHeight() {
        if (statusBarHeight == 0) {
            try {
                Class<?> c = Class.forName("com.android.internal.R$dimen");
                Object o = c.newInstance();
                Field field = c.getField("status_bar_height");
                int x = (int) field.get(o);
                statusBarHeight = getResources().getDimensionPixelSize(x);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return statusBarHeight;
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                d("onTouch: ACTION_DOWN");
                //手指按下时记录必要的数据，纵坐标的值都要减去状态栏高度
                xInView = event.getX();
                yInView = event.getY();
                xDownInScreen = event.getRawX();
                yDownInScreen = event.getRawY() - getStatusBarHeight();
                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - getStatusBarHeight();
                if (mIsNeedUpdateImage) {
                    e("onTouch: 设备背景图片文按下状态");
                    mPercent.setBackgroundResource(R.drawable.ftp_down_percent_press);
                    mIsNeedUpdateImage = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                d("onTouch: ACTION_DOWN");
                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - getStatusBarHeight();
//                updateViewPosition();
                break;
            case MotionEvent.ACTION_UP:
                d("onTouch: ACTION_UP");
                mPercent.setBackgroundResource(R.drawable.ftp_down_percent_normal);
                mIsNeedUpdateImage = true;
                //如果手指离开屏幕时，xDownInScreen和xInScreen相等，
                //且yDownInScreen和yInScreen相等，则视为触发了单击事件
                if (xDownInScreen == xInScreen && yDownInScreen == yInScreen) {
                    if (mListener != null) {
                        mListener.onSmallWindowClick();
                    }
                }
                break;
            default:
                break;
        }
        return false;
    }
}
