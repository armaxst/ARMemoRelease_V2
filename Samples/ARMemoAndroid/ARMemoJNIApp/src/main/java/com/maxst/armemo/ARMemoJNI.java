/*
 * Copyright (c) 2018. Maxst, Inc. All Rights Reserved.
 */

package com.maxst.armemo;

import android.content.Context;
import android.renderscript.Matrix3f;

import com.maxst.ar.CameraDevice;
import com.maxst.ar.ColorFormat;
import com.maxst.ar.MaxstAR;
import com.maxst.ar.SensorDevice;
import com.maxst.ar.SurfaceThumbnail;
import com.maxst.ar.Trackable;
import com.maxst.ar.TrackedImage;
import com.maxst.ar.TrackerManager;
import com.maxst.ar.TrackingResult;
import com.maxst.ar.TrackingState;

import java.io.File;
import java.util.Locale;

public class ARMemoJNI {

	static {
		System.loadLibrary("ARMemoJNI");
	}
	/**
	 * Only for android app
	 */
	public static native int initialize(Context context, String appSignature);

	/**
	 * @brief Destroy engine.
	 */
	public static native int destroy();

	/**
	 * @brief start Engine thread.
	 */
	public static native int start();

	/**
	 * @brief stop Engine thread.
	 */
	public static native int stop();

	/*****************************************************************************/
	/*                             Learner function.                             */
	/*****************************************************************************/
	/**
	 * @param image       image data buffer
	 * @param length      image data buffer length(size)
	 * @param width       image width
	 * @param height      image height
	 * @param pixelFormat image pixel format
	 *                    pixelFormat = 0 : GRAY
	 *                    pixelFormat = 1 : RGB
	 *                    pixelFormat = 2 : YUV
	 * @return True when input image has less blur and fit complexity.\n
	 * False when input image has blur or less complexity.
	 * @brief check image good for learning.
	 */
	public static native int checkLearnable(byte[] image, int length, int width, int height, int pixelFormat);

	/**
	 * @param image       image data buffer
	 * @param length      image data buffer length(size)
	 * @param width       image width
	 * @param height      image height
	 * @param pixelFormat image pixel format
	 *                    pixelFormat = 0 : GRAY
	 *                    pixelFormat = 1 : RGB
	 *                    pixelFormat = 2 : YUV
	 * @param stroke      stroke data
	 * @param size        stroke data size
	 * @brief learn data using input image and stroke(center point).
	 */
	public static native int learn(byte[] image, int length, int width, int height, int pixelFormat, int[] stroke, int size);

	/**
	 * @param filePath save file path will located in, it must have ".armemo" extention.
	 * @brief save Learned data.
	 */
	public static native int saveLearnedFile(String filePath);

	/**
	 * @brief clear Learned data.
	 */
	public static native int clearLearnedTrackable();

	/*****************************************************************************/
	/*                             Tracker function.                             */
	/*****************************************************************************/
	/**
	 * @param image       image data buffer
	 * @param length      image data buffer length(size)
	 * @param width       image width
	 * @param height      image height
	 * @param pixelFormat image pixel format
	 *                    pixelFormat = 0 : GRAY
	 *                    pixelFormat = 1 : RGB
	 *                    pixelFormat = 2 : YUV
	 * @brief set image for tracking.
	 */
	public static native int inputTrackingImage(byte[] image, int length, int width, int height, int pixelFormat);

	/**
	 * @param filePath learned file path, it must have ".armemo" extention.
	 * @brief set trackable data(learned data) file for tracking.
	 */
	public static native int setTrackingFile(String filePath);

	/**
	 * @param transformMatrix3x3 Get result 3x3 transform matrix(image coordinate).
	 * @brief get tracking result.
	 */
	public static native int getTrackingResult(float[] transformMatrix3x3);

	/**
	 * @brief delete all of tracker's trackable(learned data).
	 */
	public static native int clearTrackingTrackable();
}

