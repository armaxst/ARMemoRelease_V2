/*
 * Copyright (c) 2018. Maxst, Inc. All Rights Reserved.
 */

package com.maxst.armemo.app.main.tracking;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maxst.armemo.ARMemoJNI;
import com.maxst.armemo.ResultCode;
import com.maxst.armemo.app.R;
import com.maxst.armemo.app.StrokesData;
import com.maxst.armemo.app.cameracontroller.CameraController;
import com.maxst.armemo.app.cameracontroller.NewCameraFrameCallback;
import com.maxst.armemo.app.cameracontroller.SurfaceManager;
import com.maxst.armemo.app.cameracontroller.SystemUtil;
import com.maxst.armemo.app.main.learn.FingerPaintView;
import com.maxst.armemo.app.util.ARMemoUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class CameraTrackingFragment extends Fragment {

	private static final String TAG = CameraTrackingFragment.class.getSimpleName();

	@BindView(R.id.ar_drawing_view)
	FingerPaintView fingerPaintView;

	@BindView(R.id.start_engine)
	Button startEngine;

	@BindView(R.id.camera_resolution)
	TextView cameraResolution;

	private Unbinder unbinder;

	private RenderHandler renderHandler;
	private int preferCameraWidth = 0;
	private int preferCameraHeight = 0;

	private int actualCameraWidth = 0;
	private int actualCameraHeight = 0;

	float xScaleFactor;
	float yScaleFactor;

	boolean trackerAlive = false;

	private CameraController cameraController;

	public static CameraTrackingFragment newInstance() {
		CameraTrackingFragment fragment = new CameraTrackingFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.fragment_camera_tracking, container, false);
		unbinder = ButterKnife.bind(this, view);

		startEngine.setEnabled(false);
		fingerPaintView.enableTouch(false);

		SurfaceManager.init();
		SystemUtil.init(getActivity());
		cameraController = SurfaceManager.getInstance().getCameraController();
		cameraController.setNewCameraFrameCallback(newCameraFrameCallback);

		int resolution =
				getActivity().getSharedPreferences(ARMemoUtils.PREF_NAME, Activity.MODE_PRIVATE).getInt(ARMemoUtils.PREF_KEY_CAM_RESOLUTION, ARMemoUtils.PREF_RESOLUTION_VALUE_HD);
		switch (resolution) {
			case ARMemoUtils.PREF_RESOLUTION_VALUE_SD:
				preferCameraWidth = 640;
				preferCameraHeight = 480;
				break;

			case ARMemoUtils.PREF_RESOLUTION_VALUE_HD:
				preferCameraWidth = 1280;
				preferCameraHeight = 720;
				break;

			case ARMemoUtils.PREF_RESOLUTION_VALUE_FULL_HD:
				preferCameraWidth = 1920;
				preferCameraHeight = 1080;
				break;
		}

		cameraResolution.setText(String.format(Locale.US, "Camera resolution %dx%d", preferCameraWidth, preferCameraHeight));
		int result = ARMemoJNI.initialize(getActivity(), getString(R.string.app_key));
		if (result == ResultCode.INVALID_APP) {
			Toast.makeText(getActivity(), "Invalid App Signature", Toast.LENGTH_LONG).show();
			Log.e(TAG, "initialize : " + result);
		} else {
			startEngine.setEnabled(true);
		}
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
		cameraController.start(0, preferCameraWidth, preferCameraHeight);
		renderHandler = new RenderHandler(this);
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

		unbinder.unbind();

		cameraController.stop();
		cameraController.setNewCameraFrameCallback(null);
		SurfaceManager.deinit();
		SystemUtil.deinit();

		if (trackerAlive) {
			ARMemoJNI.clearTrackingTrackable();
			ARMemoJNI.stop();
		}
		ARMemoJNI.destroy();

		System.gc();
	}

	private void calcFactor() {
		int touchXMin = 0;
		int touchXMax = SurfaceManager.getInstance().getCameraSurfaceView().getWidth();

		int touchYMin = 0;
		int touchYMax = SurfaceManager.getInstance().getCameraSurfaceView().getHeight();

		int imageXMin = 0;
		int imageXMax = actualCameraWidth;

		int imageYMin = 0;
		int imageYMax = actualCameraHeight;

		xScaleFactor = (float) (touchXMax - touchXMin) / (float) (imageXMax - imageXMin);
		yScaleFactor = (float) (touchYMax - touchYMin) / (float) (imageYMax - imageYMin);

		ARMemoUtils.resizeView(getResources(), fingerPaintView, actualCameraWidth, actualCameraHeight);
	}

	@OnClick(R.id.start_engine)
	public void startEngine() {
		if (!trackerAlive) {
			int result = ARMemoJNI.start();
			Log.e(TAG, "start : " + result);

			loadTrackableFile();
			trackerAlive = true;
			startEngine.setText("Stop");
		} else {
			int result = ARMemoJNI.clearTrackingTrackable();
			Log.e(TAG, "clearTrackingTrackable : " + result);

			result = ARMemoJNI.stop();
			Log.e(TAG, "stop : " + result);
			trackerAlive = false;

			fingerPaintView.clearCanvas();
			startEngine.setText("Start");
		}
	}

	public void loadTrackableFile() {
		calcFactor();

		//stroke file load
		File strokeFile = new File(ARMemoUtils.STROKE_FILE_NAME);
		if (!strokeFile.exists()) {
			Toast.makeText(getActivity(), "No stroke file exists", Toast.LENGTH_SHORT).show();
			return;
		}

		try {
			int fileSize = (int) strokeFile.length();
			byte[] strokeBytes = new byte[fileSize];
			FileInputStream fileInputStream = new FileInputStream(strokeFile);
			fileInputStream.read(strokeBytes);
			fileInputStream.close();

			String jsonString = new String(strokeBytes);
			Type strokesType = new TypeToken<StrokesData>() {
			}.getType();
			Gson gson = new Gson();
			StrokesData strokes = gson.fromJson(jsonString, strokesType);
			learnToTrackingStrokes(strokes);
			fingerPaintView.setTouchPointList(strokes.strokes);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		File trackingFile = new File(ARMemoUtils.TRACKABLE_FILE_NAME);
		if (!trackingFile.exists()) {
			Toast.makeText(getActivity(), "No tracking file exists", Toast.LENGTH_SHORT).show();
			return;
		}

		int result = ARMemoJNI.setTrackingFile(ARMemoUtils.TRACKABLE_FILE_NAME);
		Log.d(TAG, "setTrackingFile result : " + result);
	}

	private void learnToTrackingStrokes(StrokesData strokesData) {
		int srcW = strokesData.imageWidth;
		int srcH = strokesData.imageHeight;
		int dstW = actualCameraWidth;
		int dstH = actualCameraHeight;

		float wr = (float) dstW / srcW;
		float hr = (float) dstH / srcH;
		float halfDiffHeight = (srcH * wr - dstH) / 2.f;

		for (List<Point> points : strokesData.strokes) {
			for (Point point : points) {
				point.x = (int) (point.x * wr);
				point.y = (int) (point.y * wr - halfDiffHeight);
			}
		}
	}

	boolean needToResizeCameraSurfaceView = true;

	private NewCameraFrameCallback newCameraFrameCallback = new NewCameraFrameCallback() {

		@Override
		public void onNewCameraFrame(byte[] buffer, int length, final int width, final int height, int pixelFormat) {
			actualCameraWidth = width;
			actualCameraHeight = height;

			if (needToResizeCameraSurfaceView) {
				ARMemoUtils.resizeView(getResources(), SurfaceManager.getInstance().getCameraSurfaceView(), width, height);
				needToResizeCameraSurfaceView = false;
			}

			if (trackerAlive) {
				int result = ARMemoJNI.inputTrackingImage(buffer, buffer.length, width, height, 2);
				renderHandler.sendEmptyMessage(0);
			}
		}
	};

	private static class RenderHandler extends Handler {
		private WeakReference<CameraTrackingFragment> realTimeDrawingFragmentWeakReference;

		RenderHandler(CameraTrackingFragment fragment) {
			realTimeDrawingFragmentWeakReference = new WeakReference<>(fragment);
		}

		@Override
		public void handleMessage(Message msg) {
			CameraTrackingFragment fragment = realTimeDrawingFragmentWeakReference.get();
			if (fragment != null) {
				final float[] transformMatrix = new float[9];
				final int isTracking = ARMemoJNI.getTrackingResult(transformMatrix);
//				Log.e(TAG, "isTracking : " + isTracking + ", matrix : " + Arrays.toString(transformMatrix));
				if (isTracking == 0) {
					fragment.fingerPaintView.applyTrackingResult(transformMatrix, 0, fragment.xScaleFactor, fragment.yScaleFactor);
				} else {
					fragment.fingerPaintView.clearCanvas();
					fragment.fingerPaintView.postInvalidate();
				}
			}
		}
	}
}