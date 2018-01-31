/*
* Copyright 2017 Maxst, Inc. All Rights Reserved.
*/

package com.maxstar.ardrawing.app.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maxstar.ardrawing.ARDrawing;
import com.maxstar.ardrawing.CameraController;
import com.maxstar.ardrawing.CameraFrame;
import com.maxstar.ardrawing.NewCameraFrameCallback;
import com.maxstar.ardrawing.ResultCode;
import com.maxstar.ardrawing.SurfaceManager;
import com.maxstar.ardrawing.app.ARDrawingUtils;
import com.maxstar.ardrawing.app.FingerPaintView;
import com.maxstar.ardrawing.app.LinearPointConverter;
import com.maxstar.ardrawing.app.R;
import com.maxstar.ardrawing.app.task.ImageLearnTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CameraTestFragment extends Fragment {

	private static final String TAG = CameraTestFragment.class.getSimpleName();

	@Bind(R.id.ar_drawing_view)
	FingerPaintView fingerPaintView;

	@Bind(R.id.start_tracker)
	Button startTracker;

	@Bind(R.id.engine_version)
	TextView engineVersion;

	@Bind(R.id.camera_resolution)
	TextView cameraResolution;

	@Bind(R.id.debug_capture_view)
	ImageView debugCaptureView;

	private RenderHandler renderHandler;
	private int preferCameraWidth = 0;
	private int preferCameraHeight = 0;

	private int actualCameraWidth = 0;
	private int actualCameraHeight = 0;

	float xScaleFactor;
	float yScaleFactor;

	boolean trackerAlive = false;
	int imageIndex = 0;

	private CameraController cameraController;

	public CameraTestFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_camera_test, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ButterKnife.bind(this, view);

		fingerPaintView.enableTouch(false);

		engineVersion.setText(String.format(Locale.US, "%s : %s", "Engine Version", ARDrawing.getEngineVersion()));

		SurfaceManager.init();
		cameraController = SurfaceManager.getInstance().getCameraController();
		cameraController.setNewCameraFrameCallback(newCameraFrameCallback);

		int resolution =
				getActivity().getSharedPreferences(ARDrawingUtils.PREF_NAME, Activity.MODE_PRIVATE).getInt(ARDrawingUtils.PREF_KEY_TRACKING_CAM_RESOLUTION, 1);
		switch (resolution) {
			case 0:
				preferCameraWidth = 640;
				preferCameraHeight = 480;
				break;

			case 1:
				preferCameraWidth = 1280;
				preferCameraHeight = 720;
				break;

			case 2:
				preferCameraWidth = 1920;
				preferCameraHeight = 1080;
				break;
		}

		cameraResolution.setText(String.format(Locale.US, "Camera resolution %dx%d", preferCameraWidth, preferCameraHeight));

		int resultCode = ARDrawing.initialize(getActivity(), getString(R.string.app_key));
		if (resultCode == ResultCode.INVALID_APP) {
			Toast.makeText(getActivity(), "Invalid App Signature", Toast.LENGTH_LONG).show();
		}

		File strokeFile = new File(ARDrawingUtils.STROKE_FILE_NAME);
		if (!strokeFile.exists()) {
			Toast.makeText(getActivity(), "No stroke file exists", Toast.LENGTH_SHORT).show();
			return;
		}

		try {
			int fileSize = (int)strokeFile.length();
			byte [] strokeBytes = new byte[fileSize];
			FileInputStream fileInputStream = new FileInputStream(strokeFile);
			fileInputStream.read(strokeBytes);
			fileInputStream.close();

			String jsonString = new String(strokeBytes);

			Type touchPointType = new TypeToken<List<List<Point>>>() {}.getType();
			Gson gson = new Gson();
			List<List<Point>> touchPointList = gson.fromJson(jsonString, touchPointType);
			fingerPaintView.setTouchPointList(touchPointList);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		cameraController.start(0, preferCameraWidth, preferCameraHeight);
		renderHandler = new RenderHandler(this);
	}

	@Override
	public void onPause() {
		super.onPause();

		renderHandler.removeCallbacksAndMessages(null);
		renderHandler = null;
		cameraController.stop();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		ButterKnife.unbind(this);

		cameraController.stop();
		cameraController.setNewCameraFrameCallback(null);
		SurfaceManager.deinit();

		ARDrawing.clearTrackingTrackable();
		ARDrawing.stopTracking();
		ARDrawing.destroy();

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

		xScaleFactor = (float)(touchXMax - touchXMin) / (float)(imageXMax - imageXMin);
		yScaleFactor = (float)(touchYMax - touchYMin) / (float)(imageYMax - imageYMin);

		ARDrawingUtils.resizeView(getResources(), fingerPaintView, actualCameraWidth, actualCameraHeight);
	}

	@OnClick(R.id.start_tracker)
	public void startTracking() {
		if (!trackerAlive) {
			calcFactor();
			trackerAlive = true;
			imageIndex = 0;
			startTracker.setText("Stop tracker");

			byte[] trackable = ARDrawingUtils.readByteFromFile(ARDrawingUtils.TRACKABLE_FILE_NAME);

			ARDrawing.startTracking();
			ARDrawing.setRecognitionDelay(100);
			ARDrawing.setTrackingTrackableArray(trackable, trackable.length);
		} else {
			startTracker.setText("Start tracker");

			trackerAlive = false;
			ARDrawing.clearTrackingTrackable();
			ARDrawing.stopTracking();
		}
	}

	Bitmap bitmap;
	int [] intArrayForColor;
	IntBuffer intBufferForColor;
	private static final int DEBUG_CAPTURE_WIDTH = 1280;
	private static final int DEBUG_CAPTURE_HEIGHT = 720;

	@OnClick(R.id.capture_debug)
	public void captureDebug() {
		float [] transformMatrix3x3 = new float[9];
		int [] timeMillis = new int[1];
		int [] imageIndex = new int[1];
		byte [] resultImage = new byte[DEBUG_CAPTURE_WIDTH * DEBUG_CAPTURE_HEIGHT];

		ARDrawing.getTrackingResultWithImage(transformMatrix3x3, timeMillis, imageIndex, resultImage);

		if (bitmap == null) {
			bitmap = Bitmap.createBitmap(DEBUG_CAPTURE_WIDTH, DEBUG_CAPTURE_HEIGHT, Bitmap.Config.ARGB_8888);
		}

		if (intArrayForColor == null) {
			intArrayForColor = new int[DEBUG_CAPTURE_WIDTH * DEBUG_CAPTURE_HEIGHT * 4];
		}

		if (intBufferForColor == null) {
			intBufferForColor = IntBuffer.wrap(intArrayForColor);
		}

		intBufferForColor.rewind();

		ARDrawing.gray2rgba(resultImage, DEBUG_CAPTURE_WIDTH * DEBUG_CAPTURE_HEIGHT, intArrayForColor);

		bitmap.copyPixelsFromBuffer(intBufferForColor);

		debugCaptureView.setImageBitmap(bitmap);
		debugCaptureView.setVisibility(View.VISIBLE);
	}

	boolean needToResizeCameraSurfaceView = true;

	private NewCameraFrameCallback newCameraFrameCallback = new NewCameraFrameCallback() {

		@Override
		public void onNewCameraFrame(byte[] buffer, int length, final int width, final int height, int pixelFormat) {

			actualCameraWidth = width;
			actualCameraHeight = height;

			if (needToResizeCameraSurfaceView) {
				ARDrawingUtils.resizeView(getResources(), SurfaceManager.getInstance().getCameraSurfaceView(), width, height);
				needToResizeCameraSurfaceView = false;
			}

			if (trackerAlive) {
				int result = ARDrawing.inputTrackingImage(buffer, width, height, 2, imageIndex++);
				renderHandler.sendEmptyMessage(0);
			}
		}
	};

	private static class RenderHandler extends Handler {
		private WeakReference<CameraTestFragment> realTimeDrawingFragmentWeakReference;

		RenderHandler(CameraTestFragment fragment) {
			realTimeDrawingFragmentWeakReference = new WeakReference<>(fragment);
		}

		@Override
		public void handleMessage(Message msg) {
			CameraTestFragment fragment = realTimeDrawingFragmentWeakReference.get();
			if (fragment != null) {
				final float[] transformMatrix = new float[9];
				int[] millis = new int[1];
				final int[] idx = new int[1];
				final int isTracking = ARDrawing.getTrackingResult(transformMatrix, millis, idx);
				Log.d(TAG, "tracking time : " + millis[0] + ", index : " + idx[0]);

				if (isTracking == 0) {
					fragment.fingerPaintView.applyTrackingResult(transformMatrix, idx[0], fragment.xScaleFactor, fragment.yScaleFactor);
				} else {
					fragment.fingerPaintView.clearCanvas();
					fragment.fingerPaintView.postInvalidate();
				}
			}
		}
	}
}