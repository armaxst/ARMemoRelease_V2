/*
* Copyright 2017 Maxst, Inc. All Rights Reserved.
*/

package com.maxstar.ardrawing.app.fragment;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.maxstar.ardrawing.ARDrawing;
import com.maxstar.ardrawing.ARDrawingTest;
import com.maxstar.ardrawing.CameraFrame;
import com.maxstar.ardrawing.NewCameraFrameCallback;
import com.maxstar.ardrawing.PixelFormat;
import com.maxstar.ardrawing.ResultCode;
import com.maxstar.ardrawing.app.ARDrawingUtils;
import com.maxstar.ardrawing.app.FingerPaintView;
import com.maxstar.ardrawing.app.ImageFileReader;
import com.maxstar.ardrawing.app.LinearPointConverter;
import com.maxstar.ardrawing.app.R;
import com.maxstar.ardrawing.app.task.CheckImageLearnableTask;
import com.maxstar.ardrawing.app.task.ImageLearnTask;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ImageTestFragment extends ARDrawingFragment {

	private static final String TAG = ImageTestFragment.class.getSimpleName();

	@Bind(R.id.image_seq_view)
	ImageView imgSeqView;

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

	private ImageFileReader imageFileReader;

	float xScaleFactor;
	float yScaleFactor;
	Bitmap bitmap;

	public ImageTestFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_img_test, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		imageFileReader = new ImageFileReader(ARDrawingUtils.IMG_SEQUENCE_PATH + "/YUVFrame/", newCameraFrameCallback);

		ButterKnife.bind(this, view);

		engineVersion.setText(String.format(Locale.US, "%s : %s", "Engine Version", ARDrawing.getEngineVersion()));

		learnImage.setEnabled(false);
		startTracker.setEnabled(false);

		int resultCode = ARDrawing.initialize(getActivity(), getString(R.string.app_key));
		if (resultCode == ResultCode.INVALID_APP) {
			Toast.makeText(getActivity(), "Invalid App Signature", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		imageFileReader.startPlay();
	}

	@Override
	public void onPause() {
		super.onPause();

		imageFileReader.stopPlay();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		ButterKnife.unbind(this);

		ARDrawing.clearTrackingTrackable();
		ARDrawing.stopTracking();
		ARDrawing.destroy();

		imageFileReader.stopPlay();

		System.gc();
	}

	@Override
	public void imageCheckCompleted(CameraFrame newFrame) {
		if (newFrame != null) {
			imageFileReader.stopPlay();

			cameraFrameForLearn = newFrame;

			intBufferForColor.rewind();

			ARDrawing.gray2rgba(cameraFrame.imageBuffer, newFrame.width * newFrame.height, intArrayForColor);

			bitmap.copyPixelsFromBuffer(intBufferForColor);

			captureImageView.setImageBitmap(bitmap);
			captureImageView.setVisibility(View.VISIBLE);

			ARDrawingUtils.resizeView(getResources(), captureImageView, cameraFrameForLearn.width, cameraFrameForLearn.height);

			fingerPaintView.enableTouch(true);
			fingerPaintView.clearTouchPoint();
			fingerPaintView.setVisibility(View.VISIBLE);
			ARDrawingUtils.resizeView(getResources(), fingerPaintView, cameraFrameForLearn.width, cameraFrameForLearn.height);

			captureImage.setEnabled(false);
			learnImage.setEnabled(true);
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
		}

		captureImageView.setVisibility(View.GONE);
		fingerPaintView.clearCanvas();
		fingerPaintView.enableTouch(false);

		imageFileReader.startPlay();
	}

	@OnClick(R.id.capture_image)
	public void captureImage() {
		learnImage.setEnabled(false);
		startTracker.setEnabled(false);

		fingerPaintView.clearTouchPoint();
		fingerPaintView.clearCanvas();
		fingerPaintView.postInvalidate();

		new CheckImageLearnableTask(ImageTestFragment.this).execute();
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

		ARDrawing.clearLearnedTrackable();

		// Start thread for image learn
		new ImageLearnTask(ImageTestFragment.this).execute(cameraFrameForLearn);
	}

	@OnClick(R.id.start_tracker)
	public void startTracking() {
		if (!trackerAlive) {
			trackerAlive = true;
			imageIndex = 0;
			captureImage.setEnabled(false);
			startTracker.setText("Stop tracker");

			byte[] trackable = ARDrawingUtils.readByteFromFile(ARDrawingUtils.TRACKABLE_FILE_NAME);

			ARDrawingTest.unloadTrackerData();
			ARDrawingTest.loadTrackerData(trackable, trackable.length);
		} else {
			startTracker.setText("Start tracker");

			trackerAlive = false;
			ARDrawingTest.unloadTrackerData();

			captureImage.setEnabled(true);
		}
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


	int [] intArrayForColor;
	IntBuffer intBufferForColor;

	private NewCameraFrameCallback newCameraFrameCallback = new NewCameraFrameCallback() {

		@Override
		public void onNewCameraFrame(byte[] buffer, int length, final int width, final int height, int pixelFormat) {

			imageCaptureLock.lock();

			if (cameraFrame == null) {
				cameraFrame = new CameraFrame();
				cameraFrame.imageBuffer = buffer.clone();
				cameraFrame.length = length;
				cameraFrame.width = width;
				cameraFrame.height = height;
				cameraFrame.pixelFormat = PixelFormat.YUV;
			} else {
				System.arraycopy(buffer, 8, cameraFrame.imageBuffer, 0, length);
			}

			imageCaptureLock.unlock();

			if (bitmap == null) {
				bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			}

			if (intArrayForColor == null) {
				intArrayForColor = new int[length * 4];
			}

			if (intBufferForColor == null) {
				intBufferForColor = IntBuffer.wrap(intArrayForColor);
			}

			intBufferForColor.rewind();

			ARDrawing.gray2rgba(cameraFrame.imageBuffer, width * height, intArrayForColor);

			bitmap.copyPixelsFromBuffer(intBufferForColor);

			imgSeqView.setImageBitmap(bitmap);

			if (trackerAlive) {
				ARDrawing.inputTrackingImage(cameraFrame.imageBuffer, width, height, 2, imageIndex++);
				ARDrawingTest.trackImage();

				final float[] transformMatrix = new float[9];
				int[] millis = new int[1];
				final int[] idx = new int[1];
				final int isTracking = ARDrawing.getTrackingResult(transformMatrix, millis, idx);
				Log.d(TAG, "tracking time : " + millis[0] + ", index : " + idx[0]);

				if (isTracking == 0) {
					fingerPaintView.applyTrackingResult(transformMatrix, idx[0], xScaleFactor, yScaleFactor);
				} else {
					fingerPaintView.clearCanvas();
					fingerPaintView.postInvalidate();
				}
			}
		}
	};
}