/*
* Copyright 2017 Maxst, Inc. All Rights Reserved.
*/

package com.maxstar.ardrawing.app.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.maxstar.ardrawing.app.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class TestDataFragment extends Fragment {

	public TestDataFragment() {
		// Required empty public constructor
	}

	public static TestDataFragment newInstance(String param1, String param2) {
		TestDataFragment fragment = new TestDataFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_test_data, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ButterKnife.bind(this, view);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		ButterKnife.unbind(this);
	}

	@OnClick(R.id.camera_test)
	public void cameraTest() {
		FragmentUtil.clickedOn(getActivity(), new CameraTestFragment());
	}

	@OnClick(R.id.image_test)
	public void imageTest() {
		FragmentUtil.clickedOn(getActivity(), new ImageTestFragment());
	}
}
