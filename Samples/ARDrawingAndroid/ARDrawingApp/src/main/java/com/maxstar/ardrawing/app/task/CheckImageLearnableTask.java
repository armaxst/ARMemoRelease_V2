/*
* Copyright 2017 Maxst, Inc. All Rights Reserved.
*/

package com.maxstar.ardrawing.app.task;

import android.os.AsyncTask;
import android.util.Log;

import com.maxstar.ardrawing.ARDrawing;
import com.maxstar.ardrawing.CameraFrame;
import com.maxstar.ardrawing.ResultCode;
import com.maxstar.ardrawing.app.fragment.ARDrawingFragment;

import java.lang.ref.WeakReference;

public class CheckImageLearnableTask extends AsyncTask<Void, Void, CameraFrame> {

	private static final String TAG = CheckImageLearnableTask.class.getSimpleName();

	private static final int MAX_CHECK_COUNT = 5;
	private WeakReference<ARDrawingFragment> arDrawingFragmentWeakReference;
	private int tryCount = 0;
	private CameraFrame cameraFrame;

	public CheckImageLearnableTask(ARDrawingFragment fragment) {
		arDrawingFragmentWeakReference = new WeakReference<>(fragment);
		cameraFrame = new CameraFrame();
	}

	@Override
	protected CameraFrame doInBackground(Void... noParams) {
		ARDrawingFragment fragment = arDrawingFragmentWeakReference.get();
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

			int result = ARDrawing.checkLearnable(cameraFrame.imageBuffer, cameraFrame.width, cameraFrame.height, cameraFrame.pixelFormat.getValue(), 70);
			Log.d(TAG, "Check image result : " + result);

			if (result == ResultCode.SUCCESS) {
				return cameraFrame;
			}
		}
		return null;
	}

	@Override
	protected void onPostExecute(final CameraFrame result) {
		final ARDrawingFragment fragment = arDrawingFragmentWeakReference.get();
		if (fragment == null) {
			return;
		}

		fragment.imageCheckCompleted(result);
	}
}
