/*
* Copyright 2017 Maxst, Inc. All Rights Reserved.
*/
package com.maxstar.ardrawing.app;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Created by Gidools on 2016-10-31.
 */

public class ListAdapter extends ArrayAdapter<String> {
	public ListAdapter(Context context, List<String> logList) {
		super(context, R.layout.list_item, R.id.item_text, logList);
	}
}
