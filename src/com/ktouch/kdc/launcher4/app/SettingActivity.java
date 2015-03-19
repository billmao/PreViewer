package com.ktouch.kdc.launcher4.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ktouch.kdc.launcher4.R;
import com.ktouch.kdc.launcher4.draglistview.SettingFilter;
import com.ktouch.kdc.launcher4.feedback.FeedBackActivity;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;

public class SettingActivity extends Activity {
	ListView mListView;
	MyAdapter adapter;
	ImageView mImageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_page);
		mListView = (ListView) findViewById(R.id.listViewSetting);
		mImageView = (ImageView) findViewById(R.id.imageViewsettingBack);
		adapter = new MyAdapter(this);
		mListView.setAdapter(adapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position) {
				case 0:
					Intent i0 = new Intent(SettingActivity.this,
							SettingFilter.class);
					startActivity(i0);
					break;
				case 1:
					// do nothing
					break;
				case 2:
					Intent i1 = new Intent(SettingActivity.this,
							FeedBackActivity.class);
					startActivity(i1);
					break;
				case 3:
					Intent i2 = new Intent(SettingActivity.this,
							AboutActivity.class);
					startActivity(i2);
					break;
				case 4:
					UmengUpdateAgent.setUpdateAutoPopup(false);
					UmengUpdateAgent
							.setUpdateListener(new UmengUpdateListener() {
								@Override
								public void onUpdateReturned(int updateStatus,
										UpdateResponse updateInfo) {
									switch (updateStatus) {
									case UpdateStatus.Yes: // has update
										UmengUpdateAgent.showUpdateDialog(
												SettingActivity.this,
												updateInfo);
										break;
									case UpdateStatus.No: // has no update
										Toast.makeText(SettingActivity.this,
												"当前已经是最新版本", Toast.LENGTH_SHORT)
												.show();
										break;
									// case UpdateStatus.NoneWifi: // none wifi
									// Toast.makeText(SettingActivity.this,
									// "没有wifi连接， 只在wifi下更新",
									// Toast.LENGTH_SHORT).show();
									// break;
									case UpdateStatus.Timeout: // time out
										Toast.makeText(SettingActivity.this,
												"获取更新超时", Toast.LENGTH_SHORT)
												.show();
										break;
									}
								}
							});
					UmengUpdateAgent.forceUpdate(SettingActivity.this);// 友盟
																		// 检查更新
																		// 默认Wi-Fi
																		// Only
					break;
				}
			}
		});

		mImageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});

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

	class MyAdapter extends BaseAdapter {

		Context context;
		TextView mTextView;
		ImageView mImageView;

		public MyAdapter(Context context) {
			this.context = context;
		}

		@Override
		public int getCount() {
			return 5;
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
			LinearLayout li = null;
			if (convertView == null) {
				switch (position) {
				case 0:
					li = (LinearLayout) LinearLayout.inflate(context,
							R.layout.setting_item2, null);
					mTextView = (TextView) li
							.findViewById(R.id.textViewsettingitem2);
					mImageView = (ImageView) li
							.findViewById(R.id.imageViewSettingBefore);
					mImageView.setImageResource(R.drawable.folder1);
					mTextView.setText("隐藏文件夹");
					// mTextView.setTextColor(Color.BLACK);
					break;
				case 1:
					li = (LinearLayout) LinearLayout.inflate(context,
							R.layout.setting_item3, null);
					break;
				case 2:
					li = (LinearLayout) LinearLayout.inflate(context,
							R.layout.setting_item2, null);
					mTextView = (TextView) li
							.findViewById(R.id.textViewsettingitem2);
					mImageView = (ImageView) li
							.findViewById(R.id.imageViewSettingBefore);
					mImageView.setImageResource(R.drawable.feedback);
					// mTextView.setTextColor(Color.BLACK); //bill delete
					mTextView.setText("意见反馈");
					break;
				case 3:
					li = (LinearLayout) LinearLayout.inflate(context,
							R.layout.setting_item2, null);
					mTextView = (TextView) li
							.findViewById(R.id.textViewsettingitem2);
					mImageView = (ImageView) li
							.findViewById(R.id.imageViewSettingBefore);
					mImageView.setImageResource(R.drawable.apage);
					// mTextView.setTextColor(Color.BLACK); //bill delete
					mTextView.setText("关于");
					break;
				case 4:
					li = (LinearLayout) LinearLayout.inflate(context,
							R.layout.setting_item2, null);
					mTextView = (TextView) li
							.findViewById(R.id.textViewsettingitem2);
					mImageView = (ImageView) li
							.findViewById(R.id.imageViewSettingBefore);
					mImageView.setImageResource(R.drawable.apage);
					// mTextView.setTextColor(Color.BLACK); //bill delete
					mTextView.setText("检查更新");
					break;
				default:

					break;
				}

			} else {
				li = (LinearLayout) convertView;
			}
			return li;
		}

	}
}
