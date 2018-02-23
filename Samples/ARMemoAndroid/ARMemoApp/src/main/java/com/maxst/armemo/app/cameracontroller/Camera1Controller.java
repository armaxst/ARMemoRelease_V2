/*
 * Copyright (c) 2018. Maxst, Inc. All Rights Reserved.
 */

package com.maxst.armemo.app.cameracontroller;

import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
class Camera1Controller extends CameraController implements PreviewCallback, SurfaceHolder.Callback {
	private static final String TAG = Camera1Controller.class.getSimpleName();

	private Camera camera;
	private CameraSize cameraSize = new CameraSize(0, 0);
	private SurfaceHolder surfaceHolder;
	private boolean keepAlive = true;
	private int cameraId;
	private int preferredWidth = 640;
	private int preferredHeight = 480;

	private void startInternal() {
		camera = Camera.open(cameraId);

		Camera.Parameters params = camera.getParameters();
		List<String> focusModes = params.getSupportedFocusModes();

		if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
			params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
			Log.i(TAG, "FOCUS_MODE_CONTINUOUS_VIDEO");
		} else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
			params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
			Log.i(TAG, "FOCUS_MODE_AUTO");
		}

//		List<String> sceneModes = params.getSupportedSceneModes();
//		if(sceneModes.contains(Camera.Parameters.SCENE_MODE_SPORTS)) {
//			params.setSceneMode(Camera.Parameters.SCENE_MODE_SPORTS);
//			Log.i(TAG, "SCENE_MODE_SPORTS");
//		} else if(sceneModes.contains(Camera.Parameters.SCENE_MODE_ACTION)) {
//			params.setSceneMode(Camera.Parameters.SCENE_MODE_ACTION);
//			Log.i(TAG, "SCENE_MODE_ACTION");
//		} else if(sceneModes.contains(Camera.Parameters.SCENE_MODE_AUTO)) {
//			params.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
//			Log.i(TAG, "SCENE_MODE_AUTO");
//		}

		List<Size> cameraPreviewList = params.getSupportedPreviewSizes();
		ArrayList<CameraSize> cameraSizes = new ArrayList<>();
		for (Size size : cameraPreviewList) {
			cameraSizes.add(new CameraSize(size.width, size.height));
		}

		cameraSize = getOptimalPreviewSize(cameraSizes, preferredWidth, preferredHeight);

		params.setPreviewSize(cameraSize.width, cameraSize.height);
		params.setPreviewFormat(ImageFormat.NV21);

		PixelFormat p = new PixelFormat();
		PixelFormat.getPixelFormatInfo(params.getPreviewFormat(), p);
		int bufSize = (cameraSize.width * cameraSize.height * p.bitsPerPixel) / 8 * 2;

		byte[] buffer = new byte[bufSize];
		camera.addCallbackBuffer(buffer);
		buffer = new byte[bufSize];
		camera.addCallbackBuffer(buffer);
		buffer = new byte[bufSize];
		camera.addCallbackBuffer(buffer);
		buffer = new byte[bufSize];
		camera.addCallbackBuffer(buffer);

		camera.setPreviewCallbackWithBuffer(this);

		camera.setParameters(params);

		keepAlive = true;

		try {
			camera.setPreviewDisplay(cameraSurfaceView.getHolder());
			camera.startPreview();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void start(final int cameraId, final int width, final int height) {

		SystemUtil.getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Camera1Controller.this.cameraId = cameraId;
				Camera1Controller.this.preferredWidth = width;
				Camera1Controller.this.preferredHeight = height;

				if (camera != null) {
					return;
				}

				keepAlive = true;
				surfaceManager.createSurface();
				cameraSurfaceView.getHolder().addCallback(Camera1Controller.this);

				if (surfaceHolder != null) {
					startInternal();
				}
			}
		});
	}

	@Override
	public void stop() {

		SystemUtil.getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (camera == null) {
					return;
				}

				keepAlive = false;

				camera.stopPreview();
				camera.setPreviewCallbackWithBuffer(null);
				camera.release();
				camera = null;
				surfaceManager.destroySurface();

				cameraSize.width = 0;
				cameraSize.height = 0;
			}
		});
	}

	public int getWidth() {
		return cameraSize.width;
	}

	public int getHeight() {
		return cameraSize.height;
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		if (keepAlive) {
			if (newCameraFrameCallback != null) {
				newCameraFrameCallback.onNewCameraFrame(data, data.length, cameraSize.width, cameraSize.height, 2);
			}
			//MaxstAR.setNewCameraFrame(data, data.length, cameraSize.width, cameraSize.height, 2);
			//surfaceManager.requestRender();
			camera.addCallbackBuffer(data);
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {
		this.surfaceHolder = surfaceHolder;
		if (camera == null) {
			startInternal();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
		this.surfaceHolder = null;
	}
}
