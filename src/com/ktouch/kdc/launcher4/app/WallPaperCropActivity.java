package com.ktouch.kdc.launcher4.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.ktouch.kdc.launcher4.R;
import com.ktouch.kdc.launcher4.ui.ZoomImageView;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

//bill create
public class WallPaperCropActivity extends Activity {
	private static final int CROPLOADSAVE_DIALOG = 1;
	private static final int CROPFINISHSAVE_DIALOGSUCCESS = 2;
	private static final int CROPSAVE_FAILED = 3;
	private static final int CROPSAVE_NOPICTURES = 4;
	private ImageView back;
	private ImageView cropsave;
	// 自定义的ImageView控制，可对图片进行多点触控缩放和拖动
	private ZoomImageView zoomImageView;
	private Bitmap bitmap;
	private ImageView progress;
	private Dialog proDia;
	private TextView promptinfo;
	private String pathName;
	private String fileName;
	private String newFileName;
	private int screenHeight = 0;
	private int screenWidth = 0;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if (msg.what == CROPLOADSAVE_DIALOG) {
				proDia = new Dialog(WallPaperCropActivity.this);
				proDia.requestWindowFeature(Window.FEATURE_NO_TITLE);
				View view = LayoutInflater.from(WallPaperCropActivity.this)
						.inflate(R.layout.wallpaper_progressbar, null);
				progress = (ImageView) view.findViewById(R.id.progeress);
				promptinfo = (TextView) view.findViewById(R.id.promptinfo);
				promptinfo.setText(R.string.cropsaving);
				Animation anim = AnimationUtils.loadAnimation(
						WallPaperCropActivity.this, R.anim.album_loading);
				progress.startAnimation(anim);
				proDia.setContentView(view);
				proDia.show();
				return;
			} else if (msg.what == CROPFINISHSAVE_DIALOGSUCCESS) {
				if (proDia.isShowing()) {
					proDia.dismiss();
					Toast.makeText(WallPaperCropActivity.this,
							"图片保存在" + ":" + newFileName, 0).show();
					progress.clearAnimation();
				}
				return;
			} else if (msg.what == CROPSAVE_FAILED) {
				Toast.makeText(WallPaperCropActivity.this,
						R.string.cropsavefailed, 0).show();
				return;
			} else if (msg.what == CROPSAVE_NOPICTURES) {
				Toast.makeText(WallPaperCropActivity.this,
						R.string.cropsavepic, 0).show();
				return;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.wallpapercrop);
		zoomImageView = (ZoomImageView) findViewById(R.id.zoom_image_view);
		back = (ImageView) this.findViewById(R.id.cropback);
		cropsave = (ImageView) this.findViewById(R.id.cropsave);
		setListener();
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		screenHeight = displayMetrics.heightPixels;
		screenWidth = displayMetrics.widthPixels;
		// 取出图片路径，并解析成Bitmap对象，然后在ZoomImageView中显示
		Intent intent = getIntent();
		String path = intent.getStringExtra("picstring");
		bitmap = BitmapFactory.decodeFile(path);
		zoomImageView.setImageBitmap(bitmap, screenWidth, screenHeight);
		int index = path.lastIndexOf("/");
		pathName = path.substring(0, index + 1);
		fileName = path.substring(index + 1, path.length() - 4);
	}

	public void setListener() {
		back.setOnClickListener((new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}

		}));
		cropsave.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				handler.sendEmptyMessage(1);
				Thread thread = new Thread() {
					public void run() {
						Bitmap fialBitmap = getBitmap();
						if (fialBitmap != null) {
							SimpleDateFormat formatter = new SimpleDateFormat(
									"yyyyMMddHHmmss");
							Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
							String str = formatter.format(curDate);
							newFileName = pathName + fileName + "crop" + str
									+ ".png";
							File file = new File(newFileName);
							if (file.exists()) {
								try {
									file.delete();
									file.createNewFile();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							try {
								FileOutputStream fos = new FileOutputStream(
										file);
								boolean result = fialBitmap.compress(
										Bitmap.CompressFormat.PNG, 100, fos);
								if (result) {
									Intent scanIntent = new Intent(
											Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
									scanIntent.setData(Uri.fromFile(new File(
											newFileName)));
									sendBroadcast(scanIntent);
									handler.sendEmptyMessage(2);
								} else {
									handler.sendEmptyMessage(3);
								}
							} catch (FileNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} else {
							handler.sendEmptyMessage(4);
							return;
						}
						finish();
					}
				};
				thread.start();

			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 将Bitmap对象回收掉
		if (bitmap != null) {
			bitmap.recycle();
		}
	}

	private Bitmap getBitmap() {
		Bitmap screenShoot = takeScreenShot();
		Bitmap finalBitmap = Bitmap.createBitmap(screenShoot, 0, 0,
				screenWidth, screenHeight);
		return finalBitmap;
	}

	private Bitmap takeScreenShot() {
		zoomImageView.setDrawingCacheEnabled(true);
		zoomImageView.buildDrawingCache();
		return zoomImageView.getDrawingCache();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPause(this);// 友盟统计
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onResume(this);// 友盟统计
	}
}