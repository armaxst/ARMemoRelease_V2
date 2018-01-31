/*
* Copyright 2017 Maxst, Inc. All Rights Reserved.
*/

package com.maxstar.ardrawing.app.fragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.maxstar.ardrawing.app.ARDrawingUtils;
import com.maxstar.ardrawing.app.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingsFragment extends Fragment {

	@Bind(R.id.learn_sd_resolution)
	RadioButton learnSdResolution;

	@Bind(R.id.learn_hd_resolution)
	RadioButton learnHdResolution;

	@Bind(R.id.learn_full_hd_resolution)
	RadioButton learnFullHdResolution;

	@Bind(R.id.tracking_sd_resolution)
	RadioButton trackingSdResolution;

	@Bind(R.id.tracking_hd_resolution)
	RadioButton trackingHdResolution;

	@Bind(R.id.tracking_full_hd_resolution)
	RadioButton trackingFullHdResolution;

	private SharedPreferences.Editor editor;

	public SettingsFragment() {
		// Required empty public constructor
	}

	public static SettingsFragment newInstance(String param1, String param2) {
		SettingsFragment fragment = new SettingsFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_settings, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ButterKnife.bind(this, view);

		int learnResolution = getActivity().getSharedPreferences(ARDrawingUtils.PREF_NAME , Activity.MODE_PRIVATE).getInt(ARDrawingUtils.PREF_KEY_LEARN_CAM_RESOLUTION, 1);
		switch (learnResolution) {
			case 0:
				learnSdResolution.setChecked(true);
				break;

			case 1:
				learnHdResolution.setChecked(true);
				break;

			case 2:
				learnFullHdResolution.setChecked(true);
				break;
		}

		int trackingResolution = getActivity().getSharedPreferences(ARDrawingUtils.PREF_NAME , Activity.MODE_PRIVATE).getInt(ARDrawingUtils.PREF_KEY_TRACKING_CAM_RESOLUTION, 1);
		switch (trackingResolution) {
			case 0:
				trackingSdResolution.setChecked(true);
				break;

			case 1:
				trackingHdResolution.setChecked(true);
				break;

			case 2:
				trackingFullHdResolution.setChecked(true);
				break;
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		ButterKnife.unbind(this);
	}

	@OnClick({R.id.learn_sd_resolution, R.id.learn_hd_resolution, R.id.learn_full_hd_resolution,
			R.id.tracking_sd_resolution, R.id.tracking_hd_resolution, R.id.tracking_full_hd_resolution})
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.learn_sd_resolution:
				editor = getActivity().getSharedPreferences(ARDrawingUtils.PREF_NAME , Activity.MODE_PRIVATE).edit();
				editor.putInt(ARDrawingUtils.PREF_KEY_LEARN_CAM_RESOLUTION, 0);
				editor.apply();
				break;

			case R.id.learn_hd_resolution:
				editor = getActivity().getSharedPreferences(ARDrawingUtils.PREF_NAME , Activity.MODE_PRIVATE).edit();
				editor.putInt(ARDrawingUtils.PREF_KEY_LEARN_CAM_RESOLUTION, 1);
				editor.apply();
				break;

			case R.id.learn_full_hd_resolution:
				editor = getActivity().getSharedPreferences(ARDrawingUtils.PREF_NAME , Activity.MODE_PRIVATE).edit();
				editor.putInt(ARDrawingUtils.PREF_KEY_LEARN_CAM_RESOLUTION, 2);
				editor.apply();
				break;

			case R.id.tracking_sd_resolution:
				editor = getActivity().getSharedPreferences(ARDrawingUtils.PREF_NAME , Activity.MODE_PRIVATE).edit();
				editor.putInt(ARDrawingUtils.PREF_KEY_TRACKING_CAM_RESOLUTION, 0);
				editor.apply();
				break;

			case R.id.tracking_hd_resolution:
				editor = getActivity().getSharedPreferences(ARDrawingUtils.PREF_NAME , Activity.MODE_PRIVATE).edit();
				editor.putInt(ARDrawingUtils.PREF_KEY_TRACKING_CAM_RESOLUTION, 1);
				editor.apply();
				break;

			case R.id.tracking_full_hd_resolution:
				editor = getActivity().getSharedPreferences(ARDrawingUtils.PREF_NAME , Activity.MODE_PRIVATE).edit();
				editor.putInt(ARDrawingUtils.PREF_KEY_TRACKING_CAM_RESOLUTION, 2);
				editor.apply();
				break;
		}
	}
}
