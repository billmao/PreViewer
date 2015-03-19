package com.ktouch.kdc.launcher4.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
//bill create
public class AlbumImageView extends ImageView {
	private int index = -1;
	
	public AlbumImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	
	public AlbumImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO: do something here if you want
	}
	
	public AlbumImageView(Context context) {
		super(context);
		// TODO: do something here if you want
	}
	
	public int getIndex() {
	    return index;
	}
	
	public void setIndex(int index) {
        this.index = index;
	}

}
