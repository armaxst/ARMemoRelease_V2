/*
 * Copyright (c) 2018. Maxst, Inc. All Rights Reserved.
 */

package com.maxst.armemo.app.util;

import android.os.Handler;
import android.os.Message;

import com.maxst.armemo.app.cameracontroller.NewCameraFrameCallback;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ImageFileReader {

	private List<String> fileList = new ArrayList<>();
	private int fileIndex = 0;
	private ImageReadHandler imageReadHandler;
	private NewCameraFrameCallback newImageCallback;

	public ImageFileReader(String path, NewCameraFrameCallback callback) {
		newImageCallback = callback;
		File[] tempFiles = new File(path).listFiles();

		for (File file : tempFiles) {
			fileList.add(file.getAbsolutePath());
		}

		imageReadHandler = new ImageReadHandler(this);
	}

	public void startPlay() {
		fileIndex = 0;
		imageReadHandler.sendEmptyMessageDelayed(0, 10);
	}

	public void stopPlay() {
		fileIndex = 0;
		imageReadHandler.removeCallbacksAndMessages(null);
	}

	private static final class ImageReadHandler extends Handler {

		private WeakReference<ImageFileReader> imageSeqReaderWeakReference;

		ImageReadHandler(ImageFileReader reader) {
			imageSeqReaderWeakReference = new WeakReference<>(reader);
		}

		@Override
		public void handleMessage(Message msg) {
			ImageFileReader reader = imageSeqReaderWeakReference.get();
			if (reader == null) {
				return;
			}

			if (reader.fileList.size() == 0) {
				return;
			}

			if (reader.fileIndex >= reader.fileList.size()) {
				reader.fileIndex = 0;
			}

			String fileName = reader.fileList.get(reader.fileIndex++);
			byte [] yuvData = ARMemoUtils.readByteFromFile(fileName);

			// Read width (4bytes)
			int width = ARMemoUtils.byteArrayToInt(yuvData, 0);

			// Read height (4bytes)
			int height = ARMemoUtils.byteArrayToInt(yuvData, 4);

			reader.newImageCallback.onNewCameraFrame(yuvData, yuvData.length - 8, width, height, 2);

			sendEmptyMessage(0);
		}
	}
}
