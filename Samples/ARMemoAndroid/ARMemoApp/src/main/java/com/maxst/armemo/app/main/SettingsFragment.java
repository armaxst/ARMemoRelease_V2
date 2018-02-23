/*
 * Copyright (c) 2018. Maxst, Inc. All Rights Reserved.
 */

package com.maxst.armemo.app.main;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.maxst.armemo.app.R;
import com.maxst.armemo.app.util.ARMemoUtils;
import com.maxst.armemo.app.util.FragmentUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class SettingsFragment extends Fragment {

	private static final String TAG = SettingsFragment.class.getName();

	@BindView(R.id.sd_resolution)
	RadioButton sdResolution;

	@BindView(R.id.hd_resolution)
	RadioButton hdResolution;

	@BindView(R.id.full_hd_resolution)
	RadioButton fullHdResolution;

	private SharedPreferences.Editor editor;

	private Unbinder unbinder;

	public static SettingsFragment newInstance() {
		SettingsFragment fragment = new SettingsFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.fragment_settings, container, false);
		unbinder = ButterKnife.bind(this, view);
		int learnResolution = getActivity().getSharedPreferences(ARMemoUtils.PREF_NAME, Activity.MODE_PRIVATE).getInt(ARMemoUtils.PREF_KEY_CAM_RESOLUTION, ARMemoUtils.PREF_RESOLUTION_VALUE_FULL_HD);
		switch (learnResolution) {
			case ARMemoUtils.PREF_RESOLUTION_VALUE_SD:
				sdResolution.setChecked(true);
				break;
			case ARMemoUtils.PREF_RESOLUTION_VALUE_HD:
				hdResolution.setChecked(true);
				break;
			case ARMemoUtils.PREF_RESOLUTION_VALUE_FULL_HD:
				fullHdResolution.setChecked(true);
				break;
		}
		return view;
	}

	@Override
	public void onDestroyView() {
		Log.d(TAG, "onDestroyView");
		unbinder.unbind();
		super.onDestroyView();
	}

	@OnClick({R.id.sd_resolution, R.id.hd_resolution, R.id.full_hd_resolution})
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.sd_resolution:
				editor = getActivity().getSharedPreferences(ARMemoUtils.PREF_NAME, Activity.MODE_PRIVATE).edit();
				editor.putInt(ARMemoUtils.PREF_KEY_CAM_RESOLUTION, ARMemoUtils.PREF_RESOLUTION_VALUE_SD);
				editor.apply();
				break;

			case R.id.hd_resolution:
				editor = getActivity().getSharedPreferences(ARMemoUtils.PREF_NAME, Activity.MODE_PRIVATE).edit();
				editor.putInt(ARMemoUtils.PREF_KEY_CAM_RESOLUTION, ARMemoUtils.PREF_RESOLUTION_VALUE_HD);
				editor.apply();
				break;

			case R.id.full_hd_resolution:
				editor = getActivity().getSharedPreferences(ARMemoUtils.PREF_NAME, Activity.MODE_PRIVATE).edit();
				editor.putInt(ARMemoUtils.PREF_KEY_CAM_RESOLUTION, ARMemoUtils.PREF_RESOLUTION_VALUE_FULL_HD);
				editor.apply();
				break;
		}
	}

	@OnClick(R.id.setting_ok)
	public void onSettingOk() {
		FragmentUtil.clickedOn(getActivity(), MainFragment.newInstance());
	}
}
