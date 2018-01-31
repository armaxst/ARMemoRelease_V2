/*
* Copyright 2017 Maxst, Inc. All Rights Reserved.
*/

package com.maxstar.ardrawing.app.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.maxstar.ardrawing.app.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MakeDataFragment extends Fragment {

	public MakeDataFragment() {
		// Required empty public constructor
	}

	public static MakeDataFragment newInstance(String param1, String param2) {
		MakeDataFragment fragment = new MakeDataFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_make_data, container, false);
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

	@OnClick(R.id.save_image)
	public void saveImage() {
		FragmentUtil.clickedOn(getActivity(), new SaveImageFragment());
	}

	@OnClick(R.id.learn_image)
	public void learImage() {
		FragmentUtil.clickedOn(getActivity(), new LearnCameraFragment());
	}
}
