package com.zzx.police.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zzx.police.R;
import com.zzx.police.data.Values;

/**@author Tomy
 * Created by Tomy on 2014/7/14.
 */
public class PictureModeSettingFragment extends Fragment implements View.OnClickListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.btn, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        view.findViewById(R.id.btn_picture_setting).setOnClickListener(this);
        view.findViewById(R.id.btn_record_setting).setOnClickListener(this);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        Fragment fragment = new PictureSettingFragment();
        Bundle bundle = new Bundle();
        switch (v.getId()) {
            case R.id.btn_picture_setting:
                bundle.putInt(Values.FRAGMENT_KEY, Values.FRAGMENT_SETTING_PICTURE);
                break;
            case R.id.btn_record_setting:
                bundle.putInt(Values.FRAGMENT_KEY, Values.FRAGMENT_SETTING_RECORD);
                break;
        }
        fragment.setArguments(bundle);
        getFragmentManager().beginTransaction().replace(R.id.container, fragment).addToBackStack(null).commit();
    }
}