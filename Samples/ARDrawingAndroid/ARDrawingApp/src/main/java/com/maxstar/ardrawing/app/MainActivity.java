/*
* Copyright 2017 Maxst, Inc. All Rights Reserved.
*/

package com.maxstar.ardrawing.app;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.maxstar.ardrawing.app.fragment.MainFragment;

public class MainActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.replace(android.R.id.content, new MainFragment(), this.toString())
					.commit();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}