package com.ktouch.kdc.launcher4.app;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import com.ktouch.kdc.launcher4.R;
import com.ktouch.kdc.launcher4.common.Utils;
import com.ktouch.kdc.launcher4.data.AlbumFilterDatabase;
import com.ktouch.kdc.launcher4.data.ContentListener;
import com.ktouch.kdc.launcher4.data.DataManager;
import com.ktouch.kdc.launcher4.data.MediaItem;
import com.ktouch.kdc.launcher4.data.MediaObject;
import com.ktouch.kdc.launcher4.data.MediaSet;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

//bill create
public class AlbumSetActivityDataAdapter extends BaseAdapter {
	private static final int MSG_LOAD_START = 1;
	private static final int MSG_LOAD_FINISH = 2;
	private static final int MSG_RUN_OBJECT = 3;
	private static final int INDEX_NONE = -1;
	private final long[] mItemVersion;
	private final long[] mSetVersion;
	private long mSourceVersion = MediaObject.INVALID_DATA_VERSION;
	private int mContentStart = 0;
	private int mContentEnd = 0;
	private int mSize = 0;
	private int adapterLen = 0;
	private int selectItem = -1; // 设置选中项
	private final MediaSet mSource;
	private final Handler mMainHandler;
	private final MediaSet[] mData;
	private final MediaItem[][] mCoverData;
	private ReloadTask mReloadTask;
	private Context context;
	private MySourceListener mSourceListener = new MySourceListener();
	private LoadingListener mLoadingListener;
	AlbumFilterDatabase mDatabase;// lianglei
	int FilterNum = 0;// lianglei
	private final MediaSet[] nData;// lianglei
	private String sPath = "";

	// private AlbumSetActivityDataAdapter albumSetAdapter = null;

	public AlbumSetActivityDataAdapter(Context context, GalleryApp app,
			MediaSet albumSet, String path, int cacheSize) {
		this.context = context;
		mSource = Utils.checkNotNull(albumSet);
		mCoverData = new MediaItem[cacheSize][];
		mData = new MediaSet[cacheSize];
		sPath = path;
		mItemVersion = new long[cacheSize];
		mSetVersion = new long[cacheSize];
		mDatabase = new AlbumFilterDatabase(context);// lianglei
		mDatabase.openMyDataBase();// lianglei
		FilterNum = mDatabase.queryAlbumNumByFilt("Y");// lianglei
		mDatabase.closeMyDataBase();// lianglei
		nData = new MediaSet[cacheSize];// lianglei
		Arrays.fill(mItemVersion, MediaObject.INVALID_DATA_VERSION);
		Arrays.fill(mSetVersion, MediaObject.INVALID_DATA_VERSION);
		// 20140903
		if (path.contains("cluster")) {
			mSource.setDataVersion(-1);
		}
		mMainHandler = new Handler() {
			@Override
			public void handleMessage(Message message) {
				switch (message.what) {
				case MSG_RUN_OBJECT:
					((Runnable) message.obj).run();
					return;
				case MSG_LOAD_START:
					if (mLoadingListener != null)
						mLoadingListener.onLoadingStarted();
					return;
				case MSG_LOAD_FINISH:
					if (mLoadingListener != null)
						mLoadingListener.onLoadingFinished();
					return;
				}
			}
		};
		// albumSetAdapter = this;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return adapterLen - FilterNum;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	// 设置选中项
	public void setSelectItem(int selectItem) {
		this.selectItem = selectItem;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.layout_previewer_albumset_list_item, null);
			holder = new ViewHolder();
			// 初始化实例
			holder.name = (TextView) convertView.findViewById(R.id.name);
			holder.num = (TextView) convertView.findViewById(R.id.num);
			holder.icon = (ImageView) convertView.findViewById(R.id.icon_item);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		// del by lianglei
		// if (mData[position] != null) {
		// holder.name.setText(mData[position].getName()); //
		// 相册名称mData[position].getName()
		// holder.num.setText("" + mData[position].getMediaItemCount()); //
		// 相册中图片个数+mData[position].getMediaItemCount()
		// if (mData[position].getName().equals("我的壁纸")) {
		// holder.icon.setImageResource(R.drawable.mywallpaper);
		// } else if (mData[position].getName().equals("Camera")) {
		if (nData[position] != null) {
			holder.name.setText(nData[position].getName()); // 相册名称mData[position].getName()
			holder.name.setTextColor(Color.parseColor("#595959"));
			holder.num.setText("(" + nData[position].getMediaItemCount() + ")"); // 相册中图片个数+mData[position].getMediaItemCount()
			holder.num.setTextColor(Color.parseColor("#595959"));
			if (nData[position].getName().equals("我的收藏")) {
				holder.icon.setImageResource(R.drawable.mywallpaper);
			} else if (nData[position].getName().equals("Camera")) {
				holder.icon.setImageResource(R.drawable.album);
			} else {
				holder.icon.setImageResource(R.drawable.other);
			}
		}
		if (position == selectItem) {
			convertView.setBackgroundColor(Color.parseColor("#f5f5f5"));
			if (nData[position].getName().equals("我的收藏")) {
				holder.name.setTextColor(Color.parseColor("#ffc600"));
				holder.num.setTextColor(Color.parseColor("#ffc600"));
			} else if (nData[position].getName().equals("Camera")) {
				holder.name.setTextColor(Color.parseColor("#08a8ea"));
				holder.num.setTextColor(Color.parseColor("#08a8ea"));
			} else {
				holder.name.setTextColor(Color.parseColor("#55d41d"));
				holder.num.setTextColor(Color.parseColor("#55d41d"));
			}

		} else {
			convertView.setBackgroundColor(Color.parseColor("#ffffff"));
		}
		return convertView;
	}

