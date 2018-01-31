/*
* Copyright 2017 Maxst, Inc. All Rights Reserved.
*/

package com.maxstar.ardrawing.app.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.maxstar.ardrawing.ARDrawing;
import com.maxstar.ardrawing.CameraController;
import com.maxstar.ardrawing.NewCameraFrameCallback;
import com.maxstar.ardrawing.ResultCode;
import com.maxstar.ardrawing.SurfaceManager;
import com.maxstar.ardrawing.app.ARDrawingUtils;
import com.maxstar.ardrawing.app.R;

import java.io.File;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SaveImageFragment extends Fragment {

	private static final String TAG = SaveImageFragment.class.getSimpleName();

	@Bind(R.id.save_image)
	Button saveImage;

	@Bind(R.id.stop_save)
	Button stopSave;

	@Bind(R.id.folder_name)
	EditText folderNameEdit;

	String imageFolder = null;

	CameraController cameraController;

	public SaveImageFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_save_image, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ButterKnife.bind(this, view);

		File dir = new File(ARDrawingUtils.IMG_SEQUENCE_PATH);
		if (!dir.exists()) {
			boolean result = dir.mkdirs();
			Log.i(TAG, "Make directory result : " + result);
		}

		SurfaceManager.init();

		cameraController = SurfaceManager.getInstance().getCameraController();
		cameraController.setNewCameraFrameCallback(newCameraFrameCallback);

		int resultCode = ARDrawing.initialize(getActivity(), getString(R.string.app_key));
		if (resultCode == ResultCode.INVALID_APP) {
			Toast.makeText(getActivity(), "Invalid App Signature", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		int resolution =
				getActivity().getSharedPreferences(ARDrawingUtils.PREF_NAME, Activity.MODE_PRIVATE).getInt(ARDrawingUtils.PREF_KEY_LEARN_CAM_RESOLUTION, 1);
		switch (resolution) {
			case 0:
				cameraController.start(0, 640, 480);
				break;

			case 1:
				cameraController.start(0, 1280, 720);
				break;

			case 2:
				cameraController.start(0, 1920, 1080);
				break;
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		cameraController.stop();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		ButterKnife.unbind(this);

		cameraController.stop();
		cameraController.setNewCameraFrameCallback(null);
		SurfaceManager.deinit();

		System.gc();
	}

	private int frameCount = 0;

	@OnClick(R.id.save_image)
	public void saveImage() {
		imageFolder = ARDrawingUtils.IMG_SEQUENCE_PATH + "/" + folderNameEdit.getText().toString();
		File file = new File(imageFolder);
		if (!file.exists()) {
			boolean result = file.mkdirs();
			Log.i(TAG, "Make Directory Result : " + result);
		}

		File [] oldFiles = new File(imageFolder).listFiles();
		if (oldFiles != null) {
			for (File f : oldFiles) {
				boolean result = f.delete();
				Log.d(TAG, "Delete file result : " + result);
			}
		}

		frameCount = 0;

		saveImage.setEnabled(false);
		stopSave.setEnabled(true);
	}

	@OnClick(R.id.stop_save)
	public void stopSave() {
		saveImage.setEnabled(true);
		stopSave.setEnabled(false);
	}

	boolean needToResizeCameraSurfaceView = true;

	private NewCameraFrameCallback newCameraFrameCallback = new NewCameraFrameCallback() {

		@Override
		public void onNewCameraFrame(byte[] buffer, int length, final int width, final int height, int pixelFormat) {

			if (needToResizeCameraSurfaceView) {
				ARDrawingUtils.resizeView(getResources(), SurfaceManager.getInstance().getCameraSurfaceView(), width, height);
				needToResizeCameraSurfaceView = false;
			}

			if (stopSave.isEnabled()) {
				String fileName = String.format(Locale.US, "%04d.yuv", frameCount++);
				String fullPath = imageFolder + "/" + fileName;

				ARDrawingUtils.writeYuvToFile(buffer, width, height, fullPath);
			}
		}

	};
}