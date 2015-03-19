package com.ktouch.kdc.launcher4.app;

import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import com.ktouch.kdc.launcher4.R;
import com.ktouch.kdc.launcher4.common.BitmapUtils;
import com.ktouch.kdc.launcher4.common.LruCache;
import com.ktouch.kdc.launcher4.common.Utils;
import com.ktouch.kdc.launcher4.data.ContentListener;
import com.ktouch.kdc.launcher4.data.DataManager;
import com.ktouch.kdc.launcher4.data.LocalImage;
import com.ktouch.kdc.launcher4.data.MediaDetails;
import com.ktouch.kdc.launcher4.data.MediaItem;
import com.ktouch.kdc.launcher4.data.MediaObject;
import com.ktouch.kdc.launcher4.data.MediaSet;
import com.ktouch.kdc.launcher4.data.Path;
import com.ktouch.kdc.launcher4.picasasource.PicasaSource;
import com.ktouch.kdc.launcher4.ui.AbstractDisplayItem;
import com.ktouch.kdc.launcher4.ui.AlbumImageView;
import com.ktouch.kdc.launcher4.ui.AlbumView;
import com.ktouch.kdc.launcher4.ui.BitmapTexture;
import com.ktouch.kdc.launcher4.ui.FadeInTexture;
import com.ktouch.kdc.launcher4.ui.GLCanvas;
import com.ktouch.kdc.launcher4.ui.Log;
import com.ktouch.kdc.launcher4.ui.PositionRepository;
import com.ktouch.kdc.launcher4.ui.PreviewerSelectionManager.AddAllItemToList;
import com.ktouch.kdc.launcher4.ui.SynchronizedHandler;
import com.ktouch.kdc.launcher4.ui.Texture;
import com.ktouch.kdc.launcher4.ui.albumTimeTextView;
import com.ktouch.kdc.launcher4.util.Future;
import com.ktouch.kdc.launcher4.util.FutureListener;
import com.ktouch.kdc.launcher4.util.GalleryUtils;
import com.ktouch.kdc.launcher4.util.JobLimiter;
import com.ktouch.kdc.launcher4.util.ThreadPool;
import com.ktouch.kdc.launcher4.util.ThreadPool.Job;
import com.ktouch.kdc.launcher4.util.ThreadPool.JobContext;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

//bill create 20140728
public class AlbumActivityDataAdapter extends BaseAdapter implements 
 					StickyListHeadersAdapter, SectionIndexer,AddAllItemToList{
	private final int DATA_CACHE_SIZE = 1000;
	private final int MSG_LOAD_START = 1;
	private final int MSG_LOAD_FINISH = 2;
	private final int MSG_RUN_OBJECT = 3;
	private final int MAX_LOAD_COUNT = 64;
	private final int IMAGENUM_LISTVIEW_PER_ITEM = 8;
	private final Handler mMainHandler;
	// private final MediaItem[] mData;
	private final ThreadPool mThreadPool;
	private final long[] mItemVersion;
	private final long[] mSetVersion;
	private long mSourceVersion = MediaObject.INVALID_DATA_VERSION;
	private final int MSG_LOAD_BITMAP_DONE = 0; // 以下是图片加载到视图中
	private int mContentStart = 0;
	private int mContentEnd = 0;
	private int mSize = 0;
	private int remainder = 0; // list.size()%8 //最后一个item中包含的子项数目
	private int result = 0; // listview中item个数
	private int mCacheThumbSize; // 0: Don't cache the thumbnails
	private int adapterLen = 0;
	private boolean isSelectTemplate = false;
	private Path mMediaSetPath;
	private MediaSet mSource; // 原来为final
	private MySourceListener mSourceListener = new MySourceListener();
	private LoadingListener mLoadingListener;
	private ReloadTask mReloadTask;
	private Context context;
	private AlbumDisplayItem update = null;
	private AlbumActivityDataAdapter albumAdapter = null;
	private GalleryApp galleryApp = null;
	private ArrayList<MediaItem> albumImages = new ArrayList<MediaItem>();
	public static LruCache<Path, Bitmap> mImageCache = new LruCache<Path, Bitmap>(
			1000);
	private Bitmap bm1 = null, bm2 = null, bm3 = null, bm4 = null, bm5 = null,
			bm6 = null, bm7 = null, bm8 = null;
	private Bitmap bmArray[] = { bm1, bm2, bm3, bm4, bm5, bm6, bm7, bm8 };
	private AlbumDisplayItem mDisplayData[];
	private StickyListHeadersListView mlistView = null;
	private boolean isAllPaper = false;
	private float height_screen, width_screen;
	private float scaleMax, scaleMin;
    private WindowManager window;
    private int screenwidth = 0;
    private int screenHeight = 0;
	private float density=0;
    private Position[] mSectionIndices;
    private String[] mSectionLetters;
    private int todayNum = 0;
    private int yestodayNum = 0;
    private int weekNum = 0;
    private int monthNum = 0;
    private int otherNum = 0;
    private int mSectionLen = 0;  //mSectionIndices的长度
    private int timeNume = 0;	  //每个时间间隔内的图片个数
    private int endPosition =0;   //每个时间间隔内的最后一个position
    private int totalLackNum = 0; //各个时间间隔缺少的图片总数
    class Position{
    	int row;
    	int position;
    	int lackNum;
    }
    private String activityStr ="";
    public interface PreviewLongClick{
    	void onPreviewLongClick(int index);
    }
    public interface SelectModeChange{
    	void onSelectModeChange();
    }
    public PreviewLongClick longClickListener;
	public AlbumActivityDataAdapter(final Context context, GalleryApp app,
			MediaSet mediaSet, Path mediaSetPath, StickyListHeadersListView listView,String activityName) {
		this.context = context;
		mSource = mediaSet;
		mMediaSetPath = mediaSetPath;
		adapterLen = 0;
		galleryApp = app;
		totalLackNum=0;
		// mData = new MediaItem[DATA_CACHE_SIZE];
		mItemVersion = new long[DATA_CACHE_SIZE];
		mSetVersion = new long[DATA_CACHE_SIZE];
		mDisplayData = new AlbumDisplayItem[64];
		mlistView = listView;
		Arrays.fill(mItemVersion, MediaObject.INVALID_DATA_VERSION);
		Arrays.fill(mSetVersion, MediaObject.INVALID_DATA_VERSION);
		DisplayMetrics displayMetrics = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay()
				.getMetrics(displayMetrics);
		height_screen = displayMetrics.heightPixels;
		width_screen = displayMetrics.widthPixels;
		scaleMax = height_screen / width_screen + 0.15f;
		scaleMin = height_screen / width_screen - 0.15f;
		activityStr = activityName;
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
				case MSG_LOAD_FINISH: {
					if (mLoadingListener != null)
						mLoadingListener.onLoadingFinished();

					if (isSelectTemplate) {
						return;
					}
					//20150108 解决首页和内置相机内进入相册后在返回都恢复原来位置 begin
					if(activityStr.equals("AlbumCameraActivity")){
						if(((AlbumCameraActivity)context)!=null){
							((AlbumCameraActivity)context).getSetSelectedPos();
						}
						
					}else if(activityStr.equals("AlbumActivity")){
							if(((AlbumActivity)context)!=null){
								((AlbumActivity)context).getSetSelectedPos();
							}
							if(isAllPaper){
								((AlbumActivity)context).closeProgressDialog();
							}
					}
					//20150108 解决首页和内置相机内进入相册后在返回都恢复原来位置 end
					if (isAllPaper == true) {
						((AlbumActivity) context).refreshAllpapersNum();
					} else {
						if(activityStr.equals("AlbumActivity")){
							((AlbumActivity) context).refreshAlbumNum(); // 更新界面个数
						}
					}

					return;
				}
				case MSG_LOAD_BITMAP_DONE: {
					((AlbumDisplayItem) message.obj).onLoadBitmapDone();
					return;
				}
				}
			}
		};
		albumAdapter = this;
		if (albumImages.size() > 0) {
			albumImages.clear();
		}
		mThreadPool = app.getThreadPool();
		//初始化屏幕宽度和高度
		window = (WindowManager) app.getAndroidContext().getSystemService(Context.WINDOW_SERVICE);
		screenwidth = window.getDefaultDisplay().getWidth();
		screenHeight = window.getDefaultDisplay().getHeight();
