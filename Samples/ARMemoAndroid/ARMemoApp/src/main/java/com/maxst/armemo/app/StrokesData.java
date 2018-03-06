/*
 * Copyright (c) 2018. Maxst, Inc. All Rights Reserved.
 */

package com.maxst.armemo.app;

import android.graphics.Point;

import java.util.List;

/**
 * Created by Jenny-pc on 2018-02-26.
 */

public class StrokesData {
	public int imageWidth;
	public int imageHeight;
	public List<List<Point>> strokes;

	public StrokesData(){}

	public StrokesData(int imageWidth, int imageHeight, List<List<Point>> strokes){
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		this.strokes = strokes;
	}
}
