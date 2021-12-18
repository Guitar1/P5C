package com.zzx.police.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.zzx.police.R;
import com.zzx.police.adapter.CaptureRecordPopListAdapter;
import com.zzx.police.data.Values;
import com.zzx.police.utils.RemotePreferences;

/**@author Tomy
 * Created by Tomy on 2014/6/15.
 */
public class CustomDialogPreference extends DialogPreference {
    private static final String TAG = "CustomDialogPreference: ";
    private static final String KEY_CAMERA_SERVICE_RECORD_SOUND = "record_sound";
    private static final String KEY_CAMERA_SERVICE_RECORD_BOOT = "record_boot";
    private static final String KEY_CAMERA_SERVICE_RECORD_LOOP = "record_loop";
    private String mKey = null;
    private CaptureRecordPopListAdapter mListAdapter = null;
    private CharSequence[] mEntries = null;
    private CharSequence[] mEntryValues = null;
    private int mClickedDialogEntryIndex = 0;
    private LayoutInflater mInflater = null;
    private String mValue = null;
    private Dialog mDialog = null;
    private Context mContext = null;
    private SharedPreferences mSp = null;
    private String mDefaultValue;
    private Resources mRes;
    private BroadcastReceiver mReceiver;
    private int mYOffset = 0;

    public CustomDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.layout_camera_service_dialog);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CustomDialogPreference);
        mEntries = array.getTextArray(R.styleable.CustomDialogPreference_entries);
        mEntryValues = array.getTextArray(R.styleable.CustomDialogPreference_entryValues);
        mValue = array.getString(R.styleable.CustomDialogPreference_defaultValue);
        mDefaultValue = array.getString(R.styleable.CustomDialogPreference_defaultValue);
        array.recycle();
        mKey = getKey();
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mListAdapter = new CaptureRecordPopListAdapter(context, mEntries, 0, 0, false, false);
        mContext = context;
        mRes = mContext.getResources();
