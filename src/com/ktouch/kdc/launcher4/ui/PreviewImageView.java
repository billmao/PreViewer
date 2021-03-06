package com.ktouch.kdc.launcher4.ui;

//file description
//author:bill 
//function:全屏预览图片
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class PreviewImageView extends GLView {
	private Context mContext;
	private BasicTexture mBackground;
	private int resId = 0;
	private int screenWidth = 0;
	private int screenHeight = 0;
	private String path = "";
	public PreviewImageView(Context context, int resid, int width, int height) {
		mContext = context;
		resId = resid;
		screenWidth = width;
		screenHeight = height;
		mBackground = new ResourceTexture(mContext, resId);

	}

	public PreviewImageView(Context context, String path, int width, int height) {
		mContext = context;
		this.path = path;
		//bill add begin 优化内存
		BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
		screenWidth = width;
		screenHeight = height;

		mBackground = new BitmapTexture(bitmap);
		if(bitmap!=null){
			bitmap.recycle();
		}
        //bill add end
	}

	@Override
	protected void render(GLCanvas canvas) {
		super.render(canvas);
		mBackground.draw(canvas, 0, 0, screenWidth, screenHeight);
	}
	//bill modify begin
	public void setImageId(int resid) {
		if (resId != resid) {
			resId = resid;
		}
		ResourceTexture temp = new ResourceTexture(mContext, resId);
		temp.setInSamleFlag(false);
		mBackground = temp;
		invalidate();
	}
	//lianglei1 begin
	public void setImagePath(String path) {
		if (!this.path.equals(path)) {
			this.path = path;
		}
		BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
		mBackground = new BitmapTexture(bitmap, false,path);
		invalidate();
	}
	//lianglei end
	//bill modify end
	public void setViewVisible(int visible) {
		setVisibility(visible);
	}
}
