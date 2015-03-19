package com.ktouch.kdc.launcher4.app;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.ktouch.kdc.launcher4.R;
import com.umeng.analytics.MobclickAgent;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ShareImage extends Dialog implements OnItemClickListener {

	private boolean qqIsInstalled, wechatIsInstalled, qzoneIsInstalled,
			weiboIsInstalled;
	private String imgPath;
	private String packageName;
	private List<ShareItem> mListData;
	private LinearLayout mLayout;
	private GridView mGridView;
	private float mDensity;
	private TextView mTextView;

	public ShareImage(Context context, int theme, String imgPath) {
		super(context, theme);
		this.imgPath = imgPath;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		queryPackage(getContext());
		init(getContext());
		setContentView(mLayout);
		Adapter adapter = new Adapter();
		mGridView.setAdapter(adapter);
		mGridView.setOnItemClickListener(this);
	}

	void init(Context context) {
		DisplayMetrics dm = new DisplayMetrics();
		dm = context.getResources().getDisplayMetrics();
		this.mDensity = dm.density;
		Log.v("lianglei", dm.heightPixels + "");
		double gvHMax, gvHMin;
		if (dm.heightPixels < 1920) {
			gvHMax = 0.45;
			gvHMin = 0.41;
		} else {
			gvHMax = 0.35;
			gvHMin = 0.3;
		}
		this.mLayout = new LinearLayout(context);
		this.mLayout.setOrientation(1);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
		params.leftMargin = ((int) (10.0F * this.mDensity));
		params.rightMargin = ((int) (10.0F * this.mDensity));
		this.mLayout.setLayoutParams(params);
		this.mLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));// 整体背景颜色

		this.mTextView = new TextView(context);
		this.mTextView.setLayoutParams(new ViewGroup.LayoutParams(-1,
				(int) (0.08 * dm.heightPixels)));
		this.mTextView.setHeight((int) (30.0F * this.mDensity));
		this.mTextView.setTextSize(23);
		this.mTextView.setTextColor(Color.parseColor("#434343"));
		this.mTextView.setText("分享到");
		this.mTextView.setGravity(Gravity.CENTER);

		this.mGridView = new GridView(context);
		if (mListData.size() < 6) {
			this.mGridView.setLayoutParams(new ViewGroup.LayoutParams(-1,
					(int) (gvHMin * dm.heightPixels)));
		} else {
			this.mGridView.setLayoutParams(new ViewGroup.LayoutParams(-1,
					(int) (gvHMax * dm.heightPixels)));
		}
		this.mGridView.setGravity(Gravity.CENTER);// 对齐方式
		this.mGridView.setNumColumns(3);// 列数
		this.mGridView.setHorizontalSpacing(0);// 元素间水平间距
		this.mGridView.setVerticalSpacing(10);// 元素间垂直间距
		this.mGridView.setStretchMode(1);// 拉伸模式
		this.mGridView.setColumnWidth((int) (100.0F * this.mDensity));// 列的宽度
		this.mGridView.setHorizontalScrollBarEnabled(false);
		this.mGridView.setVerticalScrollBarEnabled(true);
		this.mGridView.setSelector(new ColorDrawable(Color
				.parseColor("#eaeaea")));

		this.mLayout.addView(this.mTextView);
		this.mLayout.addView(this.mGridView);
	}

	public void queryPackage(Context context) {// 查询符合条件的软件包
		Intent mIntent = new Intent("android.intent.action.SEND");
		mIntent.setType("image/*");// 条件1:可以分享inage文件
		List<ResolveInfo> packages = context.getPackageManager()
				.queryIntentActivities(mIntent, 0);
		List<ResolveInfo> Newpackages = new ArrayList<ResolveInfo>();

		wechatIsInstalled = false;
		qqIsInstalled = false;
		qzoneIsInstalled = false;
		weiboIsInstalled = false;

		for (int i = packages.size() - 1; i >= 0; i--) {
			ResolveInfo packageInfo = packages.get(i);
			packageInfo.loadLabel(context.getPackageManager()).toString();
			packageName = packageInfo.activityInfo.packageName;
			packageInfo.loadIcon(context.getPackageManager());

			if (packageName.equals("com.tencent.mm")) {
				wechatIsInstalled = true;
			}
			if (packageName.equals("com.tencent.mobileqq")) {
				qqIsInstalled = true;
			}
			if (packageName.equals("com.qzone")) {
				qzoneIsInstalled = true;
			}
			if (packageName.equals("com.sina.weibo")) {
				weiboIsInstalled = true;
			}

			mListData = new ArrayList<ShareItem>();
			if (wechatIsInstalled) {
				mListData.add(new ShareItem("微信", "com.tencent.mm",
						"com.tencent.mm.ui.tools.ShareImgUI", context
								.getResources().getDrawable(
										R.drawable.logo_wechat)));
				mListData.add(new ShareItem("朋友圈", "com.tencent.mm",
						"com.tencent.mm.ui.tools.ShareToTimeLineUI", context
								.getResources().getDrawable(
										R.drawable.logo_wechatmoments)));
			}
			if (qqIsInstalled) {
				mListData
						.add(new ShareItem("QQ好友", "com.tencent.mobileqq",
								"com.tencent.mobileqq.activity.JumpActivity",
								context.getResources().getDrawable(
										R.drawable.logo_qq)));
			}
			if (qzoneIsInstalled) {
				mListData.add(new ShareItem("QQ空间", "com.qzone",
						"com.qzone.ui.operation.QZonePublishMoodActivity",
						context.getResources().getDrawable(
								R.drawable.logo_qzone)));
			}
			if (weiboIsInstalled) {
				mListData.add(new ShareItem("微博", "com.sina.weibo",
						"com.sina.weibo.EditActivity", context.getResources()
								.getDrawable(R.drawable.logo_sinaweibo)));
			}

			mListData.add(new ShareItem("短信", "", "", context.getResources()
					.getDrawable(R.drawable.logo_shortmessage)));
			mListData.add(new ShareItem("邮件", "", "", context.getResources()
					.getDrawable(R.drawable.logo_email)));

			if (!packageName.contains("mail") && !packageName.contains("mms")
					&& !packageName.equals("com.tencent.mm")
					&& !packageName.equals("com.tencent.mobileqq")
					&& !packageName.equals("com.qzone")
					&& !packageName.equals("com.sina.weibo")) {// 条件2:不包含限定字符的软件包名
				Newpackages.add(packageInfo);
			}

			for (int j = 0; j < Newpackages.size(); j++) {
				mListData.add(new ShareItem(Newpackages.get(j)
						.loadLabel(context.getPackageManager()).toString(),
						Newpackages.get(j).activityInfo.packageName,
						Newpackages.get(j).activityInfo.name, Newpackages
								.get(j).loadIcon(context.getPackageManager())));
			}
		}
	}

	private class ShareItem {
		private String appName;
		private String packageName;
		private String activityName;
		private Drawable appIcon;

		public ShareItem(String appName, String packageName,
				String activityName, Drawable appIcon) {
			this.appName = appName;
			this.packageName = packageName;
			this.activityName = activityName;
			this.appIcon = appIcon;
		}
	}

	private class Adapter extends BaseAdapter {
		private final int image_id = 256;
		private final int tv_id = 512;

		@Override
		public int getCount() {
			return mListData.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getItemView();
			}
			ImageView iv = (ImageView) convertView.findViewById(image_id);
			TextView tv = (TextView) convertView.findViewById(tv_id);
			ShareImage.ShareItem item = (ShareImage.ShareItem) mListData
					.get(position);
			iv.setImageDrawable(item.appIcon);
			tv.setText(item.appName);
			return convertView;
		}

		private View getItemView() {
			LinearLayout item = new LinearLayout(getContext());
			item.setOrientation(1);
			int padding = (int) (10.0F * mDensity);
			item.setPadding(padding, padding, padding, padding);
			item.setGravity(17);

			ImageView iv = new ImageView(getContext());
			item.addView(iv);
			iv.setLayoutParams(new LinearLayout.LayoutParams(96, 96));
			iv.setId(image_id);

			TextView tv = new TextView(getContext());
			item.addView(tv);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					-1, 100);
			layoutParams.topMargin = ((int) (5.0F * mDensity));
			tv.setLayoutParams(layoutParams);
			tv.setTextColor(Color.parseColor("#212121"));
			tv.setMaxLines(2);
			tv.setEllipsize(android.text.TextUtils.TruncateAt.valueOf("END"));
			tv.setTextSize(14.0F);
			tv.setGravity(Gravity.CENTER);
			tv.setId(tv_id);

			return item;
		}

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		ShareItem share = (ShareItem) this.mListData.get(position);
		
		HashMap<String, String> map = new HashMap<String, String>();// 友盟统计
		map.put("分享App", share.appName);// 友盟统计
		MobclickAgent.onEvent(getContext(), "Share", map);// 友盟统计

		Intent it = new Intent(Intent.ACTION_SEND);
		it.setType("image/*");
		List<ResolveInfo> resInfo = getContext().getPackageManager()
				.queryIntentActivities(it, 0);

		if (share.appName.equals("短信")) {
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
					targeted.putExtra(Intent.EXTRA_TEXT, "");
					targeted.setPackage(activityInfo.packageName);
					targetedShareIntents.add(targeted);
				}
			}

			Intent chooserIntent = Intent.createChooser(
					targetedShareIntents.remove(0), "选择以下方式分享给好友:");
			if (chooserIntent == null) {
				dismiss();
				return;
			}

			chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
					targetedShareIntents.toArray(new Parcelable[] {}));

			try {
				getContext().startActivity(chooserIntent);
			} catch (android.content.ActivityNotFoundException ex) {
			}
			dismiss();
			return;
		} else if (share.appName.equals("邮件")) {
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
					targeted.putExtra(Intent.EXTRA_TEXT, "");
					targeted.setPackage(activityInfo.packageName);
					targetedShareIntents.add(targeted);
				}
			}
			Intent chooserIntent = Intent.createChooser(
					targetedShareIntents.remove(0), "选择以下方式分享给好友:");
			if (chooserIntent == null) {
				dismiss();
				return;
			}
			chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
					targetedShareIntents.toArray(new Parcelable[] {}));
			try {
				getContext().startActivity(chooserIntent);
			} catch (android.content.ActivityNotFoundException ex) {
			}
			dismiss();
			return;
		}

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

		intent.putExtra(Intent.EXTRA_SUBJECT, "");
		intent.putExtra(Intent.EXTRA_TEXT, "");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		if (!share.packageName.isEmpty()) {
			intent.setComponent(new ComponentName(share.packageName,
					share.activityName));
			getContext().startActivity(intent);
		} else {
			getContext().startActivity(Intent.createChooser(intent, ""));
		}
		dismiss();
	}
}