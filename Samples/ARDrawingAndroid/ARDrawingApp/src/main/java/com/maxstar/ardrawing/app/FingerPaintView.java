package com.maxstar.ardrawing.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class FingerPaintView extends View {

	private static final String TAG = FingerPaintView.class.getSimpleName();
	private static final float DEFAULT_STROKE_WIDTH = 20;

	private Paint paint;
	private Bitmap bitmap;
	private Canvas canvas;
	private Path path;
	private Paint bitmapPaint;
	private List<List<Point>> touchPointList = new ArrayList<>();
	private boolean enableTouch = false;

	public FingerPaintView(Context context, AttributeSet attrs) {
		super(context, attrs);

		path = new Path();
		bitmapPaint = new Paint(Paint.DITHER_FLAG);
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setColor(0xFFFF0000);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStrokeWidth(DEFAULT_STROKE_WIDTH);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		canvas = new Canvas(bitmap);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawColor(0x00000000);
		canvas.drawBitmap(bitmap, 0, 0, bitmapPaint);
		canvas.drawPath(path, paint);
	}

	private float mX, mY;
	private static final float TOUCH_TOLERANCE = 4;
	private List<Point> tempPointLIst;

	private void touchStart(float x, float y) {
		tempPointLIst = new ArrayList<>();
		tempPointLIst.add(new Point((int)x, (int)y));
		path.reset();
		path.moveTo(x, y);
		mX = x;
		mY = y;
	}

	private void touchMove(float x, float y) {
		float dx = Math.abs(x - mX);
		float dy = Math.abs(y - mY);
		if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
			path.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
			mX = x;
			mY = y;

			tempPointLIst.add(new Point((int)x, (int)y));
		}
	}

	private void touchEnd() {
		tempPointLIst.add(new Point((int)mX, (int)mY));
		path.lineTo(mX, mY);
		canvas.drawPath(path, paint);
		path.reset();

		touchPointList.add(tempPointLIst);
	}


	private void drawStart(float x, float y) {
		tempPointLIst = new ArrayList<>();
		tempPointLIst.add(new Point((int)x, (int)y));
		path.reset();
		path.moveTo(x, y);
		mX = x;
		mY = y;
	}

	private void drawMove(float x, float y) {
		float dx = Math.abs(x - mX);
		float dy = Math.abs(y - mY);
		if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
			path.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
			mX = x;
			mY = y;

			tempPointLIst.add(new Point((int)x, (int)y));
		}
	}

	private void drawEnd() {
		tempPointLIst.add(new Point((int)mX, (int)mY));
		path.lineTo(mX, mY);
		canvas.drawPath(path, paint);
		path.reset();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!enableTouch) {
			return true;
		}

		float x = event.getX();
		float y = event.getY();

		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				touchStart(x, y);
				invalidate();
				break;

			case MotionEvent.ACTION_MOVE:
				touchMove(x, y);
				invalidate();
				break;

			case MotionEvent.ACTION_UP:
				touchEnd();
				invalidate();
				break;
		}
		return true;
	}

	public void enableTouch(boolean enabled) {
		enableTouch = enabled;
	}

	public void clearCanvas() {
		canvas.drawColor(0, PorterDuff.Mode.CLEAR);
	}

	public void clearTouchPoint() {
		touchPointList.clear();
	}

	public List<List<Point>> getTouchPointList() {
		return touchPointList;
	}

	public void applyTrackingResult(float[] transformMatrix, int idx, float sx, float sy) {
		clearCanvas();

	/*	transformMatrix[0] = transformMatrix[0];
		transformMatrix[1] = transformMatrix[1] * sx / sy;
		transformMatrix[2] = transformMatrix[2] * sx;
		transformMatrix[3] = transformMatrix[3] * sy / sx;
		transformMatrix[4] = transformMatrix[4];
		transformMatrix[5] = transformMatrix[5] * sy;
		transformMatrix[6] = transformMatrix[6] / sx;
		transformMatrix[7] = transformMatrix[7] / sy;
		transformMatrix[8] = transformMatrix[8];*/

		transformMatrix[0] = transformMatrix[0] * sx;
		transformMatrix[1] = transformMatrix[1] * sx;
		transformMatrix[2] = transformMatrix[2] * sx;
		transformMatrix[3] = transformMatrix[3] * sy;
		transformMatrix[4] = transformMatrix[4] * sy;
		transformMatrix[5] = transformMatrix[5] * sy;
		transformMatrix[6] = transformMatrix[6];
		transformMatrix[7] = transformMatrix[7];
		transformMatrix[8] = transformMatrix[8];

		for(int i = 0; i < touchPointList.size(); i++) {
			//Log.e(TAG, "applyTrackingResult: 1" );
			List<Point> points = touchPointList.get(i);
			float x = points.get(0).x;
			float y = points.get(0).y;
			float z = 1;

			float rz = x * transformMatrix[6] + y * transformMatrix[7] + z * transformMatrix[8];
			float rx = (x * transformMatrix[0] + y * transformMatrix[1] + z * transformMatrix[2]) / rz;
			float ry = (x * transformMatrix[3] + y * transformMatrix[4] + z * transformMatrix[5]) / rz;

			drawStart(rx, ry);
			for (int j = 1; j < points.size(); j++) {
				//Log.e(TAG, "applyTrackingResult: 2" );
				x = points.get(j).x;
				y = points.get(j).y;
				z = 1;

				rz = x * transformMatrix[6] + y * transformMatrix[7] + z * transformMatrix[8];
				rx = (x * transformMatrix[0] + y * transformMatrix[1] + z * transformMatrix[2]) / rz;
				ry = (x * transformMatrix[3] + y * transformMatrix[4] + z * transformMatrix[5]) / rz;

				drawMove(rx, ry);
			}
			drawEnd();
		}
		invalidate();
	}

	public void setTouchPointList(List<List<Point>> touchPointList) {
		this.touchPointList = touchPointList;
	}
}