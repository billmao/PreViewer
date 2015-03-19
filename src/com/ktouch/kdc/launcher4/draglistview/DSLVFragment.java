package com.ktouch.kdc.launcher4.draglistview;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.support.v4.app.ListFragment;
import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ktouch.kdc.launcher4.R;
import com.ktouch.kdc.launcher4.data.AlbumFilterDatabase;
import com.ktouch.kdc.launcher4.ui.Log;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortController;

public class DSLVFragment extends ListFragment {

	filterAdapter adapter;

	private String[] array;
	private Cursor mCursor;
	private Handler mHandler = new Handler();
	List<HashMap<String, String>> list = null;
	HashMap<String, String> mHashMap = null;

	private AlbumFilterDatabase mDatabase;

	private DragSortListView.DropListener onDrop = new DragSortListView.DropListener() {
		@Override
		public void drop(int from, int to) {
			if (from != to) {
				HashMap<String, String> item = adapter.getItem(from);
				adapter.remove(item);
				adapter.insert(item, to);
				mHandler.post(updataDatabase);
			}
		}
	};

	private Runnable updataDatabase = new Runnable() {

		@Override
		public void run() {
			mDatabase.openMyDataBase();
			mDatabase.delAll();
			for (int i = 0; i < adapter.getCount(); i++) {
				mDatabase.insertAlbumName(list.get(i).get("name"), list.get(i)
						.get("size"), list.get(i).get("filt"));
			}
			mDatabase.closeMyDataBase();
		}
	};

	// 初始化数据
	public void initData() {

		list = new ArrayList<HashMap<String, String>>();
		list.clear();
		mDatabase = new AlbumFilterDatabase(getActivity());
		mDatabase.openMyDataBase();
		mCursor = mDatabase.queryAll();
		mCursor.moveToPrevious();
		while (mCursor.moveToNext()) {
			String name = mCursor
					.getString(mCursor.getColumnIndex("AlbumName"));
			String filt = mCursor.getString(mCursor.getColumnIndex("Filt"));
			String size = mCursor
					.getString(mCursor.getColumnIndex("AlbumSize"));
			mHashMap = new HashMap<String, String>();
			mHashMap.put("name", name);
			mHashMap.put("size", size);
			mHashMap.put("filt", filt);
			list.add(mHashMap);
		}
		mDatabase.closeMyDataBase();
	}

	private DragSortListView.RemoveListener onRemove = new DragSortListView.RemoveListener() {
		@Override
		public void remove(int which) {
			adapter.remove(adapter.getItem(which));
		}
	};

	protected int getLayout() {
		return R.layout.dslv_fragment_main;
	}

	protected int getItemLayout() {
		if (removeMode == DragSortController.CLICK_REMOVE) {
			return R.layout.list_item_click_remove;
		} else {
			return R.layout.list_item_handle_left;
		}
	}

	private DragSortListView mDslv;
	private DragSortController mController;

	public int dragStartMode = DragSortController.ON_DOWN;
	public boolean removeEnabled = true;
	public int removeMode = DragSortController.FLING_REMOVE;
	public boolean sortEnabled = true;
	public boolean dragEnabled = true;

	public static DSLVFragment newInstance(int headers, int footers) {
		DSLVFragment f = new DSLVFragment();

		Bundle args = new Bundle();
		args.putInt("headers", headers);
		args.putInt("footers", footers);
		f.setArguments(args);

		return f;
	}

	public DragSortController getController() {
		return mController;
	}

	public void setListAdapter() {
		// mDatabase = new AlbumFilterDatabase(getActivity());
		// mDatabase.openMyDataBase();
		// mCursor = mDatabase.queryAll();
		// mCursor.moveToPrevious();
		// array = new String[mDatabase.queryRaw()];
		// int i = 0;
		// while (mCursor.moveToNext()) {
		// array[i] = mCursor.getString(mCursor.getColumnIndex("AlbumName"));
		// i++;
		// }
		// mDatabase.closeMyDataBase();

		initData();

		adapter = new filterAdapter(getActivity(), list);

		setListAdapter(adapter);
	}

	public DragSortController buildController(DragSortListView dslv) {
		DragSortController controller = new DragSortController(dslv);
		controller.setDragHandleId(R.id.drag_handle);
		controller.setClickRemoveId(R.id.click_remove);
		controller.setRemoveEnabled(removeEnabled);
		controller.setSortEnabled(sortEnabled);
		controller.setDragInitMode(dragStartMode);
		controller.setRemoveMode(removeMode);
		return controller;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mDslv = (DragSortListView) inflater.inflate(getLayout(), container,
				false);

		mController = buildController(mDslv);
		mDslv.setFloatViewManager(mController);
		mDslv.setOnTouchListener(mController);
		mDslv.setDragEnabled(dragEnabled);

		return mDslv;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mDslv = (DragSortListView) getListView();

		mDslv.setDropListener(onDrop);
		mDslv.setRemoveListener(onRemove);

		Bundle args = getArguments();
		int headers = 0;
		int footers = 0;
		if (args != null) {
			headers = args.getInt("headers", 0);
			footers = args.getInt("footers", 0);
		}

		for (int i = 0; i < headers; i++) {
			addHeader(getActivity(), mDslv);
		}
		for (int i = 0; i < footers; i++) {
			addFooter(getActivity(), mDslv);
		}

		setListAdapter();
	}

