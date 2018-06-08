/*
 * Copyright (c) 2018. Maxst, Inc. All Rights Reserved.
 */

package com.maxst.armemo;

public class ResultCode {
	public static final int SUCCESS = 0;
	public static final int FAIL = 1;

	public static final int MEMORY_ALLOCATION_ERROR = 2;

	public static final int INVALID_APP = 10;

	public static final int TRACKABLE_ALREADY_EXIST = 50;
	public static final int TRACKABLE_IS_NOT_EXIST = 51;
	public static final int PIXEL_FORMAT_ERROR = 60;
	public static final int INPUT_IMAGE_EMPTY = 61;
	public static final int INPUT_IMAGE_RESOLUTION_ERROR = 62;

	public static final int INVALID_FILE_EXTENTION = 70;

	public static final int ENGINE_ALREADY_INITIALIZED = 170;
	public static final int ENGINE_IS_NOT_INITIALIZED = 180;

	public static final int LEARN_STROKE_EMPTY = 200;
	public static final int LEARN_STROKE_OVERFLOW = 201;
	public static final int CHECK_LEARNABLE_MISSED = 202;
	public static final int SAVE_LEARNED_FILE_INPROGRESS = 210;

	public static final int TRACKER_ALREADY_STARTED = 300;
	public static final int TRACKER_IS_NOT_STARTED = 301;
	public static final int TRACKER_ALREADY_STOPPED = 310;

	public static final int INPUT_TRACKABLE_EMPTY = 320;

	public static final int UNDEFINE_ERROR = 99;
}
