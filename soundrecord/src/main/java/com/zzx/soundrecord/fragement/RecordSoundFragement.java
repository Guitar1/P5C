package com.zzx.soundrecord.fragement;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zzx.soundrecord.R;
import com.zzx.soundrecord.service.RecordService;
import com.zzx.soundrecord.values.Values;

import java.util.ArrayList;

public class RecordSoundFragement extends Fragment implements
		View.OnClickListener {

    private static final String TAG = "RecordSoundFragement";
    private Activity mContext = null;
	private ViewGroup mRootLayout = null;
	private ViewGroup startBtnLayout = null;
	private ViewGroup functionBtnLayout = null;
	private ImageView startBtn = null;
	private ImageView soundListBtn = null;
	private ImageView stopBtn = null;
	private ImageView pauseBtn = null;
	private ImageView cancelBtn = null;
	private TextView timeText = null;
	private TextView fileNameText = null;
	/**
	 * Y轴缩小的比例
	 */
	private int rateY = 0;
	/**
	 * Y轴基线
	 */
	private int baseLine = 0;
	/**
	 * 为了节约绘画时间，每三个像素画一个数据
	 */
	private int divider = 2;
	private SurfaceView sfv = null;
	private Paint mPaint = null;
	private UpdateUIBroadCastReceiver updateUIBroadCastReceiver = null;
	private IntentFilter filter = null;
	private int recordingState = 0;
	private AlertDialog.Builder builder = null;
	private AlertDialog alert = null;

	@Override
	public void onAttach(Activity activity) {
		mContext = activity;
		mPaint = new Paint();
		mPaint.setARGB(255, 0, 160, 233);// 画笔为亮蓝色
		mPaint.setStrokeWidth(1);// 设置画笔粗细
		updateUIBroadCastReceiver = new UpdateUIBroadCastReceiver();
		filter = new IntentFilter();
		filter.addAction("updateFileName");
		filter.addAction("updateFunctionButton");
		filter.addAction("updateRecordingTime");
		filter.addAction("updateAudioWarv");
		mContext.registerReceiver(updateUIBroadCastReceiver, filter);
		Intent intent = new Intent();
		intent.setAction("initUI");
		mContext.sendBroadcast(intent);
		createCancelDialog();
		super.onAttach(activity);
	}

	private void createCancelDialog() {
		builder = new AlertDialog.Builder(mContext);
		builder.setMessage(
				mContext.getResources().getString(
						R.string.are_you_sure_cancel_the_record))
				.setCancelable(false)
				.setNegativeButton(
						mContext.getResources().getString(R.string.cancel),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						})
				.setPositiveButton(
						mContext.getResources().getString(R.string.desret),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								cancelRecord();
							}
						});
		alert = builder.create();
	}

	public RecordSoundFragement() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		mRootLayout = (ViewGroup) inflater.inflate(R.layout.layout_main,
				container, false);
		startBtnLayout = (ViewGroup) inflater.inflate(R.layout.startbtn_layout,
				mRootLayout, false);
		functionBtnLayout = (ViewGroup) inflater.inflate(
				R.layout.functionbtn_layout, mRootLayout, false);
		if (recordingState == 1) {
			mRootLayout.addView(functionBtnLayout);
		} else {
			mRootLayout.addView(startBtnLayout);
		}
		return mRootLayout;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		startBtn = (ImageView) startBtnLayout
				.findViewById(R.id.start_record_btn);
		soundListBtn = (ImageView) startBtnLayout
				.findViewById(R.id.sound_list_btn);
		stopBtn = (ImageView) functionBtnLayout
				.findViewById(R.id.stop_record_btn);
		pauseBtn = (ImageView) functionBtnLayout
				.findViewById(R.id.pause_record_btn);
		cancelBtn = (ImageView) functionBtnLayout
				.findViewById(R.id.cancel_record_btn);
		timeText = (TextView) mRootLayout.findViewById(R.id.time_text);
		fileNameText = (TextView) mRootLayout.findViewById(R.id.file_name);
		sfv = (SurfaceView) mRootLayout.findViewById(R.id.music_record);
		startBtn.setOnClickListener(this);
		soundListBtn.setOnClickListener(this);
		stopBtn.setOnClickListener(this);
		pauseBtn.setOnClickListener(this);
		cancelBtn.setOnClickListener(this);
		if (RecordService.isRecording){
		    String recordTime = Settings.System.getString(getActivity().getContentResolver(),"recordTime");
            timeText.setText(recordTime);
        }
		if (RecordService.is4GACDHN(mContext) || RecordService.isP5XCDHN(mContext)){
			soundListBtn.setVisibility(View.GONE);
		}
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.start_record_btn:
			Values.isButtonKey = true;
			Values.isSubsection = true;
            notifyRrecord(Values.LOGCAT_MIC, Values.ZZX_STATE, true);
			recordStart();
			break;
		case R.id.sound_list_btn:
			browseFile();
			break;
		case R.id.pause_record_btn:
			recordPause();
			break;
		case R.id.stop_record_btn:
			Values.isButtonKey = false;
			Values.isSubsection = false;
            notifyRrecord(Values.LOGCAT_MIC, Values.ZZX_STATE, false);
			recordStop();
			break;
		case R.id.cancel_record_btn:
			recordCancel();
			break;
		default:
			break;
		}

	}

	@Override
	public void onStart() {
		super.onStart();
	}

	private void recordCancel() {
		showQuitDialog();
	}

	private void recordPause() {
		Intent intent = new Intent();
		intent.setAction("pauseRecord");
		mContext.sendBroadcast(intent);
	}

	private void recordStop() {
//		Intent intent = new Intent();
//		intent.setAction("stopRecord");
//		mContext.sendBroadcast(intent);
		Intent intent = new Intent();
		intent.setClass(mContext, RecordService.class);
		mContext.startService(intent);
//		stopRecordService();
	}
	
	

	private void recordStart() {
		Intent intent = new Intent();
		intent.setClass(mContext, RecordService.class);
		mContext.startService(intent);
	}

	private void cancelRecord() {
		Intent intent = new Intent();
		intent.setAction("cancelRecord");
		mContext.sendBroadcast(intent);
	}

	private void browseFile() {//TODO:换包名
		Intent intent = new Intent();
//		intent.setClassName("com.zzx.filemanager",
//				"com.zzx.filemanager.MainActivity");
		intent.setClassName("com.byandy.files",
				"com.byandy.files.MainActivity");
		intent.putExtra("index", 2);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mContext.startActivity(intent);
	}

	private void showQuitDialog() {
		alert.show();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		mContext.unregisterReceiver(updateUIBroadCastReceiver);
		super.onDestroy();
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	private class UpdateUIBroadCastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("updateFileName")) {

			} else if (action.equals("updateFunctionButton")) {
				String fileName = intent.getStringExtra("fileName");
				if (fileName!=null){
					Log.e("onReceive: ",fileName );
					fileNameText.setText(fileName);
					Log.e("RecordService",""+intent.getIntExtra("recordingState",0));
					int recordState = intent.getIntExtra("recordingState", 0);
					if (recordState != recordingState) {
						updateFunctionBtn();
						recordingState = recordState;
					}
				}
			} else if (action.equals("updateRecordingTime")) {
				String recordTime = intent.getStringExtra("recordTime");
				timeText.setText(recordTime);
			} else if (action.equals("updateAudioWarv")) {
				@SuppressWarnings("unchecked")
                ArrayList<Short> inBuf = (ArrayList<Short>) intent
						.getSerializableExtra("warData");
				SimpleDraw(inBuf);
			}
		}

	}

	private void SimpleDraw(ArrayList<Short> buf) {
		if (sfv == null) {
			return;
		}
		if (sfv.getHeight() == 0) {
			return;
		}
		if (rateY == 0) {
			rateY = 2000 / sfv.getHeight();//50000
			baseLine = sfv.getHeight() / 2;
		}
		Canvas canvas = sfv.getHolder().lockCanvas(
				new Rect(0, 0, sfv.getWidth(), sfv.getHeight()));// 关键:获取画布
		if (canvas == null) {
			return;
		}
//		canvas.drawColor(Color.BLACK);// 清除背景
		canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
		int start = sfv.getWidth() - buf.size() * divider;
		int py = baseLine;
		if (buf.size() > 0)
			py += buf.get(0) / rateY;
		int y;
		//canvas.drawLine(0, baseLine, start - divider, baseLine, mPaint);
		for (int i = 0; i < buf.size(); i++) {
			y = buf.get(i) / rateY + baseLine;// 调节缩小比例，调节基准线
			canvas.drawLine(start + (i - 1) * divider, py, start + i * divider,
					y, mPaint);
			py = y;
		}
		sfv.getHolder().unlockCanvasAndPost(canvas);// 解锁画布，提交画好的图像
	}

	public void updateFunctionBtn() {
		if (recordingState == 0) {
			mRootLayout.removeView(startBtnLayout);
			mRootLayout.addView(functionBtnLayout);
			fileNameText.setVisibility(View.VISIBLE);
		} else {
			mRootLayout.removeView(functionBtnLayout);
			mRootLayout.addView(startBtnLayout);
			fileNameText.setVisibility(View.GONE);
		}
	}
    private void notifyRrecord(String logcatRecord, String zzxState, boolean value) {
        Intent recordintent = new Intent();
        recordintent.setAction(logcatRecord);
        recordintent.putExtra(zzxState, value);
        mContext.sendBroadcast(recordintent);
    }

}