	public void pause() {
		if (mReloadTask != null) {
			mReloadTask.terminate();
			mReloadTask = null;
			mSource.removeContentListener(mSourceListener);
		}
	}

	public void resume() {
		mSource.addContentListener(mSourceListener);
		mReloadTask = new ReloadTask();
		mReloadTask.start();
	}

	public MediaSet getMediaSet(int index) {
		// del by lianglei
		// return mData[index % mData.length];
		return nData[index % nData.length];
	}

	private class MySourceListener implements ContentListener {
		public void onContentDirty() {
			mReloadTask.notifyDirty();
		}
	}

	public void setLoadingListener(LoadingListener listener) {
		mLoadingListener = listener;
	}

	private static class UpdateInfo {
		public long version;
		public int index;

		public int size;
		public MediaSet item;
		public MediaItem covers[];
	}

	private class GetUpdateInfo implements Callable<UpdateInfo> {

		private final long mVersion;

		public GetUpdateInfo(long version) {
			mVersion = version;
		}

		private int getInvalidIndex(long version) {
			long setVersion[] = mSetVersion;
			int length = setVersion.length;
			for (int i = mContentStart, n = mContentEnd; i < n; ++i) {
				int index = i % length;
				if (setVersion[i % length] != version)
					return i;
			}
			return INDEX_NONE;
		}

		@Override
		public UpdateInfo call() throws Exception {
			int index = getInvalidIndex(mVersion);
			if (index == INDEX_NONE && mSourceVersion == mVersion)
				return null;
			UpdateInfo info = new UpdateInfo();
			info.version = mSourceVersion;
			info.index = index;
			info.size = mSize;
			return info;
		}
	}

	private class UpdateContent implements Callable<Void> {
		private final UpdateInfo mUpdateInfo;

		public UpdateContent(UpdateInfo info) {
			mUpdateInfo = info;
		}

