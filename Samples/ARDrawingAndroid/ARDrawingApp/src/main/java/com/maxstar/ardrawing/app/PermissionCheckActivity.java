/*
* Copyright 2017 Maxst, Inc. All Rights Reserved.
*/
package com.maxstar.ardrawing.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;

public class PermissionCheckActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		permissionCheck();
	}

	private void permissionCheck() {
		PermissionListener permissionlistener = new PermissionListener() {
			@Override
			public void onPermissionGranted() {
				startActivity(new Intent(PermissionCheckActivity.this, MainActivity.class));
				finish();
			}

			@Override
			public void onPermissionDenied(ArrayList<String> deniedPermissions) {
				Toast.makeText(PermissionCheckActivity.this, "권한 허용해야 한다구요!!", Toast.LENGTH_LONG).show();
				finish();
			}
		};

		new TedPermission(this)
				.setPermissionListener(permissionlistener)
				.setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
				.check();
	}
}