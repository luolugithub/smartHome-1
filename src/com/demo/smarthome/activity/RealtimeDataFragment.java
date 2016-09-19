package com.demo.smarthome.activity;


import android.app.Fragment;

import android.os.Bundle;

import android.support.annotation.Nullable;

import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import com.demo.smarthome.R;

/**********************************************************

 **********************************************************/
public class RealtimeDataFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_content, container, false);
        return view;
    }

}
