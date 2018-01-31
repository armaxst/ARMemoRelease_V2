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

public class MainFragment extends Fragment {

	public MainFragment() {
		// Required empty public constructor
	}

	public static MainFragment newInstance(String param1, String param2) {
		MainFragment fragment = new MainFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_main, container, false);
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

	@OnClick(R.id.make_data)
	public void makeData() {
		FragmentUtil.clickedOn(getActivity(), new MakeDataFragment());
	}

	@OnClick(R.id.test_data)
	public void testData() {
		FragmentUtil.clickedOn(getActivity(), new TestDataFragment());
	}

	@OnClick(R.id.settings)
	public void settings() {
		FragmentUtil.clickedOn(getActivity(), new SettingsFragment());
	}
}
