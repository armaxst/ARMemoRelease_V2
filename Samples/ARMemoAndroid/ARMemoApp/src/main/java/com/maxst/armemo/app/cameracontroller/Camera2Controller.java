/*
 * Copyright (c) 2018. Maxst, Inc. All Rights Reserved.
 */

package com.maxst.armemo.app.cameracontroller;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;

@TargetApi(Build.VERSION_CODES.M)
class Camera2Controller extends CameraController implements SurfaceHolder.Callback {
	private static final String TAG = Camera2Controller.class.getSimpleName();

	private CameraSize cameraSize = new CameraSize(0, 0);
	private SurfaceHolder surfaceHolder;
	private boolean keepAlive = true;
	private int cameraId;
	private int preferWidth = 640;
	private int preferHeight = 480;
	private static final int REQUEST_CAMERA_PERMISSION = 1;

	private HandlerThread backgroundThread;
	private Handler backgroundHandler;

	private ImageReader previewReader;

	private Semaphore cameraOpenCloseLock = new Semaphore(1);
	private CameraDevice cameraDevice;
	private CaptureRequest.Builder previewRequestBuilder;
	private CameraCaptureSession captureSession;
	private CaptureRequest previewRequest;

	private void startInternal() {
		startBackgroundThread();

		try {
			CameraManager manager = (CameraManager) SystemUtil.getActivity().getSystemService(Context.CAMERA_SERVICE);
			String strCameraId = manager.getCameraIdList()[cameraId];
			CameraCharacteristics characteristics = manager.getCameraCharacteristics(strCameraId);
			StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
			Size[] sizes = map.getOutputSizes(SurfaceHolder.class);

			ArrayList<CameraSize> cameraSizes = new ArrayList<>();
			for (Size size : sizes) {
				cameraSizes.add(new CameraSize(size.getWidth(), size.getHeight()));
			}

			cameraSize = getOptimalPreviewSize(cameraSizes, preferWidth, preferHeight);

			previewReader = ImageReader.newInstance(cameraSize.width, cameraSize.height, ImageFormat.YUV_420_888, 2);
			previewReader.setOnImageAvailableListener(mOnPreviewFrameListener, null);
			if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
				throw new RuntimeException("Time out waiting to lock camera opening.");
			}
			manager.openCamera(strCameraId, mStateCallback, backgroundHandler);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
		}
	}

	private int testCount = 0;

	@Override
	public void start(final int cameraId, final int width, final int height) {

		SystemUtil.getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Camera2Controller.this.cameraId = cameraId;
				Camera2Controller.this.preferWidth = width;
				Camera2Controller.this.preferHeight = height;

				if (backgroundThread != null) {
					return;
				}

				keepAlive = true;
				surfaceManager.createSurface();
				cameraSurfaceView.getHolder().addCallback(Camera2Controller.this);

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
				if (backgroundThread == null) {
					return;
				}

				keepAlive = false;
				closeCamera();
				stopBackgroundThread();
				surfaceManager.destroySurface();
				cameraSize.width = 0;
				cameraSize.height = 0;
			}
		});
	}

	private void requestCameraPermission() {
		if (ActivityCompat.shouldShowRequestPermissionRationale(SystemUtil.getActivity(), Manifest.permission.CAMERA)) {
			new AlertDialog.Builder(SystemUtil.getActivity()).setTitle("required permission")
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							ActivityCompat.requestPermissions(SystemUtil.getActivity(),
									new String[]{Manifest.permission.CAMERA},
									REQUEST_CAMERA_PERMISSION);
						}
					})
					.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							SystemUtil.getActivity().finish();
						}
					})
					.create()
					.show();
		} else {
			ActivityCompat.requestPermissions(SystemUtil.getActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
		}
	}

	private byte[] yuvBuffer = null;
	private static final int MAX_FRAME_COUNT = 300;
	private int frameCount = 0;
	int colorFormat = 4;

	private final ImageReader.OnImageAvailableListener mOnPreviewFrameListener = new ImageReader.OnImageAvailableListener() {
		@Override
		public void onImageAvailable(final ImageReader reader) {
			Image acquiredImage = reader.acquireLatestImage();
			if (acquiredImage == null) {
				return;
			}

			Assert.assertTrue("Image format is not ImageFormat.YUV_420_888", acquiredImage.getFormat() == ImageFormat.YUV_420_888);

			int width = acquiredImage.getWidth();
			int height = acquiredImage.getHeight();

			Image.Plane yPlane = acquiredImage.getPlanes()[0];
			int ySize = yPlane.getBuffer().remaining();

			Image.Plane uPlane = acquiredImage.getPlanes()[1];
			Image.Plane vPlane = acquiredImage.getPlanes()[2];

			// be aware that this size does not include the padding at the end, if there is any
			// (e.g. if pixel stride is 2 the size is ySize / 2 - 1)
			int uSize = uPlane.getBuffer().remaining();
			int vSize = vPlane.getBuffer().remaining();

			int margin = 0;
			if (ySize / 2 - vSize > 0) {
				margin = ySize / 2 - vSize;
			}

			if (yuvBuffer == null || yuvBuffer.length < ySize + uSize + vSize) {
				yuvBuffer = new byte[ySize + uSize + vSize + 100]; // Preventing overflow. 100 has no meaning.
			}

			yPlane.getBuffer().get(yuvBuffer, 0, ySize);

			int uvPixelStride = uPlane.getPixelStride(); //stride guaranteed to be the same for u and v planes
			if (uvPixelStride == 1) {
				uPlane.getBuffer().get(yuvBuffer, ySize, uSize);
				vPlane.getBuffer().get(yuvBuffer, ySize + uSize, vSize);
				colorFormat = 3;
			} else {
				vPlane.getBuffer().get(yuvBuffer, ySize, vSize);
				uPlane.getBuffer().get(yuvBuffer, ySize + vSize + margin, uSize);
				colorFormat = 4;

				// Below code consume cpu very high!!
//				vPlane.getBuffer().get(yuvBuffer, ySize, vSize);
//				for (int i = 0; i < uSize; i += 2) {
//					yuvBuffer[ySize + i + 1] = uPlane.getBuffer().get(i);
//				}
			}

			acquiredImage.close();

			if (keepAlive) {
				if (newCameraFrameCallback != null) {
					newCameraFrameCallback.onNewCameraFrame(yuvBuffer, yuvBuffer.length, width, height, colorFormat);
				}
//				MaxstAR.setNewCameraFrame(yuvBuffer, yuvBuffer.length, width, height, colorFormat);
//				if (frameCount < MAX_FRAME_COUNT) {
//					String fileName = String.format(Locale.US, "%s/%dx%d_%04d.yuv", Environment.getExternalStorageDirectory(), width, height,
//							frameCount);
//					FileUtil.writeYuvBytesToFile(yuvBuffer, width, height, fileName);
//					frameCount++;
//				}
				surfaceManager.requestRender();
			}
		}
	};

	private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

		@Override
		public void onOpened(@NonNull CameraDevice cameraDevice) {
			cameraOpenCloseLock.release();
			Camera2Controller.this.cameraDevice = cameraDevice;
			createCameraPreviewSession();
		}

		@Override
		public void onDisconnected(@NonNull CameraDevice cameraDevice) {
			cameraOpenCloseLock.release();
			cameraDevice.close();
			Camera2Controller.this.cameraDevice = null;
		}

		@Override
		public void onError(@NonNull CameraDevice cameraDevice, int error) {
			cameraOpenCloseLock.release();
			cameraDevice.close();
			Camera2Controller.this.cameraDevice = null;
		}
	};

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void createCameraPreviewSession() {
		try {
			Surface surface = surfaceHolder.getSurface();
			previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
			previewRequestBuilder.addTarget(surface);
			previewRequestBuilder.addTarget(previewReader.getSurface());
			cameraDevice.createCaptureSession(asList(surface, previewReader.getSurface()), new CameraCaptureSession.StateCallback() {
						@Override
						public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
							if (null == cameraDevice) {
								return;
							}
							captureSession = cameraCaptureSession;
							//previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
							previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO);
							previewRequestBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CaptureRequest.CONTROL_SCENE_MODE_SPORTS);
							previewRequest = previewRequestBuilder.build();
							try {
								captureSession.setRepeatingRequest(previewRequest, null, backgroundHandler);
							} catch (CameraAccessException e) {
								e.printStackTrace();
							}
						}

						@Override
						public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
						}
					}, null
			);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}

	private void closeCamera() {
		try {
			cameraOpenCloseLock.acquire();
			if (null != captureSession) {
				captureSession.close();
				captureSession = null;
			}
			if (null != cameraDevice) {
				cameraDevice.close();
				cameraDevice = null;
			}
			if (null != previewReader) {
				previewReader.close();
				previewReader = null;
			}
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
		} finally {
			cameraOpenCloseLock.release();
		}
	}

	private void startBackgroundThread() {
		backgroundThread = new HandlerThread("CameraBackground");
		backgroundThread.start();
		backgroundHandler = new Handler(backgroundThread.getLooper());
	}

	private void stopBackgroundThread() {
		backgroundThread.quitSafely();
		try {
			backgroundThread.join();
			backgroundThread = null;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public int getWidth() {
		return cameraSize.width;
	}

	public int getHeight() {
		return cameraSize.height;
	}

	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {
		this.surfaceHolder = surfaceHolder;
		if (backgroundThread == null) {
			startInternal();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
		this.surfaceHolder = null;
		Log.d(TAG, "surfaceDestroyed");
	}
}
