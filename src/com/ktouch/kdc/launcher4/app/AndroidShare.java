package com.ktouch.kdc.launcher4.app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.ktouch.kdc.launcher4.R;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AndroidShare extends Dialog implements
		AdapterView.OnItemClickListener {
	private LinearLayout mLayout;
	private GridView mGridView;
	private float mDensity;
	private String msgText = "";
	private String mImgPath;
	private int mScreenOrientation;
	private List<ShareItem> mListData;
	private TextView mTextView;// lianglei
	private Handler mHandler = new Handler();

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
				((AndroidShare.MyAdapter) mGridView.getAdapter())
						.notifyDataSetChanged();
			}
			mHandler.postDelayed(this, 1000L);
		}
	};

	public AndroidShare(Context context) {
		super(context, R.style.shareDialogTheme);
	}

	// lianglei begin
	public AndroidShare(Context context, final String imgUri) {
		super(context, R.style.shareDialogTheme);
		if (Patterns.WEB_URL.matcher(imgUri).matches())
			new Thread(new Runnable() {
				public void run() {
					try {
						mImgPath = getImagePath(imgUri, getFileCache());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
		else
			this.mImgPath = imgUri;
	}

	// lianglei end

	public AndroidShare(Context context, int theme, String msgText,
			final String imgUri) {
		super(context, theme);
		this.msgText = msgText;

		if (Patterns.WEB_URL.matcher(imgUri).matches())
			new Thread(new Runnable() {
				public void run() {
					try {
						mImgPath = getImagePath(imgUri, getFileCache());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
		else
			this.mImgPath = imgUri;
	}

	public AndroidShare(Context context, String msgText, final String imgUri) {
		super(context, R.style.shareDialogTheme);
		this.msgText = msgText;

		if (Patterns.WEB_URL.matcher(imgUri).matches())
			new Thread(new Runnable() {
				public void run() {
					try {
						mImgPath = getImagePath(imgUri, getFileCache());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
		else
			this.mImgPath = imgUri;
	}

	void init(Context context) {
		DisplayMetrics dm = new DisplayMetrics();
		dm = context.getResources().getDisplayMetrics();
		this.mDensity = dm.density;
		this.mListData = new ArrayList<ShareItem>();
		this.mListData.add(new ShareItem("微信", R.drawable.share_weshat,
				"com.tencent.mm.ui.tools.ShareImgUI", "com.tencent.mm"));
		this.mListData.add(new ShareItem("朋友圈", R.drawable.share_wechatmoment,
				"com.tencent.mm.ui.tools.ShareToTimeLineUI", "com.tencent.mm"));
		this.mListData.add(new ShareItem("QQ好友", R.drawable.share_qq,
				"com.tencent.mobileqq.activity.JumpActivity",
				"com.tencent.mobileqq"));
		this.mListData
				.add(new ShareItem("QQ空间", R.drawable.share_qzone,
						"com.qzone.ui.operation.QZonePublishMoodActivity",
						"com.qzone"));
		this.mListData.add(new ShareItem("微博", R.drawable.share_weibo,
				"com.sina.weibo.EditActivity", "com.sina.weibo"));
		this.mListData
				.add(new ShareItem("彩信", R.drawable.share_mms,
						"com.android.mms.ui.ComposeMessageActivity",
						"com.android.mms"));
		this.mListData.add(new ShareItem("邮件", R.drawable.share_email,
				"com.android.email.activity.ComposeActivityEmail",
				"com.android.email"));
		this.mListData.add(new ShareItem("全部", R.drawable.share_all, "", ""));

		this.mLayout = new LinearLayout(context);
		this.mLayout.setOrientation(1);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
		params.leftMargin = ((int) (10.0F * this.mDensity));
		params.rightMargin = ((int) (10.0F * this.mDensity));
		this.mLayout.setLayoutParams(params);
		this.mLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));// 整体背景颜色

		// lianglei begin
		this.mTextView = new TextView(context);
		this.mTextView.setLayoutParams(new ViewGroup.LayoutParams(-1, -2));
		this.mTextView.setBackgroundResource(R.drawable.bg_share);// 这里之后填充为带分割线的图片
		this.mTextView.setHeight((int) (30.0F * this.mDensity));
		this.mTextView.setTextSize(23);
		this.mTextView.setTextColor(Color.parseColor("#434343"));
		this.mTextView.setText("分享到:");
		this.mTextView.setGravity(Gravity.CENTER);
		// lianglei end

		this.mGridView = new GridView(context);
		this.mGridView.setLayoutParams(new ViewGroup.LayoutParams(-1, -2));
		this.mGridView.setGravity(17);
		// this.mGridView.setHorizontalSpacing((int) (10.0F * this.mDensity));
		// this.mGridView.setVerticalSpacing((int) (10.0F * this.mDensity));
		this.mGridView.setHorizontalSpacing((int) (1.0F * this.mDensity));
		this.mGridView.setVerticalSpacing((int) (0.0F * this.mDensity));
		this.mGridView.setStretchMode(1);
		// this.mGridView.setColumnWidth((int) (90.0F * this.mDensity));
		this.mGridView.setColumnWidth((int) (75.0F * this.mDensity));
		this.mGridView.setHorizontalScrollBarEnabled(false);
		this.mGridView.setVerticalScrollBarEnabled(false);
		this.mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));

		// lianglei begin
		this.mLayout.addView(this.mTextView);
		// lianglei end

		this.mLayout.addView(this.mGridView);
	}

	public List<ComponentName> queryPackage() {
		List<ComponentName> cns = new ArrayList<ComponentName>();
		Intent i = new Intent("android.intent.action.SEND");
		i.setType("image/*");
		List<ResolveInfo> resolveInfo = getContext().getPackageManager()
				.queryIntentActivities(i, 0);
		for (ResolveInfo info : resolveInfo) {
			ActivityInfo ac = info.activityInfo;
			ComponentName cn = new ComponentName(ac.packageName, ac.name);
			cns.add(cn);
		}
		return cns;
	}

	public boolean isAvilible(Context context, String packageName) {
		PackageManager packageManager = context.getPackageManager();

		List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
		for (int i = 0; i < pinfo.size(); i++) {
			if (((PackageInfo) pinfo.get(i)).packageName
					.equalsIgnoreCase(packageName))
				return true;
		}
		return false;
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Context context = getContext();
		init(context);
		setContentView(this.mLayout);

		// lianglei begin
		// getWindow().setGravity(80);
		getWindow().setGravity(Gravity.CENTER);
		// lianglei end

		if (getScreenOrientation() == 0) {
			this.mScreenOrientation = 0;
			this.mGridView.setNumColumns(4);
		} else {
			this.mGridView.setNumColumns(6);
			this.mScreenOrientation = 1;
		}
		this.mGridView.setAdapter(new MyAdapter());
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

	public int getScreenOrientation() {
		int landscape = 0;
		int portrait = 1;
		Point pt = new Point();
		getWindow().getWindowManager().getDefaultDisplay().getSize(pt);
		int width = pt.x;
		int height = pt.y;
		return width > height ? portrait : landscape;
	}

	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		ShareItem share = (ShareItem) this.mListData.get(position);
		shareMsg(getContext(), "选择以下方式分享给好友", this.msgText, this.mImgPath,
				share, position);
		this.dismiss();
	}

	private void shareMsg(Context context, String msgTitle, String msgText,
			String imgPath, ShareItem share, int position) {
		if (!share.packageName.isEmpty()
				&& !isAvilible(getContext(), share.packageName)) {
			Toast.makeText(getContext(), "请先安装" + share.title,
					Toast.LENGTH_SHORT).show();
			Intent toMarket = new Intent(Intent.ACTION_VIEW);
			toMarket.setData(Uri.parse("market://details?id="
					+ share.packageName + ""));
			context.startActivity(toMarket);
			return;
		}

		String contentDetails = "";
		Intent it = new Intent(Intent.ACTION_SEND);
		it.setType("image/*");
		List<ResolveInfo> resInfo = context.getPackageManager()
				.queryIntentActivities(it, 0);

		switch (position) {
		case 5:
			if (!resInfo.isEmpty()) {
				List<Intent> targetedShareIntents = new ArrayList<Intent>();
				for (ResolveInfo info : resInfo) {
					Intent targeted = new Intent(Intent.ACTION_SEND);
					targeted.setType("image/*");
					File f = new File(imgPath);
					if ((f != null) && (f.exists()) && (f.isFile())) {
						targeted.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
					}
					ActivityInfo activityInfo = info.activityInfo;

					if (activityInfo.packageName.contains("mms")
							|| activityInfo.name.contains("mms")) {
						targeted.putExtra(Intent.EXTRA_TEXT, contentDetails);
						targeted.setPackage(activityInfo.packageName);
						targetedShareIntents.add(targeted);
					}
				}

				Intent chooserIntent = Intent.createChooser(
						targetedShareIntents.remove(0), "选择以下方式分享给好友:");
				if (chooserIntent == null) {
					return;
				}

				chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
						targetedShareIntents.toArray(new Parcelable[] {}));

				try {
					context.startActivity(chooserIntent);
				} catch (android.content.ActivityNotFoundException ex) {
				}
			}
			break;
		case 6:

			if (!resInfo.isEmpty()) {
				List<Intent> targetedShareIntents = new ArrayList<Intent>();
				for (ResolveInfo info : resInfo) {
					Intent targeted = new Intent(Intent.ACTION_SEND);
					targeted.setType("image/*");
					File f = new File(imgPath);
					if ((f != null) && (f.exists()) && (f.isFile())) {
						targeted.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
					}
					ActivityInfo activityInfo = info.activityInfo;

					if (activityInfo.packageName.contains("mail")
							|| activityInfo.name.contains("mail")) {
						targeted.putExtra(Intent.EXTRA_TEXT, contentDetails);
						targeted.setPackage(activityInfo.packageName);
						targetedShareIntents.add(targeted);
					}
				}

				Intent chooserIntent = Intent.createChooser(
						targetedShareIntents.remove(0), "Select app to share");
				if (chooserIntent == null) {
					return;
				}

				chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
						targetedShareIntents.toArray(new Parcelable[] {}));

				try {
					context.startActivity(chooserIntent);
				} catch (android.content.ActivityNotFoundException ex) {
				}
			}
			break;

		default:
			Intent intent = new Intent("android.intent.action.SEND");
			if ((imgPath == null) || (imgPath.equals(""))) {
				intent.setType("text/plain");
			} else {
				File f = new File(imgPath);
				if ((f != null) && (f.exists()) && (f.isFile())) {
					intent.setType("image/png");
					intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
				}
			}

			intent.putExtra(Intent.EXTRA_SUBJECT, msgTitle);
			intent.putExtra(Intent.EXTRA_TEXT, msgText);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			if (!share.packageName.isEmpty()) {
				intent.setComponent(new ComponentName(share.packageName,
						share.activityName));
				context.startActivity(intent);
			} else {
				context.startActivity(Intent.createChooser(intent, msgTitle));
			}
			break;
		}
	}

	private File getFileCache() {
		File cache = null;

		if (Environment.getExternalStorageState().equals("mounted"))
			cache = new File(Environment.getExternalStorageDirectory() + "/."
					+ getContext().getPackageName());
		else {
			cache = new File(getContext().getCacheDir().getAbsolutePath()
					+ "/." + getContext().getPackageName());
		}
		if ((cache != null) && (!cache.exists())) {
			cache.mkdirs();
		}
		return cache;
	}

	public String getImagePath(String imageUrl, File cache) throws Exception {
		String name = imageUrl.hashCode()
				+ imageUrl.substring(imageUrl.lastIndexOf("."));
		File file = new File(cache, name);

		if (file.exists()) {
			return file.getAbsolutePath();
		}

		URL url = new URL(imageUrl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(5000);
		conn.setRequestMethod("GET");
		conn.setDoInput(true);
		if (conn.getResponseCode() == 200) {
			InputStream is = conn.getInputStream();
			FileOutputStream fos = new FileOutputStream(file);
			byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = is.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
			}
			is.close();
			fos.close();

			return file.getAbsolutePath();
		}

		return null;
	}

	private final class MyAdapter extends BaseAdapter {
		private static final int image_id = 256;
		private static final int tv_id = 512;

		public MyAdapter() {
		}

		public int getCount() {
			return mListData.size();
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return 0L;
		}

		private View getItemView() {
			LinearLayout item = new LinearLayout(getContext());
			item.setOrientation(1);
			int padding = (int) (10.0F * mDensity);
			item.setPadding(padding, padding, padding, padding);
			item.setGravity(17);

			ImageView iv = new ImageView(getContext());
			item.addView(iv);
			iv.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
			iv.setId(image_id);

			TextView tv = new TextView(getContext());
			item.addView(tv);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					-2, -2);
			layoutParams.topMargin = ((int) (5.0F * mDensity));
			tv.setLayoutParams(layoutParams);
			tv.setTextColor(Color.parseColor("#212121"));
			tv.setTextSize(14.0F);
			tv.setId(tv_id);

			return item;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getItemView();
			}
			ImageView iv = (ImageView) convertView.findViewById(image_id);
			TextView tv = (TextView) convertView.findViewById(tv_id);
			AndroidShare.ShareItem item = (AndroidShare.ShareItem) mListData
					.get(position);
			iv.setImageResource(item.logo);
			tv.setText(item.title);
			return convertView;
		}
	}

	private class ShareItem {
		String title;
		int logo;
		String activityName;
		String packageName;

		public ShareItem(String title, int logo, String activityName,
				String packageName) {
			this.title = title;
			this.logo = logo;
			this.activityName = activityName;
			this.packageName = packageName;
		}
	}
}