//		DisplayMetrics dm = new DisplayMetrics();  
//		dm = context.getResources().getDisplayMetrics();  
		density  = displayMetrics.density;	
	}
	
	public void setLongClickListener(PreviewLongClick mLongClickListener){
		longClickListener = mLongClickListener;
	}
	
	public void setIsAllPaper(boolean mIsAllPaper) {
		isAllPaper = mIsAllPaper;
	}

	public void resume() {
		mSource.addContentListener(mSourceListener);
		mReloadTask = new ReloadTask();
		mReloadTask.start();
	}

	public void pause() {
		if(mReloadTask!=null){
			mReloadTask.terminate();
			mReloadTask = null;
			mSource.removeContentListener(mSourceListener);
		}
	}

	public void resumeNoListener() {
		mReloadTask = new ReloadTask();
		mReloadTask.start();
	}

	public void clearAdapter() {
		albumImages.clear();
	}
	
	public ArrayList<MediaItem> getAlbumImages(){
		return albumImages;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		int count = 0;
		if (0 == adapterLen) {
			return 0;
		} 
//		else if (0 == adapterLen % IMAGENUM_LISTVIEW_PER_ITEM) {
//			return adapterLen / IMAGENUM_LISTVIEW_PER_ITEM;
//		} else {
//			return adapterLen / IMAGENUM_LISTVIEW_PER_ITEM + 1;
//		}
		else{
			if(todayNum%8!=0){
				count +=todayNum/8+1;
			}
			else{
				count +=todayNum/8;
			}
			if(yestodayNum%8!=0){
				count +=yestodayNum/8+1;
			}
			else{
				count +=yestodayNum/8;
			}
			if(weekNum%8!=0){
				count +=weekNum/8+1;
			}
			else{
				count +=weekNum/8;
			}
			if(monthNum%8!=0){
				count +=monthNum/8+1;
			}
			else{
				count +=monthNum/8;
			}
			if(otherNum%8!=0){
				count +=otherNum/8+1;
			}
			else{
				count +=otherNum/8;
			}
		}
		return count;
	}
	//下面两个函数计算listView中view总高度
	public int getmHeight(int remainNum,int count){
		int mHeight = 0;
		if(1<=remainNum && remainNum<=3){
			mHeight+=(236+3)*density;
		}
		else if(4<=remainNum && remainNum<=5){
			mHeight+=(236+115+6)*density;
		}
		else{
			mHeight+=(236+230+9)*density;
		}
		if(count>=1){
			mHeight+=(count-1)*(236+230+9)*density+1*density;
		}
		return mHeight;
	}
	public int getTotalHeight()
	{
		int mHeight =0;
		for(int i=0;i<5;i++){
			if(i==0){
				int count =0;
				int remainNum = 0;
				if(todayNum>0){
					if(todayNum%8!=0){
						count +=todayNum/8+1;
					}
					else{
						count +=todayNum/8;
					}
					remainNum = todayNum%8;
					mHeight += getmHeight(remainNum,count);
				}
			}
			else if(i==1){
				int count =0;
				int remainNum = 0;
				if(yestodayNum>0){
					if(yestodayNum%8!=0){
						count =yestodayNum/8+1;
					}
					else{
						count =yestodayNum/8;
					}
					remainNum = yestodayNum%8;
					mHeight += getmHeight(remainNum,count);
				}
			}
			else if(i==2){
				int count =0;
				int remainNum = 0;
				if(weekNum>0){
					if(weekNum%8!=0){
						count =weekNum/8+1;
					}
					else{
						count =weekNum/8;
					}
					remainNum = weekNum%8;
					mHeight += getmHeight(remainNum,count);
				}
			}
			else if(i==3){
				int count =0;
				int remainNum = 0;
				if(monthNum>0){
					if(monthNum%8!=0){
						count =monthNum/8+1;
					}
					else{
						count =monthNum/8;
					}
					remainNum = monthNum%8;
					mHeight += getmHeight(remainNum,count);
				}
			}
			else if(i==4){
				int count =0;
				int remainNum = 0;
				if(otherNum>0){
					if(otherNum%8!=0){
						count =otherNum/8+1;
					}
					else{
						count =otherNum/8;
					}
					remainNum = otherNum%8;
					mHeight += getmHeight(remainNum,count);
				}
			}
		}
		return mHeight;
	}
	
	public int getCurrentSection(float position){
		int i=0;
		double totalNum =todayNum+yestodayNum+weekNum+monthNum+otherNum;
		if(position<=(double)(todayNum/totalNum) && position>0 && todayNum>0){
			i++;
			return i;
		}
		else if(position>(double)(todayNum/totalNum) && position<=(double)((todayNum+yestodayNum)/totalNum)&& yestodayNum>0){
			i++;
			return i;
		}
		else if(position>(double)((todayNum+yestodayNum)/totalNum) && position<=(double)((todayNum+yestodayNum+weekNum)/totalNum)&& weekNum>0){
			i++;
			return i;
		}
		else if(position>(double)((todayNum+yestodayNum+weekNum)/totalNum) && position<=(double)((todayNum+yestodayNum+weekNum+monthNum)/totalNum)&& monthNum>0){
			i++;
			return i;
		}
		else{
			i++;
		}
		return i;
	}	

	// 获取album的个数
	public int getAlbumItemSize() {
		return adapterLen;
	}

	// 设置是选择模板还是
	public void setSelectTemplate(boolean mode) {
		isSelectTemplate = mode;
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
	//获取该相册第一张图片的path
	public String getFirstImagePath(){
		String firstImagePath = albumImages.get(0).getPath().toString();
		return firstImagePath;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		try{
	    boolean hideFlag = false;
		//时间轴显示处理begin
		totalLackNum = 0;
		for(int k=0;k<mSectionLen-1;k++){
			if(mSectionIndices[k].row <= position && position <mSectionIndices[k+1].row||
					position >=mSectionIndices[mSectionLen-1].row){
				if(position >=mSectionIndices[mSectionLen-1].row){
					k = mSectionLen-1;
				}
				timeNume = getTimeNum(mSectionLetters[k]);
				remainder =timeNume%8;
				if(timeNume%8==0){
					endPosition=mSectionIndices[k].row+timeNume/8-1;	
				}
				else{
					endPosition=mSectionIndices[k].row+timeNume/8;	
				}
				if(mSectionIndices[k].row == position && position!=0){
					hideFlag = true;
				}
				break;
			}
		}
		if(mSectionLen==1){
			timeNume = getTimeNum(mSectionLetters[0]);
			remainder =timeNume%8;
			if(timeNume%8==0){
				endPosition=mSectionIndices[0].row+timeNume/8-1;	
			}
			else{
				endPosition=mSectionIndices[0].row+timeNume/8;	
			}
		}

		totalLackNum = getLackNum(position);
		//时间轴显示处理end
		ViewHolder holder = null;
		if (convertView == null) {
			Log.v("billmao","1111111111111111111111");
			convertView = LayoutInflater.from(context).inflate(
					R.layout.layout_previewer_album_list_item, null);
			holder = new ViewHolder();
			// 初始化实例
			holder.imageview1 = (AlbumImageView) convertView
					.findViewById(R.id.imageview1);
			holder.imageview2 = (AlbumImageView) convertView
					.findViewById(R.id.imageview2);
			holder.imageview3 = (AlbumImageView) convertView
					.findViewById(R.id.imageview3);
			holder.imageview4 = (AlbumImageView) convertView
					.findViewById(R.id.imageview4);
			holder.imageview5 = (AlbumImageView) convertView
					.findViewById(R.id.imageview5);
			holder.imageview6 = (AlbumImageView) convertView
					.findViewById(R.id.imageview6);
			holder.imageview7 = (AlbumImageView) convertView
					.findViewById(R.id.imageview7);
			holder.imageview8 = (AlbumImageView) convertView
					.findViewById(R.id.imageview8);
			holder.linebackground1 = (ImageView) convertView
					 .findViewById(R.id.linebackground1);
			holder.linebackground2 = (ImageView) convertView
					 .findViewById(R.id.linebackground2);
			holder.linebackground3 = (ImageView) convertView
					 .findViewById(R.id.linebackground3);
			holder.linebackground4 = (ImageView) convertView
					 .findViewById(R.id.linebackground4);
			holder.linebackground0 = (ImageView) convertView
					 .findViewById(R.id.linebackground0);
			holder.selected1 = (ImageView) convertView
					 .findViewById(R.id.selected1);
			holder.selected2 = (ImageView) convertView
					 .findViewById(R.id.selected2);
			holder.selected3 = (ImageView) convertView
					 .findViewById(R.id.selected3);
			holder.selected4 = (ImageView) convertView
					 .findViewById(R.id.selected4);
			holder.selected5 = (ImageView) convertView
					 .findViewById(R.id.selected5);
			holder.selected6 = (ImageView) convertView
					 .findViewById(R.id.selected6);
			holder.selected7 = (ImageView) convertView
					 .findViewById(R.id.selected7);
			holder.selected8 = (ImageView) convertView
					 .findViewById(R.id.selected8);
			holder.view1 = (View) convertView
					 .findViewById(R.id.view1);
			holder.view2 = (View) convertView
					 .findViewById(R.id.view2);
			holder.view3 = (View) convertView
					 .findViewById(R.id.view3);
			holder.view4 = (View) convertView
					 .findViewById(R.id.view4);
			holder.view5 = (View) convertView
					 .findViewById(R.id.view5);
			holder.view6 = (View) convertView
					 .findViewById(R.id.view6);
			holder.view7 = (View) convertView
					 .findViewById(R.id.view7);
			holder.view8 = (View) convertView
					 .findViewById(R.id.view8);
			if(hideFlag){
				holder.linebackground0.setVisibility(View.GONE);
			}
			if (endPosition != position
					|| (endPosition == position && remainder == 0)
					|| (endPosition == position && remainder > 5 && remainder < 8)) {
				holder.imageview4.setVisibility(View.VISIBLE);
				holder.imageview5.setVisibility(View.VISIBLE);
				holder.imageview6.setVisibility(View.VISIBLE);
				holder.imageview7.setVisibility(View.VISIBLE);
				holder.imageview8.setVisibility(View.VISIBLE);
				if(position >= 0 && endPosition == position && (remainder == 0||		
						(remainder > 5 && remainder < 8))){
					if(position>=0){
						holder.linebackground2.setVisibility(View.VISIBLE);
					}
					if(remainder==6){
						holder.imageview7.setVisibility(View.GONE);
						holder.imageview8.setVisibility(View.GONE);
						LinearLayout.LayoutParams par = (LinearLayout.LayoutParams)holder.imageview6.getLayoutParams();
						par.width= (int) (screenwidth-6*density);
						holder.imageview6.setLayoutParams(par);
						par = (LinearLayout.LayoutParams)holder.view6.getLayoutParams();
						par.width= (int) (screenwidth-6*density);
						par.leftMargin = (int)-(screenwidth-6*density);
						holder.view6.setLayoutParams(par);
						LinearLayout.LayoutParams par6 = (LinearLayout.LayoutParams)holder.selected6.getLayoutParams();
						par6.leftMargin = (int)((-20)*density);
						holder.selected6.setLayoutParams(par6);
					}
					if(remainder==7){
						holder.imageview8.setVisibility(View.GONE);
						LinearLayout.LayoutParams par = (LinearLayout.LayoutParams)holder.imageview6.getLayoutParams();
						par.width= (int) (236*density);
						holder.imageview6.setLayoutParams(par);
						par = (LinearLayout.LayoutParams)holder.view6.getLayoutParams();
						par.width= (int) (236*density);
						par.leftMargin = (int)-(236*density);
						holder.view6.setLayoutParams(par);
						LinearLayout.LayoutParams par6 = (LinearLayout.LayoutParams)holder.selected6.getLayoutParams();
						par6.leftMargin = (int)((-20)*density);
						holder.selected6.setLayoutParams(par6);
						LinearLayout.LayoutParams par7 = (LinearLayout.LayoutParams)holder.selected7.getLayoutParams();
						par7.leftMargin = (int)((-20)*density);
						holder.selected7.setLayoutParams(par7);
					}
				}
			} 
			else 
			{
				if (endPosition == position && 0 < remainder	
						&& remainder < 4) {
					holder.imageview4.setVisibility(View.GONE);
					holder.imageview5.setVisibility(View.GONE);
					holder.imageview6.setVisibility(View.GONE);
					holder.imageview7.setVisibility(View.GONE);
					holder.imageview8.setVisibility(View.GONE);
					holder.linebackground1.setVisibility(View.GONE);
					if(holder.linebackground2.getVisibility()==View.VISIBLE){
						holder.linebackground2.setVisibility(View.GONE);
					}
					holder.linebackground3.setVisibility(View.GONE);
					if(remainder==1){
						holder.imageview2.setVisibility(View.GONE);
						holder.imageview3.setVisibility(View.GONE);
						ViewGroup.LayoutParams par = (ViewGroup.LayoutParams)holder.imageview1.getLayoutParams();
						par.width= (int) (screenwidth-6*density);
						holder.imageview1.setLayoutParams(par);
						holder.view1.setLayoutParams(par);
						RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams)holder.selected1.getLayoutParams();
						params1.addRule(RelativeLayout.ALIGN_BOTTOM, holder.imageview1.getId()); 
						params1.addRule(RelativeLayout.ALIGN_RIGHT, holder.imageview1.getId()); 
						params1.leftMargin = (int)(334*density);
						params1.topMargin = (int)(216*density);
						holder.selected1.setLayoutParams(params1);
					}
					if(remainder==2){
						holder.imageview3.setVisibility(View.GONE);
						ViewGroup.LayoutParams par = (ViewGroup.LayoutParams)holder.imageview2.getLayoutParams();
						par.height = (int) (236*density);
						holder.imageview2.setLayoutParams(par);
						holder.view2.setLayoutParams(par);
						RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams)holder.selected2.getLayoutParams();
						params2.addRule(RelativeLayout.ALIGN_BOTTOM, holder.imageview2.getId()); 
						params2.addRule(RelativeLayout.ALIGN_RIGHT, holder.imageview2.getId()); 
						params2.topMargin = (int)(216*density);
						holder.selected2.setLayoutParams(params2);
					}
				}
				else if (endPosition == position && remainder > 3
						&& remainder < 6) {
					holder.imageview4.setVisibility(View.VISIBLE);
					holder.imageview5.setVisibility(View.VISIBLE);
					holder.imageview6.setVisibility(View.GONE);
					holder.imageview7.setVisibility(View.GONE);
					holder.imageview8.setVisibility(View.GONE);
					if(holder.linebackground2.getVisibility()==View.VISIBLE){
						holder.linebackground2.setVisibility(View.GONE);
					}
					if(remainder==4){
						holder.imageview5.setVisibility(View.GONE);
						LinearLayout.LayoutParams par = (LinearLayout.LayoutParams)holder.imageview4.getLayoutParams();
						par.width= (int) (screenwidth-6*density);
						holder.imageview4.setLayoutParams(par);
						par = (LinearLayout.LayoutParams)holder.view4.getLayoutParams();
						par.width= (int) (screenwidth-3*density);
						par.leftMargin = (int)(-(screenwidth-3*density));
						holder.view4.setLayoutParams(par);
						holder.linebackground3.setVisibility(View.GONE);
						LinearLayout.LayoutParams par4 = (LinearLayout.LayoutParams)holder.selected4.getLayoutParams();
						par4.leftMargin = (int)((-20)*density);
						holder.selected4.setLayoutParams(par4);
					}
				}
			}
			convertView.setTag(holder);
		} else {
			Log.v("billmao","2222222222222222222222222222");
			holder = (ViewHolder) convertView.getTag();
			holder.linebackground1.setVisibility(View.VISIBLE);
			holder.linebackground2.setVisibility(View.GONE);
			holder.linebackground3.setVisibility(View.VISIBLE);
			holder.linebackground4.setVisibility(View.VISIBLE);
			holder.imageview1.setVisibility(View.VISIBLE);
			ViewGroup.LayoutParams par = (ViewGroup.LayoutParams)holder.imageview1.getLayoutParams();
			par.width= (int) (236*density);
			holder.imageview1.setLayoutParams(par);
			holder.view1.setLayoutParams(par);
			holder.view1.setVisibility(View.GONE);
			holder.imageview1.setBackgroundColor(Color.parseColor("#ffffff"));
			holder.imageview2.setVisibility(View.VISIBLE);
			holder.imageview2.setBackgroundColor(Color.parseColor("#ffffff"));
			par = (ViewGroup.LayoutParams)holder.imageview2.getLayoutParams();
			par.height= (int) (116*density);
			holder.imageview2.setLayoutParams(par);
			holder.view2.setLayoutParams(par);
			holder.view2.setVisibility(View.GONE);
			holder.imageview3.setVisibility(View.VISIBLE);
			holder.view3.setVisibility(View.GONE);
			holder.imageview3.setBackgroundColor(Color.parseColor("#ffffff"));
			holder.imageview4.setVisibility(View.VISIBLE);
			holder.imageview4.setBackgroundColor(Color.parseColor("#ffffff"));
			LinearLayout.LayoutParams param = (LinearLayout.LayoutParams)holder.imageview4.getLayoutParams();
			param.width= (int) (115*density);
			holder.imageview4.setLayoutParams(param);
			LinearLayout.LayoutParams param11 = (LinearLayout.LayoutParams)holder.view4.getLayoutParams();
			param11.leftMargin = (int)(-(115*density));
			param11.width= (int) (115*density);
			holder.view4.setLayoutParams(param11);
			holder.view4.setVisibility(View.GONE);
			holder.imageview5.setVisibility(View.VISIBLE);
			holder.imageview5.setBackgroundColor(Color.parseColor("#ffffff"));
			param = (LinearLayout.LayoutParams)holder.imageview5.getLayoutParams();
			param.width= (int) (236*density);
			holder.imageview5.setLayoutParams(param);
			param11 = (LinearLayout.LayoutParams)holder.view5.getLayoutParams();
			param11.leftMargin = (int)(-(236*density));
			param11.width= (int) (236*density);
			holder.view5.setLayoutParams(param11);
			holder.view5.setVisibility(View.GONE);
			holder.imageview6.setVisibility(View.VISIBLE);
			holder.imageview6.setBackgroundColor(Color.parseColor("#ffffff"));
			param = (LinearLayout.LayoutParams)holder.imageview6.getLayoutParams();
			param.width= (int) (115*density);
			holder.imageview6.setLayoutParams(param);
			param11 = (LinearLayout.LayoutParams)holder.view6.getLayoutParams();
			param11.leftMargin = (int)(-(115*density));
			param11.width= (int) (115*density);
			holder.view6.setLayoutParams(param11);
			holder.view6.setVisibility(View.GONE);
			holder.imageview7.setVisibility(View.VISIBLE);
			param = (LinearLayout.LayoutParams)holder.imageview7.getLayoutParams();
			param.width= (int) (118*density);
			holder.imageview7.setLayoutParams(param);
			param = (LinearLayout.LayoutParams)holder.view7.getLayoutParams();
			param.width= (int) (118*density);
			param.leftMargin = (int)((-118)*density);
			holder.view7.setLayoutParams(param);
			holder.view7.setVisibility(View.GONE);
			holder.imageview7.setBackgroundColor(Color.parseColor("#ffffff"));
			holder.imageview8.setVisibility(View.VISIBLE);
			holder.view8.setVisibility(View.GONE);
			holder.imageview8.setBackgroundColor(Color.parseColor("#ffffff"));
		    //初始化选中图标位置
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)holder.selected1.getLayoutParams();
			params.leftMargin = (int)(216*density);
			params.topMargin = (int)(216*density);
			holder.selected1.setLayoutParams(params);
			params = (RelativeLayout.LayoutParams)holder.selected2.getLayoutParams();
			params.topMargin = (int)(94*density);
			holder.selected2.setLayoutParams(params);
			LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams)holder.selected4.getLayoutParams();
			params2.leftMargin = (int)((-20)*density);
			holder.selected4.setLayoutParams(params2);
			params2 = (LinearLayout.LayoutParams)holder.selected6.getLayoutParams();
			params2.leftMargin = (int)((-20)*density);
			holder.selected6.setLayoutParams(params2);
			params2 = (LinearLayout.LayoutParams)holder.selected7.getLayoutParams();
			params2.leftMargin = (int)((-20)*density);
			holder.selected7.setLayoutParams(params2);
			
			holder.linebackground0.setVisibility(View.VISIBLE);
			if(hideFlag){
				holder.linebackground0.setVisibility(View.GONE);
			}
			if (endPosition != position
					|| (endPosition == position && remainder == 0)
					|| (endPosition == position && remainder > 5 && remainder < 8)) {
				holder.imageview4.setVisibility(View.VISIBLE);
				holder.imageview5.setVisibility(View.VISIBLE);
				holder.imageview6.setVisibility(View.VISIBLE);
				holder.imageview7.setVisibility(View.VISIBLE);
				holder.imageview8.setVisibility(View.VISIBLE);
						if(position >= 0 && endPosition == position && (remainder == 0||
						(remainder > 5 && remainder < 8))){
					if(position>=0){
						holder.linebackground2.setVisibility(View.VISIBLE);
					}
					if(remainder==6){
						holder.imageview7.setVisibility(View.GONE);
						holder.imageview8.setVisibility(View.GONE);
						LinearLayout.LayoutParams par1 = (LinearLayout.LayoutParams)holder.imageview6.getLayoutParams();
						par1.width= (int) (screenwidth-6*density);
						holder.imageview6.setLayoutParams(par1);
						par1 = (LinearLayout.LayoutParams)holder.view6.getLayoutParams();
						par1.width= (int)(screenwidth-3*density);
						par1.leftMargin= (int) (-(screenwidth-3*density));
						holder.view6.setLayoutParams(par1);
						LinearLayout.LayoutParams par6 = (LinearLayout.LayoutParams)holder.selected6.getLayoutParams();
						par6.leftMargin = (int)((-20)*density);
						holder.selected6.setLayoutParams(par6);
					}
					if(remainder==7){
						holder.imageview8.setVisibility(View.GONE);
						LinearLayout.LayoutParams par2 = (LinearLayout.LayoutParams)holder.imageview6.getLayoutParams();
						par2.width= (int) (236*density);
						holder.imageview6.setLayoutParams(par2);
						par2 = (LinearLayout.LayoutParams)holder.view6.getLayoutParams();
						par2.width= (int) (236*density);
						par2.leftMargin= (int) (-(236*density));
						holder.view6.setLayoutParams(par2);
						LinearLayout.LayoutParams par6 = (LinearLayout.LayoutParams)holder.selected6.getLayoutParams();
						par6.leftMargin = (int)((-20)*density);
						holder.selected6.setLayoutParams(par6);
						LinearLayout.LayoutParams par7 = (LinearLayout.LayoutParams)holder.selected7.getLayoutParams();
						par7.leftMargin = (int)((-20)*density);
						holder.selected7.setLayoutParams(par7);
					}
				}
			} else if (endPosition == position && 0 < remainder
					&& remainder < 4) {
				holder.imageview4.setVisibility(View.GONE);
				holder.imageview5.setVisibility(View.GONE);
				holder.imageview6.setVisibility(View.GONE);
				holder.imageview7.setVisibility(View.GONE);
				holder.imageview8.setVisibility(View.GONE);
				holder.linebackground1.setVisibility(View.GONE);
				if(holder.linebackground2.getVisibility()==View.VISIBLE){
					holder.linebackground2.setVisibility(View.GONE);
				}
				holder.linebackground3.setVisibility(View.GONE);
				if(remainder==1){
					holder.imageview2.setVisibility(View.GONE);
					holder.imageview3.setVisibility(View.GONE);
					ViewGroup.LayoutParams par1 = (ViewGroup.LayoutParams)holder.imageview1.getLayoutParams();
					par1.width= (int) (screenwidth-6*density);
					holder.imageview1.setLayoutParams(par1);
					holder.linebackground4.setVisibility(View.GONE);
					RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams)holder.selected1.getLayoutParams();
					params1.addRule(RelativeLayout.ALIGN_BOTTOM, holder.imageview1.getId()); 
					params1.addRule(RelativeLayout.ALIGN_RIGHT, holder.imageview1.getId()); 
					params1.leftMargin = (int)(334*density);
					params1.topMargin = (int)(216*density);
					holder.selected1.setLayoutParams(params1);
				}
				if(remainder==2){
					holder.imageview3.setVisibility(View.GONE);
					ViewGroup.LayoutParams par2 = (ViewGroup.LayoutParams)holder.imageview2.getLayoutParams();
					par2.height = (int) (236*density);
					holder.imageview2.setLayoutParams(par2);
					RelativeLayout.LayoutParams params0 = (RelativeLayout.LayoutParams)holder.selected2.getLayoutParams();
					params0.addRule(RelativeLayout.ALIGN_BOTTOM, holder.imageview2.getId()); 
					params0.addRule(RelativeLayout.ALIGN_RIGHT, holder.imageview2.getId()); 
					params0.topMargin = (int)(216*density);
					holder.selected2.setLayoutParams(params0);
				}
			} else if (endPosition == position && remainder > 3
					&& remainder < 6) {
				holder.imageview4.setVisibility(View.VISIBLE);
				holder.imageview5.setVisibility(View.VISIBLE);
				holder.imageview6.setVisibility(View.GONE);
				holder.imageview7.setVisibility(View.GONE);
				holder.imageview8.setVisibility(View.GONE);
				if(holder.linebackground2.getVisibility()==View.VISIBLE){
					holder.linebackground2.setVisibility(View.GONE);
				}
				if(remainder==4){
					holder.imageview5.setVisibility(View.GONE);
					LinearLayout.LayoutParams par1 = (LinearLayout.LayoutParams)holder.imageview4.getLayoutParams();
					par1.width= (int) (screenwidth-6*density);
					holder.imageview4.setLayoutParams(par1);
					LinearLayout.LayoutParams par2 = (LinearLayout.LayoutParams)holder.view4.getLayoutParams();
					par2.width= (int) (screenwidth-3*density);
					par2.leftMargin = (int)(-(screenwidth-3*density));
					holder.view4.setLayoutParams(par2);
					holder.linebackground3.setVisibility(View.GONE);
					LinearLayout.LayoutParams par4 = (LinearLayout.LayoutParams)holder.selected4.getLayoutParams();
					par4.leftMargin = (int)((-20)*density);
					holder.selected4.setLayoutParams(par4);
				}
			}
		}
		int mLackNum =getLackNum(position);
		//是否显示选中图标
		if(activityStr.equals("AlbumActivity")){
			boolean isSlecetMode = ((AlbumActivity) context).getSelectMode(); // 更新界面个数
			if(isSlecetMode){
				holder.selected1.setVisibility(View.GONE);
				holder.selected2.setVisibility(View.GONE);
				holder.selected3.setVisibility(View.GONE);
				holder.selected4.setVisibility(View.GONE);
				holder.selected5.setVisibility(View.GONE);
				holder.selected6.setVisibility(View.GONE);
				holder.selected7.setVisibility(View.GONE);
				holder.selected8.setVisibility(View.GONE);	
				holder.view1.setVisibility(View.GONE);
				holder.view2.setVisibility(View.GONE);
				holder.view3.setVisibility(View.GONE);
				holder.view4.setVisibility(View.GONE);
				holder.view5.setVisibility(View.GONE);
				holder.view6.setVisibility(View.GONE);
				holder.view7.setVisibility(View.GONE);
				holder.view8.setVisibility(View.GONE);	
				if(holder.imageview1.getVisibility()==View.VISIBLE){
					setSelectedIconVisible(isSlecetMode,position,0,holder.selected1,holder.view1,mLackNum);
				}
				if(holder.imageview2.getVisibility()==View.VISIBLE){
					setSelectedIconVisible(isSlecetMode,position,1,holder.selected2,holder.view2,mLackNum);
				}
				if(holder.imageview3.getVisibility()==View.VISIBLE){
					setSelectedIconVisible(isSlecetMode,position,2,holder.selected3,holder.view3,mLackNum);
				}
				if(holder.imageview4.getVisibility()==View.VISIBLE){
					setSelectedIconVisible(isSlecetMode,position,3,holder.selected4,holder.view4,mLackNum);
				}
				if(holder.imageview5.getVisibility()==View.VISIBLE){
					setSelectedIconVisible(isSlecetMode,position,4,holder.selected5,holder.view5,mLackNum);
				}
				if(holder.imageview6.getVisibility()==View.VISIBLE){
					setSelectedIconVisible(isSlecetMode,position,5,holder.selected6,holder.view6,mLackNum);
				}
				if(holder.imageview7.getVisibility()==View.VISIBLE){
					setSelectedIconVisible(isSlecetMode,position,6,holder.selected7,holder.view7,mLackNum);
				}
				if(holder.imageview8.getVisibility()==View.VISIBLE){
					setSelectedIconVisible(isSlecetMode,position,7,holder.selected8,holder.view8,mLackNum);
				}
			}
			else{
				holder.selected1.setVisibility(View.GONE);
				holder.selected2.setVisibility(View.GONE);
				holder.selected3.setVisibility(View.GONE);
				holder.selected4.setVisibility(View.GONE);
				holder.selected5.setVisibility(View.GONE);
				holder.selected6.setVisibility(View.GONE);
				holder.selected7.setVisibility(View.GONE);
				holder.selected8.setVisibility(View.GONE);	
				holder.view1.setVisibility(View.GONE);
				holder.view2.setVisibility(View.GONE);
				holder.view3.setVisibility(View.GONE);
				holder.view4.setVisibility(View.GONE);
				holder.view5.setVisibility(View.GONE);
				holder.view6.setVisibility(View.GONE);
				holder.view7.setVisibility(View.GONE);
				holder.view8.setVisibility(View.GONE);	
			}
		}
		final AlbumImageView imageArray[] = { holder.imageview1,
				holder.imageview2, holder.imageview3, holder.imageview4,
				holder.imageview5, holder.imageview6, holder.imageview7,
				holder.imageview8 };
		if ((endPosition != position && remainder >0) ||remainder ==0) {
			for (int j = 0; j < 8; j++) {
				if (imageArray[j] != null) 
					imageArray[j].setIndex(position);
			}
		} else {
			for (int j = 0; j < remainder; j++) {
				imageArray[j].setIndex(position);
			}
			for (int j = remainder; j < 8; j++) {
				imageArray[j].setIndex(0);
			}
		}
    	if(activityStr.equals("AlbumActivity")){
    		holder.imageview1.setOnLongClickListener(new PicOnLongClick(holder));
    		holder.imageview2.setOnLongClickListener(new PicOnLongClick(holder));
    		holder.imageview3.setOnLongClickListener(new PicOnLongClick(holder));
    		holder.imageview4.setOnLongClickListener(new PicOnLongClick(holder));
    		holder.imageview5.setOnLongClickListener(new PicOnLongClick(holder));
    		holder.imageview6.setOnLongClickListener(new PicOnLongClick(holder));
    		holder.imageview7.setOnLongClickListener(new PicOnLongClick(holder));
    		holder.imageview8.setOnLongClickListener(new PicOnLongClick(holder));
    	}

		holder.imageview1.setOnClickListener(new PicOnClick(holder.selected1,0,holder.view1) {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(activityStr.equals("AlbumActivity")){
					boolean inSelectMode = ((AlbumActivity) context).getSelectMode();
					super.onClick(v);
					if(inSelectMode){
						return;
					}
				}
				// 非选择模式进入图片预览
				if (((AlbumImageView) v).getBackground() == null) {
					return;
				}
				String path = "";
				int lackNum =getLackNum(((AlbumImageView) v).getIndex());
				if (!isSelectTemplate) {
					startGallery(v,0,lackNum,path);

				} else {
					startTemplete(v,0,lackNum,path);
				}
			}

		});

		holder.imageview2.setOnClickListener(new PicOnClick(holder.selected2,1,holder.view2) {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(activityStr.equals("AlbumActivity")){
					boolean inSelectMode = ((AlbumActivity) context).getSelectMode();
					super.onClick(v);
					if(inSelectMode){
						return;
					}
				}
				if (((AlbumImageView) v).getBackground() == null) {
					return;
				}
				String path = "";
				int lackNum =getLackNum(((AlbumImageView) v).getIndex());
				if (!isSelectTemplate) {
					startGallery(v,1,lackNum,path);
				} else {
					startTemplete(v,1,lackNum,path);
				}
			}

		});
		if (holder.imageview3 != null) {
			holder.imageview3.setOnClickListener(new PicOnClick(holder.selected3,2,holder.view3) {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(activityStr.equals("AlbumActivity")){
						boolean inSelectMode = ((AlbumActivity) context).getSelectMode();
						super.onClick(v);
						if(inSelectMode){
							return;
						}
					}
					if (((AlbumImageView) v).getBackground() == null) {
						return;
					}
					String path = "";
					int lackNum =getLackNum(((AlbumImageView) v).getIndex());
					if (!isSelectTemplate) {
						startGallery(v,2,lackNum,path);
					} else {
						startTemplete(v,2,lackNum,path);
					}
				}

			});
		}
		if (holder.imageview4 != null) {
			holder.imageview4.setOnClickListener(new PicOnClick(holder.selected4,3,holder.view4) {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(activityStr.equals("AlbumActivity")){
						boolean inSelectMode = ((AlbumActivity) context).getSelectMode();
						super.onClick(v);
						if(inSelectMode){
							return;
						}
					}
					if (((AlbumImageView) v).getBackground() == null) {
						return;
					}
					String path = "";
					int lackNum =getLackNum(((AlbumImageView) v).getIndex());
					if (!isSelectTemplate) {
						startGallery(v,3,lackNum,path);
					} else {
						startTemplete(v,3,lackNum,path);
					}
				}

			});
		}

		if (holder.imageview5 != null) {
			holder.imageview5.setOnClickListener(new PicOnClick(holder.selected5,4,holder.view5) {

				@Override
				public void onClick(View v) {
					if(activityStr.equals("AlbumActivity")){
						boolean inSelectMode = ((AlbumActivity) context).getSelectMode();
						super.onClick(v);
						if(inSelectMode){
							return;
						}
					}
					if (((AlbumImageView) v).getBackground() == null) {
						return;
					}
					// TODO Auto-generated method stub
					String path = "";
					int lackNum =getLackNum(((AlbumImageView) v).getIndex());
					if (!isSelectTemplate) {
						startGallery(v,4,lackNum,path);
					} else {
						startTemplete(v,4,lackNum,path);
					}
					
				}

			});
		}
		if (holder.imageview6 != null) {
			holder.imageview6.setOnClickListener(new PicOnClick(holder.selected6,5,holder.view6) {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(activityStr.equals("AlbumActivity")){
						boolean inSelectMode = ((AlbumActivity) context).getSelectMode();
						super.onClick(v);
						if(inSelectMode){
							return;
						}
					}
					if (((AlbumImageView) v).getBackground() == null) {
						return;
					}
					String path = "";
					int lackNum =getLackNum(((AlbumImageView) v).getIndex());
					if (!isSelectTemplate) {
						startGallery(v,5,lackNum,path);
					} else {
						startTemplete(v,5,lackNum,path);
					}
				}

			});
		}
		if (holder.imageview7 != null) {
			holder.imageview7.setOnClickListener(new PicOnClick(holder.selected7,6,holder.view7){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(activityStr.equals("AlbumActivity")){
						boolean inSelectMode = ((AlbumActivity) context).getSelectMode();
						super.onClick(v);
						if(inSelectMode){
							return;
						}
					}
					if (((AlbumImageView) v).getBackground() == null) {
						return;
					}
					String path = "";
					int lackNum =getLackNum(((AlbumImageView) v).getIndex());
					if (!isSelectTemplate) {
						startGallery(v,6,lackNum,path);
					} else {
						startTemplete(v,6,lackNum,path);
					}
				}

			});
		}
		if (holder.imageview8 != null) {
			holder.imageview8.setOnClickListener(new PicOnClick(holder.selected8,7,holder.view8){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(activityStr.equals("AlbumActivity")){
						boolean inSelectMode = ((AlbumActivity) context).getSelectMode();
						super.onClick(v);
						if(inSelectMode){
							return;
						}
					}
					if (((AlbumImageView) v).getBackground() == null) {
						return;
					}
					String path = "";
					int lackNum =getLackNum(((AlbumImageView) v).getIndex());
					if (!isSelectTemplate) {
						startGallery(v,7,lackNum,path);
					} else {
						startTemplete(v,7,lackNum,path);
					}
				}

			});
		}
	
		if (mSize != 0 && endPosition > position||(endPosition == position && remainder == 0)) {
			for (int i = 0; i < 8; i++) {
				if (imageArray[i] != null)
					imageArray[i].setTag(position * IMAGENUM_LISTVIEW_PER_ITEM
							+ i);
				if(remainder!=0){
					mDisplayData[(position * IMAGENUM_LISTVIEW_PER_ITEM + i)
							% MAX_LOAD_COUNT] = new AlbumDisplayItem(position
							* IMAGENUM_LISTVIEW_PER_ITEM + i,
							albumImages.get(position * IMAGENUM_LISTVIEW_PER_ITEM
									+ i-(totalLackNum)), holder);
				}
				else{
					mDisplayData[(position * IMAGENUM_LISTVIEW_PER_ITEM + i)
									% MAX_LOAD_COUNT] = new AlbumDisplayItem(position
									* IMAGENUM_LISTVIEW_PER_ITEM + i,
									albumImages.get(position * IMAGENUM_LISTVIEW_PER_ITEM
											+ i-totalLackNum), holder);
				}
				mDisplayData[(position * IMAGENUM_LISTVIEW_PER_ITEM + i)
						% MAX_LOAD_COUNT].setImageLoadListener(new ImageLoad() {

					@Override
					public void onImageLoader(Bitmap bitmap, int mindex,
							ViewHolder mHolder) {
						// TODO Auto-generated method stub
						AlbumImageView mImageView = null;
						View imageView = (View) mlistView
								.findViewWithTag(mHolder);
						if (imageView != null) {
							mImageView = (AlbumImageView) imageView
									.findViewWithTag(mindex);
							
				             Log.v("maobo","findViewWithTag 22222"+mindex);
				             Log.v("maobo","findViewWithTag 33333"+mImageView.toString());
						}
						// 如果是第4个AlbumImageView，宽高不相同的view
						if (bitmap != null
								&& mindex % IMAGENUM_LISTVIEW_PER_ITEM == 4) {
							if ((bitmap.getHeight() / 2) > 0) {
								bitmap = Bitmap.createBitmap(bitmap, 0,
										bitmap.getHeight() / 4,
										bitmap.getWidth(),
										bitmap.getHeight() / 2, null, false);
							}
						}
						if(bitmap!=null){
							Drawable drawable = new BitmapDrawable(bitmap);
							if (mImageView != null) {
								mImageView.setBackground(drawable);
							}
						}
						else{
							if (mImageView != null) {
								mImageView.setBackgroundColor(Color.parseColor("#cdcdcd"));
							}
						}
	
					}

				});
				mDisplayData[(position * IMAGENUM_LISTVIEW_PER_ITEM + i)
						% MAX_LOAD_COUNT].requestImage();

			}
		}
		if (remainder != 0 && position == endPosition) {
			final int mRemainder = remainder;
			for (int i = 0; i < mRemainder; i++) {
				imageArray[i].setTag(position * IMAGENUM_LISTVIEW_PER_ITEM + i);
				if(mRemainder!=0){
					mDisplayData[(position * IMAGENUM_LISTVIEW_PER_ITEM + i)
							% MAX_LOAD_COUNT] = new AlbumDisplayItem(position
							* IMAGENUM_LISTVIEW_PER_ITEM + i,
							albumImages.get(position * IMAGENUM_LISTVIEW_PER_ITEM
									+ i-(totalLackNum)), holder);
				}
				else{
					mDisplayData[(position * IMAGENUM_LISTVIEW_PER_ITEM + i)
									% MAX_LOAD_COUNT] = new AlbumDisplayItem(position
									* IMAGENUM_LISTVIEW_PER_ITEM + i,
									albumImages.get(position * IMAGENUM_LISTVIEW_PER_ITEM
											+ i-totalLackNum), holder);
				}
					mDisplayData[(position * IMAGENUM_LISTVIEW_PER_ITEM + i)
						% MAX_LOAD_COUNT].setImageLoadListener(new ImageLoad() {

					@Override
					public void onImageLoader(Bitmap bitmap, int mindex,
							ViewHolder mHolder) {
						// TODO Auto-generated method stub
						AlbumImageView mImageView = null;
						View imageView = (View) mlistView
								.findViewWithTag(mHolder);
						if (imageView != null) {
							mImageView = (AlbumImageView) imageView
									.findViewWithTag(mindex);
						}
						if (bitmap != null
								&& mindex % IMAGENUM_LISTVIEW_PER_ITEM == 1 && mRemainder==2) {
							if ((bitmap.getWidth() / 2) > 0) {
								bitmap = Bitmap.createBitmap(bitmap, bitmap.getWidth()/4,
										0,
										(int)(bitmap.getWidth()*0.5),
										bitmap.getHeight(), null, false);
							}
						}
						//注意此处当余数时7时，将第6个图片（mindex为5）的图片进行处理，而不是第mindex为6，目的是将布局显示起来更美观
						//mindex余数为4时不需要加条件mRemainder==5，因为余数为67,7,0时该图片也是要这样显示
						if (bitmap != null
								&& ((mindex % IMAGENUM_LISTVIEW_PER_ITEM == 4)||
								(mindex % IMAGENUM_LISTVIEW_PER_ITEM == 5 && mRemainder==7))) {
							if ((bitmap.getHeight() / 2) > 0) {
								bitmap = Bitmap.createBitmap(bitmap, 0,
										bitmap.getHeight()/4,
										bitmap.getWidth(),
										(int)(bitmap.getHeight()*0.5), null, false);
							}
						}
						if (bitmap != null
								&& ((mindex % IMAGENUM_LISTVIEW_PER_ITEM == 3 && mRemainder==4)||
								(mindex % IMAGENUM_LISTVIEW_PER_ITEM == 5 && mRemainder==6))) {
							if ((bitmap.getHeight() / 2) > 0) {
								Matrix m = new Matrix();
								m.reset();
								m.postScale((float)1.2, (float)1.2);
								bitmap = Bitmap.createBitmap(bitmap, 0,
										bitmap.getHeight()/4,
										bitmap.getWidth(),
										(int)(bitmap.getHeight()*0.5), null, false);
							}
						}
						if (bitmap != null
								&& mindex % IMAGENUM_LISTVIEW_PER_ITEM == 0 && mRemainder==1) {
							if ((bitmap.getWidth() / 2) > 0 && bitmap.getHeight()>=2) {
								Matrix m = new Matrix();
								m.reset();
								m.postScale((float)1.2, (float)1.2);
								bitmap = Bitmap.createBitmap(bitmap, 0,
										bitmap.getHeight()/6,
										bitmap.getWidth(),
										(int)(bitmap.getHeight()*0.7), null, false);
							}
						}
						if(bitmap!=null){
							Drawable drawable = new BitmapDrawable(bitmap);
							if (mImageView != null)
								mImageView.setBackground(drawable);
						}
						else{
							if (mImageView != null)
								mImageView.setBackgroundColor(Color.parseColor("#cdcdcd"));
						}
					}

				});
				mDisplayData[(position * IMAGENUM_LISTVIEW_PER_ITEM + i)
						% MAX_LOAD_COUNT].requestImage();
			}
			for (int j = remainder; j < IMAGENUM_LISTVIEW_PER_ITEM; j++) {
				if (imageArray[j] != null)
					imageArray[j].setBackground(null);
			}
		}
		}
		catch(java.lang.OutOfMemoryError e){
			 System.gc();
		     e.printStackTrace();
		}
		return convertView;
	}
	//启动gallery
	private void startGallery(View v,int index,int lackNum,String path){
		if(activityStr.equals("AlbumActivity")){
			if(((AlbumActivity) context).isOpenOfLeftLayout()){
				((AlbumActivity) context).hideLeftLayout();
				return;
			}
		}
		path = albumImages
				.get(((AlbumImageView) v).getIndex()
						* IMAGENUM_LISTVIEW_PER_ITEM + index-lackNum)
				.getPath().toString();
		String absolutePath = albumImages
		.get(((AlbumImageView) v).getIndex()
				* IMAGENUM_LISTVIEW_PER_ITEM + index-lackNum)
		.getDetails().getDetail(MediaDetails.INDEX_PATH).toString();
		Bundle data = new Bundle();
		data.putInt(PhotoPage.KEY_INDEX_HINT,
				((AlbumImageView) v).getIndex()
						* IMAGENUM_LISTVIEW_PER_ITEM + index-lackNum);
		data.putString(PhotoPage.KEY_MEDIA_SET_PATH,
				mMediaSetPath.toString());
		data.putString(PhotoPage.KEY_MEDIA_ITEM_PATH, path);
		Intent intent = new Intent();
		intent.setClass(context, Gallery.class);
		intent.putExtra("startpage", "PhotoPage");
		intent.putExtra("startTemplete", "false");
		intent.putExtra("absolutePath", absolutePath);
		intent.putExtras(data);
		context.startActivity(intent);
	}
	private void startTemplete(View v,int index,int lackNum,String path){
		Path imgPath = null;
		MediaDetails mediaDetails = albumImages.get(
				((AlbumImageView) v).getIndex()
						* IMAGENUM_LISTVIEW_PER_ITEM + index-lackNum)
				.getDetails();
		float imgHight = Float.valueOf((String) mediaDetails
				.getDetail(MediaDetails.INDEX_HEIGHT));
		float imgWidth = Float.valueOf((String) mediaDetails
				.getDetail(MediaDetails.INDEX_WIDTH));
		if ((imgHight==0f)||(imgHight / imgWidth <= scaleMax
				&& imgHight / imgWidth >= scaleMin)) {
			path = mediaDetails.getDetail(
					MediaDetails.INDEX_PATH).toString();
			sendMessageToTemplate(path);
		} else {
			imgPath = albumImages.get(
					((AlbumImageView) v).getIndex()
							* IMAGENUM_LISTVIEW_PER_ITEM + index-lackNum)
					.getPath();
			sendMessageToTemplate(imgPath);
		}
	}
	// 向选择模板发送图片路径
	private void sendMessageToTemplate(String path) {
		Intent in = new Intent();
		in.setAction("upload");
		in.putExtra("upload", path);
		context.sendBroadcast(in);
		((AlbumSelectTempleteActivity) context).finish();
		Gallery.getGalleryIntance().finish();
	}

	// 向选择模板发送图片路径
	private void sendMessageToTemplate(Path path) {
		DataManager manager = ((GalleryApp) ((AlbumSelectTempleteActivity) context)
				.getApplication()).getDataManager();
		Intent intent = new Intent(context,CropImage.class);
		String mimeType = "image/*";
		Bundle bundle = new Bundle();
		bundle.putInt("aspectX", (int)width_screen);
		bundle.putInt("aspectY", (int)height_screen);
		intent.putExtras(bundle);
		intent.setDataAndType(manager.getContentUri(path), mimeType);
		intent.putExtra("mimeType", mimeType);
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		context.startActivity(intent);
		((AlbumSelectTempleteActivity) context).finish();
		Gallery.getGalleryIntance().finish();
	}

	private class MySourceListener implements ContentListener {
		public void onContentDirty() {
			if (mReloadTask != null)
				mReloadTask.notifyDirty();
		}
	}

	public void setLoadingListener(LoadingListener listener) {
		mLoadingListener = listener;
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

	private static class UpdateInfo {
		public long version;
		public int reloadStart;
		public int reloadCount;

		public int size;
		public ArrayList<MediaItem> items;
	}

	private class GetUpdateInfo implements Callable<UpdateInfo> {
		private final long mVersion;

		public GetUpdateInfo(long version) {
			mVersion = version;
		}

		public UpdateInfo call() throws Exception {
			UpdateInfo info = new UpdateInfo();
			long version = mVersion;
			info.version = mSourceVersion;
			info.size = mSize;
			long setVersion[] = mSetVersion;
			for (int i = mContentStart, n = mContentEnd; i < n; ++i) {
				int index = i % DATA_CACHE_SIZE;
				if (setVersion[index] != version) {
					info.reloadStart = i;
					info.reloadCount = Math.min(MAX_LOAD_COUNT, n - i);
					return info;
				}
			}
			return mSourceVersion == mVersion ? null : info;
		}
	}

	private class UpdateContent implements Callable<Void> {

		private UpdateInfo mUpdateInfo;

		public UpdateContent(UpdateInfo info) {
			mUpdateInfo = info;
		}

		@Override
		public Void call() throws Exception {
			UpdateInfo info = mUpdateInfo;
			mSourceVersion = info.version;
			if (mSize != info.size) {
				mSize = info.size;
				mContentEnd = mSize;
				if (mContentEnd > mSize)
					mContentEnd = mSize;
			}

			ArrayList<MediaItem> items = info.items;
			if (items == null)
				return null;
			int start = Math.max(info.reloadStart, mContentStart);
			int end = Math.min(info.reloadStart + items.size(), mContentEnd);
			for (int i = start; i < end; ++i) {
				int index = i % DATA_CACHE_SIZE;
				mSetVersion[index] = info.version;
				MediaItem updateItem = items.get(i - info.reloadStart);
				if(updateItem!=null){
					long itemVersion = updateItem.getDataVersion();
					if (mItemVersion[index] != itemVersion) {
						mItemVersion[index] = itemVersion;
						albumImages.add(updateItem);
						adapterLen++;
					}
				}
				if (i == end - 1 && i == info.size - 1) {
					Collections.sort(albumImages, new FileComparator());    //0919
//20150108 delete 解决首页和内置相机内进入相册后在返回都恢复原来位置 begin
//					if(!isStartActivity){
//						if(activityStr.equals("AlbumActivity")){
//							if(((AlbumActivity)context)!=null){
//								((AlbumActivity)context).getSetSelectedPos();
//							}
//							if(isAllPaper){
//								((AlbumActivity)context).closeProgressDialog();
//							}
//						}
//					}
//20150108 delete 解决首页和内置相机内进入相册后在返回都恢复原来位置 begin
					mSectionIndices = getSectionIndices();
					mSectionLetters = getSectionLetters();
					//initPositionInfo();  //bill add 1216
					albumAdapter.notifyDataSetChanged(); // 最后更新list
				}
			}
			return null;
		}
	}

	/*
	 * The thread model of ReloadTask * [Reload Task] [Main Thread] | |
	 * getUpdateInfo() --> | (synchronous call) (wait) <---- getUpdateInfo() | |
	 * Load Data | | | updateContent() --> | (synchronous call) (wait)
	 * updateContent() | | | |
	 */
	private class ReloadTask extends Thread {

		private volatile boolean mActive = true;
		private volatile boolean mDirty = true;
		private boolean mIsLoading = false;

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
				if (updateComplete){
					continue;
				}
				synchronized (DataManager.LOCK) {
					if (info.version != version) {
						info.size = mSource.getMediaItemCount();
						info.version = version;
					}
					if (info.reloadCount > 0) {
						info.items = mSource.getMediaItem(info.reloadStart,
								info.reloadCount);
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

	// 将图片放入缓存
	private class AlbumDisplayItem extends AbstractDisplayItem implements
			FutureListener<Bitmap>, Job<Bitmap> {
		private Future<Bitmap> mFuture;
		private final int mSlotIndex;
		private final int mMediaType;
		private Texture mContent;
		private boolean mIsPanorama;
		private ImageLoad imageLoad;
		private int index = 0;
		private ViewHolder holder;
		final Handler mHander = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				imageLoad.onImageLoader((Bitmap) msg.obj, index, holder);
			}

		};

		// 请求更新图片前，需要设置图片加载监听
		public void setImageLoadListener(ImageLoad loader) {
			imageLoad = loader;
		}

		public AlbumDisplayItem(int slotIndex, MediaItem item, ViewHolder holder) {
			super(item);
			mMediaType = (item == null) ? MediaItem.MEDIA_TYPE_UNKNOWN : item
					.getMediaType();
			mSlotIndex = slotIndex;
			mIsPanorama = GalleryUtils.isPanorama(item);
			this.holder = holder;
			index = slotIndex;

		}

		@Override
		protected void onBitmapAvailable(Bitmap bitmap) {
			Message msg = mHander.obtainMessage();
			msg.obj = bitmap;
			mHander.sendMessage(msg);
		}

		@Override
		public void startLoadBitmap() {
			if (mCacheThumbSize > 0) {
				Path path = mMediaItem.getPath();
				if (mImageCache.containsKey(path)) {
					Bitmap bitmap = mImageCache.get(path);
					updateImage(bitmap, false);
					return;
				}
				mFuture = mThreadPool.submit(this, this);
			} else {
				mFuture = mThreadPool.submit(
						mMediaItem.requestImage(MediaItem.TYPE_MICROTHUMBNAIL),
						this);
			}
		}

		// This gets the bitmap and scale it down.
		public Bitmap run(JobContext jc) {
			Job<Bitmap> job = mMediaItem
					.requestImage(MediaItem.TYPE_MICROTHUMBNAIL);
			Bitmap bitmap = job.run(jc);
			if (bitmap != null) {
				bitmap = BitmapUtils.resizeDownBySideLength(bitmap,
						mCacheThumbSize, true);
			}
			return bitmap;
		}

		@Override
		public void cancelLoadBitmap() {
			if (mFuture != null) {
				mFuture.cancel();
			}
		}

		@Override
		public void onFutureDone(Future<Bitmap> bitmap) {
			mMainHandler.sendMessage(mMainHandler.obtainMessage(
					MSG_LOAD_BITMAP_DONE, this));

		}

		private void onLoadBitmapDone() {
			Future<Bitmap> future = mFuture;
			mFuture = null;
			Bitmap bitmap = future.get();
			boolean isCancelled = future.isCancelled();
			if (mCacheThumbSize > 0 && (bitmap != null || !isCancelled)) {
				Path path = mMediaItem.getPath();
				mImageCache.put(path, bitmap);
			}
			updateImage(bitmap, isCancelled);
		}

		@Override
		public String toString() {
			return String.format("AlbumDisplayItem[%s]", mSlotIndex);
		}

		@Override
		public int render(GLCanvas canvas, int pass) {
			// TODO Auto-generated method stub
			return 0;
		}
	}

	// 图片加载接口
	public interface ImageLoad {
		public void onImageLoader(Bitmap bitmap, int index, ViewHolder holder);
	}

	// ListView的ViewHolder
	class ViewHolder {
		public AlbumImageView imageview1;
		public AlbumImageView imageview2;
		public AlbumImageView imageview3;
		public AlbumImageView imageview4;
		public AlbumImageView imageview5;
		public AlbumImageView imageview6;
		public AlbumImageView imageview7;
		public AlbumImageView imageview8;
		public ImageView linebackground1;
		public ImageView linebackground2;
		public ImageView linebackground3;
		public ImageView linebackground4;
		public ImageView linebackground0;
		public ImageView selected1;
		public ImageView selected2;
		public ImageView selected3;
		public ImageView selected4;
		public ImageView selected5;
		public ImageView selected6;
		public ImageView selected7;
		public ImageView selected8;
		public View view1;
		public View view2;
		public View view3;
		public View view4;
		public View view5;
		public View view6;
		public View view7;
		public View view8;
	}

	static class AlbumOnClickListener implements OnClickListener {

		private static AlbumOnClickListener instance = null;

		private AlbumOnClickListener() {
		}

		public static AlbumOnClickListener getInstance() {
			if (instance == null)
				instance = new AlbumOnClickListener();
			return instance;
		}

		@Override
		public void onClick(View view) {
			// TODO: do something here

		}
	}

	// 按时间降序排列
	static class FileComparator implements Comparator<MediaItem> {
		public int compare(MediaItem item1, MediaItem item2) {
			return -Utils.compare(((LocalImage) item1).dateModifiedInSec,
					((LocalImage) item2).dateModifiedInSec);
		}
	}
	//以下是时间轴显示部分
	//获取时间
	public String getCurrentImageTime(MediaItem currentImages,boolean countFlag){
		long differenceTime =0;
		long currentModifiedTime = System.currentTimeMillis()/1000;			//当前时间
		differenceTime = currentModifiedTime-((LocalImage)(currentImages)).dateModifiedInSec;
		if(differenceTime >0&& differenceTime<24*60*60){
			if(countFlag){
				todayNum++;
			}
			return "今天";
		}
		else if(differenceTime>24*60*60 && differenceTime <2*24*60*60){
			if(countFlag){
				yestodayNum++;
			}
			return "昨天";
		}
		else if(differenceTime>2*24*60*60 && differenceTime <7*24*60*60){
			if(countFlag){
				weekNum++;
			}
			return "一周内";
		}
		else if(differenceTime>7*24*60*60 && differenceTime <4*7*24*60*60){
			if(countFlag){
				monthNum++;
			}
			return "一个月内";
		}
		else{
			if(countFlag){
				otherNum++;
			}
			return "一个月前";
		}
	}
	
	private int getTimeNum(String time){
		if(time.equals("今天")){
			return todayNum;
		}
		else if(time.equals("昨天")){
			return yestodayNum;
		}
		else if(time.equals("一周内")){
			return weekNum;
		}
		else if(time.equals("一个月内")){
			return monthNum;
		}
		else if(time.equals("一个月前")){
			return otherNum;
		}
		return 0;
	}
	
    private Position[] getSectionIndices() {
        ArrayList<Position> sectionIndices = new ArrayList<Position>();
        todayNum = 0;
        yestodayNum = 0;
        weekNum = 0;
        monthNum = 0;
        otherNum  =0;
        String time="";
        for (int i = 0; i < adapterLen; i++) {
        	String sTime = getCurrentImageTime(albumImages.get(i),true);
            if (!sTime.equals(time)) {
            	time = sTime;
            	if(i==0){
            	   	Position position = new Position();
                	position.position = 0;
                	position.row = 0;
                	position.lackNum = getTimeNum(time)%8;
            		sectionIndices.add(position);
            	}
            	else{
            		String tempTime = getCurrentImageTime(albumImages.get(sectionIndices.get(sectionIndices.size()-1).position),false);
        			Position position = new Position();
        			position.position = i;
        			if(getTimeNum(tempTime)%8==0){
        				position.row = sectionIndices.get(sectionIndices.size()-1).row+getTimeNum(tempTime)/8;
        			}
        			else{
        				position.row = sectionIndices.get(sectionIndices.size()-1).row+getTimeNum(tempTime)/8+1;
        			}
     
        			sectionIndices.add(position);
            	}
        	} 
        }
        mSectionLen = sectionIndices.size();
        Position[] sections = new Position[mSectionLen];
        for (int j = 0; j < mSectionLen; j++) {
        	//sections[j]=sectionIndices.get(j);
        	sections[j]=sectionIndices.get(j);
    		String tempTime = getCurrentImageTime(albumImages.get(sectionIndices.get(j).position),false);
        	sections[j].lackNum = (getTimeNum(tempTime)%8);
        }
        
        return sections;
    }
    
    private String[] getSectionLetters() {
        String[] letters = new String[mSectionIndices.length];
        for (int i = 0; i < mSectionIndices.length; i++) {
            letters[i] = getCurrentImageTime(albumImages.get(mSectionIndices[i].position),false);
        }
        return letters;
    }
    
	@Override
	public Object[] getSections() {
		// TODO Auto-generated method stub
		return mSectionLetters;
	}

	@Override
	public int getPositionForSection(int section) {
		// TODO Auto-generated method stub
	      if (mSectionIndices.length == 0) {
	            return 0;
	        }
	        
	        if (section >= mSectionIndices.length) {
	            section = mSectionIndices.length - 1;
	        } else if (section < 0) {
	            section = 0;
	        }
	        return mSectionIndices[section].row;
	}

	@Override
	public int getSectionForPosition(int position) {
		// TODO Auto-generated method stub
        for (int i = 0; i < mSectionIndices.length; i++) {
            if (position < mSectionIndices[i].row) {
                return i - 1;
            }
        }

        return mSectionIndices.length - 1;
	}

	@Override
	public long getHeaderId(int arg1) {
		// TODO Auto-generated method stub
		//int headerLackNum = 0;
		//headerLackNum = getLackNum(arg1);
		int id =0;		
		//if(albumImages.size()>arg1*8-headerLackNum){
			//id =getTimeId(getCurrentImageTime(albumImages.get(arg1*8-headerLackNum),false));
		//}
		for(int k=0;k<mSectionLen-1;k++){
			if( mSectionIndices[k].row <= arg1 && arg1 <mSectionIndices[k+1].row||
					arg1 >=mSectionIndices[mSectionLen-1].row){
				if(arg1 >=mSectionIndices[mSectionLen-1].row){
					k = mSectionLen-1;
				}
				id =getTimeId(mSectionLetters[k]);
				break;
			}
		}
		if(mSectionLen ==1){
			id =getTimeId(mSectionLetters[0]);
		}
		return id;
	}
	//获取每个时间名称的对应id，直接用字符串首字符的int值，后三个名称的值是相同的，导致headview添加不上
	public int getTimeId(String time){
		int id=0;
		if(time.equals("今天")){
			id =1;
		}
		else if(time.equals("昨天")){
			id =2;
		}
		else if(time.equals("一周内")){
			id =3;
		}
		else if(time.equals("一个月内")){
			id =4;
		}
		else if(time.equals("一个月前")){
			id =5;
		}
		else{
			
		}
		return id;
	}
	
	@Override
	public View getHeaderView(int arg0, View convertView, ViewGroup arg2) {
		// TODO Auto-generated method stub
		//int headerLackNum = 0;
		//headerLackNum = getLackNum(arg0);
        HeaderViewHolder holder;
        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.layout_previewer_album_header, null, false);
            holder.text = (albumTimeTextView) convertView.findViewById(R.id.text1);
            ViewGroup.LayoutParams par = (ViewGroup.LayoutParams)holder.text.getLayoutParams();
            par.height =1;
            holder.text.setLayoutParams(par);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        // set header text as first char in name
        // if(albumImages.size()>arg0*8-headerLackNum){
        //String time = getCurrentImageTime(albumImages.get(arg0*8-headerLackNum),false);
        // 	holder.text.setText(time);
        //}
		for(int k=0;k<mSectionLen-1;k++){
			if( mSectionIndices[k].row <= arg0 && arg0 <mSectionIndices[k+1].row||
					arg0 >=mSectionIndices[mSectionLen-1].row){
				if(arg0 >=mSectionIndices[mSectionLen-1].row){
					k = mSectionLen-1;
				}
		        holder.text.setText(mSectionLetters[k]);
				//AlbumActivity.timeSection.setText(mSectionLetters[k]);
				break;
			}
		}
		if(mSectionLen ==1){
			 holder.text.setText(mSectionLetters[0]);
				//AlbumActivity.timeSection.setText(mSectionLetters[0]);
		}
        return convertView;
	}
	
    class HeaderViewHolder {
    	albumTimeTextView text;
    }
    //获取每个position时，相对于每个position是8张图片时，缺少的图片个数，适用于图片列表的和header
    public int getLackNum(int position){
		int iLackNum = 0;
		int temp=0;
		
		for(int k=0;k<mSectionLen-1;k++){
			if( mSectionIndices[k].row <= position && position <mSectionIndices[k+1].row||
					position >=mSectionIndices[mSectionLen-1].row){
				if(position >=mSectionIndices[mSectionLen-1].row){
					k = mSectionLen-1;
				}
				if(k==0){
					iLackNum = 0;
				}
				else{
					for(int i=0;i<k;i++){
						//String name = mSectionLetters[i];
						timeNume = getTimeNum(mSectionLetters[i]);
						temp =timeNume%8;
						if(temp!=0){
							iLackNum+=(8-temp);
						}
					}
				}
				break;
			}
		}

		return iLackNum;
    }
    public String getTimeText(int position){
    	for(int k=0;k<mSectionLen-1;k++){
			if( mSectionIndices[k].row <= position && position <mSectionIndices[k+1].row||
					position >=mSectionIndices[mSectionLen-1].row){
				if(position >=mSectionIndices[mSectionLen-1].row){
					k = mSectionLen-1;
				}
		        return mSectionLetters[k];
				//AlbumActivity.timeSection.setText(mSectionLetters[k]);
			}
		}
		if(mSectionLen ==1){
			 return mSectionLetters[0];
				//AlbumActivity.timeSection.setText(mSectionLetters[0]);
		}
		return "";
    }
    public int getItemViewHeight(int position){
    	int mHeight = 0;
    	boolean findFlag = false;
    	for(int k=1;k<=mSectionLen-1;k++){
    		if(mSectionIndices[k].row-1==position){
    			findFlag = true;
    			int remainNum = mSectionIndices[k-1].lackNum;
    			if(1<=remainNum && remainNum<=3){
    				mHeight+=(236+3)*density;
    				break;
    			}
    			else if(4<=remainNum && remainNum<=5){
    				mHeight+=(236+115+6)*density;
    			    break;
    			}
    			else{
    				mHeight+=(236+230+9)*density;
    				break;
    			}
    		}
    	}
    	if(!findFlag){
    		if(getCount()-1==position){
    			findFlag = true;
    			int remainNum = mSectionIndices[mSectionLen-1].lackNum;
    			if(1<=remainNum && remainNum<=3){
    				mHeight+=(236+3)*density;
    			}
    			else if(4<=remainNum && remainNum<=5){
    				mHeight+=(236+115+6)*density;
    			}
    			else{
    				mHeight+=(236+230+9)*density;
    			}
    		}
    	}
    	if(!findFlag){
    		mHeight+=(236+230+9)*density;
    	}
    	return mHeight;
    }
    private class PicOnClick implements OnClickListener{
    	public ImageView image;
    	public int mIndex=0;
    	public View view;
    	public PicOnClick(ImageView  selected1,int index,View masking){
    		image = selected1;
    		mIndex  = index;
    		view = masking;
    	}
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub

			int lackNum =getLackNum(((AlbumImageView) v).getIndex());
			if(((AlbumActivity) context).getSelectMode()){
				setItemVisible(true,mIndex,image,lackNum,v,view);
			}
		}
    	
    }
    //长按事件
    private class PicOnLongClick implements OnLongClickListener{
    	public ViewHolder holder;
    	
    	public PicOnLongClick(ViewHolder  mHolder){
    		holder = mHolder;
    	}
    	
		@Override
		public boolean onLongClick(View v) {
			// TODO Auto-generated method stub
			AlbumImageView view = (AlbumImageView)v;
			int id= view.getId();
			boolean isSlecetMode = false;
			int mLackNum =getLackNum((view).getIndex());
			if(activityStr.equals("AlbumActivity")){
				isSlecetMode = ((AlbumActivity) context).getSelectMode(); // 更新界面个数
			}
			switch(id){
				case R.id.imageview1: 
				{	
					setItemVisible(isSlecetMode,0,holder.selected1,mLackNum,v,holder.view1);
					break;
				}
				case R.id.imageview2:
				{	
					setItemVisible(isSlecetMode,1,holder.selected2,mLackNum,v,holder.view2);
					break;
				}
				case R.id.imageview3:
				{	
					setItemVisible(isSlecetMode,2,holder.selected3,mLackNum,v,holder.view3);
					break;
				}
				case R.id.imageview4:
				{	
					setItemVisible(isSlecetMode,3,holder.selected4,mLackNum,v,holder.view4);
					break;
				}
				case R.id.imageview5:
				{	
					setItemVisible(isSlecetMode,4,holder.selected5,mLackNum,v,holder.view5);
					break;
				}
				case R.id.imageview6:
				{	
					setItemVisible(isSlecetMode,5,holder.selected6,mLackNum,v,holder.view6);
					break;
				}
				case R.id.imageview7:
				{	
					setItemVisible(isSlecetMode,6,holder.selected7,mLackNum,v,holder.view7);
					break;
				}
				case R.id.imageview8:
				{	
					setItemVisible(isSlecetMode,7,holder.selected8,mLackNum,v,holder.view8);
					break;
				}
			}
			return true;
		}  
    	
    }
    //长按后是否显示图片
    public void setItemVisible(boolean isSlecetMode,int index,ImageView selectindex,int lackNum,
    		View clickView,View masking){
		int mIndex = 0;
		mIndex = ((AlbumImageView) clickView).getIndex()
				* IMAGENUM_LISTVIEW_PER_ITEM + index-lackNum;
    	Path path = getMediaItem(mIndex).getPath();
		longClickListener.onPreviewLongClick(mIndex);
		//第一次长按传递的isSlecetMode为false
		if(activityStr.equals("AlbumActivity")&&!isSlecetMode){
			isSlecetMode = ((AlbumActivity) context).getSelectMode(); // 更新界面个数
		}
	    boolean selectedFlag = ((AlbumActivity) context).isSelectedItem(path);
		if(isSlecetMode){
			if(!selectedFlag){
				selectindex.setVisibility(View.GONE);
				masking.setVisibility(View.GONE);
			}
			else{
				selectindex.setVisibility(View.VISIBLE);
				masking.setVisibility(View.VISIBLE);
			}
		}
    }
    
    public MediaItem getMediaItem(int index){
    	return albumImages.get(index);
    }
    //滑动listview重新加载界面时判断是否显示选中标记
    public void setSelectedIconVisible(boolean isSlecetMode,int position,int index,ImageView selectindex,
    		View masking,int mLckNum){
    	int mIndex = 0;
		mIndex = position* IMAGENUM_LISTVIEW_PER_ITEM + index-mLckNum;
		Path path = getMediaItem(mIndex).getPath();
		boolean selectedFlag = ((AlbumActivity) context).isSelectedItem(path);
		if(isSlecetMode){
			if(selectedFlag){
				selectindex.setVisibility(View.VISIBLE);
				masking.setVisibility(View.VISIBLE);
			}
			else{
				selectindex.setVisibility(View.GONE);
				masking.setVisibility(View.GONE);
			}
		}
		else{
			selectindex.setVisibility(View.GONE);
			masking.setVisibility(View.GONE);
		}
    }

	@Override
	public void onAddAllItemToList(Set<Path> mClickedSet) {
		// TODO Auto-generated method stub
	      for (MediaItem item : albumImages) {
	            Path id = item.getPath();
	            mClickedSet.add(id);
	        }
	}
	//退出选择模式刷新图标
	public void refreshSelectIcon(int position,View view){
		boolean isSlecetMode = ((AlbumActivity) context).getSelectMode(); // 更新界面个数
		int mLackNum =getLackNum(position);
		if(view==null){
			return;
		}
		AlbumImageView imageview1 = (AlbumImageView) view.findViewById(R.id.imageview1);
		AlbumImageView imageview2 = (AlbumImageView) view.findViewById(R.id.imageview2);
		AlbumImageView imageview3 = (AlbumImageView) view.findViewById(R.id.imageview3);
		AlbumImageView imageview4 = (AlbumImageView) view.findViewById(R.id.imageview4);
		AlbumImageView imageview5 = (AlbumImageView) view.findViewById(R.id.imageview5);
		AlbumImageView imageview6 = (AlbumImageView) view.findViewById(R.id.imageview6);
		AlbumImageView imageview7 = (AlbumImageView) view.findViewById(R.id.imageview7);
		AlbumImageView imageview8 = (AlbumImageView) view.findViewById(R.id.imageview8);
		ImageView sele1 = (ImageView)view.findViewById(R.id.selected1);
		ImageView sele2 = (ImageView)view.findViewById(R.id.selected2);
		ImageView sele3 = (ImageView)view.findViewById(R.id.selected3);
		ImageView sele4 = (ImageView)view.findViewById(R.id.selected4);
		ImageView sele5 = (ImageView)view.findViewById(R.id.selected5);
		ImageView sele6 = (ImageView)view.findViewById(R.id.selected6);
		ImageView sele7 = (ImageView)view.findViewById(R.id.selected7);
		ImageView sele8 = (ImageView)view.findViewById(R.id.selected8);
		View msking1 = (View)view.findViewById(R.id.view1);
		View msking2 = (View)view.findViewById(R.id.view2);
		View msking3 = (View)view.findViewById(R.id.view3);
		View msking4 = (View)view.findViewById(R.id.view4);
		View msking5 = (View)view.findViewById(R.id.view5);
		View msking6 = (View)view.findViewById(R.id.view6);
		View msking7 = (View)view.findViewById(R.id.view7);
		View msking8 = (View)view.findViewById(R.id.view8);
		//在有图片时才显示selecticon
		if(imageview1.getVisibility()==View.VISIBLE){
			setSelectedIconVisible(isSlecetMode,position,0,sele1,msking1,mLackNum);
		}
		if(imageview2.getVisibility()==View.VISIBLE){
			setSelectedIconVisible(isSlecetMode,position,1,sele2,msking2,mLackNum);
		}
		if(imageview3.getVisibility()==View.VISIBLE){
			setSelectedIconVisible(isSlecetMode,position,2,sele3,msking3,mLackNum);
		}
		if(imageview4.getVisibility()==View.VISIBLE){
			setSelectedIconVisible(isSlecetMode,position,3,sele4,msking4,mLackNum);
		}
		if(imageview5.getVisibility()==View.VISIBLE){
			setSelectedIconVisible(isSlecetMode,position,4,sele5,msking5,mLackNum);
		}
		if(imageview6.getVisibility()==View.VISIBLE){
			setSelectedIconVisible(isSlecetMode,position,5,sele6,msking6,mLackNum);
		}
		if(imageview7.getVisibility()==View.VISIBLE){
			setSelectedIconVisible(isSlecetMode,position,6,sele7,msking7,mLackNum);
		}
		if(imageview8.getVisibility()==View.VISIBLE){
			setSelectedIconVisible(isSlecetMode,position,7,sele8,msking8,mLackNum);
		}
	}
}