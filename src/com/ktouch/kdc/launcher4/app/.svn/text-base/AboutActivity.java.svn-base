package com.ktouch.kdc.launcher4.app;

import com.ktouch.kdc.launcher4.R;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

//liufengzen create
public class AboutActivity extends Activity {
	private RelativeLayout actionBar;
	private RelativeLayout modemenubar;
	private TextView title;
	private ImageView albumsetback;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_previewer_about);
		getViews();
		setListener();
	}

	public void getViews() {
		actionBar = (RelativeLayout) findViewById(R.id.actionbar);
		modemenubar = (RelativeLayout) findViewById(R.id.modemenubar);
		albumsetback = (ImageView) findViewById(R.id.albumsetback);
		title = (TextView) findViewById(R.id.modetitle);
	}

	public void setListener() {
		actionBar.setVisibility(View.GONE);
		modemenubar.setVisibility(View.VISIBLE);
		title.setText(R.string.previewer_about_title);
		albumsetback.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				AboutActivity.this.finish();
			}

		});
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onResume(this);// 友盟统计
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPause(this);// 友盟统计
	}
}
