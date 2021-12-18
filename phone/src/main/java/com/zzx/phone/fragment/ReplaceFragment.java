package com.zzx.phone.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zzx.phone.R;

/**@author Tomy
 * Created by Tomy on 2014/6/25.
 */
public class ReplaceFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.replace, container, false);
    }
}
