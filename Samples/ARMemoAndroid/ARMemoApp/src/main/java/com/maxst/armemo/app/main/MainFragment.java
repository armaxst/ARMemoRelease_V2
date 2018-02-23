/*
 * Copyright (c) 2018. Maxst, Inc. All Rights Reserved.
 */

package com.maxst.armemo.app.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.maxst.armemo.app.R;
import com.maxst.armemo.app.main.learn.CameraLearnFragment;
import com.maxst.armemo.app.main.tracking.CameraTrackingFragment;
import com.maxst.armemo.app.util.FragmentUtil;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class MainFragment extends Fragment {

	private static final String TAG = MainFragment.class.getName();

	private Unbinder unbinder;

	public static MainFragment newInstance() {
		MainFragment fragment = new MainFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.fragment_main, container, false);
		unbinder = ButterKnife.bind(this, view);
		return view;
	}

	@Override
	public void onDestroyView() {
		Log.d(TAG, "onDestroyView");
		unbinder.unbind();
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@OnClick(R.id.learn)
	public void onLearnClick() {
		FragmentUtil.clickedOn(getActivity(), CameraLearnFragment.newInstance());
	}

	@OnClick(R.id.tracking)
	public void onTrackingClick() {
		FragmentUtil.clickedOn(getActivity(), CameraTrackingFragment.newInstance());
	}
}
