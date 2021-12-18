package com.zzx.police.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.zzx.police.R;

/**@author Tomy
 * Created by Tomy on 2014/6/13.
 */
public class PopupListAdapter extends BaseAdapter {
    private TypedArray mIconList = null;
    private String[] mInfoList  = null;
    private LayoutInflater mInflater = null;
    private Resources mRes = null;

    public PopupListAdapter(Context context) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRes = context.getResources();
        mInfoList = new String[0];
    }

    public void setList(int iconListId, int infoListId) {
        mInfoList = null;
        mInfoList = mRes.getStringArray(infoListId);
        if (mIconList != null) {
            mIconList.recycle();
            mIconList = null;
        }
        mIconList = mRes.obtainTypedArray(iconListId);
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        public RadioButton mRadioBtn = null;
        public ImageView mIcon = null;
        public TextView mInfo = null;
    }

    @Override
    public int getCount() {
        return mInfoList.length;
    }

    @Override
    public Object getItem(int position) {
        return mInfoList[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.layout_resolution_popup_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.mRadioBtn = (RadioButton) convertView.findViewById(R.id.radio_button_popup_window_item);
            viewHolder.mIcon = (ImageView) convertView.findViewById(R.id.iv_popup_window_item);
            viewHolder.mInfo = (TextView) convertView.findViewById(R.id.tv_popup_window_item);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.mIcon.setBackground(mIconList.getDrawable(position));
        viewHolder.mInfo.setText(mInfoList[position]);
        viewHolder.mRadioBtn.setButtonDrawable(R.drawable.radio_button);
        return convertView;
    }
}
