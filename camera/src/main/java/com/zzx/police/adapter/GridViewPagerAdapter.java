package com.zzx.police.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.provider.Settings;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zzx.police.MainActivity;
import com.zzx.police.R;
import com.zzx.police.ZZXConfig;
import com.zzx.police.activity.ApkActivity;
import com.zzx.police.activity.PasswordBaseActivity;
import com.zzx.police.data.Values;
import com.zzx.police.manager.UsbMassManager;
import com.zzx.police.services.CameraService;
import com.zzx.police.utils.ConfigXMLParser;
import com.zzx.police.utils.DeviceUtils;
import com.zzx.police.utils.EventBusUtils;
import com.zzx.police.utils.TTSToast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.zzx.police.data.Values.IsOpenDialer;

/**
 * @author Tomy
 *         Created by Tomy on 2014/7/2.
 */
public class GridViewPagerAdapter extends PagerAdapter implements AdapterView.OnItemClickListener {
    private final String TAG = "GridVIewPagerAdapter: ";
    private LayoutInflater mInflater = null;
    private List<String[]> mInfoList = null;
    private List<TypedArray> mTypeList = null;
    private Resources mRes = null;
    private Activity mContext = null;
    private Intent mIntent = null;

    /**
     * 选择启用对讲机应用索引
     **/
    public static final String TALK_BACK_NEW = "ZZXTalkBack";
    /**
     * 原生 -- 集群通PTT
     **/
    public static final int TALK_BACK_ORI_JQT = 0;
    /**
     * 星柯蓝 -- 易对讲
     **/
    public static final int TALK_BACK_XKL_YI = 1;
    /**
     * 浙江力石 -- 沃对讲
     **/
    public static final int TALK_BACK_LS_WO = 2;
    /**
     * 酷对讲.
     **/
    public static final int TALK_BACK_CLOUD_KU = 3;
    /**
     * 北京赛普乐
     **/
    public static final int TALK_BACK_SPL = 4;
    /**
     * 银川
     **/
    public static final int TALK_BACK_YC = 5;
    /**
     * 保定警务对讲
     **/
    public static final int TALK_BACK_BD_JWDJ = 6;
    /**
     * 融合指挥调度平台对讲
     */
    public static final int TALK_BACK_RONG_HE_ZHI_HUI = 13;
    /**
     * h9
     */
    public static final int TALK_BACK_H9 = 7;

    private static final int MOBILE_DATA_ENABLED = -1;
    private static final int MOBILE_DATA_ENABLED_NO_PHONE = 0;
    private static final int MOBILE_DATA_DISABLE = -1;
    private List<String> mItemList;
    private TypedArray mIconList;
    private List<String> mListInfo;
    private List<Integer> mListIcon;
    //每页显示几个图标
    private final int COUNT_IN_PAGE = 6;


    private int checkMobileData(Context context) {
        return Settings.System.getInt(context.getContentResolver(), MainActivity.MOBILE_DATA, MOBILE_DATA_ENABLED);
    }

    private int getTalkBackAppIndex(Context context) {
        return Settings.System.getInt(context.getContentResolver(), TALK_BACK_NEW, -1);
    }

