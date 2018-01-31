/*
* Copyright 2017 Maxst, Inc. All Rights Reserved.
*/

package com.maxstar.ardrawing.app.task;

import android.os.AsyncTask;
import android.util.Log;

import com.maxstar.ardrawing.ARDrawing;
import com.maxstar.ardrawing.app.ARDrawingUtils;
import com.maxstar.ardrawing.CameraFrame;
import com.maxstar.ardrawing.ResultCode;
import com.maxstar.ardrawing.app.fragment.ARDrawingFragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class ImageLearnTask extends AsyncTask<CameraFrame, Void, Integer> {

	private static final String TAG = ImageLearnTask.class.getSimpleName();

	boolean SAVE = false;

	private WeakReference<ARDrawingFragment> arDrawingFragmentWeakReference;

	public ImageLearnTask(ARDrawingFragment fragment) {
		arDrawingFragmentWeakReference = new WeakReference<>(fragment);
	}
	@Override
	protected Integer doInBackground(CameraFrame[] objects) {
		ARDrawingFragment fragment = arDrawingFragmentWeakReference.get();
		if (fragment == null) {
			return ResultCode.FAIL;
		}

		CameraFrame cameraFrame = fragment.cameraFrameForLearn;

		int[] timeMillis = new int[1];
		int result = ARDrawing.learn(cameraFrame.imageBuffer, cameraFrame.width, cameraFrame.height, cameraFrame.pixelFormat.getValue(),
				fragment.touchStroke,
				fragment.touchStroke.length / 2, timeMillis);

		if(result != ResultCode.SUCCESS){
			Log.e(TAG, "learn. Result : " + result);
			return result;
		}

		int [] trackableSize = new int[1];
		result = ARDrawing.getLearnedTrackableArraySize(trackableSize);
		if (result != ResultCode.SUCCESS) {
			Log.e(TAG, "Get learned trackable array size. Result : " + result + ", size : " + trackableSize[0]);
			return result;
		}

		byte [] trackable = new byte[trackableSize[0]];
		result = ARDrawing.getLearnedTrackableArray(trackable);
		if (result != ResultCode.SUCCESS) {
			Log.e(TAG, "Get learned trackable array. Result : " + result);
			return result;
		}

		ARDrawingUtils.saveByteToFile(trackable, ARDrawingUtils.TRACKABLE_FILE_NAME);
		result = ARDrawing.clearLearnedTrackable();
		if (result != ResultCode.SUCCESS) {
			Log.e(TAG, "Clear learned trackable. Result : " + result);
			return result;
		}
		return ResultCode.SUCCESS;
	}

	@Override
	protected void onPostExecute(Integer result) {
		ARDrawingFragment fragment = arDrawingFragmentWeakReference.get();
		if (fragment == null) {
			return;
		}

		fragment.learningCompleted(result);
	}
}
