package com.zzx.police.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.zzx.police.R;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**@author Tomy
 * Created by Tomy on 2014/8/23.
 */
public class CameraFragment extends Fragment implements View.OnClickListener, SurfaceHolder.Callback {
    private static final String TAG = "CameraFragment";
    private Camera mCamera;
    private Camera.Parameters mParameters;
    private SurfaceHolder mHolder;
    private Context mContext;
    public static final String CAMERA = "preferred-preview-size-for-video=1280x720;zoom=0;hue=middle;fb-smooth-level-max=4;recording-hint=true;max-num-detected-faces-hw=15;video-stabilization=false;zoom-supported=true;fb-smooth-level=0;cap-mode=normal;fb-sharp=0;aflamp-mode-values=off,on,auto;contrast=middle;whitebalance=auto;scene-mode=auto;jpeg-quality=90;afeng-min-focus-step=0;preview-format-values=yuv420sp,yuv420p,yuv420i-yyuvyy-3plane;rotation=90;jpeg-thumbnail-quality=100;burst-num=1;preview-format=yuv420sp;metering-areas=(0,0,0,0,0);video-size-values=176x144,480x320,640x480,864x480,1280x720,1920x1080;preview-size=960x540;focal-length=3.5;iso-speed=auto;cap-mode-values=normal,face_beauty,continuousshot,smileshot,bestshot,evbracketshot,autorama;flash-mode-values=off,on,auto,red-eye,torch;preview-frame-rate-values=15,24,30;hue-values=low,middle,high;max-num-metering-areas=9;aflamp-mode=off;fb-sharp-max=4;preview-frame-rate=30;focus-mode-values=auto,macro,infinity,continuous-picture,continuous-video,manual,fullscan;jpeg-thumbnail-width=160;video-size=1280x720;scene-mode-values=auto,portrait,landscape,night,night-portrait,theatre,beach,snow,sunset,steadyphoto,fireworks,sports,party,candlelight,hdr;preview-fps-range-values=(5000,60000);fb-sharp-min=-4;jpeg-thumbnail-size-values=0x0,160x128,320x240;contrast-values=low,middle,high;zoom-ratios=100,114,132,151,174,200,229,263,303,348,400;preview-size-values=176x144,320x240,352x288,480x320,480x368,640x480,720x480,800x480,800x600,864x480,960x540,1280x720;picture-size-values=320x240,640x480,1024x768,1280x720,1280x768,1280x960,1600x1200,2048x1536,2560x1440,2560x1920,3264x2448,3328x1872,2880x1728,3600x2160;edge-values=low,middle,high;preview-fps-range=5000,60000;auto-whitebalance-lock=false;min-exposure-compensation=-3;antibanding=auto;max-num-focus-areas=0;vertical-view-angle=42;fb-smooth-level-min=-4;horizontal-view-angle=55;fb-skin-color=0;brightness=middle;video-stabilization-supported=true;jpeg-thumbnail-height=128;brightness_value=-50;cam-mode=2;smooth-zoom-supported=true;capfname=/sdcard/DCIM/cap00;saturation-values=low,middle,high;zsd-mode=off;focus-mode=continuous-video;fb-skin-color-max=4;auto-whitebalance-lock-supported=true;fb-skin-color-min=-4;edge=middle;max-num-detected-faces-sw=0;picture-format-values=jpeg;iso-speed-values=auto,100,200,400,800,1600;max-exposure-compensation=3;video-snapshot-supported=true;exposure-compensation=0;exposure-compensation-step=1.0;brightness-values=low,middle,high;flash-mode=off;auto-exposure-lock=false;effect-values=none,mono,negative,sepia,aqua,whiteboard,blackboard;picture-size=3328x1872;max-zoom=10;effect=none;saturation=middle;whitebalance-values=auto,incandescent,fluorescent,warm-fluorescent,daylight,cloudy-daylight,twilight,shade;picture-format=jpeg;focus-distances=0.95,1.9,Infinity;zsd-mode-values=off;auto-exposure-lock-supported=true;afeng-max-focus-step=0;antibanding-values=off,50hz,60hz,auto;";

    @Override
    public void onAttach(Activity activity) {
//        initCamera();
        mContext = activity;
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        view.findViewById(R.id.btn_release_camera).setOnClickListener(this);
        view.findViewById(R.id.btn_start_camera).setOnClickListener(this);
        //拍照/录像
        SurfaceView mSurfaceView = (SurfaceView) view.findViewById(R.id.surface_view);
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        super.onViewCreated(view, savedInstanceState);
    }

    private void initCamera() {
        mCamera = Camera.open();
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void releaseCamera() {
        mCamera.stopPreview();
        try {
            mCamera.setPreviewDisplay(null);
            mCamera.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            //
            case R.id.btn_release_camera:
                //releaseCamera();
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        takePicture();
                    }
                }.start();
                break;
            case R.id.btn_start_camera:
                //showWindow();
                initCamera();
                break;
        }
    }

    private void showWindow() {
        WindowManager manager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams mWindowParams = new WindowManager.LayoutParams();
        /**设置成ERROR即能覆盖状态栏也能获取点击事件
         * @see android.view.WindowManager.LayoutParams#TYPE_SYSTEM_OVERLAY 能覆盖但是焦点还是在下面的Activity所以获取不到点击事件
         * @see android.view.WindowManager.LayoutParams#TYPE_SYSTEM_ALERT 无法覆盖状态栏
         * @see android.view.WindowManager.LayoutParams#FLAG_NOT_FOCUSABLE 此标志位使该Window没有输入焦点,不会接收按钮及按键事件,而传递给它下面可以获得焦点的Window.若不设置则被覆盖的Activity无法响应返回键等操作.
         * */
        mWindowParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        mWindowParams.alpha = 1;
        mWindowParams.format = PixelFormat.RGBA_8888;
        mWindowParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        TextView mRootContainer = new TextView(mContext);
        mWindowParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mRootContainer.setText("1111111111111111111111");
        manager.addView(mRootContainer, mWindowParams);
    }

    //拍照
    private void takePicture() {
        mParameters = mCamera.getParameters();
        try {
            mParameters.set("cap-mode", "continuousshot");
            mParameters.set("burst-num", 20);
            mParameters.set("focus-mode", "auto");
//            mParameters.set("");
            mParameters.set("picture-format", "jpeg");
            mCamera.setParameters(mParameters);
            Log.i(TAG,mParameters.flatten());
            mCamera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    /*try {
                        FileOutputStream outputStream = new FileOutputStream("/sdcard/test.png");
                        outputStream.write(data);
                        outputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/
                    Log.e(TAG, "onPictureTaken");
//                    mCamera.stopPreview();
//                    mCamera.startPreview();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mHolder = holder;
        initCamera();
        /*try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
