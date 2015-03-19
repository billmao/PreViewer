package com.ktouch.kdc.launcher4.ui;

import com.ktouch.kdc.launcher4.app.AlbumActivity;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class LayoutTouch extends LinearLayout {

	private AlbumActivity activity;

	public LayoutTouch(Context context) {
		super(context);
	}

	public LayoutTouch(Context context, AttributeSet attrs) {
		super(context, attrs);
		activity = (AlbumActivity) context;
	}

	public LayoutTouch(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		//activity.onTouch(this, event);
		return super.dispatchTouchEvent(event);
	}

}
