/*
* Copyright 2017 Maxst, Inc. All Rights Reserved.
*/
package com.maxstar.ardrawing.app;

import android.opengl.GLSurfaceView.Renderer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class SampleGLRenderer implements Renderer {

	@Override
	public void onDrawFrame(GL10 gl) {
		drawScene();
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		updateRendering(width, height);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		initRendering();
	}

	private native void initRendering();

	private native void updateRendering(int width, int height);

	private native void drawScene();
}
