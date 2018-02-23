/*
 * Copyright (c) 2018. Maxst, Inc. All Rights Reserved.
 */

package com.maxst.armemo.app.main.learn;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.maxst.armemo.ARMemo;
import com.maxst.armemo.ResultCode;
import com.maxst.armemo.app.R;
import com.maxst.armemo.app.cameracontroller.CameraController;
import com.maxst.armemo.app.cameracontroller.CameraFrame;
import com.maxst.armemo.app.cameracontroller.NewCameraFrameCallback;
import com.maxst.armemo.app.cameracontroller.PixelFormat;
import com.maxst.armemo.app.cameracontroller.SurfaceManager;
import com.maxst.armemo.app.cameracontroller.SystemUtil;
import com.maxst.armemo.app.main.learn.task.CheckImageLearnableTask;
import com.maxst.armemo.app.main.learn.task.ImageLearnTask;
import com.maxst.armemo.app.util.ARMemoUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class CameraLearnFragment extends ARMemoFragment {

	private static final String TAG = CameraLearnFragment.class.getSimpleName();

	@BindView(R.id.camera_resolution)
	TextView cameraResolution;

	@BindView(R.id.capture_image_view)
	ImageView captureImageView;

	@BindView(R.id.ar_drawing_view)
	FingerPaintView fingerPaintView;

	@BindView(R.id.capture_image)
	Button captureImage;

	@BindView(R.id.learn_image)
	Button learnImage;

	@BindView(R.id.start_tracker)
	Button startTracker;

	@BindView(R.id.clear)
	Button learnClear;

	private Unbinder unbinder;
	private TrackingResultRenderHandler renderHandler;
	private int cameraWidth = 0;
	private int cameraHeight = 0;

	private float xScaleFactor;
	private float yScaleFactor;

	private CameraController cameraController;

	public static CameraLearnFragment newInstance() {
		CameraLearnFragment fragment = new CameraLearnFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.fragment_camera_learn, container, false);

		unbinder = ButterKnife.bind(this, view);

		captureImage.setEnabled(false);
		learnImage.setEnabled(false);
		learnClear.setEnabled(false);

		SurfaceManager.init();
		SystemUtil.init(getActivity());
		cameraController = SurfaceManager.getInstance().getCameraController();
		cameraController.setNewCameraFrameCallback(newCameraFrameCallback);

		int resolution =
				getActivity().getSharedPreferences(ARMemoUtils.PREF_NAME, Activity.MODE_PRIVATE).getInt(ARMemoUtils.PREF_KEY_CAM_RESOLUTION, ARMemoUtils.PREF_RESOLUTION_VALUE_HD);
		switch (resolution) {
			case ARMemoUtils.PREF_RESOLUTION_VALUE_SD:
				cameraWidth = 640;
				cameraHeight = 480;
				break;

			case ARMemoUtils.PREF_RESOLUTION_VALUE_HD:
				cameraWidth = 1280;
				cameraHeight = 720;
				break;

			case ARMemoUtils.PREF_RESOLUTION_VALUE_FULL_HD:
				cameraWidth = 1920;
				cameraHeight = 1080;
				break;
		}

		cameraResolution.setText(String.format(Locale.US, "Camera resolution %dx%d", cameraWidth, cameraHeight));

		int result = ARMemo.initialize(getActivity(), getString(R.string.app_key));
		Log.e(TAG, "initialize : " + result);
		//ARMemoDebug.setDebugMode(true); //for armemo debug log
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
		cameraController.start(0, cameraWidth, cameraHeight);
		renderHandler = new TrackingResultRenderHandler(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG, "onPause");
		renderHandler.removeCallbacksAndMessages(null);
		renderHandler = null;
		cameraController.stop();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Log.d(TAG, "onDestroyView");
		unbinder.unbind();

		cameraController.stop();
		cameraController.setNewCameraFrameCallback(null);
		SurfaceManager.deinit();
		SystemUtil.deinit();

		int result = 0;
		if (isLearned) {
			result = ARMemo.clearLearnedTrackable();
			Log.e(TAG, "clearLearnedTrackable : " + result);
		}
		if (trackerAlive) {
			result = ARMemo.stopTracking();
			Log.e(TAG, "stopTracking : " + result);
		}
		result = ARMemo.destroy();
		Log.e(TAG, "destroy : " + result);
		System.gc();
	}

	@Override
	public void imageCheckCompleted(CameraFrame newFrame) {
		Log.d(TAG, "imageCheckCompleted");
		if (newFrame != null) {
			cameraController.stop();

			cameraFrameForLearn = newFrame;

			captureImageView.setImageBitmap(ARMemoUtils.yuvToBitmap(getContext(), newFrame.imageBuffer, newFrame.width, newFrame.height));
			captureImageView.setVisibility(View.VISIBLE);
			ARMemoUtils.resizeView(getResources(), captureImageView, cameraFrameForLearn.width, cameraFrameForLearn.height);

			fingerPaintView.enableTouch(true);
			fingerPaintView.clearTouchPoint();
			fingerPaintView.setVisibility(View.VISIBLE);
			ARMemoUtils.resizeView(getResources(), fingerPaintView, cameraFrameForLearn.width, cameraFrameForLearn.height);

			captureImage.setEnabled(false);
			learnImage.setEnabled(true);
		} else {
			int result = ARMemo.clearLearnedTrackable();
			Log.e(TAG, "clearLearnedTrackable : " + result);
			Toast.makeText(getContext(), "Image is not good for tracking", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void learningCompleted(int result) {
		if (result == ResultCode.SUCCESS) {
			Log.i(TAG, "Learning success!");
			learnImage.setEnabled(false);
			learnClear.setEnabled(true);

			isLearned = true;
			result = ARMemo.setTrackingFile(ARMemoUtils.TRACKABLE_FILE_NAME);
			Log.d(TAG, "setTrackingFile result : " + result);
		} else {
			captureImage.setEnabled(true);
			learnImage.setEnabled(false);
			Toast.makeText(getContext(), "Fail to learn image", Toast.LENGTH_SHORT).show();
			isLearned = false;
			result = ARMemo.clearLearnedTrackable();
			Log.e(TAG, "clearLearnedTrackable : " + result);
		}

		captureImageView.setVisibility(View.GONE);
		fingerPaintView.clearCanvas();
		fingerPaintView.enableTouch(false);

		cameraController.start(0, cameraWidth, cameraHeight);
		needToResizeCameraSurfaceView = true;
	}

	@OnClick(R.id.capture_image)
	public void captureImage() {
		fingerPaintView.clearTouchPoint();
		fingerPaintView.clearCanvas();
		fingerPaintView.postInvalidate();

		new CheckImageLearnableTask(CameraLearnFragment.this).execute();
	}

	@OnClick(R.id.learn_image)
	public void learnImage() {
		learnImage.setEnabled(false);
		captureImage.setEnabled(false);

		xScaleFactor = (float) captureImageView.getWidth() / (float) cameraFrameForLearn.width;
		yScaleFactor = (float) captureImageView.getHeight() / (float) cameraFrameForLearn.height;

		//region ---- Convert touch point to image point
		List<List<Point>> touchPointList = fingerPaintView.getTouchPointList();
		List<List<Point>> imagePointList = new ArrayList<>();
		for (List<Point> touchPoints : touchPointList) {
			List<Point> imagePoints = new ArrayList<>();
			for (Point point : touchPoints) {
				int newX = (int) (point.x / xScaleFactor);
				int newY = (int) (point.y / yScaleFactor);
				imagePoints.add(new Point(newX, newY));
			}
			imagePointList.add(imagePoints);
		}

		fingerPaintView.clearTouchPoint();
		fingerPaintView.setTouchPointList(imagePointList);
		//endregion ---- Convert touch point to image point

		//region ---- Convert point list to simple array for learn to engine
		List<Point> tempImagePointList = new ArrayList<>();
		for (List<Point> points : imagePointList) {
			for (Point point : points) {
				tempImagePointList.add(new Point(point.x, point.y));
			}
		}

		touchStroke = new int[tempImagePointList.size() * 2];
		for (int i = 0; i < tempImagePointList.size(); i++) {
			touchStroke[i * 2] = tempImagePointList.get(i).x;
			touchStroke[i * 2 + 1] = tempImagePointList.get(i).y;
		}
		//endregion ---- Convert point list to simple array for learn to engine

		//region ---- save strokes file
		Gson gson = new Gson();
		String jsonString = gson.toJson(imagePointList);

		File rootDir = new File(ARMemoUtils.ROOT_PATH);
		if (!rootDir.exists()) {
			boolean mkDirResult = rootDir.mkdirs();
			Log.d(TAG, "Make directory result : " + mkDirResult);
		}

		File strokeFile = new File(ARMemoUtils.STROKE_FILE_NAME);
		try {
			FileOutputStream outputStream = new FileOutputStream(strokeFile);
			try {
				outputStream.write(jsonString.getBytes());
				outputStream.flush();
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		//endregion ---- save strokes file

		// Start thread for image learn
		new ImageLearnTask(CameraLearnFragment.this).execute(cameraFrameForLearn);
	}

	@OnClick(R.id.start_tracker)
	public void startTracking() {
		if (!trackerAlive) {
			int result = ARMemo.startTracking();
			Log.e(TAG, "startTracking : " + result);
			trackerAlive = true;

			captureImage.setEnabled(true);
			startTracker.setText("Stop tracker");
		} else {
			//if capture iamge showing
			if (learnImage.isEnabled()) {
				cameraController.start(0, cameraWidth, cameraHeight);
			}
			captureImageView.setVisibility(View.GONE);
			fingerPaintView.clearCanvas();
			fingerPaintView.enableTouch(false);

			//if learned
			if (isLearned) {
				trackableClear();
			}

			int result = ARMemo.stopTracking();
			Log.e(TAG, "stopTracking : " + result);
			trackerAlive = false;

			captureImage.setEnabled(false);
			learnImage.setEnabled(false);
			learnClear.setEnabled(false);
			startTracker.setText("Start tracker");
		}
	}

	@OnClick(R.id.clear)
	public void trackableClear() {
		int result = ARMemo.clearTrackingTrackable();
		Log.e(TAG, "clearLearnedTrackable : " + result);
		isLearned = false;
		captureImage.setEnabled(true);
		learnImage.setEnabled(false);
		learnClear.setEnabled(false);
		fingerPaintView.clearCanvas();
		fingerPaintView.enableTouch(false);
	}

	@Override
	public void getCameraFrame(CameraFrame frame) {
		imageCaptureLock.lock();
		if (frame.imageBuffer == null) {
			frame.imageBuffer = cameraFrame.imageBuffer.clone();
			frame.length = cameraFrame.length;
			frame.width = cameraFrame.width;
			frame.height = cameraFrame.height;
			frame.pixelFormat = cameraFrame.pixelFormat;
		} else {
			System.arraycopy(cameraFrame.imageBuffer, 0, frame.imageBuffer, 0, frame.length);
		}
		imageCaptureLock.unlock();
	}

	boolean needToResizeCameraSurfaceView = true;

	private NewCameraFrameCallback newCameraFrameCallback = new NewCameraFrameCallback() {

		@Override
		public void onNewCameraFrame(byte[] buffer, int length, final int width, final int height, int pixelFormat) {

			if (needToResizeCameraSurfaceView) {
				ARMemoUtils.resizeView(getResources(), SurfaceManager.getInstance().getCameraSurfaceView(), width, height);
				needToResizeCameraSurfaceView = false;
			}

			imageCaptureLock.lock();

			if (cameraFrame == null) {
				cameraFrame = new CameraFrame();
				cameraFrame.imageBuffer = buffer.clone();
				cameraFrame.length = length;
				cameraFrame.width = width;
				cameraFrame.height = height;
				cameraFrame.pixelFormat = PixelFormat.YUV;
			} else {
				System.arraycopy(buffer, 0, cameraFrame.imageBuffer, 0, length);
			}

			imageCaptureLock.unlock();

			if (isLearned) {
				int result = ARMemo.inputTrackingImage(cameraFrame.imageBuffer, cameraFrame.imageBuffer.length, width, height, 2);
				renderHandler.sendEmptyMessage(0);
			}
		}
	};

	private static class TrackingResultRenderHandler extends Handler {
		private WeakReference<CameraLearnFragment> realTimeDrawingFragmentWeakReference;

		TrackingResultRenderHandler(CameraLearnFragment fragment) {
			realTimeDrawingFragmentWeakReference = new WeakReference<>(fragment);
		}

		@Override
		public void handleMessage(Message msg) {
			CameraLearnFragment fragment = realTimeDrawingFragmentWeakReference.get();
			if (fragment != null) {
				final float[] transformMatrix = new float[9];
				final int isTracking = ARMemo.getTrackingResult(transformMatrix);
//				Log.e(TAG, "isTracking : " + isTracking + ", matrix : " + Arrays.toString(transformMatrix));
				if (isTracking == 0) {
					fragment.fingerPaintView.applyTrackingResult(transformMatrix, 0, fragment.xScaleFactor, fragment.yScaleFactor);
				} else {
					fragment.fingerPaintView.clearCanvas();
					fragment.fingerPaintView.postInvalidate();
					Log.e(TAG, "getTrackingResult : " + isTracking);
				}
			}
		}
	}
}