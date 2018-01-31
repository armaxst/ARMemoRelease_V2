package com.maxstar.ardrawing.app.fragment;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

public class FragmentUtil {

	public static void clickedOn(FragmentActivity activity, @NonNull Fragment fragment) {
		final String tag = fragment.getClass().toString();
		activity.getSupportFragmentManager()
				.beginTransaction()
				.addToBackStack(tag)
				.replace(android.R.id.content, fragment, tag)
				.commit();
	}
}
