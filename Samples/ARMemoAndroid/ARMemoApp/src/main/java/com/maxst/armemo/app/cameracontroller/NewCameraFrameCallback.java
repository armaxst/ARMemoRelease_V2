/*
 * Copyright (c) 2018. Maxst, Inc. All Rights Reserved.
 */

package com.maxst.armemo.app.cameracontroller;

public interface NewCameraFrameCallback {
	void onNewCameraFrame(byte[] buffer, int length, int width, int height, int pixelFormat);
}
