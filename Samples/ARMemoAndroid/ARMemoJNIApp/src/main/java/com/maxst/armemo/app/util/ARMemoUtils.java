/*
 * Copyright (c) 2018. Maxst, Inc. All Rights Reserved.
 */

package com.maxst.armemo.app.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Build;
import android.os.Environment;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import junit.framework.Assert;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ARMemoUtils {

	private static final String TAG = ARMemoUtils.class.getSimpleName();

	public static final String PREF_NAME = "ARMemo";
	public static final String PREF_KEY_CAM_RESOLUTION = "cam_resolution";

	public static final int PREF_RESOLUTION_VALUE_SD = 0;
	public static final int PREF_RESOLUTION_VALUE_HD = 1;
	public static final int PREF_RESOLUTION_VALUE_FULL_HD = 2;

	public static final String ROOT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ARMemo/";
	public static final String TRACKABLE_FILE_NAME = ROOT_PATH + "Trackable.armemo";
	public static final String STROKE_FILE_NAME = ROOT_PATH + "Stroke.txt";
//	public static final String IMG_SEQUENCE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ARMemo/ImageSequence/";


	public static void resizeView(Resources resources, View view, int cameraWidth, int cameraHeight) {
		DisplayMetrics displayMetrics = resources.getDisplayMetrics();
		int screenWidth = displayMetrics.widthPixels;
		int screenHeight = displayMetrics.heightPixels;

//		Log.w(TAG, "resizieView screen W/H: " + screenWidth + ", " + screenHeight);

		float cameraRatio = (float) cameraHeight / cameraWidth;
		float screenRatio = (float) screenHeight / screenWidth;

		int viewWidth = screenWidth;
		int viewHeight = screenHeight;

		// Fat camera resolution than screen resolution
		if (cameraRatio > screenRatio) {
			viewHeight = (int) (viewHeight * cameraRatio / screenRatio);
		} else {
			viewWidth = (int) (viewWidth * screenRatio / cameraRatio);
		}

		ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
		int widthMargin = (screenWidth - viewWidth) / 2;
		int topMargin = (screenHeight - viewHeight) / 2;
		layoutParams.width = (int) (viewWidth * 1.0f);
		layoutParams.height = (int) (viewHeight * 1.0f);

		if (layoutParams instanceof RelativeLayout.LayoutParams) {
			((RelativeLayout.LayoutParams) layoutParams).leftMargin = widthMargin;
			((RelativeLayout.LayoutParams) layoutParams).topMargin = topMargin;
			((RelativeLayout.LayoutParams) layoutParams).rightMargin = widthMargin;
			((RelativeLayout.LayoutParams) layoutParams).bottomMargin = topMargin;
		} else if (layoutParams instanceof FrameLayout.LayoutParams) {
			((FrameLayout.LayoutParams) layoutParams).leftMargin = widthMargin;
			((FrameLayout.LayoutParams) layoutParams).topMargin = topMargin;
			((FrameLayout.LayoutParams) layoutParams).rightMargin = widthMargin;
			((FrameLayout.LayoutParams) layoutParams).bottomMargin = topMargin;
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

	public static void saveByteToFile(byte[] input, String dstFile) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(new File(dstFile));
			fos.write(input, 0, input.length);
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void writeYuvToFile(byte[] input, int width, int height, String dstFile) {
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
		return (bytes[start + 3] & 0xFF) |
				(bytes[start + 2] & 0xFF) << 8 |
				(bytes[start + 1] & 0xFF) << 16 |
				(bytes[start] & 0xFF) << 24;
	}

	public static Bitmap yuvToBitmap(Context context, byte[] yuvFrameBuffer, int width, int height) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			RenderScript rs = RenderScript.create(context);

			ScriptIntrinsicYuvToRGB yuvToRgb;
			yuvToRgb = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));

			Type.Builder yuvType = new Type.Builder(rs, Element.U8(rs)).setX(yuvFrameBuffer.length);
			Allocation in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);

			Type.Builder rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(width).setY(height);
			Allocation out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);

			Bitmap imageBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			in.copyFrom(yuvFrameBuffer);
			yuvToRgb.setInput(in);
			yuvToRgb.forEach(out);
			out.copyTo(imageBitmap);

			in.destroy();
			out.destroy();
			yuvToRgb.destroy();
			rs.destroy();
			return imageBitmap;

		} else {
			YuvImage yuvimage = new YuvImage(yuvFrameBuffer, ImageFormat.NV21, width, height, null);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			yuvimage.compressToJpeg(new Rect(0, 0, width, height), 60, bos);
			byte[] jpegArray = bos.toByteArray();

			return BitmapFactory.decodeByteArray(jpegArray, 0, jpegArray.length);
		}
	}
}
