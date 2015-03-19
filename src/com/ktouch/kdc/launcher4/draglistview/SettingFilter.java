package com.ktouch.kdc.launcher4.draglistview;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.ktouch.kdc.launcher4.R;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.umeng.analytics.MobclickAgent;

public class SettingFilter extends FragmentActivity implements
		RemoveModeDialog.RemoveOkListener, DragInitModeDialog.DragOkListener,
		EnablesDialog.EnabledOkListener {

	private int mNumHeaders = 0;
	private int mNumFooters = 0;

	private int mDragStartMode = DragSortController.ON_DRAG;
	private boolean mRemoveEnabled = false;
	private int mRemoveMode = DragSortController.FLING_REMOVE;
	private boolean mSortEnabled = true;
	private boolean mDragEnabled = true;

	private String mTag = "dslvTag";
	ImageView mimImageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_bed_main);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.test_bed, getNewDslvFragment(), mTag).commit();
		}
		mimImageView = (ImageView) findViewById(R.id.imageViewsettingFilterBack);
		mimImageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}

	@Override
	public void onRemoveOkClick(int removeMode) {
		if (removeMode != mRemoveMode) {
			mRemoveMode = removeMode;
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.test_bed, getNewDslvFragment(), mTag)
					.commit();
		}
	}

	@Override
	public void onDragOkClick(int dragStartMode) {
		mDragStartMode = dragStartMode;
		DSLVFragment f = (DSLVFragment) getSupportFragmentManager()
				.findFragmentByTag(mTag);
		f.getController().setDragInitMode(dragStartMode);
	}

	@Override
	public void onEnabledOkClick(boolean drag, boolean sort, boolean remove) {
		mSortEnabled = sort;
		mRemoveEnabled = remove;
		mDragEnabled = drag;
		DSLVFragment f = (DSLVFragment) getSupportFragmentManager()
				.findFragmentByTag(mTag);
		DragSortListView dslv = (DragSortListView) f.getListView();
		f.getController().setRemoveEnabled(remove);
		f.getController().setSortEnabled(sort);
		dslv.setDragEnabled(drag);
	}

	private Fragment getNewDslvFragment() {
		DSLVFragmentClicks f = DSLVFragmentClicks.newInstance(mNumHeaders,
				mNumFooters);
		f.removeMode = mRemoveMode;
		f.removeEnabled = mRemoveEnabled;
		f.dragStartMode = mDragStartMode;
		f.sortEnabled = mSortEnabled;
		f.dragEnabled = mDragEnabled;
		return f;
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
