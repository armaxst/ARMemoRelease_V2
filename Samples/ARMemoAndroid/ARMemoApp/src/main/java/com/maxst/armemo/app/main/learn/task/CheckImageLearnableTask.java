/*
 * Copyright (c) 2018. Maxst, Inc. All Rights Reserved.
 */

package com.maxst.armemo.app.main.learn.task;

import android.os.AsyncTask;
import android.util.Log;

import com.maxst.armemo.ARMemo;
import com.maxst.armemo.ResultCode;
import com.maxst.armemo.app.cameracontroller.CameraFrame;
import com.maxst.armemo.app.main.learn.ARMemoFragment;

import java.lang.ref.WeakReference;

public class CheckImageLearnableTask extends AsyncTask<Void, Void, CameraFrame> {
	private static final String TAG = CheckImageLearnableTask.class.getSimpleName();

	private static final int MAX_CHECK_COUNT = 10;
	private WeakReference<ARMemoFragment> arMemoFragmentWeakReference;
	private int tryCount = 0;
	private CameraFrame cameraFrame;

	public CheckImageLearnableTask(ARMemoFragment fragment) {
		arMemoFragmentWeakReference = new WeakReference<>(fragment);
		cameraFrame = new CameraFrame();
	}

	@Override
	protected CameraFrame doInBackground(Void... noParams) {
		ARMemoFragment fragment = arMemoFragmentWeakReference.get();
		if (fragment == null) {
			return null;
		}

		while (tryCount++ < MAX_CHECK_COUNT) {
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			fragment.getCameraFrame(cameraFrame);

			int result = ARMemo.checkLearnable(cameraFrame.imageBuffer, cameraFrame.imageBuffer.length, cameraFrame.width, cameraFrame.height, cameraFrame.pixelFormat.getValue());
			Log.d(TAG, "checkLearnable : " + result);

			if (result == ResultCode.SUCCESS) {
				return cameraFrame;
			}
		}
		return null;
	}

	@Override
	protected void onPostExecute(final CameraFrame result) {
		final ARMemoFragment fragment = arMemoFragmentWeakReference.get();
		if (fragment == null) {
			return;
		}

		fragment.imageCheckCompleted(result);
	}
}
