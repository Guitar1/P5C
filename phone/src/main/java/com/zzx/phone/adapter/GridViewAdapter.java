package com.zzx.phone.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zzx.phone.R;

import java.util.ArrayList;
import java.util.List;

/**@author Tomy
 * Created by Tomy on 2014/6/25.
 */
public class GridViewAdapter extends BaseAdapter {
    List<String> mList  = null;
    private Activity mContext = null;
    class ViewHolder {
        View view = null;
        TextView tvTag = null;
    }
    public GridViewAdapter(Context context) {
        this.mList = new ArrayList<String>(4);
        mContext = (Activity) context;
    }

    public void addItem(String tag) {
        mList.add(tag);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public String getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = mContext.getLayoutInflater().inflate(R.layout.table_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.view = convertView.findViewById(R.id.view);
            viewHolder.tvTag = (TextView) convertView.findViewById(R.id.tv_tag);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (position == 0) {
            viewHolder.view.setVisibility(View.GONE);
        }
        viewHolder.tvTag.setText(getItem(position));
        return convertView;
    }
}
