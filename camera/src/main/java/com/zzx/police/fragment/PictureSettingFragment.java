package com.zzx.police.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.zzx.police.R;
import com.zzx.police.data.Values;
import com.zzx.police.view.SettingCustomListPreference;

import java.util.ArrayList;
import java.util.List;

/**@author Tomy
 * Created by Tomy on 2014/7/14.
 */
public class PictureSettingFragment extends PreferenceFragment {
    private static final String TAG = "PictureSettingFragment: ";
    private Resources mRes = null;
    private Context mContext;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRes = getResources();
        int key = getArguments().getInt(Values.FRAGMENT_KEY, 0);
        switch (key) {
            case Values.FRAGMENT_SETTING_PICTURE:
                addPreferencesFromResource(R.xml.preference_setting_picture);
                break;
            case Values.FRAGMENT_SETTING_RECORD:
                addPreferencesFromResource(R.xml.preference_setting_record);
                break;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        mContext = activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mContext = null;
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ListView listView = ((ListView) view.findViewById(android.R.id.list));
        listView.setSelector(R.drawable.bg_setting_item);
        listView.setCacheColorHint(getResources().getColor(android.R.color.transparent));
        super.onViewCreated(view, savedInstanceState);
    }

    private String getKey(int keyId) {
        if (mRes == null) {
            mRes = getResources();
        }
        return mRes.getString(keyId);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        Values.LOG_I(TAG, "onPreferenceClick.preference.key = " + preference.getKey());
        if (preference.getKey().equals(getKey(R.string.key_picture_set_reset))) {
            List<Integer> resetList = new ArrayList<>();
            if (preferenceScreen.getKey().equals(getKey(R.string.key_setting_picture))) {
                resetList.add(R.string.key_mode_interval);
                resetList.add(R.string.key_mode_picture);
                resetList.add(R.string.key_mode_quick_series);
                resetList.add(R.string.key_mode_timer);
                resetList.add(R.string.key_ratio_picture);
                resetList.add(R.string.key_sound_take_picture);
                /*PreferenceManager.getDefaultSharedPreferences(mContext).edit().remove(getKey(R.string.key_mode_interval))
                        .remove(getKey(R.string.key_mode_picture)).remove(getKey(R.string.key_mode_quick_series)).remove(getKey(R.string.key_mode_timer))
                        .remove(getKey(R.string.key_ratio_picture)).apply();*/
            } else if (preferenceScreen.getKey().equals(getKey(R.string.key_setting_record))) {
                resetList.add(R.string.key_record_sound);
                resetList.add(R.string.key_record_auto);
                resetList.add(R.string.key_record_loop);
                resetList.add(R.string.key_record_delay);
                resetList.add(R.string.key_record_pre);
                resetList.add(R.string.key_record_section);
                resetList.add(R.string.key_record_sound);
                /*PreferenceManager.getDefaultSharedPreferences(mContext).edit().remove(getKey(R.string.key_record_circle))
                        .remove(getKey(R.string.key_record_delay)).remove(getKey(R.string.key_record_pre)).remove(getKey(R.string.key_record_section))
                        .remove(getKey(R.string.key_record_sound)).apply();*/
            }
            resetPreferences(resetList);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void resetPreferences(List<Integer> resetList) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
        for (int id : resetList) {
            String key = getKey(id);
            Preference preference = findPreference(key);
            if (preference instanceof SettingCustomListPreference) {
                ((SettingCustomListPreference) findPreference(key)).resetSummary();
            } else if (preference instanceof CheckBoxPreference) {
                ((CheckBoxPreference) preference).setChecked(true);
                preference.setSummary(R.string.open);
            }
            editor.remove(key);
        }
        editor.apply();
    }
}
