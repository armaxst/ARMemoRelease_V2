/*
 * Copyright (c) 2018. Maxst, Inc. All Rights Reserved.
 */

package com.maxst.armemo.app.cameracontroller;

import android.app.Activity;

public class SystemUtil {

    private static Activity activityInternal;

    public static void init(Activity activity) {
        activityInternal = activity;
    }

    public static void deinit() {
        activityInternal = null;
    }

    public static Activity getActivity() {
        return activityInternal;
    }

    private SystemUtil() { }
}