//        mSp = PreferenceManager.getDefaultSharedPreferences(context);
        mSp = RemotePreferences.getRemotePreferences(context);
        Values.LOG_I("mKey = ", mKey + "; mSp = " + mSp);
        restoreSummary();
        if (mKey.equals(getPreferenceKey(R.string.key_record_pre)) || mKey.equals(getPreferenceKey(R.string.key_record_delay)) || mKey.equals(Values.PREFERENCE_KEY_RECORD_LOOP)) {
            mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Values.LOG_I(TAG, "mKey = " + mKey + ": action = " + intent.getAction());
                    restoreSummary();
                }
            };
            mContext.registerReceiver(mReceiver, new IntentFilter(Values.ACTION_RECORD_PREFERENCE_NOTIFY));
        }
    }

    @Override
    public void onActivityDestroy() {
        if (mReceiver != null) {
            mContext.unregisterReceiver(mReceiver);
        }
        super.onActivityDestroy();
    }

    private void restoreSummary() {
        int index = getIndex();
        setSummary(mEntries[index]);
    }

    private int getIndex() {
        String preValue;

        SharedPreferences sp = RemotePreferences.getRemotePreferences(mContext);
        if (mKey.equals(KEY_CAMERA_SERVICE_RECORD_LOOP)) {
            if (sp.getBoolean(getPreferenceKey(R.string.key_record_loop), true)) {
                preValue = mRes.getString(R.string.open_value);
            } else {
                preValue = mRes.getString(R.string.close_value);
            }
        } else if (mKey.equals(KEY_CAMERA_SERVICE_RECORD_SOUND)) {
            if (sp.getBoolean(getPreferenceKey(R.string.key_record_sound), true)) {
                preValue = mRes.getString(R.string.open_value);
            } else {
                preValue = mRes.getString(R.string.close_value);
            }
        }  else if (mKey.equals(KEY_CAMERA_SERVICE_RECORD_BOOT)) {
            if (sp.getBoolean(getPreferenceKey(R.string.key_record_boot), false)) {
                preValue = mRes.getString(R.string.open_value);
            } else {
                preValue = mRes.getString(R.string.close_value);
            }
        }

        else {
            preValue = sp.getString(mKey, mDefaultValue);
        }
        Values.LOG_I(TAG, "mKey = " + mKey + "; restoreSummary.preValue = " + preValue);
        return findIndexOfValue(preValue);
    }

    private void refreshAdapter() {
        int index = getIndex();
        mListAdapter.setCheck(index, true);
    }

    private int findIndexOfValue(String value) {
        Values.LOG_I(TAG, "value = " + value + "; mEntryValues.length = " + mEntryValues.length);
        if (value != null && mEntryValues != null) {
            for (int i = mEntryValues.length - 1; i >= 0; i--) {
                if (mEntryValues[i].equals(value)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private String getValue() {
        return mValue;
    }

    private int getValueIndex() {
        return findIndexOfValue(mValue);
    }

    /**
     * Sets the value to the given index from the entry values.
     *
     * @param index The index of the value to set.
     */
    public void setValueIndex(int index) {
        if (mEntryValues != null) {
            setValue(mEntryValues[index].toString());
        }
    }

    public void setValue(String value) {
        // Always persist/notify the first time.
        mValue = value;
        persistString(value);
        SharedPreferences.Editor editor = mSp.edit();
        editor.putString(mKey, value).apply();
        checkKeyForSpecialKey();
        notifyChanged();
    }

    @Override
    protected boolean persistString(String value) {
        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putString(mKey, value).apply();
        return true;
    }

    private String getPreferenceKey(int id) {
        return mContext.getResources().getString(id);
    }

    /**此方法用来判断当前的Preference是否是特殊的几个.
     * 录像声音和循环录像两者在CameraService中是 {@link #DialogPreference},保存的值是1或者0.
     * 而在Setting里是CheckBoxPreference,保存的值是true或者false.
     * 因此两者要相互关联.
     * */
    private void checkKeyForSpecialKey() {
        if (mKey.equals(KEY_CAMERA_SERVICE_RECORD_SOUND)) {
            //录像声音的Key
            if (mValue.equals("1")) {
                mSp.edit().putBoolean(getPreferenceKey(R.string.key_record_sound), true).apply();
            } else {
                mSp.edit().putBoolean(getPreferenceKey(R.string.key_record_sound), false).apply();
            }
        }
        else if (mKey.equals(KEY_CAMERA_SERVICE_RECORD_BOOT)) {
            //开机录像的值
            if (mValue.equals("1")) {
                mSp.edit().putBoolean(getPreferenceKey(R.string.key_record_boot), true).apply();
            } else {
                mSp.edit().putBoolean(getPreferenceKey(R.string.key_record_boot), false).apply();
            }
        }
        else if (mKey.equals(KEY_CAMERA_SERVICE_RECORD_LOOP)) {
            //循环录像的Key
            if (mValue.equals("1")) {
                mSp.edit().putBoolean(getPreferenceKey(R.string.key_record_loop), true).putString(getPreferenceKey(R.string.key_record_delay), "0")
                        .putString(getPreferenceKey(R.string.key_record_pre), "0").apply();
                sendBroadcast();
            } else {
                mSp.edit().putBoolean(getPreferenceKey(R.string.key_record_loop), false).apply();
            }
        } else if (mKey.equals(getPreferenceKey(R.string.key_record_pre))) {
            if (!mValue.equals("0")) {
//                mSp.edit().putString(getPreferenceKey(R.string.key_record_delay), "0").putBoolean(getPreferenceKey(R.string.key_record_loop), false).apply();
                mSp.edit().putBoolean(getPreferenceKey(R.string.key_record_loop), false).apply();
                sendBroadcast();
            }
        } else if (mKey.equals(getPreferenceKey(R.string.key_record_delay))) {
            if (!mValue.equals("0")) {
                /*mSp.edit().putString(getPreferenceKey(R.string.key_record_pre), "0")
                    .putBoolean(getPreferenceKey(R.string.key_record_loop), false).apply()*/;
                mSp.edit().putBoolean(getPreferenceKey(R.string.key_record_loop), false).apply();
                sendBroadcast();
            }
        }
    }

    private void sendBroadcast() {
        Intent intent = new Intent(Values.ACTION_RECORD_PREFERENCE_NOTIFY);
        mContext.sendBroadcast(intent);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
       /* if (mEntries == null || mEntryValues == null) {
            throw new IllegalStateException(
                    "ListPreference requires an entries array and an entryValues array.");
        }

        mClickedDialogEntryIndex = getValueIndex();
        builder.setNegativeButton(null, null);
        builder.setCancelable(true);
        builder.setInverseBackgroundForced(true);
        builder.setAdapter(mListAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mClickedDialogEntryIndex = which;
                Log.i(TAG, "which = " + which);
                CustomDialogPreference.this.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                ((RecordResolutionPopListAdapter) mListAdapter).setCheck(mClickedDialogEntryIndex, true);
                dialog.dismiss();
            }
        });*/
    }

    @Override
    protected View onCreateDialogView() {
        View rootView = mInflater.inflate(R.layout.layout_camera_service_dialog, null);
        if (mKey.equals(Values.PREFERENCE_KEY_RECORD_SOUND) || mKey.equals(Values.PREFERENCE_KEY_RECORD_LOOP) || mKey.equals(Values.PREFERENCE_KEY_RECORD_BOOT)) {
            rootView.setBackgroundResource(R.drawable.bg_down);
            mYOffset = -120;
//            mYOffset = 0;
        } else if (mKey.equals(getPreferenceKey(R.string.key_record_delay))) {
            rootView.setBackgroundResource(R.drawable.bg_hor_middle);
            mYOffset = -15;
//            mYOffset = 40;
        } else if (mKey.equals(getPreferenceKey(R.string.key_record_section))) {
            rootView.setBackgroundResource(R.drawable.bg_down);
            mYOffset = -60;
//            mYOffset = 30;
        } else if (mKey.equals(getPreferenceKey(R.string.key_record_pre))) {
            rootView.setBackgroundResource(R.drawable.bg_hor_middle);
            mYOffset = -60;
//            mYOffset = 10;
        }
        ListView listView = (ListView) rootView.findViewById(R.id.list_view);
        listView.setAdapter(mListAdapter);
        listView.setSelector(R.drawable.bg_setting_item);
        refreshAdapter();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mClickedDialogEntryIndex = position;
//                CustomDialogPreference.this.onClick(mDialog, DialogInterface.BUTTON_POSITIVE);
                mListAdapter.setCheck(mClickedDialogEntryIndex, true);
                setSummary(mEntries[position]);
                if (mClickedDialogEntryIndex >= 0 && mEntryValues != null) {
                    String value = mEntryValues[mClickedDialogEntryIndex].toString();
                    if (callChangeListener(value)) {
                        setValue(value);
                    }
                }
                mDialog.dismiss();
            }
        });
        return rootView;
    }

    @Override
    protected boolean persistInt(int value) {
        if (shouldPersist()) {
            SharedPreferences.Editor editor = mSp.edit();
            editor.putInt(mKey, value);
            editor.apply();
            return true;
        }
        return false;
    }

    @Override
    protected void showDialog(Bundle state) {
        Context context = mContext;

        mDialog = new Dialog(context, R.style.CustomDialogTheme);
        View contentView = onCreateDialogView();
        if (contentView != null) {
            mDialog.setContentView(contentView);
        }
        // Create the mDialog
        if (state != null) {
            mDialog.onRestoreInstanceState(state);
        }
        Window dialogWindow = mDialog.getWindow();
        /**设置成此Type是因为WindowManager设置成了{@link android.view.WindowManager.LayoutParams#TYPE_SYSTEM_ERROR},这里必须得比它高才能显示在它上面.
         * */
        dialogWindow.setType(WindowManager.LayoutParams.TYPE_INPUT_METHOD_DIALOG);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width    = 100;
        lp.height   = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.x = 80;
        lp.y = mYOffset;
        dialogWindow.setAttributes(lp);
        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

            }
        });
        try {
            mDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
