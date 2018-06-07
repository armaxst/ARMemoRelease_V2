/*
 * Copyright (c) 2018. Maxst, Inc. All Rights Reserved.
 */

package com.maxst.armemo.app.cameracontroller;

import android.opengl.GLSurfaceView;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import junit.framework.Assert;

public class SurfaceManager {

	private static final String TAG = SurfaceManager.class.getSimpleName();

	private static SurfaceManager instance;

	public static void init() {
		if (instance == null) {
			instance = new SurfaceManager();
		}
	}

	public static void deinit() {
		CameraController.destroy();
		instance = null;
	}

	public static void setNewCameraFrameCallback(NewCameraFrameCallback callback) {
		SurfaceManager.getInstance().cameraController.setNewCameraFrameCallback(callback);
	}

	public static SurfaceManager getInstance() {
		return instance;
	}

	private CameraSurfaceView cameraSurfaceView;
	private ViewGroup surfaceViewParent;
	private CameraController cameraController;
	private GLSurfaceView glSurfaceView;

	private SurfaceManager() {
		cameraController = CameraController.create();
		cameraController.setSurfaceManager(this);
	}

	// Should be called on UI thread
	void createSurface() {
		Assert.assertEquals("Surface should be created in UI thread", Looper.getMainLooper(), Looper.myLooper());
		if (cameraSurfaceView == null) {
			surfaceViewParent = retrieveGLSurfaceView();
			cameraSurfaceView = new CameraSurfaceView(SystemUtil.getActivity());
			//surfaceViewParent.addView(cameraSurfaceView, 0, new LayoutParams(1, 1));
			surfaceViewParent.addView(cameraSurfaceView, 0, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			applyRenderWhenDirty(true);
			cameraController.setCameraSurfaceView(cameraSurfaceView);
		}
	}

	// Should be called on UI thread
	void destroySurface() {
		Assert.assertEquals("Surface should be created in UI thread", Looper.getMainLooper(), Looper.myLooper());
		if (cameraSurfaceView != null) {
			cameraController.setCameraSurfaceView(null);

			surfaceViewParent.removeView(cameraSurfaceView);
			cameraSurfaceView = null;
			applyRenderWhenDirty(false);
		}
	}

	void requestRender() {
		if (glSurfaceView != null) {
			glSurfaceView.requestRender();
		}
	}

	private ViewGroup retrieveGLSurfaceView() {
		ViewGroup glSurfaceViewParent;

		View decorView = SystemUtil.getActivity().getWindow().getDecorView();
		glSurfaceView = searchForGLSurfaceView(decorView);

		if (glSurfaceView == null) {
			glSurfaceViewParent = (ViewGroup) decorView;
			Log.i(TAG, "Can't find GLSurfaceView");
		} else {
			glSurfaceViewParent = (ViewGroup) glSurfaceView.getParent();
			Log.i(TAG, "Find GLSurfaceView");
		}

		return glSurfaceViewParent;
	}

	private GLSurfaceView searchForGLSurfaceView(View rootView) {
		GLSurfaceView result = null;
		ViewGroup rootViewGroup = (ViewGroup) rootView;

		int numChildren = rootViewGroup.getChildCount();
		for (int i = 0; i < numChildren; i++) {
			View childView = rootViewGroup.getChildAt(i);

			if ((childView instanceof GLSurfaceView)) {
				result = (GLSurfaceView) childView;
				break;
			} else if ((childView instanceof ViewGroup)) {
				result = searchForGLSurfaceView(childView);
				if (result != null) {
					break;
				}
			}
		}
		return result;
	}

	private boolean applyRenderWhenDirty(boolean renderWhenDirtyEnabled) {
		if (glSurfaceView != null) {
			glSurfaceView.setRenderMode(renderWhenDirtyEnabled ? 0 : 1);
			return true;
		}

		return false;
	}

	public CameraController getCameraController() {
		return cameraController;
	}

	public SurfaceView getCameraSurfaceView() {
		return cameraSurfaceView;
	}
}
