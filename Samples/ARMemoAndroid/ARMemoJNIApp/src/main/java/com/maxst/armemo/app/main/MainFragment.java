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
import android.widget.TextView;

import com.maxst.armemo.app.R;
import com.maxst.armemo.app.main.learn.CameraLearnFragment;
import com.maxst.armemo.app.main.tracking.CameraTrackingFragment;
import com.maxst.armemo.app.util.ARMemoUtils;
import com.maxst.armemo.app.util.FragmentUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class MainFragment extends Fragment {

	private static final String TAG = MainFragment.class.getName();

	@BindView(R.id.sd_resolution)
	RadioButton sdResolution;

	@BindView(R.id.hd_resolution)
	RadioButton hdResolution;

	@BindView(R.id.full_hd_resolution)
	RadioButton fullHdResolution;

	@BindView(R.id.resolution_text_view)
	TextView resolutionTextView;

	private SharedPreferences.Editor editor;

	private Unbinder unbinder;

	public static MainFragment newInstance() {
		MainFragment fragment = new MainFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.fragment_main, container, false);
		unbinder = ButterKnife.bind(this, view);
		int learnResolution = getActivity().getSharedPreferences(ARMemoUtils.PREF_NAME, Activity.MODE_PRIVATE).getInt(ARMemoUtils.PREF_KEY_CAM_RESOLUTION, ARMemoUtils.PREF_RESOLUTION_VALUE_HD);
		switch (learnResolution) {
			case ARMemoUtils.PREF_RESOLUTION_VALUE_SD:
				sdResolution.setChecked(true);
				resolutionTextView.setText(getString(R.string.sd_resolution));
				break;
			case ARMemoUtils.PREF_RESOLUTION_VALUE_HD:
				hdResolution.setChecked(true);
				resolutionTextView.setText(getString(R.string.hd_resolution));
				break;
			case ARMemoUtils.PREF_RESOLUTION_VALUE_FULL_HD:
				fullHdResolution.setChecked(true);
				resolutionTextView.setText(getString(R.string.full_hd_resolution));
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

	@OnClick(R.id.learn)
	public void onLearnClick() {
		FragmentUtil.clickedOn(getActivity(), CameraLearnFragment.newInstance());
	}

	@OnClick(R.id.tracking)
	public void onTrackingClick() {
		FragmentUtil.clickedOn(getActivity(), CameraTrackingFragment.newInstance());
	}

	@OnClick({R.id.sd_resolution, R.id.hd_resolution, R.id.full_hd_resolution})
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.sd_resolution:
				editor = getActivity().getSharedPreferences(ARMemoUtils.PREF_NAME, Activity.MODE_PRIVATE).edit();
				editor.putInt(ARMemoUtils.PREF_KEY_CAM_RESOLUTION, ARMemoUtils.PREF_RESOLUTION_VALUE_SD);
				editor.apply();
				resolutionTextView.setText(getString(R.string.sd_resolution));
				break;

			case R.id.hd_resolution:
				editor = getActivity().getSharedPreferences(ARMemoUtils.PREF_NAME, Activity.MODE_PRIVATE).edit();
				editor.putInt(ARMemoUtils.PREF_KEY_CAM_RESOLUTION, ARMemoUtils.PREF_RESOLUTION_VALUE_HD);
				editor.apply();
				resolutionTextView.setText(getString(R.string.hd_resolution));
				break;

			case R.id.full_hd_resolution:
				editor = getActivity().getSharedPreferences(ARMemoUtils.PREF_NAME, Activity.MODE_PRIVATE).edit();
				editor.putInt(ARMemoUtils.PREF_KEY_CAM_RESOLUTION, ARMemoUtils.PREF_RESOLUTION_VALUE_FULL_HD);
				editor.apply();
				resolutionTextView.setText(getString(R.string.full_hd_resolution));
				break;
		}
	}
}