    public GridViewPagerAdapter(Activity context) {
        mContext = context;
        mIntent = new Intent();
        mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mRes = context.getResources();
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInfoList = new ArrayList<>();
        mTypeList = new ArrayList<>();
        Resources res = context.getResources();
        mListInfo = new ArrayList<>();
        mListIcon = new ArrayList<>();
        String[] nameList = res.getStringArray(R.array.launcher_p5c);
        boolean isAppEnabled = Settings.System.getInt(context.getContentResolver(),ZZXConfig.ZZXAPPEnabled,1)==1;
        Log.i(TAG, "isAppEnabled: "+isAppEnabled);
        if (ZZXConfig.isP5C_SPXZ){
            nameList = res.getStringArray(R.array.launcher_p5c_spxz);
            addP5CSPXZ();
        }else if (!isAppEnabled){
            nameList = res.getStringArray(R.array.launcher_p5c_eng);
            addP5CEng();
        } else {
            addP5C();
        }
        Collections.addAll(mListInfo, nameList);
        //电话
        addItemforConfiguration(R.drawable.call, R.string.call, ZZXConfig.ZZXPoneEnabled, mContext);
        //微信
        addItemforConfiguration(R.drawable.wechat, R.string.wechat, ZZXConfig.ZZXWXEnabled, mContext);
        //对讲
        addItemforConfiguration(R.drawable.talking, R.string.talk_back, ZZXConfig.ZZXTalkEnabled, mContext);
        //山东通亚vos
        addItemforConfiguration(R.drawable.main_icon_police,R.string.vos,ZZXConfig.ZZXVOS,mContext);
        //地图
        addItemforConfiguration(R.drawable.map, R.string.map, ZZXConfig.ZZXMapEnabled, mContext);
        //身份证
        addItemforConfiguration(R.drawable.id_card, R.string.id_card, ConfigXMLParser.CFG_ENABLED_ID_CARD, mContext);
        //铁务通
        addItemforConfiguration(R.drawable.btn_tiewutong, R.string.tiewutong, ZZXConfig.ZZXTieWuTongEnabled, mContext);
        //智能翻译
        addItemforConfiguration(R.drawable.talking,R.string.translation, ZZXConfig.ZZXTranslationEnabled, mContext);
        //汾阳执法（惟新科技）
        addItemforConfiguration(R.drawable.chengguanzhifa_nor, R.string.fenyang, ConfigXMLParser.CFG_ENABLED_FENYANG, mContext);
//        if (Settings.System.getInt(context.getContentResolver(), ConfigXMLParser.CFG_TALK_BACK_VISABLE, 1) == 1){
//            switch (getTalkBackAppIndex(mContext)){
//                case TALK_BACK_RONG_HE_ZHI_HUI:
//                    addItem(R.drawable.talking, R.string.talk_back);
//                    break;
//            }
//        }
    }
    /**
     * P5C UI 图片
     */
    private void addP5C(){
        mListIcon.add(R.drawable.video_record);
        mListIcon.add(R.drawable.sound_record);
        mListIcon.add(R.drawable.apk_manager);
        mListIcon.add(R.drawable.file_manager);
        mListIcon.add(R.drawable.setting);
    }
    /**
     * P5C_Eng UI 图片
     */
    private void addP5CEng(){
        mListIcon.add(R.drawable.video_record);
        mListIcon.add(R.drawable.sound_record);
        mListIcon.add(R.drawable.file_manager);
        mListIcon.add(R.drawable.setting);
    }

    /**
     *P5C_SPXZ
     */
    private void addP5CSPXZ(){
        mListIcon.add(R.drawable.video_assist);
        mListIcon.add(R.drawable.video_record);
        mListIcon.add(R.drawable.sound_record);
        mListIcon.add(R.drawable.apk_manager);
        mListIcon.add(R.drawable.file_manager);
        mListIcon.add(R.drawable.setting);
    }

