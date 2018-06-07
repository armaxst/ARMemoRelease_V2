/*
 * Copyright (c) 2018. Maxst, Inc. All Rights Reserved.
 */

package com.maxst.armemo.app.main.learn.task;

import android.os.AsyncTask;
import android.util.Log;

import com.maxst.armemo.ARMemoJNI;
import com.maxst.armemo.ResultCode;
import com.maxst.armemo.app.cameracontroller.CameraFrame;
import com.maxst.armemo.app.main.learn.ARMemoFragment;
import com.maxst.armemo.app.util.ARMemoUtils;

import java.lang.ref.WeakReference;

public class ImageLearnTask extends AsyncTask<CameraFrame, Void, Integer> {
	private static final String TAG = ImageLearnTask.class.getSimpleName();

	private WeakReference<ARMemoFragment> arMemoFragmentWeakReference;

	private static final int MAX_CHECK_COUNT = 5;
	private int tryCount = 0;


	public ImageLearnTask(ARMemoFragment fragment) {
		arMemoFragmentWeakReference = new WeakReference<>(fragment);
	}

	@Override
	protected Integer doInBackground(CameraFrame[] objects) {
		ARMemoFragment fragment = arMemoFragmentWeakReference.get();
		if (fragment == null) {
			return ResultCode.FAIL;
		}

		CameraFrame cameraFrame = fragment.cameraFrameForLearn;
		int result = ARMemoJNI.learn(cameraFrame.imageBuffer, cameraFrame.length, cameraFrame.width, cameraFrame.height, 2,
				fragment.touchStroke, fragment.touchStroke.length / 2);
		Log.d(TAG, "learn result : " + result);
		if (result == ResultCode.SUCCESS) {

			while (tryCount++ < MAX_CHECK_COUNT) {
				try {
					Thread.sleep(30);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				result = ARMemoJNI.saveLearnedFile(ARMemoUtils.TRACKABLE_FILE_NAME);
				Log.d(TAG, "saveLearnedFile result : " + result);
				if (result == ResultCode.SUCCESS) {
					result = ARMemoJNI.clearLearnedTrackable();
					Log.d(TAG, "clearLearnedTrackable result : " + result);
					return ResultCode.SUCCESS;
				}
			}
		}
		 else {
			Log.e(TAG, "learn : " + result);
		}
		//fail!!
		return result;
	}

	@Override
	protected void onPostExecute(Integer result) {
		ARMemoFragment fragment = arMemoFragmentWeakReference.get();
		if (fragment == null) {
			return;
		}

		fragment.learningCompleted(result);
	}
}