		public Void call() {
			// Avoid notifying listeners of status change after pause
			// Otherwise gallery will be in inconsistent state after resume.
			if (mReloadTask == null)
				return null;
			UpdateInfo info = mUpdateInfo;
			mSourceVersion = info.version;
			if (mSize != info.size) {
				mSize = info.size;
				mContentEnd = mSize;
				if (mContentEnd > mSize)
					mContentEnd = mSize;
			}
			adapterLen = info.size; // 个数
			// Note: info.index could be INDEX_NONE, i.e., -1
			if (info.index >= mContentStart && info.index < mContentEnd) {
				int pos = info.index % mCoverData.length;
				mSetVersion[pos] = info.version;
				long itemVersion = info.item.getDataVersion();
				if (mItemVersion[pos] == itemVersion)
					return null;
				mItemVersion[pos] = itemVersion;
				if (info.item.getName().equals("我的收藏")) {
					for (int j = pos; j >= 0; j--) {
						mData[j + 1] = mData[j];
					}
					mData[0] = info.item;
				} else {
					mData[pos] = info.item;
				}
				// lianglei begin
				if (!sPath.startsWith("/cluster")) {
					if (pos + 1 == adapterLen) {
						mDatabase.openMyDataBase();
						FilterNum = mDatabase.queryAlbumNumByFilt("Y"); // bill
																		// add
						for (int i = 0; i < adapterLen; i++) {
							if (mDatabase.queryAlbumNameByAlbumName(
									mData[i].getName(), "Y")
									|| mDatabase.queryAlbumNameByAlbumName(
											mData[i].getName(), "N")) {
								// do nothing
							} else {
								if (mData[i].getName().equals("我的收藏")
										|| mData[i].getName().equals("Camera")) {
									mDatabase.insertAlbumName(
											mData[i].getName(),
											mData[i].getMediaItemCount() + "",
											"N");
								} else {
									mDatabase.insertAlbumName(
											mData[i].getName(),
											mData[i].getMediaItemCount() + "",
											"Y");
								}
								Log.v("lianglei", mData[i].getName() + ":"
										+ mData[i].getMediaItemCount());
							}
						}
						int m1 = 0;
						for (int i = 0; i < adapterLen; i++) {
							if (mDatabase.queryAlbumNameByAlbumName(
									mData[i].getName(), "N")) {
								nData[m1] = mData[i];
								m1++;
							} else {
								// do nothing
							}
						}
						// 对比数据
						Cursor mCursor = mDatabase.queryAll();
						mCursor.moveToPrevious();
						String AlbumName;
						while (mCursor.moveToNext()) {
							boolean inData = false;
							AlbumName = mCursor.getString(mCursor
									.getColumnIndex("AlbumName"));
							for (int j = 0; j < adapterLen; j++) {// 对比数据
								if (AlbumName.equals(mData[j].getName())) {
									inData = true;
								}
							}
							if (!inData) {
								mDatabase.delByAlbumName(AlbumName);
							}
						}
						mCursor.close();
						mDatabase.closeMyDataBase();
						// lianglei end
					}
				}
				mCoverData[pos] = info.covers;
			}
			notifyDataSetChanged();

			return null;
		}
	}

	private <T> T executeAndWait(Callable<T> callable) {
		FutureTask<T> task = new FutureTask<T>(callable);
		mMainHandler.sendMessage(mMainHandler.obtainMessage(MSG_RUN_OBJECT,
				task));
		try {
			return task.get();
		} catch (InterruptedException e) {
			return null;
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	private class ReloadTask extends Thread {
		private volatile boolean mActive = true;
		private volatile boolean mDirty = true;
		private volatile boolean mIsLoading = false;

		private void updateLoading(boolean loading) {
			if (mIsLoading == loading)
				return;
			mIsLoading = loading;
			mMainHandler.sendEmptyMessage(loading ? MSG_LOAD_START
					: MSG_LOAD_FINISH);
		}

		@Override
		public void run() {
			boolean updateComplete = false;
			while (mActive) {
				synchronized (this) {
					if (mActive && !mDirty && updateComplete) {
						updateLoading(false);
						Utils.waitWithoutInterrupt(this);
						continue;
					}
				}
				mDirty = false;
				updateLoading(true);
				long version;
				synchronized (DataManager.LOCK) {
					version = mSource.reload();
				}
				UpdateInfo info = executeAndWait(new GetUpdateInfo(version));
				updateComplete = info == null;
				if (updateComplete) {
					continue;
				}

				synchronized (DataManager.LOCK) {
					if (info.version != version) {
						info.version = version;
						info.size = mSource.getSubMediaSetCount();
						// If the size becomes smaller after reload(), we may
						// receive from GetUpdateInfo an index which is too
						// big. Because the main thread is not aware of the size
						// change until we call UpdateContent.
						if (info.index >= info.size) {
							info.index = INDEX_NONE;
						}
					}
					if (info.index != INDEX_NONE) {
						info.item = mSource.getSubMediaSet(info.index);
						if (info.item == null)
							continue;
						MediaItem cover = info.item.getCoverMediaItem();
						info.covers = cover == null ? new MediaItem[0]
								: new MediaItem[] { cover };
					}
				}
				executeAndWait(new UpdateContent(info));
			}
			updateLoading(false);
		}

		public synchronized void notifyDirty() {
			mDirty = true;
			notifyAll();
		}

		public synchronized void terminate() {
			mActive = false;
			notifyAll();
		}
	}

	class ViewHolder {
		public TextView name;
		public TextView num;
		public ImageView icon;
	}
}