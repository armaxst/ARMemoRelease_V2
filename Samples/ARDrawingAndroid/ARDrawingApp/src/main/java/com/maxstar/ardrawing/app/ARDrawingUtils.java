/*
 * Copyright 2017 Maxst, Inc. All Rights Reserved.
 */

package com.maxstar.ardrawing.app;

import android.content.res.Resources;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import junit.framework.Assert;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ARDrawingUtils {

	private static final String TAG = ARDrawingUtils.class.getSimpleName();

	public static final String PREF_NAME = "ARDrawing";
	public static final String PREF_KEY_LEARN_CAM_RESOLUTION = "learn_cam_resolution";
	public static final String PREF_KEY_TRACKING_CAM_RESOLUTION = "tracking_cam_resolution";

	public static final String ROOT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ARDrawing/";
	public static final String TRACKABLE_FILE_NAME = ROOT_PATH + "Trackable.trk";
	public static final String STROKE_FILE_NAME = ROOT_PATH + "Stroke.txt";
	public static final String IMG_SEQUENCE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ARDrawing/ImageSequence/";

	public static void resizeView(Resources resources, View view, int cameraWidth, int cameraHeight) {
		DisplayMetrics displayMetrics = resources.getDisplayMetrics();
		int screenWidth = displayMetrics.widthPixels;
		int screenHeight = displayMetrics.heightPixels;

		float cameraRatio = (float)cameraHeight / cameraWidth;
		float screenRatio = (float)screenHeight / screenWidth;

		int viewWidth = screenWidth;
		int viewHeight = screenHeight;

		// Fat camera resolution than screen resolution
		if (cameraRatio > screenRatio) {
			viewHeight = (int) (viewHeight * cameraRatio / screenRatio);
		} else {
			viewWidth = (int)(viewWidth * screenRatio / cameraRatio);
		}

		ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
		int widthMargin = (screenWidth - viewWidth) / 2;
		int topMargin = (screenHeight - viewHeight) / 2;
		layoutParams.width = (int)(viewWidth * 1.0f);
		layoutParams.height = (int)(viewHeight * 1.0f);

		if (layoutParams instanceof RelativeLayout.LayoutParams) {
			((RelativeLayout.LayoutParams)layoutParams).leftMargin = widthMargin;
			((RelativeLayout.LayoutParams)layoutParams).topMargin = topMargin;
			((RelativeLayout.LayoutParams)layoutParams).rightMargin = widthMargin;
			((RelativeLayout.LayoutParams)layoutParams).bottomMargin = topMargin;
		} else if (layoutParams instanceof FrameLayout.LayoutParams) {
			((FrameLayout.LayoutParams)layoutParams).leftMargin = widthMargin;
			((FrameLayout.LayoutParams)layoutParams).topMargin = topMargin;
			((FrameLayout.LayoutParams)layoutParams).rightMargin = widthMargin;
			((FrameLayout.LayoutParams)layoutParams).bottomMargin = topMargin;
		}
		view.setLayoutParams(layoutParams);
	}

	public static byte[] readByteFromFile(String srcFile) {
		File file = new File(srcFile);
		int size = (int) file.length();
		byte[] bytes = new byte[size];
		try {
			BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
			buf.read(bytes, 0, bytes.length);
			buf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bytes;
	}

	public static void saveByteToFile(byte [] input, String dstFile) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(new File(dstFile));
			fos.write(input, 0, input.length);
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void writeYuvToFile(byte [] input, int width, int height, String dstFile) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(new File(dstFile));
			fos.write(intToByteArray(width), 0, 4);
			fos.write(intToByteArray(height), 0, 4);
			fos.write(input, 0, input.length);
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static byte[] intToByteArray(int i) {
		byte[] result = new byte[4];

		result[0] = (byte) ((i & 0xFF000000) >> 24);
		result[1] = (byte) ((i & 0x00FF0000) >> 16);
		result[2] = (byte) ((i & 0x0000FF00) >> 8);
		result[3] = (byte) ((i & 0x000000FF));

		return result;
	}

	public static int byteArrayToInt(byte[] bytes, int start) {
		Assert.assertTrue("Byte buffer length is not 4", (bytes.length - start) >= 4);
		return	(bytes[start + 3] & 0xFF) |
				(bytes[start + 2] & 0xFF) << 8 |
				(bytes[start + 1] & 0xFF) << 16 |
				(bytes[start] & 0xFF) << 24;
	}
}
