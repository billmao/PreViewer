package com.ktouch.kdc.launcher4.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.ktouch.kdc.launcher4.R;
import com.ktouch.kdc.launcher4.app.Gallery;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

/*
 * author by lianglei @k-touch
 */

public class More_AndroidShare extends Dialog implements
		AdapterView.OnItemClickListener {
	private List<AppInfo> mlistAppInfo = null;
	private LinearLayout mLayout;
	TextView mTextView;
	BrowseApplicationInfoAdapter browseAppAdapter;
	private GridView mGridView;
	private float mDensity;
	private int mScreenOrientation;
	private Handler mHandler = new Handler();
	static int imgWidth;
	String path = Gallery.path;
	public static int numGridView = 0;

	private Runnable work = new Runnable() {
		public void run() {
			int orient = getScreenOrientation();
			if (orient != mScreenOrientation) {
				if (orient == 0)
					mGridView.setNumColumns(4);
				else {
					mGridView.setNumColumns(6);
				}
				mScreenOrientation = orient;
				((BrowseApplicationInfoAdapter) mGridView.getAdapter())
						.notifyDataSetChanged();
			}
			mHandler.postDelayed(this, 1000L);
		}
	};

	public More_AndroidShare(Context context, final String imgUri) {
		super(context, R.style.shareDialogTheme);
	}

	void init(Context context) {
		DisplayMetrics dm = new DisplayMetrics();
		dm = context.getResources().getDisplayMetrics();
		this.mDensity = dm.density;
		this.mLayout = new LinearLayout(context);
		this.mLayout.setOrientation(LinearLayout.VERTICAL);
		this.mLayout.setGravity(Gravity.TOP);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
		params.leftMargin = ((int) (10.0F * this.mDensity));
		params.rightMargin = ((int) (10.0F * this.mDensity));
		this.mLayout.setLayoutParams(params);
		
		this.mTextView = new TextView(context);
		this.mTextView.setLayoutParams(new ViewGroup.LayoutParams(-1, -2));
		this.mTextView.setTextSize(23);
		this.mTextView.setText(R.string.shareThis);
		this.mTextView.setGravity(Gravity.CENTER);
		
		this.mGridView = new GridView(context);
		this.mGridView.setLayoutParams(new ViewGroup.LayoutParams(-1, -2));
		this.mGridView.setGravity(17);
		this.mGridView.setHorizontalSpacing((int) (10.0F * this.mDensity));
		this.mGridView.setVerticalSpacing((int) (10.0F * this.mDensity));
		this.mGridView.setStretchMode(1);
		this.mGridView.setColumnWidth((int) (90.0F * this.mDensity));
		this.mGridView.setHorizontalScrollBarEnabled(false);
		this.mGridView.setVerticalScrollBarEnabled(false);
		this.mLayout.addView(this.mTextView);
		this.mLayout.addView(this.mGridView);
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setLayout(-1, -1);
		Context context = getContext();
		init(context);
		setContentView(this.mLayout);
		getWindow().setGravity(Gravity.CENTER);// 80
		getWindow().setTitle("1");

		if (getScreenOrientation() == 0) {
			this.mScreenOrientation = 0;
			this.mGridView.setNumColumns(4);
		} else {
			this.mGridView.setNumColumns(6);
			this.mScreenOrientation = 1;
		}
		mlistAppInfo = new ArrayList<AppInfo>();
		queryAppInfo(); // 查询所有应用程序信息
		if (mlistAppInfo.size() >= 8) {
			numGridView = 8;
		} else {
			numGridView = mlistAppInfo.size();
		}
		browseAppAdapter = new BrowseApplicationInfoAdapter(getContext(),
				mlistAppInfo);
		this.mGridView.setAdapter(browseAppAdapter);
		this.mGridView.setOnItemClickListener(this);

		this.mHandler.postDelayed(this.work, 1000L);

		setOnDismissListener(new DialogInterface.OnDismissListener() {
			public void onDismiss(DialogInterface dialog) {
				mHandler.removeCallbacks(work);
			}
		});
	}

	public void show() {
		super.show();
	}

	@SuppressWarnings("deprecation")
	public int getScreenOrientation() {
		int landscape = 0;
		int portrait = 1;
		int width = getWindow().getWindowManager().getDefaultDisplay()
				.getWidth();
		int height = getWindow().getWindowManager().getDefaultDisplay()
				.getHeight();
		return width > height ? portrait : landscape;
	}

	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		if (numGridView == 8) {
			if (position >= 0 && position < 7) {
				File file = new File(path); // 附件文件地址
				Intent intent = new Intent(Intent.ACTION_SEND);
				AppInfo appInfo = (AppInfo) browseAppAdapter.getItem(position);
				intent.setComponent(new ComponentName(appInfo.getPkgName(),
						appInfo.getAppLauncherClassName()));
				intent.putExtra("subject", file.getName()); //
				intent.putExtra("body", "android123 - email sender"); // 正文
				intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file)); // 添加附件，附件为file对象
				intent.setType("image/*"); // 设定需要传送的为图片
				getContext().startActivity(intent); // 调用系统的mail客户端进行发送
			} else {
				numGridView = mlistAppInfo.size();
				browseAppAdapter = new BrowseApplicationInfoAdapter(
						getContext(), mlistAppInfo);
				this.mGridView.setAdapter(browseAppAdapter);
			}
		} else {
			File file = new File(path); // 附件文件地址
			Intent intent = new Intent(Intent.ACTION_SEND);
			AppInfo appInfo = (AppInfo) browseAppAdapter.getItem(position);
			intent.setComponent(new ComponentName(appInfo.getPkgName(), appInfo
					.getAppLauncherClassName()));
			intent.putExtra("subject", file.getName()); //
			intent.putExtra("body", "android123 - email sender"); // 正文
			intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file)); // 添加附件，附件为file对象
			intent.setType("image/*"); // 设定需要传送的为图片
			getContext().startActivity(intent); // 调用系统的mail客户端进行发送
		}
	}

	public void queryAppInfo() {
		PackageManager pm = getContext().getPackageManager(); // 获得PackageManager对象
		Intent mainIntent = new Intent("android.intent.action.SEND");
		mainIntent.setType("image/*");
		// 通过查询，获得所有ResolveInfo对象.
		List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent,
				PackageManager.MATCH_DEFAULT_ONLY);
		// 调用系统排序 ， 根据name排序
		// 该排序很重要，否则只能显示系统应用，而不能列出第三方应用程序
		// Collections.sort(resolveInfos,
		// new ResolveInfo.DisplayNameComparator(pm));
		if (mlistAppInfo != null) {
			mlistAppInfo.clear();
			for (ResolveInfo reInfo : resolveInfos) {
				String activityName = reInfo.activityInfo.name; // 获得该应用程序的启动Activity的name
				String pkgName = reInfo.activityInfo.packageName; // 获得应用程序的包名
				String appLabel = (String) reInfo.loadLabel(pm); // 获得应用程序的Label
				Drawable icon = reInfo.loadIcon(pm); // 获得应用程序图标
				// 为应用程序的启动Activity 准备Intent
				Intent launchIntent = new Intent();
				launchIntent.setComponent(new ComponentName(pkgName,
						activityName));
				// 创建一个AppInfo对象，并赋值
				AppInfo appInfo = new AppInfo();
				appInfo.setAppLabel(appLabel);
				appInfo.setPkgName(pkgName);
				appInfo.setAppIcon(icon);
				appInfo.setIntent(launchIntent);
				appInfo.setAppLauncherClassName(activityName);
				mlistAppInfo.add(appInfo); // 添加至列表中
			}
		}
	}
}