    private void removeItem(int info) {
        try {
            String i = mContext.getString(info);
            mListInfo.remove(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void addItemforConfiguration(int icon, int info, String Configuration, Context context) {
        //if (true) {
        Log.i(TAG, "Configuration: "+Configuration+"="+Settings.System.getInt(context.getContentResolver(), Configuration, 0));
        if (Settings.System.getInt(context.getContentResolver(), Configuration, 0) == 1) {
            addItem(icon, info);
        }
    }
    private void addItem(int icon, int info) {
        try {
            mListIcon.add(icon);
            String i = mContext.getString(info);
            mListInfo.add(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getCount() {
        return mListInfo.size() / COUNT_IN_PAGE + (mListInfo.size() % COUNT_IN_PAGE > 0 ? 1 : 0);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View rootView = mInflater.inflate(R.layout.launcher_grid_layout, container, false);
        GridView gridView = (GridView) rootView.findViewById(R.id.grid_view);
        GridViewAdapter adapter = new GridViewAdapter(position);
        gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        gridView.setAdapter(adapter);
        gridView.setGravity(Gravity.CENTER_HORIZONTAL);
        gridView.setOnItemClickListener(this);
        //间距
        //gridView.setHorizontalSpacing(0);
        //gridView.setVerticalSpacing(5);
        container.addView(rootView);
        return rootView;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        GridViewAdapter adapter = (GridViewAdapter) parent.getAdapter();
        String info = adapter.getItem(position);

        try {
            if (info.equals(mRes.getString(R.string.setting))) {
                mIntent.setClassName(Values.PACKAGE_NAME_SETTING, Values.CLASS_NAME_SETTING);
            } else if (info.equals(mRes.getString(R.string.sound_record))) {
                if (ZZXConfig.isPullout){
                    Toast.makeText(mContext,R.string.power_saving_mode,Toast.LENGTH_SHORT).show();
                    return;
                }
                mIntent.setClassName(Values.PACKAGE_NAME_SOUND_RECORD, Values.CLASS_NAME_SOUND_RECORD);
            } else if (info.equals(mRes.getString(R.string.talk_back))) {
                if (ZZXConfig.is4GA_GTX){
                    mIntent.setClassName(Values.PACKAGE_NAME_TALK_BACK_XUN, Values.CLASS_NAME_TALK_BACK_XUN);
                }else {
                    mIntent.setClassName(Values.PACKAGE_NAME_TALK_BACK_RONG_HE, Values.CLASS_NAME_TALK_BACK_RONG_HE);
                }

            }
            else if (info.equals(mRes.getString(R.string.file_manager))) {
//            EventBusUtils.postEvent(Values.BUS_EVENT_MAIN_ACTIVITY, Values.REPLACE_CHECK_FRAGMENT);
//            return;
                //      mIntent.setClassName(Values.PACKAGE_NAME_FILE_MANAGER, Values.CLASS_NAME_FILE_MANAGER);
                if (ZZXConfig.isPullout){
                    Toast.makeText(mContext,R.string.power_saving_mode,Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    mIntent.setClassName(Values.PACKAGE_NAME_FILE_MANAGER_NEW, Values.CLASS_NAME_FILE_MANAGER_NEW);
                }
            }
            //应用软件
            else if (info.equals(mRes.getString(R.string.application))) {
                mIntent.setClass(mContext, ApkActivity.class);
                mContext.startActivity(mIntent);
                return;
            }
            //拨号
            else if (info.equals(mRes.getString(R.string.dial))) {
                String values;
                if (Settings.System.getString(mContext.getContentResolver(), IsOpenDialer) != null) {
                    values = Settings.System.getString(mContext.getContentResolver(), IsOpenDialer);
                } else {
                    values = "1";
                }
                if (values.equals("1")) {
                    mIntent.setClassName(Values.PACKAGE_NAME_CALL_NEW, Values.CLASS_NAME_CALL_NEW);
                    mContext.startActivity(mIntent);
                } else {
                    Toast.makeText(mContext, R.string.disable_fuction, Toast.LENGTH_SHORT).show();
                }
                return;
            }
            //h9录像
            else if (info.equals(mRes.getString(R.string.record))) {

                if (!Values.ACTION_UVC_CONNECT_STATE) {
                    mIntent.setClass(mContext, CameraService.class);
                    mIntent.setAction(Values.ACTION_MAX_WINDOW);
                    mContext.startService(mIntent);
                } else {
                    Toast.makeText(mContext, R.string.open_uvc, Toast.LENGTH_SHORT).show();
                }
                return;
            }
            //
            else if (info.equals(mRes.getString(R.string.map))) {
                mIntent.setClassName(Values.PACKAGE_NAME_MAP_BD, Values.CLASS_NAME_MAP_BD);
//                Locale locale = mContext.getResources().getConfiguration().locale;
//                if (locale.getLanguage().equalsIgnoreCase(Locale.CHINA.getLanguage())) {
//                    mIntent.setClassName(Values.PACKAGE_NAME_MAP_BD, Values.CLASS_NAME_MAP_BD);
//                } else {
//                    mIntent.setClassName(Values.PACKAGE_NAME_MAP_GOOGLE, Values.CLASS_NAME_MAP_GOOGLE);
//                }
            }
            //电话/拨号
            else if (info.equals(mRes.getString(R.string.call))) {
                mIntent.setClassName(Values.PACKAGE_NAME_CALL, Values.CLASS_NAME_CALL);
            } else if (info.equals(mRes.getString(R.string.info_reset))) {
                EventBusUtils.postEvent(Values.BUS_EVENT_SEND_SYSTEM_BROADCAST, Values.REPLACE_PASSWORD_FRAGMENT);
                return;
            } else if (info.equals(mRes.getString(R.string.camera_setting))) {
//            EventBusUtils.postEvent(Values.BUS_EVENT_SEND_SYSTEM_BROADCAST, Values.REPLACE_CAMERA_SETTING_FRAGMENT);
                return;
            } else if (info.equals(mRes.getString(R.string.wechat))) {
                mIntent.setClassName(Values.PACKAGE_NAME_WECHAT, Values.CLASS_NAME_WECHAT);
            } else if (info.equals(mRes.getString(R.string.service_control))) {
                mIntent.setClassName(Values.PACKAGE_NAME_X_PAI, Values.CLASS_NAME_X_PAI);
            } else if (info.equals(mRes.getString(R.string.rail_way))) {
                mIntent.setClassName(Values.PACKAGE_NAME_RAIL_WAY, Values.CLASS_NAME_RAIL_WAY);
            } else if (info.equals(mRes.getString(R.string.communication))) {
                mIntent.setClassName(Values.PACKAGE_NAME_TALK_BACK_YC_AQTX, Values.CLASS_NAME_TALK_BACK_YC_AQTX);
            } else if (info.equals(mRes.getString(R.string.calculator))) {
                mIntent.setClassName(Values.PACKAGE_NAME_CAL, Values.CLASS_NAME_CAL);
            } else if (info.equals(mRes.getString(R.string.police_event))) {
                mIntent.setClassName(Values.PACKAGE_NAME_TALK_BACK_YC_JLJQ, Values.CLASS_NAME_TALK_BACK_YC_JLJQ);
            } else if (info.equals(mRes.getString(R.string.search))) {
                mIntent.setClassName(Values.PACKAGE_NAME_TALK_BACK_YC_JQCX, Values.CLASS_NAME_TALK_BACK_YC_JQCX);
            } else if (info.equals(mRes.getString(R.string.clock))) {
                mIntent.setClassName(Values.PACKAGE_NAME_CLOCK, Values.CLASS_NAME_CLOCK);
            } else if (info.equals(mRes.getString(R.string.check))) {
                mIntent.setClassName(Values.PACKAGE_NAME_TALK_BACK_YC_YDHC, Values.CLASS_NAME_TALK_BACK_YC_YDHC);
            } else if (info.equals(mRes.getString(R.string.id_card))){
                mIntent.setClassName(Values.PACKAGE_NAME_ID_CARD, Values.CLASS_NAME_ID_CARD);
            }else if (info.equals(mRes.getString(R.string.video_assist))) {
                mIntent.setClassName(Values.PACKAGE_NAME_VIDEO_ASSIST, Values.CLASS_NAME_VIDEO_ASSIST);
            }else if (info.equals(mRes.getString(R.string.tiewutong))){
                mIntent.setClassName(Values.PACKAGE_NAME_UNION_BROAD, Values.CLASS_NAME_UNION_BROAD);
            }else if (info.equals(mRes.getString(R.string.translation))){
                mIntent.setClassName(Values.PACKAGE_NAME_TRANSLATION,Values.CLASS_NAME_TRANSLATION);
            }else if (info.equals(mRes.getString(R.string.vos))) {
                mIntent.setClassName(Values.PACKAGE_NAME_VOS,Values.CLASS_NAME_VOS);
            } else if (info.equals(mRes.getString(R.string.video_record))) {
                if (ZZXConfig.isPullout){
                    Toast.makeText(mContext,R.string.power_saving_mode,Toast.LENGTH_SHORT).show();
                    return;
                }
                if (ZZXConfig.isP5C_HXJ || ZZXConfig.isP5C_HXJ_TKY){
                    if (CameraService.isOccupy){
                        Toast.makeText(mContext,R.string.camera_occupy,Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                mIntent.setClass(mContext, CameraService.class);
                mIntent.setAction(Values.ACTION_MAX_WINDOW);
                mContext.startService(mIntent);
                return;
            }
            startActivity();
        } catch (
                Exception e)

        {
            e.printStackTrace();
            Toast.makeText(mContext, R.string.application_not_install, Toast.LENGTH_SHORT).show();
        }

    }

    private void toggleUsb() {
        UsbMassManager massManager = new UsbMassManager(mContext);
        if (!massManager.isMassEnabled()) {
            massManager.enableUsbMass();
        } else {
            massManager.disableUsbMass();
        }
    }

    private String getName(String key) {
        String value = Settings.System.getString(mContext.getContentResolver(), key);
        if (value == null || value.equals("") || value.equals("null") || value.equals(" ")) {
            return null;
        }
        return value;
    }

    private void startActivity() {
        try {
            mContext.startActivity(mIntent);
        } catch (Exception e) {
            TTSToast.showToast(mContext, R.string.no_class_find);
            e.printStackTrace();
        }
    }

    class GridViewAdapter extends BaseAdapter {
        private int mPosition;

        GridViewAdapter(int position) {
            mPosition = position;
        }

        class ViewHolder {
            public ImageView mIvIcon = null;
            public TextView mTvInfo = null;
        }

        @Override
        public int getCount() {
            int count = (mPosition + 1) * COUNT_IN_PAGE;
            return count > mListInfo.size() ? COUNT_IN_PAGE - (count - mListInfo.size()) : COUNT_IN_PAGE;
        }

        @Override
        public String getItem(int position) {
            return mListInfo.get(mPosition * COUNT_IN_PAGE + position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        /**
         * dsj_h9六宫格内容
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.grid_view_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.mIvIcon = (ImageView) convertView.findViewById(R.id.grid_view_item_icon);
                viewHolder.mTvInfo = (TextView) convertView.findViewById(R.id.grid_view_item_text);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            int icon = mListIcon.get(mPosition * COUNT_IN_PAGE + position);
            viewHolder.mIvIcon.setImageResource(icon);
            viewHolder.mTvInfo.setText(getItem(position));
            return convertView;
        }
    }
}
