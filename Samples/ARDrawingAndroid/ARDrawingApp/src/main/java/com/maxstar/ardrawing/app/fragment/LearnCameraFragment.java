/*
* Copyright 2017 Maxst, Inc. All Rights Reserved.
*/

package com.maxstar.ardrawing.app.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.maxstar.ardrawing.ARDrawing;
import com.maxstar.ardrawing.CameraController;
import com.maxstar.ardrawing.CameraFrame;
import com.maxstar.ardrawing.NewCameraFrameCallback;
import com.maxstar.ardrawing.PixelFormat;
import com.maxstar.ardrawing.ResultCode;
import com.maxstar.ardrawing.SurfaceManager;
import com.maxstar.ardrawing.app.ARDrawingUtils;
import com.maxstar.ardrawing.app.FingerPaintView;
import com.maxstar.ardrawing.app.LinearPointConverter;
import com.maxstar.ardrawing.app.R;
import com.maxstar.ardrawing.app.task.CheckImageLearnableTask;
import com.maxstar.ardrawing.app.task.ImageLearnTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LearnCameraFragment extends ARDrawingFragment {

	private static final String TAG = LearnCameraFragment.class.getSimpleName();

	@Bind(R.id.camera_resolution)
	TextView cameraResolution;

	@Bind(R.id.capture_image_view)
	ImageView captureImageView;

	@Bind(R.id.ar_drawing_view)
	FingerPaintView fingerPaintView;

	@Bind(R.id.capture_image)
	Button captureImage;

	@Bind(R.id.learn_image)
	Button learnImage;

	@Bind(R.id.start_tracker)
	Button startTracker;

	@Bind(R.id.engine_version)
	TextView engineVersion;

	@Bind(R.id.debug_capture_view)
	ImageView debugCaptureView;

	private RenderHandler renderHandler;
	private int cameraWidth = 0;
	private int cameraHeight = 0;

	float xScaleFactor;
	float yScaleFactor;

	private CameraController cameraController;

	public LearnCameraFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_learn_camera, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ButterKnife.bind(this, view);

		engineVersion.setText(String.format(Locale.US, "%s : %s", "Engine Version", ARDrawing.getEngineVersion()));

		learnImage.setEnabled(false);
		startTracker.setEnabled(false);

		SurfaceManager.init();
		cameraController = SurfaceManager.getInstance().getCameraController();
		cameraController.setNewCameraFrameCallback(newCameraFrameCallback);

		int resolution =
				getActivity().getSharedPreferences(ARDrawingUtils.PREF_NAME, Activity.MODE_PRIVATE).getInt(ARDrawingUtils.PREF_KEY_LEARN_CAM_RESOLUTION, 1);
		switch (resolution) {
			case 0:
				cameraWidth = 640;
				cameraHeight = 480;
				break;

			case 1:
				cameraWidth = 1280;
				cameraHeight = 720;
				break;

			case 2:
				cameraWidth = 1920;
				cameraHeight = 1080;
				break;
		}

		cameraResolution.setText(String.format(Locale.US, "Camera resolution %dx%d", cameraWidth, cameraHeight));

		int resultCode = ARDrawing.initialize(getActivity(), getString(R.string.app_key));
		if (resultCode == ResultCode.INVALID_APP) {
			Toast.makeText(getActivity(), "Invalid App Signature", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		cameraController.start(0, cameraWidth, cameraHeight);
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

	@Override
	public void imageCheckCompleted(CameraFrame newFrame) {
		if (newFrame != null) {
			cameraController.stop();

			cameraFrameForLearn = newFrame;
			final YuvImage yuvImage = new YuvImage(newFrame.imageBuffer, ImageFormat.NV21, newFrame.width, newFrame.height, null);

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			yuvImage.compressToJpeg(new Rect(0, 0, newFrame.width, newFrame.height), 100, out);
			byte[] imageBytes = out.toByteArray();
			Bitmap image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
			captureImageView.setImageBitmap(image);
			captureImageView.setVisibility(View.VISIBLE);

			ARDrawingUtils.resizeView(getResources(), captureImageView, cameraFrameForLearn.width, cameraFrameForLearn.height);

			fingerPaintView.enableTouch(true);
			fingerPaintView.clearTouchPoint();
			fingerPaintView.setVisibility(View.VISIBLE);
			ARDrawingUtils.resizeView(getResources(), fingerPaintView, cameraFrameForLearn.width, cameraFrameForLearn.height);

			captureImage.setEnabled(false);
			learnImage.setEnabled(true);
		} else {
			Toast.makeText(getContext(), "Image is not good for tracking", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void learningCompleted(int result) {
		if (result == ResultCode.SUCCESS) {
			Log.i(TAG, "Learning success!");
			captureImage.setEnabled(false);
			learnImage.setEnabled(false);
			startTracker.setEnabled(true);
		}else{
			captureImage.setEnabled(true);
			learnImage.setEnabled(false);
			startTracker.setEnabled(false);
			Toast.makeText(getContext(), "Fail to learn image", Toast.LENGTH_SHORT).show();
		}

		captureImageView.setVisibility(View.GONE);
		fingerPaintView.clearCanvas();
		fingerPaintView.enableTouch(false);

		cameraController.start(0, cameraWidth, cameraHeight);
		needToResizeCameraSurfaceView = true;
	}

	@OnClick(R.id.capture_image)
	public void captureImage() {
		learnImage.setEnabled(false);
		startTracker.setEnabled(false);

		fingerPaintView.clearTouchPoint();
		fingerPaintView.clearCanvas();
		fingerPaintView.postInvalidate();
		debugCaptureView.setVisibility(View.GONE);

		new CheckImageLearnableTask(LearnCameraFragment.this).execute();
	}

	@OnClick(R.id.learn_image)
	public void learnImage() {
		learnImage.setEnabled(false);
		captureImage.setEnabled(false);

		List<List<Point>> touchPointList = fingerPaintView.getTouchPointList();

		int touchXMin = 0;
		int touchXMax = captureImageView.getWidth();

		int touchYMin = 0;
		int touchYMax = captureImageView.getHeight();

		int imageXMin = 0;
		int imageXMax = cameraFrameForLearn.width;

		int imageYMin = 0;
		int imageYMax = cameraFrameForLearn.height;

		xScaleFactor = (float)(touchXMax - touchXMin) / (float)(imageXMax - imageXMin);
		yScaleFactor = (float)(touchYMax - touchYMin) / (float)(imageYMax - imageYMin);

		LinearPointConverter linearXPointConverter = new LinearPointConverter(touchXMin, touchXMax, imageXMin, imageXMax);
		LinearPointConverter linearYPointConverter = new LinearPointConverter(touchYMin, touchYMax, imageYMin, imageYMax);

		// Convert touch point to image point
		List<Point> imagePointList = new ArrayList<>();
		for (List<Point> points : touchPointList) {
			for (Point point : points) {
				int newX = (int) linearXPointConverter.getConvertedValue(point.x);
				int newY = (int) linearYPointConverter.getConvertedValue(point.y);
				imagePointList.add(new Point(newX, newY));
			}
		}

		// Convert point list to simple array
		touchStroke = new int[imagePointList.size() * 2];
		for (int i = 0; i < imagePointList.size(); i++) {
			touchStroke[i * 2] = (int) imagePointList.get(i).x;
			touchStroke[i * 2 + 1] = (int) imagePointList.get(i).y;
		}

		List<List<Point>> touchPointListTemp = new ArrayList<>();
		touchPointListTemp.addAll(touchPointList);
		for (List<Point> pointList : touchPointListTemp) {
			for (Point point : pointList) {
				point.x = (int)(point.x / xScaleFactor);
				point.y = (int)(point.y / yScaleFactor);
			}
		}

		Gson gson = new Gson();
		String jsonString = gson.toJson(touchPointListTemp);
		// TODO : Test 

		File rootDir = new File(ARDrawingUtils.ROOT_PATH);
		if(!rootDir.exists()){
			boolean mkDirResult = rootDir.mkdirs();
			Log.d(TAG, "Make directory result : " + mkDirResult);
		}

		File strokeFile = new File(ARDrawingUtils.STROKE_FILE_NAME);
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

		// Start thread for image learn
		new ImageLearnTask(LearnCameraFragment.this).execute(cameraFrameForLearn);
	}

	@OnClick(R.id.start_tracker)
	public void startTracking() {
		if (!trackerAlive) {
			trackerAlive = true;
			imageIndex = 0;
			captureImage.setEnabled(false);
			startTracker.setText("Stop tracker");
			ARDrawing.startTracking();

			ARDrawing.setRecognitionDelay(100);

			byte[] trackable = ARDrawingUtils.readByteFromFile(ARDrawingUtils.TRACKABLE_FILE_NAME);
			ARDrawing.setTrackingTrackableArray(trackable, trackable.length);
		} else {
			startTracker.setText("Start tracker");

			trackerAlive = false;
			ARDrawing.clearTrackingTrackable();
			ARDrawing.stopTracking();

			captureImage.setEnabled(true);
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
				ARDrawingUtils.resizeView(getResources(), SurfaceManager.getInstance().getCameraSurfaceView(), width, height);
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

			if (trackerAlive) {
				int result = ARDrawing.inputTrackingImage(cameraFrame.imageBuffer, width, height, 2, imageIndex++);
//				int result = ARDrawing.inputTrackingImage(null, width, height, 2, imageIndex++);
				renderHandler.sendEmptyMessage(0);
			}
		}
	};

	private static class RenderHandler extends Handler {
		private WeakReference<LearnCameraFragment> realTimeDrawingFragmentWeakReference;

		RenderHandler(LearnCameraFragment fragment) {
			realTimeDrawingFragmentWeakReference = new WeakReference<>(fragment);
		}

		@Override
		public void handleMessage(Message msg) {
			LearnCameraFragment fragment = realTimeDrawingFragmentWeakReference.get();
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