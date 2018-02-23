/*
 * Copyright (c) 2018. Maxst, Inc. All Rights Reserved.
 */

package com.maxst.armemo.app.main.learn;

import android.support.v4.app.Fragment;

import com.maxst.armemo.app.cameracontroller.CameraFrame;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class ARMemoFragment extends Fragment {

	private static final String TAG = ARMemoFragment.class.getSimpleName();

	protected boolean trackerAlive = false;
	protected boolean isLearned = false;

	public Lock imageCaptureLock = new ReentrantLock();
	public CameraFrame cameraFrame;
	public CameraFrame cameraFrameForLearn;
	public int[] touchStroke;

	public ARMemoFragment() {
		// Required empty public constructor
	}

	public abstract void getCameraFrame(CameraFrame frame);
	public abstract void imageCheckCompleted(CameraFrame newFrame);
	public abstract void learningCompleted(int result) ;
}