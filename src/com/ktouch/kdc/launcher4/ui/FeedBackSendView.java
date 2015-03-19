package com.ktouch.kdc.launcher4.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
//bill create
public class FeedBackSendView extends RelativeLayout {
	private OnResizeListener mOnResizeListener;
	private OnLayoutListener mOnLayoutListener;

	public interface OnResizeListener {
		void OnResize(int w, int h, int oldw, int oldh);
	}
	public interface OnLayoutListener {
		void OnLayout(boolean changed, int l, int t, int r, int b);
	}

	public void setOnResizeListener(OnResizeListener l) {
		mOnResizeListener = l;
	}
	
	public void setOnLayoutListener(OnLayoutListener l) {
		mOnLayoutListener = l;
	}

	public FeedBackSendView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public FeedBackSendView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FeedBackSendView(Context context) {
		super(context);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (mOnResizeListener != null) {
			mOnResizeListener.OnResize(w, h, oldw, oldh);
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (mOnLayoutListener != null) {
			mOnLayoutListener.OnLayout(changed, l, t, r, b);
		}
	}
	
	
}
