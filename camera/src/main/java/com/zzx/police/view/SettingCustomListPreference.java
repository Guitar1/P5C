package com.zzx.police.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.zzx.police.R;
import com.zzx.police.adapter.CaptureRecordPopListAdapter;
import com.zzx.police.data.Values;

/**@author Tomy
 * Created by Tomy on 2014/7/15.
 */
public class SettingCustomListPreference extends ListPreference {
    private static final String TAG = "SettingCustomListPreference: ";
    private Context mContext = null;
    private LayoutInflater mInflater = null;
    private Dialog mDialog;
    private CaptureRecordPopListAdapter mListAdapter;
    private int mClickedDialogEntryIndex = 0;
    private CharSequence[] mEntryValues = null;
    private CharSequence[] mEntries = null;
    private String mKey;
    private String mDefaultValue;
    private String mDefaultSummary;

    public SettingCustomListPreference(Context context) {
        this(context, null);
    }

    public SettingCustomListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CustomDialogPreference);
        mDefaultValue = array.getString(R.styleable.CustomDialogPreference_defaultValue);
        array.recycle();
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mListAdapter = new CaptureRecordPopListAdapter(mContext, getEntries(), 0, 0, false, false);
        mEntryValues = getEntryValues();
        mEntries    = getEntries();
        mKey = getKey();
        mDefaultSummary = String.valueOf(getSummary());
        restoreSummary();
    }

    private void restoreSummary() {
        String preValue = PreferenceManager.getDefaultSharedPreferences(mContext).getString(mKey, mDefaultValue);
        Values.LOG_I(TAG, "preValue = " + preValue);
        int index = findIndexOfValue(preValue);
        setSummary(mEntries[index]);
    }

    private void restoreAdapter() {
        String preValue = PreferenceManager.getDefaultSharedPreferences(mContext).getString(mKey, mDefaultValue);
        Values.LOG_I(TAG, "preValue = " + preValue);
        int index = findIndexOfValue(preValue);
        mListAdapter.setCheck(index, true);
    }

    public String getDefaultSummary() {
        return mDefaultSummary;
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        return mInflater.inflate(R.layout.custom_setting_item, parent, false);
    }

    @Override
    protected View onCreateDialogView() {
        Values.LOG_I(TAG, "onCreateDialogView()");
        View rootView = mInflater.inflate(R.layout.layout_setting_camera_dialog, null);
//        rootView.setBackgroundResource(android.R.color.transparent);
        ListView listView = (ListView) rootView.findViewById(R.id.list);
        ((TextView) rootView.findViewById(R.id.dialog_title)).setText(getTitle());
        rootView.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingCustomListPreference.this.onClick(mDialog, DialogInterface.BUTTON_NEGATIVE);
                mDialog.dismiss();
            }
        });
        listView.setAdapter(mListAdapter);
        listView.setDivider(mContext.getResources().getDrawable(R.drawable.divider));
        listView.setDividerHeight(1);
        restoreAdapter();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mClickedDialogEntryIndex = position;
                Values.LOG_I(TAG, "which = " + position);
                SettingCustomListPreference.this.onClick(mDialog, DialogInterface.BUTTON_POSITIVE);
                mListAdapter.setCheck(mClickedDialogEntryIndex, true);
                setSummary(mEntries[position]);
                mDialog.dismiss();
            }
        });
        listView.setSelector(R.drawable.bg_setting_item);
        return rootView;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult && mClickedDialogEntryIndex >= 0 && mEntryValues != null) {
            String value = mEntryValues[mClickedDialogEntryIndex].toString();
            if (callChangeListener(value)) {
                setValue(value);
            }
        }
    }

    @Override
    protected void showDialog(Bundle state) {
        Values.LOG_I(TAG, "showDialog()");
        Context context = mContext;

        mDialog = new Dialog(context, R.style.SettingCustomListPreferenceDialog);
        View contentView = onCreateDialogView();
        if (contentView != null) {
            mDialog.setContentView(contentView);
        }
        // Create the mDialog
        if (state != null) {

            mDialog.onRestoreInstanceState(state);
        }
        Window dialogWindow = mDialog.getWindow();
        dialogWindow.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width    = 400;
        lp.height   = WindowManager.LayoutParams.WRAP_CONTENT;
        dialogWindow.setAttributes(lp);
        mDialog.setOnDismissListener(this);
        mDialog.show();
    }

    @Override
    protected void onBindView(View view) {
        view.setBackground(null);
        final TextView titleView = (TextView) view.findViewById(
                android.R.id.title);
        if (titleView != null) {
            final CharSequence title = getTitle();
            if (!TextUtils.isEmpty(title)) {
                titleView.setText(title);
                titleView.setVisibility(View.VISIBLE);
            } else {
                titleView.setVisibility(View.GONE);
            }
        }

        final TextView summaryView = (TextView) view.findViewById(
                android.R.id.summary);
        if (summaryView != null) {
            final CharSequence summary = getSummary();
            if (!TextUtils.isEmpty(summary)) {
                summaryView.setText(summary);
                summaryView.setVisibility(View.VISIBLE);
            } else {
                summaryView.setVisibility(View.GONE);
            }
        }
    }

    public void resetSummary() {
        setSummary(mDefaultSummary);
    }
}
