/*
 * Copyright (c) 2018. Maxst, Inc. All Rights Reserved.
 */

package com.maxst.armemo.app.cameracontroller;

import android.util.Log;

import java.util.List;

abstract public class CameraController {

    private static final String TAG = CameraController.class.getSimpleName();

    static final int REQUEST_CAMERA_PERMISSION = 200;

    static public CameraController create() {
        CameraController instance;
//        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            instance = new Camera2Controller();
//        }
//        else
        {
            instance = new Camera1Controller();
        }
        //MaxstAR.initCameraController(instance);
        return instance;
    }

    static void destroy() {
        //MaxstAR.deinitCameraController();
    }

    public void setNewCameraFrameCallback(NewCameraFrameCallback callback) {
        newCameraFrameCallback = callback;
    }

    NewCameraFrameCallback newCameraFrameCallback;
    SurfaceManager surfaceManager;
    CameraSurfaceView cameraSurfaceView;

    void setSurfaceManager(SurfaceManager surfaceManager) {
        this.surfaceManager = surfaceManager;
    }

    void setCameraSurfaceView(CameraSurfaceView cameraSurfaceView) {
        this.cameraSurfaceView = cameraSurfaceView;
    }

    abstract public void start(int cameraId, int width, int height);

    abstract public void stop();

    abstract public int getWidth();

    abstract public int getHeight();

    CameraSize getOptimalPreviewSize(List<CameraSize> sizes, int preferredWidth, int preferredHeight) {
        double minRegion = Double.MAX_VALUE;
        CameraSize optimalSize = null;
        for (CameraSize size : sizes) {
            if (size.width <= preferredWidth && size.height <= preferredHeight) {
                if (Math.abs(size.width * size.height - preferredWidth * preferredHeight) <= minRegion) {
                    minRegion = Math.abs(size.width * size.height - preferredWidth * preferredHeight);
                    optimalSize = size;

                    Log.i(TAG, "Optimal Preview width  : " + optimalSize.width + " height : " + optimalSize.height);
                }
            }
        }

        return optimalSize;
    }
}
