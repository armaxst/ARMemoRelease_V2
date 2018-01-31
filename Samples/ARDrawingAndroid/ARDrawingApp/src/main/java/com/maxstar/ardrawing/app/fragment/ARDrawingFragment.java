/*
* Copyright 2017 Maxst, Inc. All Rights Reserved.
*/

package com.maxstar.ardrawing.app.fragment;

import android.support.v4.app.Fragment;

import com.maxstar.ardrawing.CameraFrame;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class ARDrawingFragment extends Fragment {

	private static final String TAG = ARDrawingFragment.class.getSimpleName();

	protected boolean trackerAlive = false;
	protected int imageIndex = 0;

	public Lock imageCaptureLock = new ReentrantLock();
	public CameraFrame cameraFrame;
	public CameraFrame cameraFrameForLearn;
	public int[] touchStroke;

	public ARDrawingFragment() {
		// Required empty public constructor
	}

	public abstract void getCameraFrame(CameraFrame frame);
	public abstract void imageCheckCompleted(CameraFrame newFrame);
	public abstract void learningCompleted(int result) ;
}