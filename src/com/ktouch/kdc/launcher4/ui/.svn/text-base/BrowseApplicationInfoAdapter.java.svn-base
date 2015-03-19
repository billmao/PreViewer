package com.ktouch.kdc.launcher4.ui;

import java.util.List;

import com.ktouch.kdc.launcher4.R;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/*
 * author by lianglei @k-touch
 */

public class BrowseApplicationInfoAdapter extends BaseAdapter {
	private List<AppInfo> mlistAppInfo = null;
	LayoutInflater infater = null;

	public BrowseApplicationInfoAdapter(Context context, List<AppInfo> apps) {
		infater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mlistAppInfo = apps;
	}

	@Override
	public int getCount() {
		if (mlistAppInfo.size() > More_AndroidShare.numGridView) {
			return More_AndroidShare.numGridView;
		} else {
			return mlistAppInfo.size();
		}
	}

	@Override
	public Object getItem(int position) {
		return mlistAppInfo.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertview, ViewGroup arg2) {
		View view = null;
		ViewHolder holder = null;
		if (convertview == null || convertview.getTag() == null) {
			view = infater.inflate(R.layout.browse_app_item, null);
			holder = new ViewHolder(view);
			view.setTag(holder);
		} else {
			view = convertview;
			holder = (ViewHolder) convertview.getTag();
		}

		if (More_AndroidShare.numGridView >= mlistAppInfo.size()) {
			AppInfo appInfo = (AppInfo) getItem(position);
			holder.appIcon.setImageDrawable(appInfo.getAppIcon());
			holder.tvAppLabel.setText(appInfo.getAppLabel());
		} else {
			if (position >= 0 && position < 7) {
				AppInfo appInfo = (AppInfo) getItem(position);
				holder.appIcon.setImageDrawable(appInfo.getAppIcon());
				holder.tvAppLabel.setText(appInfo.getAppLabel());
			} else {
				holder.appIcon.setImageResource(R.drawable.more);
				holder.tvAppLabel.setText(R.string.more);
			}
		}
		return view;
	}

	class ViewHolder {
		ImageView appIcon;
		TextView tvAppLabel;

		public ViewHolder(View view) {
			this.appIcon = (ImageView) view.findViewById(R.id.imgApp);
			this.tvAppLabel = (TextView) view.findViewById(R.id.tvAppLabel);
			tvAppLabel.setTextColor(Color.WHITE);
		}
	}
}