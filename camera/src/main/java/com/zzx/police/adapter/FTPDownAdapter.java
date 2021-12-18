package com.zzx.police.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zzx.police.R;
import com.zzx.police.data.FTPDownLoadInfo;
import com.zzx.police.utils.FTPDownUtil;

import java.util.LinkedHashMap;

/**
 * Created by wanderFly on 2017/11/29.
 * 显示FTP文件下载的适配器
 */

public class FTPDownAdapter extends BaseAdapter {
    private static final boolean DEBUG = false;
    private static final String TAG = "FTPDownAdapter";
    private LinkedHashMap<String, FTPDownLoadInfo> mMap;
    private Context mContext;

    public FTPDownAdapter(Context context, LinkedHashMap<String, FTPDownLoadInfo> map) {
        this.mContext = context;
        this.mMap = map == null ? new LinkedHashMap<String, FTPDownLoadInfo>() : map;

    }

    @Override
    public int getCount() {
        d("mMap.size():" + mMap.size());
        return mMap.size();
    }


    @Override
    public Object getItem(int position) {
        Object[] keys = mMap.keySet().toArray();
        String key = (String) keys[position];
        return mMap.get(key);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        d("getView: mPosition:" + position);
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.ftp_down_list_view_item, null);
            holder = new ViewHolder();
            holder.fileName = (TextView) convertView.findViewById(R.id.tv_file_name);
            holder.tvDownStatus = (TextView) convertView.findViewById(R.id.tv_down_status);
            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        FTPDownLoadInfo info = (FTPDownLoadInfo) getItem(position);
        if (info != null) {
            holder.tvDownStatus.setBackgroundResource(R.drawable.ftp_down_tv_status_transparent);
            holder.fileName.setText(info.getFileName());
            holder.tvDownStatus.setText(getStatusContent(info, holder.tvDownStatus));
            holder.progressBar.setProgress((int) (info.getProgress() * 10000));
        }
        return convertView;
    }

    private void d(String msg) {
        if (DEBUG) Log.d(TAG, "" + msg);
    }

    /**
     * 获取适配器中的集合对象
     * (最好不要去做同步操作，适配器中对该对象的调用频率挺高)
     *
     * @return 集合对象
     */
    public LinkedHashMap<String, FTPDownLoadInfo> getLinkedHashMap() {
        return mMap;
    }

    /**
     * 添加或者更新数据
     * <p>
     * 在{@link LinkedHashMap#put(Object, Object)}
     * 中会对已经存在的数据会替换，不存在的数据会创建添加到该容器中
     * </p>
     *
     * @param info             需要添加或者更新的数据
     * @param isNeedUpdateView 是否需要更新View(当View已经被{@link android.view.WindowManager 移除时就没有必要更新View})
     */
    public void addOrUpdateData(FTPDownLoadInfo info, boolean isNeedUpdateView) {
        mMap.put(info.getFileName(), info);
        if (isNeedUpdateView) notifyDataSetChanged();
    }

    /**
     * 添加或者更新数据的集合(Key值相同的会被合并掉)
     *
     * @param arrayMap         需要添加或者更新的数据集合
     * @param isNeedUpdateView 是否需要更新View
     */

    public void addOrUpdateData(LinkedHashMap<String, FTPDownLoadInfo> arrayMap, boolean isNeedUpdateView) {
        mMap.putAll(arrayMap);
        if (isNeedUpdateView) notifyDataSetChanged();
    }


    /**
     * 移除列表中的数据
     *
     * @param info             需要移除的文件下发信息
     * @param isNeedUpdateView 是否需要更新View
     * @return true:移除成功  false:移除失败(移除的文件在集合中不存在)
     */
    public boolean removeData(FTPDownLoadInfo info, boolean isNeedUpdateView) {
        boolean status = mMap.remove(info.getFileName()) != null;
        if (isNeedUpdateView) notifyDataSetChanged();
        return status;
    }


    /**
     * 根据进度值获取对应的显示字符串
     *
     * @param info 文件下载对象
     */
    private String getStatusContent(FTPDownLoadInfo info, TextView tvDownStatus) {
        String content;
        switch (info.getStatus()) {
            case FTPDownUtil.DOWN_STATUS_DEFAULT:
                content = "等待下载";
                break;
            case FTPDownUtil.DOWN_STATUS_READY:
                content = "准备下载";
                break;
            case FTPDownUtil.DOWN_STATUS_START:
                content = "开始下载";
                break;
            case FTPDownUtil.DOWN_STATUS_TRANSFERRED:
                if (info.getProgress() == 0) {
                    content = 0 + "%";
                } else if (info.getProgress() == 1) {
                    content = 100 + "%";
                } else {
                    int tempValue = Math.round(info.getProgress() * 10000);
                    if (tempValue % 10 == 0) {
                        content = (float) tempValue / 100 + "0%";
                    } else {
                        content = (float) tempValue / 100 + "%";
                    }
                }
                tvDownStatus.setBackgroundResource(R.drawable.ftp_down_tv_status_transparent);
                /**正在传输时直接返回*/
                return content;
            case FTPDownUtil.DOWN_STATUS_COMPLETED:
                content = "查看";//下载完成
                break;
            case FTPDownUtil.DOWN_STATUS_ABORTED:
                content = "下载中止";
                break;
            case FTPDownUtil.DOWN_STATUS_FAILED:
                content = "下载失败";
                break;
            case FTPDownUtil.DOWN_STATUS_NETWORK_IS_UNAVAILABLE:
                content = "无网络";
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
                //content = "下载出错";
                content = "重新下载";
                break;
            default:
                return "";
        }
        setTextOnClick(info, tvDownStatus);
        return content;
    }

    private void setTextOnClick(final FTPDownLoadInfo info, TextView tvDownStatus) {
        tvDownStatus.setBackgroundResource(R.drawable.ftp_down_tv_status);
        tvDownStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null)
                    mListener.onFtpAdapterDownStatusClick(info);
            }
        });
    }

    private OnFtpDownStatusClick mListener;

    public void setOnFtpDownStatusClick(OnFtpDownStatusClick listener) {
        this.mListener = listener;
    }

    public interface OnFtpDownStatusClick {
        void onFtpAdapterDownStatusClick(FTPDownLoadInfo info);
    }


    private class ViewHolder {
        TextView tvDownStatus;
        TextView fileName;
        ProgressBar progressBar;
    }
}
