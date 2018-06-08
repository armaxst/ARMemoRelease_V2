/*
 * Copyright (c) 2018. Maxst, Inc. All Rights Reserved.
 */

package com.maxst.armemo.app.cameracontroller;

public enum PixelFormat {
	Gray8(0),
	RGB888(1),
	YUV(2),
	RGBA32(3);

	private int value;

	private PixelFormat(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