	public static void addHeader(Activity activity, DragSortListView dslv) {
		LayoutInflater inflater = activity.getLayoutInflater();
		int count = dslv.getHeaderViewsCount();

		TextView header = (TextView) inflater.inflate(R.layout.header_footer,
				null);
		header.setText("Header #" + (count + 1));

		dslv.addHeaderView(header, null, false);
	}

	public static void addFooter(Activity activity, DragSortListView dslv) {
		LayoutInflater inflater = activity.getLayoutInflater();
		int count = dslv.getFooterViewsCount();

		TextView footer = (TextView) inflater.inflate(R.layout.header_footer,
				null);
		footer.setText("Footer #" + (count + 1));

		dslv.addFooterView(footer, null, false);
	}

	class filterAdapter extends ArrayAdapter<HashMap<String, String>> {

		public filterAdapter(Context context,
				List<HashMap<String, String>> objects) {
			super(context, 0, objects);
		}

		public List<HashMap<String, String>> getList() {
			return list;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {

			View view = convertView;
			// view = LayoutInflater.from(getContext()).inflate(
			// R.layout.setting_filter_folder_item, null);
			// TextView textView = (TextView) view
			// .findViewById(R.id.drag_list_item_text);
			// ImageView mImageView = (ImageView) view
			// .findViewById(R.id.drag_handle22);
			// final Button mButton = (Button) view
			// .findViewById(R.id.buttonFilter1);

			view = LayoutInflater.from(getContext()).inflate(
					R.layout.list_item_handle_left, null);
			TextView textView = (TextView) view
					.findViewById(R.id.drag_list_item_text2);
			TextView textViewSize = (TextView) view
					.findViewById(R.id.drag_list_item_textSize);
			ImageView mImageView = (ImageView) view
					.findViewById(R.id.drag_handle);
			final Button mButton = (Button) view
					.findViewById(R.id.buttonFilter2);

			textView.setText(getItem(position).get("name"));
			textViewSize.setText("(" + getItem(position).get("size") + ")");

			mDatabase.openMyDataBase();
			if (mDatabase.queryFiltByAlbumName(getItem(position).get("name"))) {
				if (getItem(position).get("name").equals("我的收藏")
						|| getItem(position).get("name").equals("Camera")) {
					mButton.setVisibility(View.INVISIBLE);
					if (getItem(position).get("name").equals("我的收藏")) {
						mImageView.setImageResource(R.drawable.mywallpaper);
					} else if (getItem(position).get("name").equals("Camera")) {
						mImageView.setImageResource(R.drawable.album);
					}
				} else {
					mButton.setBackgroundResource(R.drawable.more_off_bg_normal);
				}
			} else {
				if (getItem(position).get("name").equals("我的收藏")
						|| getItem(position).get("name").equals("Camera")) {
					mButton.setVisibility(View.INVISIBLE);
					if (getItem(position).get("name").equals("我的收藏")) {
						mImageView.setImageResource(R.drawable.mywallpaper);
					} else if (getItem(position).get("name").equals("Camera")) {
						mImageView.setImageResource(R.drawable.album);
					}
				} else {
					mButton.setBackgroundResource(R.drawable.more_on_bg_normal);
				}
			}
			mDatabase.closeMyDataBase();
			mButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mDatabase.openMyDataBase();
					if (!mDatabase.queryFiltByAlbumName(getItem(position).get(
							"name"))) {
						mButton.setBackgroundResource(R.drawable.more_off_bg_normal);
						mDatabase.updataFiltByName(getItem(position)
								.get("name"), "Y");
					} else {
						mButton.setBackgroundResource(R.drawable.more_on_bg_normal);
						mDatabase.updataFiltByName(getItem(position)
								.get("name"), "N");
					}
					Intent scanIntent = new Intent(
							Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
					scanIntent.setData(Uri.fromFile(new File(Environment
							.getExternalStorageDirectory().getPath()
							+ "/"
							+ getItem(position).get("name"))));
					getContext().sendBroadcast(scanIntent);
					mDatabase.closeMyDataBase();
					initData();
				}
			});
			return view;
		}
	}
}
