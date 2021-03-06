package com.ktouch.kdc.launcher4.app;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import com.ktouch.kdc.launcher4.R;
import com.ktouch.kdc.launcher4.app.AlbumActivityDataAdapter.PreviewLongClick;
import com.ktouch.kdc.launcher4.app.AlbumActivityDataAdapter.SelectModeChange;
import com.ktouch.kdc.launcher4.data.DataManager;
import com.ktouch.kdc.launcher4.data.MediaItem;
import com.ktouch.kdc.launcher4.data.MediaSet;
import com.ktouch.kdc.launcher4.data.Path;
import com.ktouch.kdc.launcher4.ui.AlbumSetSlidingLayout;
import com.ktouch.kdc.launcher4.ui.ConfirmDialog;
import com.ktouch.kdc.launcher4.ui.CustomScrollView;
import com.ktouch.kdc.launcher4.ui.PreviewerSelectionManager;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

//bill create 20140728
public class AlbumActivity extends Activity implements PreviewLongClick,
		PreviewerSelectionManager.SelectionListener, SelectModeChange {
	public static final int IMAGE_SCANSTART = 0;
	public static final int IMAGE_SCANFINISH = 1;
	private static final int DATA_CACHE_SIZE = 256;
	public static final int SNAP_VELOCITY = 20000; // lianglei add
	// public static AlbumActivity gInstance = null;
	private String selectAlbumSetName = ""; // 记录选择的albumset名称
	private Path mMediaSetPath;
	private StickyListHeadersListView albumList = null;
	private ListView listNavigation = null;
	private TextView albumNum = null;
	private TextView title = null;
	private TextView allPaper = null;
	private TextView allpapersum = null;
	private TextView littlechartitle = null;
	private TextView littlenum = null;
	// private ImageView albumsetloading = null;
	private LinearLayout listlayout = null;
	private RelativeLayout loadingLayout = null;
	private RelativeLayout iconlayout = null;
	private RelativeLayout moretitlelayout = null;
	private RelativeLayout albumpropmptlayout = null;
	private RelativeLayout albumsetpropmptlayout = null;
	private RelativeLayout littletitlelayout = null;
	private RelativeLayout allpaperlayout = null;
	// private RelativeLayout albumsetloadinglayout = null;
	private RelativeLayout settinglayout = null;
	private RelativeLayout addModeLayout = null;
	private AlbumSetSlidingLayout leftSliderLayout;
	private MediaSet mMediaSet;
	private MediaSet mAlbumSetMediaSet;
	private MediaSet mAllPaperMediaSet;
	// private ProgressDialog progressDialog;
	private AlbumActivityDataAdapter albumAdapter = null;
	private AlbumSetActivityDataAdapter albumSetAdapter = null;
	private AlbumSetActivityDataAdapter allPaperSetAdapter = null;
	private AlbumActivityDataAdapter allPaperAdapter = null;
	private SharedPreferences previewerInfo = null;
	// private Animation loadingAnim = null;
	private ImageView loadingimage = null;
	private ImageView logoImage = null;
	private boolean isSelectAllPaper = false;
	private boolean albumReresh = false; // 为了解决侧滑时albumlist会刷新，图片会挤压变窄的问题
	private boolean isStartEnter = true; // 是否是启动界面，false时为从下一级图片预览界面返回
	private boolean isStartFinish = false; // 解决连续点击两次应用图标时，有时崩溃的问题，因为两次都调用了适配器的resume
	private int curSelectPos = 0;
	private int top = 0;
	private int albumsetSelectIndex = 0; // 上次选择相册的索引，图片浏览时返回时使用
	// lianglei begin
	private long exitTime = 0;
	// lianglei End
	private boolean isFirstIn = false;
	private boolean startSplash = false;
	public boolean enterTemplete = false; // 标志是否从右上角图标进入了模板编辑界面
	private boolean isFirstLoadFinish = false;
	private Animation loadingAnim3 = null;
	public static TextView timeSection = null;
	private String currentTimeSection = "";
	private CustomScrollView fastScrollView;
	private ImageView addmode;
	protected PreviewerSelectionManager mSelectionManager;
	private LinearLayout mainTitle;
	private int selectNum = 0;
	private TextView allSelect;
	private FrameLayout allSelectLayout;
	private TextView selectItems;
	private PopupWindow popWindow;
	private DisplayMetrics displayMetrics;
	private int height_screen;
	private int width_screen;
	private TextView multipleDelete;
	private int lastSelectNum = 0;
	
   
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_previewer_album);
		UmengUpdateAgent.setUpdateCheckConfig(false);// 友盟 检查更新 Debug模式
		MobclickAgent.setDebugMode(true);// 友盟统计 Debug模式
		// UmengUpdateAgent.setUpdateOnlyWifi(false);// 友盟 检查更新 是否Wi-Fi Only
		UmengUpdateAgent.update(this);// 友盟 检查更新 默认Wi-Fi Only
		initAlbumSet(); // 获取albumset
		getViews();
		getAllPaper(); // 获取全部壁纸
		mSelectionManager = new PreviewerSelectionManager(AlbumActivity.this,
				false);
		mSelectionManager.setSelectionListener(AlbumActivity.this);
		displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		height_screen = displayMetrics.heightPixels;
		width_screen = displayMetrics.widthPixels;
	}

	// 获取albumset
	private void initAlbumSet() {
		loadingLayout = (RelativeLayout) findViewById(R.id.loading);
		loadingimage = (ImageView) findViewById(R.id.loadingimage);
		logoImage = (ImageView) findViewById(R.id.logoimage);
		loadingLayout.setVisibility(View.VISIBLE);
		Animation loadingAnim1 = null;
		Animation loadingAnim2 = null;

		loadingAnim1 = AnimationUtils.loadAnimation(this, R.anim.welcome_scale);
		loadingimage.startAnimation(loadingAnim1);
		loadingAnim2 = AnimationUtils.loadAnimation(this,
				R.anim.welcome_translate);
		logoImage.startAnimation(loadingAnim2);
		loadingAnim3 = AnimationUtils.loadAnimation(this, R.anim.welcome_alpha);
		loadingLayout.startAnimation(loadingAnim3);
		Handler hander = new Handler();
		hander.postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				// SharedPreferences preferences = getSharedPreferences(
				// SplashActivity.SHAREDPREFERENCES_NAME, MODE_PRIVATE);

				// 取得相应的值，如果没有该值，说明还未写入，用true作为默认值
				// isFirstIn = preferences.getBoolean("isFirstIn", true);
				// Intent intent = new Intent();
				// intent.setClass(AlbumActivity.this, SplashActivity.class);
				// AlbumActivity.this.startActivity(intent);
				// }
				// else
				{
					logoImage.clearAnimation();
					loadingimage.clearAnimation();
				}
			}

		}, 2600);
		Handler hander1 = new Handler();
		hander1.postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				{
					loadingLayout.clearAnimation();
					loadingLayout.setVisibility(View.GONE);
				}
			}

		}, 3700);
		previewerInfo = getSharedPreferences("previewer_info", 0);
		selectAlbumSetName = previewerInfo.getString("albumName", "");
		listNavigation = (ListView) findViewById(R.id.listnavigation);
		String path = ((GalleryApp) getApplication()).getDataManager()
				.getTopSetPath(DataManager.INCLUDE_IMAGE);
		mAlbumSetMediaSet = ((GalleryApp) getApplication()).getDataManager()
				.getMediaSet(path);
		albumSetAdapter = new AlbumSetActivityDataAdapter(AlbumActivity.this,
				(GalleryApp) getApplication(), mAlbumSetMediaSet, path,
				DATA_CACHE_SIZE);
		listNavigation.setAdapter(albumSetAdapter);
		albumSetAdapter.setLoadingListener(new AlbumSetLoadingListener());
		albumpropmptlayout = (RelativeLayout) findViewById(R.id.albumpropmptlayout);
		albumsetpropmptlayout = (RelativeLayout) findViewById(R.id.albumsetpropmptlayout);
		// progressDialog = progressDialog.show(this, null, "正在加载数据，请稍候...",
		// true, false);
	}

	public void getViews() {
		title = (TextView) findViewById(R.id.title);
		littlechartitle = (TextView) findViewById(R.id.littlechartitle);
		littlenum = (TextView) findViewById(R.id.littlenum);
		listlayout = (LinearLayout) findViewById(R.id.listlayout);
		moretitlelayout = (RelativeLayout) findViewById(R.id.moretitlelayout);
		littletitlelayout = (RelativeLayout) findViewById(R.id.littletitlelayout);
		// albumsetloadinglayout = (RelativeLayout)
		// findViewById(R.id.albumsetloadinglayout);
		iconlayout = (RelativeLayout) findViewById(R.id.iconlayout);
		listNavigation = (ListView) findViewById(R.id.listnavigation);
		albumList = (StickyListHeadersListView) findViewById(R.id.albumlist);
		albumNum = (TextView) findViewById(R.id.num);
		// albumsetloading = (ImageView)findViewById(R.id.albumsetloading);
		allPaper = (TextView) findViewById(R.id.allpaper);
		allpaperlayout = (RelativeLayout) findViewById(R.id.allpaperlayout);
		allpapersum = (TextView) findViewById(R.id.allpapersum);
		settinglayout = (RelativeLayout) findViewById(R.id.settinglayout);
		leftSliderLayout = (AlbumSetSlidingLayout) findViewById(R.id.main_slider_layout);
		addModeLayout = (RelativeLayout) findViewById(R.id.addmodelayout);
		addmode = (ImageView) findViewById(R.id.addmode);
		mainTitle = (LinearLayout) findViewById(R.id.maintitle);

		if (mMediaSetPath == null) {
			addmode.setBackground(null);
		}
		// timeSection = (TextView) findViewById(R.id.timesection);
		// leftSliderLayout.setOnLeftSliderLayoutListener(this);
		fastScrollView = (CustomScrollView) findViewById(R.id.fast_scroll_view);
		settinglayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(AlbumActivity.this, SettingActivity.class);
				startActivity(intent);
			}
		});
		allpaperlayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 每次点击全部壁纸时把显示位置设为0
				curSelectPos = 0;
				top = 0;
				getAllPaper();
				allPaperSetAdapter.resume();
				if (leftSliderLayout.isOpen()) {
					leftSliderLayout.close();
				}// 隐藏左侧列表
			}
		});
		listNavigation.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				fastScrollView.setSwitchFolderFlag(); // 解决时间控件总是在切换图片集合时显示
				if (allPaperSetAdapter != null) {
					allPaperSetAdapter.pause();
				}
				if (allPaperAdapter != null) {

					allPaperAdapter.pause();
				}
				// 每次点击相册时把显示位置设为0
				curSelectPos = 0;
				top = 0;
				albumsetSelectIndex = position;
				albumReresh = true;
				MediaSet targetSet = albumSetAdapter.getMediaSet(position);
				if (targetSet == null)
					return; // Content is dirty, we shall reload soon
				mMediaSetPath = targetSet.getPath();
				mMediaSet = ((GalleryApp) getApplication()).getDataManager()
						.getMediaSet(mMediaSetPath);
				title.setText(mMediaSet.getName());
				selectAlbumSetName = mMediaSet.getName();
				albumAdapter = null;
				albumAdapter = new AlbumActivityDataAdapter(AlbumActivity.this,
						(GalleryApp) getApplication(), mMediaSet,
						mMediaSetPath, albumList, "AlbumActivity");
				albumAdapter.setIsAllPaper(false);
				albumAdapter.setLongClickListener(AlbumActivity.this); // 多项选中
				albumList.setAdapter(albumAdapter);
				albumAdapter.resume();
				// 显示选中项颜色，同时把全部壁纸的改为默认色
				if (isSelectAllPaper == true) {
					allpaperlayout.setBackgroundColor(Color
							.parseColor("#ffffff"));
					allPaper.setTextColor(Color.parseColor("#595959"));
					allpapersum.setTextColor(Color.parseColor("#595959"));
					isSelectAllPaper = false;
				}
				albumSetAdapter.setSelectItem(position); // 设置选中项 0917
				albumSetAdapter.notifyDataSetInvalidated();
				if (leftSliderLayout.isOpen()) {
					leftSliderLayout.close();
				}// 隐藏左侧列表
			}

		});

		iconlayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (leftSliderLayout.isOpen()) {
					leftSliderLayout.close();
				} else {
					leftSliderLayout.open();
				}
			}

		});
		// 进入模板编辑界面
		addModeLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 20150115 begin 解决个别手机图片为加载完成时点击进入模板会崩溃退出
				addModeFunction();
			}

		});
	}

	// 获取全部壁纸
	public void getAllPaper() {
		title.setText("全部壁纸");
		selectAlbumSetName = "全部壁纸";
		String basePath = "/combo/{/mtp,/local/image,/picasa/image}";
		String newPath = FilterUtils.switchClusterPath(basePath,
				FilterUtils.CLUSTER_BY_TIME);
		mAllPaperMediaSet = ((GalleryApp) getApplication()).getDataManager()
				.getMediaSet(newPath);
		allPaperSetAdapter = new AlbumSetActivityDataAdapter(
				AlbumActivity.this, (GalleryApp) getApplication(),
				mAllPaperMediaSet, newPath, DATA_CACHE_SIZE);
		allPaperSetAdapter.setLoadingListener(new AllPaperLoadingListener());
		// allPaperSetAdapter.resume();改为在该函数外部调用
		// 如果选中全部壁纸，则显示选中颜色同时把listview中的选中项值改为-1，并刷新
		isSelectAllPaper = true;
		allpaperlayout.setBackgroundColor(Color.parseColor("#f5f5f5"));
		allPaper.setTextColor(Color.parseColor("#ef400a"));
		allpapersum.setTextColor(Color.parseColor("#ef400a"));
		albumSetAdapter.setSelectItem(-1); // 设置选中项 0917
		albumSetAdapter.notifyDataSetInvalidated();
		fastScrollView.listItemsChanged();
		fastScrollView.setSwitchFolderFlag(); // 解决时间控件总是在切换图片集合时显示
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
	
		
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			SharedPreferences.Editor editor = previewerInfo.edit();
			editor.putString("albumName", selectAlbumSetName);
			editor.commit();
			if (getSelectMode()) {
				mSelectionManager.deSelectAll();
				return true;
			}
			// lianglei begin
			if ((System.currentTimeMillis() - exitTime) > 2000) {
				Toast.makeText(getApplicationContext(),
						R.string.doubleClickBack, Toast.LENGTH_SHORT).show();
				exitTime = System.currentTimeMillis();
			} else {
				MobclickAgent.onPause(this);// 友盟统计
				finish();
				System.exit(0);
			}
			return true;
			// lianglei end
		}
		return super.onKeyDown(keyCode, event);

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onResume(this);// 友盟统计
		if (isStartEnter && !isStartFinish) {
			isStartFinish = true;
			// bill 20150106 modify
			// 由于交互要求改为第一次使用app时只显示三个文件夹，需要等到albumSetAdapter线程执行完成后
			// 才能执行全部壁纸的allPaperSetAdapter，否则，第一次使用时全部壁纸显示的是所有相册的集合，而不是只是这三个文件夹，注释掉这行
			// allPaperSetAdapter.resume();
			albumSetAdapter.resume();
		}
		if (enterTemplete) {
			enterTemplete = false;
		}
		// if(startSplash){
		// startSplash = false;
		// stopAnimation();
		// }

	}

	// 获取上次显示位置
	public void getSetSelectedPos() {
		albumList.setSelectionFromTop(curSelectPos, top);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		curSelectPos = albumList.getFirstVisiblePosition();
		View v = albumList.getListChildAt(0);
		top = (v == null) ? 0 : v.getTop();
		MobclickAgent.onPause(this);// 友盟统计
		// if(isFirstIn){
		// startSplash = true;
		// }
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (albumSetAdapter != null) {
			albumSetAdapter.setLoadingListener(null);
		}
		if (albumAdapter != null) {
			albumAdapter.setLoadingListener(null);
		}
	}

	public void refreshAlbumNum() {
		String num = String.valueOf(albumAdapter.getAlbumItemSize());
		displayImageNum(num);

	}

	public void displayImageNum(String num) {
		// 修改重复刷新图片数目
		if (isSelectAllPaper) {
			return;
		}
		if (selectAlbumSetName.getBytes().length <= 15) {
			if (littlechartitle != null) {
				littletitlelayout.setVisibility(View.VISIBLE);
				moretitlelayout.setVisibility(View.GONE);
				// littlechartitle.setText(selectAlbumSetName+"(" + num + ")");
				littlechartitle.setText(selectAlbumSetName);
				// 因为没有文件夹时目前显示为全部壁纸，用littlenum显示
				if (albumSetAdapter.getCount() == 0) {
					littlenum.setText("(" + 0 + ")");
				} else {
					littlenum.setText("(" + num + ")");
				}
			}
		} else {
			moretitlelayout.setVisibility(View.VISIBLE);
			littletitlelayout.setVisibility(View.GONE);
			if (albumNum != null)
				albumNum.setText("(" + num + ")");
		}
	}

	public void refreshAllpapersNum() {
		// 修改重复刷新图片数目
		if (!isSelectAllPaper) {
			return;
		}
		String num = String.valueOf(allPaperAdapter.getAlbumItemSize());
		if (selectAlbumSetName.getBytes().length <= 15) {
			if (littlechartitle != null) {
				littletitlelayout.setVisibility(View.VISIBLE);
				moretitlelayout.setVisibility(View.GONE);
				// littlechartitle.setText(selectAlbumSetName+"(" + num + ")");
				littlechartitle.setText(selectAlbumSetName);
				// 因为没有文件夹时目前显示为全部壁纸，用littlenum显示
				if (albumSetAdapter.getCount() == 0 && isFirstLoadFinish) {
					littlenum.setText("(" + 0 + ")");
				} else {
					littlenum.setText("(" + num + ")");
				}
			}
			if (!isFirstLoadFinish) {
				isFirstLoadFinish = true;
			}
		} else {
			moretitlelayout.setVisibility(View.VISIBLE);
			littletitlelayout.setVisibility(View.GONE);
			if (albumNum != null) {
				if (albumSetAdapter.getCount() == 0 && isFirstLoadFinish) {
					albumNum.setText("(" + 0 + ")");
				} else {
					albumNum.setText("(" + num + ")");
				}
			}
		}
	}

	public boolean getRefreshFlag() {
		return albumReresh;
	}

	// 第一次启动应用和点击侧滑时albumset加载完成后，进行album的加载，
	private class AlbumSetLoadingListener implements LoadingListener {
		public void onLoadingStarted() {
			// 提示正在加载相册集
		}

		public void onLoadingFinished() {
			if (albumSetAdapter.getCount() == 0) {
				// if(progressDialog.isShowing()){
				// progressDialog.dismiss();
				// }
				allPaper.setText("全部壁纸 ");
				allpapersum.setText("0");
				albumpropmptlayout.setVisibility(View.VISIBLE);
				albumsetpropmptlayout.setVisibility(View.VISIBLE);
				listlayout.setVisibility(View.GONE); // 20140917
				albumList.setVisibility(View.GONE);
				isStartEnter = false;
				littletitlelayout.setVisibility(View.VISIBLE);
				moretitlelayout.setVisibility(View.GONE);
				littlechartitle.setText("全部壁纸");
				littlenum.setText("(" + 0 + ")");
				selectAlbumSetName = "全部壁纸";
				return;
			}
			// 首次启动永远进入全部壁纸
			if (isStartEnter) {
				// bill 20150106 add 由于交互要求改为第一次使用app时只显示三个文件夹
				allPaperSetAdapter.resume();
				return;
			}
			if (albumpropmptlayout.getVisibility() == View.VISIBLE) {
				albumpropmptlayout.setVisibility(View.GONE);
				albumsetpropmptlayout.setVisibility(View.GONE);
				listlayout.setVisibility(View.VISIBLE); // 20140917
				albumList.setVisibility(View.VISIBLE);
			}
			MediaSet targetSet = null;
			// 删除原来首次进入显示上次退出时的选项，现改为每次启动进入全部壁纸
			if (enterTemplete) {
				return;
			}
			if (!isStartEnter) {
				if (selectAlbumSetName.equals("全部壁纸")) {
					// if(allPaperAdapter!=null){
					// allPaperAdapter.pause();
					// allPaperAdapter= null;
					// }
					// MediaSet allpaperMediaset;
					// String path =
					// "/cluster/{/combo/{/mtp,/local/image,/picasa/image}}/time/0";
					// mMediaSetPath = Path.fromString(path);
					// allpaperMediaset = ((GalleryApp) getApplication())
					// .getDataManager().getMediaSet(mMediaSetPath);
					// allPaperAdapter = new AlbumActivityDataAdapter(
					// AlbumActivity.this, (GalleryApp) getApplication(),
					// allpaperMediaset, mMediaSetPath,
					// albumList,"AlbumActivity");
					// albumList.setAdapter(allPaperAdapter);
					// allPaperAdapter.setIsAllPaper(true);
					// allPaperAdapter.resume();
					// int total = 0;
					// for (int i = 0; i < albumSetAdapter.getCount(); i++) {
					// if(albumSetAdapter.getMediaSet(i)!=null){
					// total += albumSetAdapter.getMediaSet(i)
					// .getTotalMediaItemCount();
					// }
					// }
					// allPaper.setText("全部壁纸");
					// allpapersum.setText("("+total+")");
					isSelectAllPaper = true;
					isStartEnter = false;
					return;
				} else {
					boolean flag = false;
					if (albumAdapter != null) {
						albumAdapter.pause();
					}
					// bill add 0915
					if (albumsetSelectIndex >= albumSetAdapter.getCount()
							&& albumSetAdapter.getCount() > 0) {
						albumsetSelectIndex = albumSetAdapter.getCount() - 1;
					}
					targetSet = albumSetAdapter
							.getMediaSet(albumsetSelectIndex);
					if (targetSet != null) {
						mMediaSetPath = targetSet.getPath();
						mMediaSet = ((GalleryApp) getApplication())
								.getDataManager().getMediaSet(mMediaSetPath);
					}
					albumSetAdapter.setSelectItem(albumsetSelectIndex); // 0917
					if (!mMediaSet.getName().equals(selectAlbumSetName)) {
						for (int i = 0; i < albumSetAdapter.getCount(); i++) {
							targetSet = albumSetAdapter.getMediaSet(i);
							mMediaSetPath = targetSet.getPath();
							mMediaSet = ((GalleryApp) getApplication())
									.getDataManager()
									.getMediaSet(mMediaSetPath);
							if (mMediaSet.getName().equals(selectAlbumSetName)) {
								flag = true;
								albumsetSelectIndex = i;
								albumSetAdapter.setSelectItem(i); // 0917
								break;
							}
						}
						if (flag == false) { // 20140916 add
							targetSet = albumSetAdapter.getMediaSet(0);
							mMediaSetPath = targetSet.getPath();
							mMediaSet = ((GalleryApp) getApplication())
									.getDataManager()
									.getMediaSet(mMediaSetPath);
							selectAlbumSetName = mMediaSet.getName();
							albumsetSelectIndex = 0;
							albumSetAdapter.setSelectItem(0); // 0917
						}
					}
				}
			}
			// 侧滑时，刷新也是当前显示的album
			title.setText(mMediaSet.getName());
			albumAdapter = new AlbumActivityDataAdapter(AlbumActivity.this,
					(GalleryApp) getApplication(), mMediaSet, mMediaSetPath,
					albumList, "AlbumActivity");
			mSelectionManager.setSourceMediaSet(mMediaSet); // 多选删除
			albumAdapter.setIsAllPaper(false);
			albumAdapter.setLongClickListener(AlbumActivity.this); // 多项选中
			albumList.setAdapter(albumAdapter);
			albumAdapter.resume();
			int total = 0;
			for (int i = 0; i < albumSetAdapter.getCount(); i++) {
				if (albumSetAdapter.getMediaSet(i) != null) {
					total += albumSetAdapter.getMediaSet(i)
							.getTotalMediaItemCount();
				}
			}
			allPaper.setText("全部壁纸");
			allpapersum.setText("(" + total + ")");
			if (isStartEnter) {
				// albumList.setVisibility(View.VISIBLE);
				// loadingLayout.setVisibility(View.GONE);
				// if(progressDialog.isShowing()){
				// progressDialog.dismiss();
				// }
			}
			if (isSelectAllPaper == true) {
				allpaperlayout.setBackgroundColor(Color.parseColor("#f5f5f5"));
				allPaper.setTextColor(Color.parseColor("#ef400a"));
				allpapersum.setTextColor(Color.parseColor("#ef400a"));
				albumSetAdapter.setSelectItem(-1); // 设置选中项 0917
				albumSetAdapter.notifyDataSetInvalidated();
			} else {
				allpaperlayout.setBackgroundColor(Color.parseColor("#ffffff"));
				allPaper.setTextColor(Color.parseColor("#595959"));
				allpapersum.setTextColor(Color.parseColor("#595959"));
				albumSetAdapter.notifyDataSetInvalidated(); // 上面其他行进行了设置选中项，这里只需要重新刷新即可
			}
			isStartEnter = false;
		}

	}

	private class AllPaperLoadingListener implements LoadingListener {
		public void onLoadingStarted() {
			// 提示正在加载图片集

		}

		public void onLoadingFinished() {
			if (enterTemplete) {
				return;
			}
			if (isSelectAllPaper) {
				if (allPaperAdapter != null) {
					allPaperAdapter.pause();
					allPaperAdapter = null;
				}
				// MediaSet allpaperMediaset = null;
				String path = "/cluster/{/combo/{/mtp,/local/image,/picasa/image}}/time/0";
				mMediaSetPath = Path.fromString(path);
				if (mMediaSetPath != null) {
					addmode.setBackgroundResource(R.drawable.ty_ic_desktop_normal);
				}
				mAllPaperMediaSet = ((GalleryApp) getApplication())
						.getDataManager().getMediaSet(mMediaSetPath);
				allPaperAdapter = new AlbumActivityDataAdapter(
						AlbumActivity.this, (GalleryApp) getApplication(),
						mAllPaperMediaSet, mMediaSetPath, albumList,
						"AlbumActivity");
				allPaperAdapter.setLongClickListener(AlbumActivity.this); // 多项选中
				albumList.setAdapter(allPaperAdapter);
				allPaperAdapter.setIsAllPaper(true);
				allPaperAdapter.resume();
				allPaper.setText("全部壁纸");
				allpapersum.setText("("
						+ mAllPaperMediaSet.getTotalMediaItemCount() + ")");
				mSelectionManager.setSourceMediaSet(mAllPaperMediaSet); // 多选删除
				isStartEnter = false; // 由原来第一次启动显示相册改为首次启动显示全部壁纸添加
			} else {
				int total = 0;
				for (int i = 0; i < albumSetAdapter.getCount(); i++) {
					if (albumSetAdapter.getMediaSet(i) != null) {
						total += albumSetAdapter.getMediaSet(i)
								.getTotalMediaItemCount();
					}
				}
				allPaper.setText("全部壁纸");
				allpapersum.setText("(" + total + ")");
			}
		}
	}

	// 关闭启动时的进度条窗口
	public void closeProgressDialog() {
		// if(progressDialog.isShowing()){
		// progressDialog.dismiss();
		// }
	}

	public void hideLeftLayout() {
		leftSliderLayout.close();
	}

	public boolean isOpenOfLeftLayout() {
		if (leftSliderLayout.isOpen()) {
			return true;
		} else {
			return false;
		}
	}

	public void stopAnimation() {
		loadingimage.clearAnimation();
		loadingLayout.setVisibility(View.GONE);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {

		super.onRestoreInstanceState(savedInstanceState);
	}

	// 长按选中删除多项
	@Override
	public void onPreviewLongClick(int index) {
		// TODO Auto-generated method stub
		MediaItem item;
		if (isSelectAllPaper) {
			item = allPaperAdapter.getMediaItem(index);
		} else {
			item = albumAdapter.getMediaItem(index);
		}
		if (item == null)
			return;
		mSelectionManager.setAutoLeaveSelectionMode(true);
		mSelectionManager.toggle(item.getPath());

	}

	@Override
	public void onSelectionModeChange(int mode) {
		// TODO Auto-generated method stub
		switch (mode) {
		case PreviewerSelectionManager.ENTER_SELECTION_MODE: {
			View view = LayoutInflater.from(AlbumActivity.this).inflate(
					R.layout.selectedmore_layout, null);
			selectItems = (TextView) view.findViewById(R.id.selecteditems);
			allSelect = (TextView) view.findViewById(R.id.isallselect);
			allSelectLayout = (FrameLayout) view
					.findViewById(R.id.isallselectlayout);
			allSelectLayout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					// 选中全部及更新标题
					if (mSelectionManager.inSelectAllMode()) {
						mSelectionManager.deSelectAll();
						allSelect.setText(R.string.select_all);
					} else {
						mSelectionManager.selectAll();
						allSelect.setText(R.string.deselect_all);
					}
				}

			});
			LayoutParams parm = new LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.FILL_PARENT);
			parm.height = (int) (49 * displayMetrics.density);
			view.setLayoutParams(parm);
			mainTitle.removeViewAt(0);
			mainTitle.addView(view);
			createPopWindow();
			break;
		}
		case PreviewerSelectionManager.LEAVE_SELECTION_MODE: {
			mainTitle.removeAllViews();
			View view = LayoutInflater.from(AlbumActivity.this).inflate(
					R.layout.layout_previewer_actionbar, null);
			moretitlelayout = (RelativeLayout) view
					.findViewById(R.id.moretitlelayout);
			littletitlelayout = (RelativeLayout) view
					.findViewById(R.id.littletitlelayout);
			littlechartitle = (TextView) view
					.findViewById(R.id.littlechartitle);
			littlenum = (TextView) view.findViewById(R.id.littlenum);
			addModeLayout = (RelativeLayout) view
					.findViewById(R.id.addmodelayout);
			iconlayout = (RelativeLayout) view.findViewById(R.id.iconlayout);
			mainTitle.addView(view);
			iconlayout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (leftSliderLayout.isOpen()) {
						leftSliderLayout.close();
					} else {
						leftSliderLayout.open();
					}
				}

			});
			// 进入模板编辑界面
			addModeLayout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// 20150115 begin 解决个别手机图片为加载完成时点击进入模板会崩溃退出
					addModeFunction();
				}

			});
			if (popWindow.isShowing()) {
				popWindow.dismiss();
			}
			if (isSelectAllPaper) {
				refreshAllpapersNum();
				curSelectPos = albumList.getFirstVisiblePosition();
				View view1 = albumList.getListChildAt(0);
				allPaperAdapter.refreshSelectIcon(0, view1);
				View view2 = albumList.getListChildAt(1);
				allPaperAdapter.refreshSelectIcon(1, view2);
				View view3 = albumList.getListChildAt(2);
				allPaperAdapter.refreshSelectIcon(2, view3);
			} else {
				refreshAlbumNum();
				curSelectPos = albumList.getFirstVisiblePosition();
				View view1 = albumList.getListChildAt(0);
				albumAdapter.refreshSelectIcon(0, view1);
				View view2 = albumList.getListChildAt(1);
				albumAdapter.refreshSelectIcon(1, view2);
				View view3 = albumList.getListChildAt(2);
				albumAdapter.refreshSelectIcon(2, view3);
			}
			break;
		}
		case PreviewerSelectionManager.SELECT_ALL_MODE: {
			int count = mSelectionManager.getSelectedCount();
			String selectNumStr = "已选中" + count + "项";
			selectItems.setText(selectNumStr);

			if (isSelectAllPaper) {
				refreshAllpapersNum();
				curSelectPos = albumList.getFirstVisiblePosition();
				View view1 = albumList.getListChildAt(0);
				allPaperAdapter.refreshSelectIcon(0, view1);
				View view2 = albumList.getListChildAt(1);
				allPaperAdapter.refreshSelectIcon(1, view2);
				View view3 = albumList.getListChildAt(2);
				allPaperAdapter.refreshSelectIcon(2, view3);
			} else {
				refreshAlbumNum();
				curSelectPos = albumList.getFirstVisiblePosition();
				View view1 = albumList.getListChildAt(0);
				albumAdapter.refreshSelectIcon(0, view1);
				View view2 = albumList.getListChildAt(1);
				albumAdapter.refreshSelectIcon(1, view2);
				View view3 = albumList.getListChildAt(2);
				albumAdapter.refreshSelectIcon(2, view3);
			}
			break;
		}
		}
	}

	@Override
	public void onSelectionChange(Path path, boolean selected) {
		// TODO Auto-generated method stub
		int count = mSelectionManager.getSelectedCount();
		if ((lastSelectNum == 0 && count == 1)) {
			multipleDelete
					.setBackgroundResource(R.drawable.album_delete_presse_selected);
		}
		// else{
		// multipleDelete.setBackgroundResource(R.drawable.album_selectdelete_delete);
		// }
		String selectNumStr = "已选中" + count + "项";
		selectItems.setText(selectNumStr);
	}

	// 是否处于选择模式
	public boolean getSelectMode() {
		return mSelectionManager.inSelectionMode();
	}

	// 某项是否被选中
	public boolean isSelectedItem(Path path) {
		return mSelectionManager.isItemSelected(path);
	}

	public void createPopWindow() {
		View mutipleSelect = LayoutInflater.from(AlbumActivity.this).inflate(
				R.layout.previewermultiple_select, null);
		TextView cancle = (TextView) mutipleSelect
				.findViewById(R.id.previewercancle);
		multipleDelete = (TextView) mutipleSelect
				.findViewById(R.id.previewerdelete);
		cancle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mSelectionManager.deSelectAll();
			}
		});
		multipleDelete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int count = mSelectionManager.getSelectedCount();
				String deleteStr = "确定要删除这" + count + "张图片吗？";
				ConfirmDialog dialog = new ConfirmDialog(AlbumActivity.this,
						(GalleryApp) (AlbumActivity.this.getApplication()),
						R.style.shareDialogTheme, deleteStr, false,
						mSelectionManager.getSelectItems());
				dialog.setSelectModeChange(AlbumActivity.this);
				dialog.show();

			}
		});
		popWindow = new PopupWindow(mutipleSelect, width_screen,
				(int) (49 * displayMetrics.density));
		popWindow.setFocusable(false);
		popWindow.setOutsideTouchable(false);
		popWindow.update();
		popWindow.setAnimationStyle(R.style.MoreDialog);
		popWindow.setBackgroundDrawable(new BitmapDrawable());

		popWindow.showAtLocation(leftSliderLayout, Gravity.BOTTOM, 0, 0);
	}

	@Override
	public void onSelectModeChange() {
		// TODO Auto-generated method stub
		mSelectionManager.deSelectAll();
	}

	public void addModeFunction() {
		// 20150115 begin 解决个别手机图片为加载完成时点击进入模板会崩溃退出
		if (mMediaSetPath == null) {
			return;
		}
		// 20150115 end 解决个别手机图片为加载完成时点击进入模板会崩溃退出
		String path = "";
		if (isSelectAllPaper) {
			if (allPaperAdapter != null) {
				path = allPaperAdapter.getFirstImagePath();
			}
		} else {
			if (albumAdapter != null) {
				path = albumAdapter.getFirstImagePath();
			}
		}
		Bundle data = new Bundle();
		data.putInt(PhotoPage.KEY_INDEX_HINT, 0);
		data.putString(PhotoPage.KEY_MEDIA_SET_PATH, mMediaSetPath.toString());
		data.putString(PhotoPage.KEY_MEDIA_ITEM_PATH, path);
		Intent intent = new Intent();
		intent.setClass(getApplication(), Gallery.class);
		intent.putExtra("startpage", "PhotoPage");
		intent.putExtra("startTemplete", "true");
		intent.putExtras(data);
		startActivity(intent);
		enterTemplete = true;
	}
}