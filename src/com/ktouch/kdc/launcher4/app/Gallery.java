/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ktouch.kdc.launcher4.app;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.Keyframe;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ConfigurationInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.policy.PolicyManager;
import com.ktouch.kdc.launcher4.R;
import com.ktouch.kdc.launcher4.adapter.ScrollAdapter;
import com.ktouch.kdc.launcher4.app.PhotoDataAdapter.NotifyChangeCollectStutas;
import com.ktouch.kdc.launcher4.camera.CameraManager;
import com.ktouch.kdc.launcher4.camera.FocusManager;
import com.ktouch.kdc.launcher4.camera.Profiler;
import com.ktouch.kdc.launcher4.camera.SettingsStorage;
import com.ktouch.kdc.launcher4.camera.SnapshotManager;
import com.ktouch.kdc.launcher4.camera.SoundManager;
import com.ktouch.kdc.launcher4.camera.Util;
import com.ktouch.kdc.launcher4.camera.feats.CaptureTransformer;
import com.ktouch.kdc.launcher4.camera.feats.SoftwareHdrCapture;
import com.ktouch.kdc.launcher4.camera.pano.MosaicProxy;
import com.ktouch.kdc.launcher4.camera.picsphere.PicSphereCaptureTransformer;
import com.ktouch.kdc.launcher4.camera.picsphere.PicSphereManager;
import com.ktouch.kdc.launcher4.camera.ui.CircleTimerView;
import com.ktouch.kdc.launcher4.camera.ui.ExposureHudRing;
import com.ktouch.kdc.launcher4.camera.ui.FocusHudRing;
import com.ktouch.kdc.launcher4.camera.ui.PanoProgressBar;
import com.ktouch.kdc.launcher4.camera.ui.SavePinger;
import com.ktouch.kdc.launcher4.camera.ui.ShutterButton;
import com.ktouch.kdc.launcher4.camera.ui.SideBar;
import com.ktouch.kdc.launcher4.camera.ui.SwitchRingPad;
import com.ktouch.kdc.launcher4.camera.ui.ThumbnailFlinger;
import com.ktouch.kdc.launcher4.camera.ui.showcase.ShowcaseView;
import com.ktouch.kdc.launcher4.common.Utils;
import com.ktouch.kdc.launcher4.data.DataManager;
import com.ktouch.kdc.launcher4.data.MediaDetails;
import com.ktouch.kdc.launcher4.data.MediaItem;
import com.ktouch.kdc.launcher4.data.MediaSet;
import com.ktouch.kdc.launcher4.data.Path;
import com.ktouch.kdc.launcher4.launcher2.Launcher;
import com.ktouch.kdc.launcher4.launcher2.LauncherApplication;
import com.ktouch.kdc.launcher4.model.MoveItem;
import com.ktouch.kdc.launcher4.picasasource.PicasaSource;
import com.ktouch.kdc.launcher4.ui.ConfirmDialog;
import com.ktouch.kdc.launcher4.ui.GLRoot;
import com.ktouch.kdc.launcher4.ui.Log;
import com.ktouch.kdc.launcher4.ui.MenuExecutor;
import com.ktouch.kdc.launcher4.ui.PhotoView;
import com.ktouch.kdc.launcher4.ui.ScrollLayout;
import com.ktouch.kdc.launcher4.ui.ScrollLayout.OnAddOrDeletePage;
import com.ktouch.kdc.launcher4.ui.ScrollLayout.OnEditModeListener;
import com.ktouch.kdc.launcher4.ui.ScrollLayout.OnPageChangedListener;
import com.ktouch.kdc.launcher4.ui.SelectionManager;
import com.ktouch.kdc.launcher4.ui.SlotView;
import com.ktouch.kdc.launcher4.util.DBUtils_openHelper;
import com.ktouch.kdc.launcher4.util.FileUtil;
import com.ktouch.kdc.launcher4.util.GalleryUtils;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.umeng.analytics.MobclickAgent;

public final class Gallery extends AbstractGalleryActivity implements
		OnCancelListener, OnAddOrDeletePage, OnPageChangedListener,
		OnEditModeListener, OnClickListener, CameraManager.CameraReadyListener,
		ShowcaseView.OnShowcaseEventListener, NotifyChangeCollectStutas {
	public static final String EXTRA_SLIDESHOW = "slideshow";
	public static final String EXTRA_CROP = "crop";

	public static final String ACTION_REVIEW = "com.android.camera.action.REVIEW";
	public static final String KEY_GET_CONTENT = "get-content";
	public static final String KEY_GET_ALBUM = "get-album";
	public static final String KEY_TYPE_BITS = "type-bits";
	public static final String KEY_MEDIA_TYPES = "mediaTypes";
	public static final String KEY_MEDIA_ITEM_PATH = "media-item-path";

	private static final String TAG = "Gallery";
	private GalleryActionBar mActionBar;
	private Dialog mVersionCheckDialog;

	// lianglei begin
	public static String path;
	public Dialog dialog;
	public Button share, wallpaper;
	public ImageView mImageView1, mImageView2;
	private SelectionManager mSelectionManager;
	private MenuExecutor mMenuExecutor;
	private MenuExecutor.ProgressListener listener;
	private CollectFolder mCollectFolder;
	// lianglei end

	// bill add begin
	public static Gallery gInstance = null;
	private static final int REQUEST_PHOTO = 2;
	private RelativeLayout photoBar = null;
	private RelativeLayout settingWallpaper = null;
	private LinearLayout menubar = null;
	private ImageView back = null;
	private ImageView tempelteedit = null;
	private ImageView shareImage = null;
	private ImageView collect = null;
	private ImageView delete = null;
	private TextView setWallpaper = null;
	private ImageView changeMode = null;
	private ImageView albumsetback = null;
	private LinearLayout actionBar = null;
	protected static final String WALLPAPER_WIDTH_KEY = "wallpaper.width";
	protected static final String WALLPAPER_HEIGHT_KEY = "wallpaper.height";
	// bill add end

	// yongnan add begin
	// 滑动控件的容器Container
	public ScrollLayout mContainer;
	public LinearLayout linearTemplate, linearTemplateParent;
	// 模板界面标题栏
	public View viewTitle;
	Button vButton;
	// Container的Adapter
	private ScrollAdapter mItemsAdapter;
	// Container中滑动控件列表
	private List<MoveItem> mList_other;
	// xUtils中操纵SQLite的助手类
	private DbUtils mDbUtils;
	public static int height_screen, width_screen;
	private ImageView backTemplate;
	Button edit;
	public TextView text_head;
	private ScrollLayoutReceiver scrollLayoutReceiver;
	// Layout添加或删除动画
	Animator customAppearingAnim, customDisappearingAnim;
	Animator customChangingAppearingAnim, customChangingDisappearingAnim;
	public final static int CAMERA_MODE_PHOTO = 1;
	public final static int CAMERA_MODE_VIDEO = 2;
	public final static int CAMERA_MODE_PANO = 3;
	public final static int CAMERA_MODE_PICSPHERE = 4;

	// whether or not to enable profiling
	private final static boolean DEBUG_PROFILE = true;

	private static int mCameraMode = CAMERA_MODE_PHOTO;

	private CameraManager mCamManager;
	private SnapshotManager mSnapshotManager;
	private MainSnapshotListener mSnapshotListener;
	private FocusManager mFocusManager;
	private PicSphereManager mPicSphereManager;
	private MosaicProxy mMosaicProxy;
	private CameraOrientationEventListener mOrientationListener;
	private GestureDetector mGestureDetector;
	private CaptureTransformer mCaptureTransformer;
	private Handler mHandler;
	private boolean mPaused;

	private int mOrientation = OrientationEventListener.ORIENTATION_UNKNOWN;
	private int mOrientationCompensation = 0;
	PopupWindow d;
	private SideBar mSideBar;
	// private WidgetRenderer mWidgetRenderer;
	private FocusHudRing mFocusHudRing;
	private ExposureHudRing mExposureHudRing;
	// private SwitchRingPad mSwitchRingPad;
	private ShutterButton mShutterButton;
	private SavePinger mSavePinger;
	private PanoProgressBar mPanoProgressBar;
	private Button mPicSphereUndo;
	private CircleTimerView mTimerView;
	private ViewGroup mRecTimerContainer;
	// private static Notifier mNotifier;
	// private ReviewDrawer mReviewDrawer;
	private ScaleGestureDetector mZoomGestureDetector;
	private ShowcaseView mShowcaseView;
	private boolean mHasPinchZoomed;
	private boolean mCancelSideBarClose;
	private boolean mIsFocusButtonDown;
	private boolean mIsShutterButtonDown;
	private boolean mUserWantsExposureRing;
	private boolean mIsFullscreenShutter;
	private int mShowcaseIndex;
	private boolean mIsCamSwitching;
	private boolean mIsShutterLongClicked = false;
	private CameraPreviewListener mCamPreviewListener;
	private GLSurfaceView mGLSurfaceView;
	private boolean mIsFocusing = false;
	public MediaSet mAlbumSetMediaSet = null; // bill add
	public boolean isMainEnterTemplete = false;// bill add
	private final static int SHOWCASE_INDEX_WELCOME_1 = 0;
	private final static int SHOWCASE_INDEX_WELCOME_2 = 1;
	private final static int SHOWCASE_INDEX_PANORAMA = 0;
	private final static int SHOWCASE_INDEX_PICSPHERE = 0;

	private final static String KEY_SHOWCASE_WELCOME = "SHOWCASE_WELCOME";
	private final static String KEY_SHOWCASE_PANORAMA = "SHOWCASE_PANORAMA";
	private final static String KEY_SHOWCASE_PICSPHERE = "SHOWCASE_PICSPHERE";

	/**
	 * Gesture listeners to apply on camera previews views
	 */
	private View.OnTouchListener mPreviewTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent ev) {
			if (ev.getAction() == MotionEvent.ACTION_UP) {
				mSideBar.clampSliding();
				// mReviewDrawer.clampSliding();
			}

			// Process HUD gestures only if we aren't pinching
			mHasPinchZoomed = false;
			mZoomGestureDetector.onTouchEvent(ev);

			if (!mHasPinchZoomed) {
				mGestureDetector.onTouchEvent(ev);
			}

			return true;
		}
	};
	private boolean flag_CameraOpen = false;
	private boolean flag_FirstLogin = true;
	private boolean flag_PhotoBar = false;
	private ImageView mAlbumButton, mConversionButton;
	private FrameLayout mLayout_Album, mLayout_Conversion;
	// private RelativeLayout photoBarLayout;
	public PopupWindow dialogPhotoBar;
	public PopupWindow dialogWallPaper;
	private Window dialogWindow;
	private RelativeLayout view;
	private WindowManager.LayoutParams layoutParams;
	private boolean isPhotoBarShow = true;
	private FrameLayout galleryCamera;
	private DisplayMetrics displayMetrics;
	private Window w;
	private Window photoWindow;
	private Window setWallpaperWindow;
	private Window cameraWindow;
	private PopupWindow cameraButtonWindow;
	private RelativeLayout cameraButtonView;
	private boolean isAlbumReturn = false;
	// yongnan add end
	// bill add begin
	private WindowManager window;
	private int screenwidth = 0;
	private int screenHeight = 0;
	private String absolutePath = null;

	public interface RealtimeWallpaper {
		void realtimeSetWallPaper(Handler handler, float width, float height);
	}

	public RealtimeWallpaper mRealtimeWallpaper;
	// 设置壁纸时使用的进度条
	private Dialog proDia;
	private ImageView progress;
	private boolean isCanCameraUse = true;
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if (msg.what == 1) {
				proDia = new Dialog(getAndroidContext());
				proDia.requestWindowFeature(Window.FEATURE_NO_TITLE);
				View view = LayoutInflater.from(getAndroidContext()).inflate(
						R.layout.wallpaper_progressbar, null);
				progress = (ImageView) view.findViewById(R.id.progeress);
				Animation anim = AnimationUtils.loadAnimation(
						getAndroidContext(), R.anim.album_loading);
				progress.startAnimation(anim);
				// proDia.setContentView(view, new
				// LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
				proDia.setContentView(view);
				proDia.show();
				return;
			} else if (msg.what == 2) {
				Toast.makeText(getAndroidContext(), R.string.setSuccessful,
						Toast.LENGTH_SHORT).show();
				if (proDia.isShowing()) {
					proDia.dismiss();
					progress.clearAnimation();
				}
				return;
			}
			else if (msg.what == 0) {
				Toast.makeText(getAndroidContext(), "设置壁纸失败",
						Toast.LENGTH_SHORT).show();
				if (proDia.isShowing()) {
					proDia.dismiss();
					progress.clearAnimation();
				}
				return;
			}
		}
	};

	// bill add endz
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_ACTION_BAR);
		requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		height_screen = displayMetrics.heightPixels;
		width_screen = displayMetrics.widthPixels;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		// yongnan add begin
		// 从缓存中初始化滑动控件列表
		getDataFromCache();
		// 初始化控件
		initView();
		// yongnan add end
		mActionBar = new GalleryActionBar(this);
		// bill add begin

		layoutParams = new LayoutParams(
				WindowManager.LayoutParams.MATCH_PARENT,
				(int) (50 * displayMetrics.density),
				WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				android.graphics.PixelFormat.TRANSPARENT);
		layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
		menubar = (LinearLayout) findViewById(R.id.menubar);
		actionBar = (LinearLayout) findViewById(R.id.mainactionbar);
		actionBar.setVisibility(View.VISIBLE);
		albumsetback = (ImageView) findViewById(R.id.albumsetback);

		// bill add end
		if (savedInstanceState != null) {
			getStateManager().restoreFromState(savedInstanceState);
		} else {
			initializeByIntent();
		}
		// 初始化屏幕宽度和高度
		window = (WindowManager) getAndroidContext().getSystemService(
				Context.WINDOW_SERVICE);
		screenwidth = window.getDefaultDisplay().getWidth();
		screenHeight = window.getDefaultDisplay().getHeight();

		// lianglei begin 初始化
		mSelectionManager = new SelectionManager(this, false);
		mMenuExecutor = new MenuExecutor(this, mSelectionManager);
		// lianglei end
		PhotoView.setImageView();
	}
	


	// public void isA
	private void initializeByIntent() {
		Intent intent = getIntent();
		String action = intent.getAction();

		if (Intent.ACTION_GET_CONTENT.equalsIgnoreCase(action)) {
			startGetContent(intent);
		} else if (Intent.ACTION_PICK.equalsIgnoreCase(action)) {
			// We do NOT really support the PICK intent. Handle it as
			// the GET_CONTENT. However, we need to translate the type
			// in the intent here.
			String type = Utils.ensureNotNull(intent.getType());
			if (type.startsWith("vnd.android.cursor.dir/")) {
				if (type.endsWith("/image"))
					intent.setType("image/*");
				if (type.endsWith("/video"))
					intent.setType("video/*");
			}
			startGetContent(intent);
		} else if (Intent.ACTION_VIEW.equalsIgnoreCase(action)
				|| ACTION_REVIEW.equalsIgnoreCase(action)) {
			startViewAction(intent);
		}
		// bill add begin
		else if (intent.getStringExtra("startpage") != null) {
			if (intent.getStringExtra("startpage").equals("PhotoPage")) {
				gInstance = this;
				if (actionBar.getVisibility() == View.VISIBLE) {
					actionBar.setVisibility(View.GONE);
				}
				Bundle data = intent.getExtras();
				absolutePath = intent.getStringExtra("absolutePath");
				PhotoDataAdapter.setNotifyChangeCollectStutas(this);
				getStateManager().startStateForResult(PhotoPage.class,
						REQUEST_PHOTO, data);
				isPhotoBarShow = false;
				// 首页右上角进入模板编辑界面
				if (intent.getStringExtra("startTemplete").equals("true")) {
					isMainEnterTemplete = true;
					//
					Intent intentTemplate = new Intent(this,
							TemplateActivity.class);
					intentTemplate.putExtra("close", true);
					startActivityForResult(intentTemplate, 300);
				}
			}
		}
		// 解决从文件夹浏览器中读取显示图片时总是显示选择模板的标题栏问题0918
		else if (intent.getStringExtra("albumsetpage") != null) {
			if (intent.getStringExtra("albumsetpage").equals("AlbumSetPage")) {
				if (menubar.getVisibility() == View.GONE) {
					menubar.setVisibility(View.VISIBLE);
				}
				startDefaultPage();
			}
		}
		// bill add end
		else {
			startDefaultPage();
		}
	}

	public void startDefaultPage() {
		// bill add 下行代码修改由于相册集合由横屏滑动改为了竖屏，但是
		// 图片预览界面的filmstripview让然要求是横屏，通过设置wide变量来实现，true竖屏，false横屏
		SlotView.setSlotViewWide(true);
		PicasaSource.showSignInReminder(this);
		Bundle data = new Bundle();
		data.putString(AlbumSetPage.KEY_MEDIA_PATH, getDataManager()
		// bill modify begin 20150112 只显示图片即可
				.getTopSetPath(DataManager.INCLUDE_IMAGE));
		// .getTopSetPath(DataManager.INCLUDE_ALL));
		// bill modify end 20150112
		getStateManager().startState(AlbumSetPage.class, data);
		mVersionCheckDialog = PicasaSource.getVersionCheckDialog(this);
		if (mVersionCheckDialog != null) {
			mVersionCheckDialog.setOnCancelListener(this);
		}
	}

	private void startGetContent(Intent intent) {
		Bundle data = intent.getExtras() != null ? new Bundle(
				intent.getExtras()) : new Bundle();
		data.putBoolean(KEY_GET_CONTENT, true);
		int typeBits = GalleryUtils.determineTypeBits(this, intent);
		data.putInt(KEY_TYPE_BITS, typeBits);
		data.putString(AlbumSetPage.KEY_MEDIA_PATH, getDataManager()
				.getTopSetPath(typeBits));
		getStateManager().setLaunchGalleryOnTop(true);
		getStateManager().startState(AlbumSetPage.class, data);
	}

	private String getContentType(Intent intent) {
		String type = intent.getType();
		if (type != null)
			return type;

		Uri uri = intent.getData();
		try {
			return getContentResolver().getType(uri);
		} catch (Throwable t) {
			Log.w(TAG, "get type fail", t);
			return null;
		}
	}

	private void startViewAction(Intent intent) {
		Boolean slideshow = intent.getBooleanExtra(EXTRA_SLIDESHOW, false);
		getStateManager().setLaunchGalleryOnTop(true);
		if (slideshow) {
			getActionBar().hide();
			DataManager manager = getDataManager();
			Path path = manager.findPathByUri(intent.getData());
			if (path == null
					|| manager.getMediaObject(path) instanceof MediaItem) {
				path = Path.fromString(manager
						.getTopSetPath(DataManager.INCLUDE_IMAGE));
			}
			Bundle data = new Bundle();
			data.putString(SlideshowPage.KEY_SET_PATH, path.toString());
			data.putBoolean(SlideshowPage.KEY_RANDOM_ORDER, true);
			data.putBoolean(SlideshowPage.KEY_REPEAT, true);
			getStateManager().startState(SlideshowPage.class, data);
		} else {
			Bundle data = new Bundle();
			DataManager dm = getDataManager();
			Uri uri = intent.getData();
			String contentType = getContentType(intent);
			if (contentType == null) {
				Toast.makeText(this, R.string.no_such_item, Toast.LENGTH_LONG)
						.show();
				finish();
				return;
			}
			if (uri == null) {
				int typeBits = GalleryUtils.determineTypeBits(this, intent);
				data.putInt(KEY_TYPE_BITS, typeBits);
				data.putString(AlbumSetPage.KEY_MEDIA_PATH, getDataManager()
						.getTopSetPath(typeBits));
				getStateManager().setLaunchGalleryOnTop(true);
				getStateManager().startState(AlbumSetPage.class, data);
			} else if (contentType
					.startsWith(ContentResolver.CURSOR_DIR_BASE_TYPE)) {
				int mediaType = intent.getIntExtra(KEY_MEDIA_TYPES, 0);
				if (mediaType != 0) {
					uri = uri
							.buildUpon()
							.appendQueryParameter(KEY_MEDIA_TYPES,
									String.valueOf(mediaType)).build();
				}
				Path setPath = dm.findPathByUri(uri);
				MediaSet mediaSet = null;
				if (setPath != null) {
					mediaSet = (MediaSet) dm.getMediaObject(setPath);
				}
				if (mediaSet != null) {
					if (mediaSet.isLeafAlbum()) {
						data.putString(AlbumPage.KEY_MEDIA_PATH,
								setPath.toString());
						getStateManager().startState(AlbumPage.class, data);
					} else {
						data.putString(AlbumSetPage.KEY_MEDIA_PATH,
								setPath.toString());
						getStateManager().startState(AlbumSetPage.class, data);
					}
				} else {
					startDefaultPage();
				}
			} else {
				Path itemPath = dm.findPathByUri(uri);
				Path albumPath = dm.getDefaultSetOf(itemPath);
				// reference it.
				boolean singleItemOnly = intent.getBooleanExtra(
						"SingleItemOnly", false);
				if (!singleItemOnly && albumPath != null) {
					data.putString(PhotoPage.KEY_MEDIA_SET_PATH,
							albumPath.toString());
				}
				data.putString(PhotoPage.KEY_MEDIA_ITEM_PATH,
						itemPath.toString());
				getStateManager().startState(PhotoPage.class, data);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		return getStateManager().createOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		GLRoot root = getGLRoot();
		root.lockRenderThread();
		try {
			return getStateManager().itemSelected(item);
		} finally {
			root.unlockRenderThread();
		}
	}

	@Override
	public void onBackPressed() {
		// send the back event to the top sub-state

		if (flag_PhotoBar) {
			setPhotoPageMenuBarVisibility(false);
		}
		GLRoot root = getGLRoot();
		root.lockRenderThread();
		try {
			getStateManager().onBackPressed();
		} finally {
			root.unlockRenderThread();
		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		GLRoot root = getGLRoot();
		root.lockRenderThread();
		try {
			getStateManager().destroy();
		} finally {
			root.unlockRenderThread();
		}
		if (null != scrollLayoutReceiver) {
			unregisterReceiver(scrollLayoutReceiver);
		}
		System.gc();// bill add
	}

	@Override
	protected void onResume() {
		MobclickAgent.onResume(this);// 友盟统计
		Utils.assertTrue(getStateManager().getStateCount() > 0);
		gInstance = this;
		super.onResume();
		if (mVersionCheckDialog != null) {
			mVersionCheckDialog.show();
		}
		Log.e("scroll", "onrusme" + isAlbumReturn + this.toString());
		if (flag_CameraOpen) {
			onResume_Camera();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);// 友盟统计
		if (mVersionCheckDialog != null) {
			mVersionCheckDialog.dismiss();
		}

		if (flag_CameraOpen) {
			onPause_Camera();
		}
	}

	// bill add begin
	@Override
	protected void onStop() {
		super.onStop();
		if (Launcher.getInstance() != null
				&& Launcher.getInstance().getLoadFlag() == true) {
			Intent mIntent = new Intent(
					"android.intent.action.IMAGEPREVIER_GETDESKIMAGE");
			sendBroadcast(mIntent);
		}
	}

	// bill add end
	@Override
	public GalleryActionBar getGalleryActionBar() {
		return mActionBar;
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		if (dialog == mVersionCheckDialog) {
			mVersionCheckDialog = null;
		}
	}

	// bill add begin
	public static Gallery getGalleryIntance() {
		return gInstance;
	}

	public void setPhotoPageMenuBarVisibility(boolean visible) {
		if (visible) {
			createDialogPhotoBar();
			createDialogSettingWallPaper(); // bill add 20150115

		} else {
			if (flag_PhotoBar) {
				flag_PhotoBar = false;
				if (dialogPhotoBar == null || dialogWallPaper == null) { // bill
																			// add
																			// 20150115
					return;
				}
				dialogPhotoBar.dismiss();
				dialogWallPaper.dismiss(); // bill add 20150115
			}
		}
		// bill add begin
		if (absolutePath != null) {
			if (CollectFolder.isExistInCollect(absolutePath)) {
				if (collect != null) {
					collect.setImageResource(R.drawable.ty_ic_havebeenin_normal);
				}
			} else {
				if (collect != null) {
					collect.setImageResource(R.drawable.ty_ic_collection_normal);
				}
			}
		}
		// liufenzhen add end
	}

	public void setListener() {
		back = (ImageView) photoBar.findViewById(R.id.back);
		tempelteedit = (ImageView) photoBar.findViewById(R.id.tempelteedit);
		shareImage = (ImageView) photoBar.findViewById(R.id.share);
		collect = (ImageView) photoBar.findViewById(R.id.collect);
		delete = (ImageView) photoBar.findViewById(R.id.delete);

		if (absolutePath != null) {
			if (CollectFolder.isExistInCollect(absolutePath)) {
				collect.setImageResource(R.drawable.ty_ic_havebeenin_normal);
			} else {
				collect.setImageResource(R.drawable.ty_ic_collection_normal);
			}
		}
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}

		});
		// bill add begin
		tempelteedit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				MediaItem current = PhotoView.mModell.getCurrentMediaItem();
				intent.putExtra("picstring",
						current.getDetails().getDetail(MediaDetails.INDEX_PATH)
								.toString());
				intent.setClass(getApplicationContext(),
						WallPaperCropActivity.class);
				startActivity(intent);
			}

		});
		// bill add end
		shareImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// FIXME 这一行代码将能够使分享功能的代码出现时有动画,但是会和bar不同步
				// Gallery.getGalleryIntance().setPhotoPageMenuBarVisibility(false);
				path = PhotoView.pathString();
				// AndroidShare as = new AndroidShare(getAndroidContext(),
				// path);
				// as.show();
				ShareImage shareImage = new ShareImage(getAndroidContext(),
						R.style.shareDialogTheme, path);
				shareImage.show();
			}

		});
		// bill modify begin
		collect.setOnTouchListener(new StatusListener());
		// collect.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// addFav();
		// }
		//
		// });
		// bill modify end
		delete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// deleteCurrentPhoto();
				ConfirmDialog dialog = new ConfirmDialog(getAndroidContext(),
						Gallery.this, R.style.shareDialogTheme, "确定要删除这张图片吗?",
						true);
				dialog.show();
			}

		});

		albumsetback.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}

		});
	}

	// bill end

	// yongnan add

	private void getDataFromCache() {
		mDbUtils = DBUtils_openHelper.getInstance(this).getDb();
		try {
			// 使用xUtils，基于orderId从SQLite数据库中获取滑动控件
			mList_other = mDbUtils.findAll(Selector.from(MoveItem.class));

			if (mList_other == null || mList_other.size() == 0) {
			} else {
				List<MoveItem> remove = new ArrayList<MoveItem>();
				for (MoveItem moveItems : mList_other) {
					if (moveItems.isFlag_delete()) {
						if (!FileUtil.isFileExist(moveItems.getImgurl())) {
							remove.add(moveItems);
							mDbUtils.delete(moveItems);
						}
					}
				}
				mList_other.removeAll(remove);
			}
		} catch (DbException e) {
			e.printStackTrace();
		}
	}

	private void initView() {
		view = (RelativeLayout) findViewById(R.id.gallery_mian);
		galleryCamera = (FrameLayout) findViewById(R.id.cameraview);
		galleryCamera.setVisibility(View.GONE);
		// 如果数据库中没有数据重新加载数据
		getData();
		if (getFirstUse()) {
			mDbUtils = DBUtils_openHelper.getInstance(this).getDb();
			try {
				mDbUtils.deleteAll(MoveItem.class);
				mDbUtils.saveAll(mList_other);
			} catch (DbException e) {
				e.printStackTrace();
			}
		}
	}

	private void getData() {
		if (mList_other == null || mList_other.size() == 0) {
			mList_other = new ArrayList<MoveItem>();

			createMoveItem("assets/img/ty_preview_lockscreen_min_temp.png",
					"assets/img/ty_preview_lockscreen_min_temp.png",
					R.drawable.ty_preview_lockscreen, 1, 1,
					getString(R.string.template_kt_lock_normal), false, true,
					false, true, true);
			createMoveItem("assets/img/ty_preview_wallpaper_min_temp.png",
					"assets/img/ty_preview_wallpaper_min_temp.png",
					R.drawable.ty_preview_wallpaper, 2, 2,
					getString(R.string.template_kt_normal), false, true, false,
					true, true);
			createMoveItem("assets/img/ty_preview_desktop_time.png",
					"assets/img/ty_preview_desktop_time.png",
					R.drawable.ty_preview_desktop_time, 3, 3,
					getString(R.string.template_kt_icon), false, true, false,
					true, true);
			createMoveItem("assets/img/choice_addto_normal_min.png",
					"assets/img/choice_addto_normal_min.png",
					R.drawable.choice_addto_normal, 4, 4,
					getString(R.string.template_kt_add), true, false, false,
					true, false);

		}
	}

	private void createMoveItem(String imgdown, String Imgurl, int url,
			int orderId, int mid, String describe, boolean add, boolean drag,
			boolean canDelete, boolean isResouce, boolean isSelected) {
		MoveItem other = new MoveItem();
		other.setImgdown(imgdown);
		other.setImgurl(Imgurl);
		other.setImg_normal_int(url);
		other.setOrderId(orderId);
		other.setMid(mid);
		other.setFlag_add(add);
		other.setText_describe(describe);
		other.setFlag_drag(drag);
		other.setSetTitle(0);
		other.setFlag_delete(false);
		other.setFlag_resouce(true);
		other.setFlag_selected(isSelected);
		mList_other.add(other);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.header_view_update_bt:
			if (edit.getText().toString()
					.equals(getString(R.string.templatedelete))) {
				mContainer.showEdit(true);
				edit.setText(R.string.templatedone);
				int i = mContainer.getShowImgList().size();
				Toast.makeText(this, "count " + i, 0).show();
			} else if (edit.getText().toString()
					.equals(getString(R.string.templatedone))) {
				mContainer.showEdit(false);
				edit.setText(R.string.templatedelete);
			}

			break;
		case R.id.img_header_view_back_key:
			if (flag_PhotoBar) {
				setPhotoPageMenuBarVisibility(false);
			}
			GLRoot root = getGLRoot();
			root.lockRenderThread();
			try {
				getStateManager().onBackPressed();
			} finally {
				root.unlockRenderThread();
			}
			finish();
			break;

		default:
			break;
		}
	}

	@Override
	public void onEdit() {

	}

	@Override
	public void onPage2Other(int n1, int n2) {

	}

	@Override
	public void onAddOrDeletePage(int page, boolean isAdd) {

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == 10) {
			if (null != data) {
				switch (data.getIntExtra("setfalg", 0)) {
				case 0:
					String path = data.getStringExtra("path");
					String describe = data.getStringExtra("describe");
					mContainer.addNewCustomView(path, describe, 0);
					break;
				case 1:
					List<MoveItem> list = mContainer.getAllMoveItems();
					int i = 1;
					for (MoveItem moveItem : list) {
						if (moveItem.getSetTitle() == 1)
							i += 1;
					}
					String path1 = data.getStringExtra("path");
					String describe1 = data.getStringExtra("describe") + i;
					mContainer.addNewCustomView(path1, describe1, 1);
					break;
				}

			}

		}
		// if(requestCode == 300){
		// if(null!=data){
		// Log.e("scroll", "backdata  "+data.getBooleanExtra("close", false));
		// if(data.getBooleanExtra("close", false)){
		//
		// finish();
		// }
		// }
		// }

	}

	private void setupTransition(LayoutTransition transition) {
		transition.setAnimator(LayoutTransition.APPEARING, customAppearingAnim);
		transition.setAnimator(LayoutTransition.DISAPPEARING,
				customDisappearingAnim);
		transition.setAnimator(LayoutTransition.CHANGE_APPEARING,
				customChangingAppearingAnim);
		transition.setAnimator(LayoutTransition.CHANGE_DISAPPEARING,
				customChangingDisappearingAnim);
	}

	private void createCustomAnimations(LayoutTransition transition) {
		// Changing while Adding
		PropertyValuesHolder pvhLeft = PropertyValuesHolder.ofInt("left", 0, 1);
		PropertyValuesHolder pvhTop = PropertyValuesHolder.ofInt("top", 0, 1);
		PropertyValuesHolder pvhRight = PropertyValuesHolder.ofInt("right", 0,
				1);
		PropertyValuesHolder pvhBottom = PropertyValuesHolder.ofInt("bottom",
				0, 1);
		PropertyValuesHolder pvhScaleX = PropertyValuesHolder.ofFloat("scaleX",
				1f, 0f, 1f);
		PropertyValuesHolder pvhScaleY = PropertyValuesHolder.ofFloat("scaleY",
				1f, 0f, 1f);
		customChangingAppearingAnim = ObjectAnimator.ofPropertyValuesHolder(
				this, pvhLeft, pvhTop, pvhRight, pvhBottom, pvhScaleX,
				pvhScaleY).setDuration(
				transition.getDuration(LayoutTransition.CHANGE_APPEARING));
		customChangingAppearingAnim.addListener(new AnimatorListenerAdapter() {
			public void onAnimationEnd(Animator anim) {
				View view = (View) ((ObjectAnimator) anim).getTarget();
				view.setScaleX(1f);
				view.setScaleY(1f);
			}
		});

		// Changing while Removing
		Keyframe kf0 = Keyframe.ofFloat(0f, 0f);
		Keyframe kf1 = Keyframe.ofFloat(.9999f, 360f);
		Keyframe kf2 = Keyframe.ofFloat(1f, 0f);
		PropertyValuesHolder pvhRotation = PropertyValuesHolder.ofKeyframe(
				"rotation", kf0, kf1, kf2);
		customChangingDisappearingAnim = ObjectAnimator
				.ofPropertyValuesHolder(this, pvhLeft, pvhTop, pvhRight,
						pvhBottom, pvhRotation)
				.setDuration(
						transition
								.getDuration(LayoutTransition.CHANGE_DISAPPEARING));
		customChangingDisappearingAnim
				.addListener(new AnimatorListenerAdapter() {
					public void onAnimationEnd(Animator anim) {
						View view = (View) ((ObjectAnimator) anim).getTarget();
						view.setRotation(0f);
					}
				});

		// Adding
		customAppearingAnim = ObjectAnimator
				.ofFloat(null, "rotationY", 90f, 0f).setDuration(
						transition.getDuration(LayoutTransition.APPEARING));
		customAppearingAnim.addListener(new AnimatorListenerAdapter() {
			public void onAnimationEnd(Animator anim) {
				View view = (View) ((ObjectAnimator) anim).getTarget();
				view.setRotationY(0f);
			}
		});

		// Removing
		customDisappearingAnim = ObjectAnimator.ofFloat(null, "rotationX", 0f,
				90f).setDuration(
				transition.getDuration(LayoutTransition.DISAPPEARING));
		customDisappearingAnim.addListener(new AnimatorListenerAdapter() {
			public void onAnimationEnd(Animator anim) {
				View view = (View) ((ObjectAnimator) anim).getTarget();
				view.setRotationX(0f);
			}
		});

	}

	class ScrollLayoutReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (null != mContainer) {
				if (intent.getAction().equals("tempdrag")) {
					getDataFromCache();
					mItemsAdapter = ScrollAdapter.getAdapterInstance(
							Gallery.this, mList_other, width_screen,
							height_screen);
					mContainer.setSaAdapter(mItemsAdapter);
					mContainer.refreView();// 通知刷新
					mContainer.cancelAnimations();
				} else if (intent.getAction().equals("deletetemp")) {
					getDataFromCache();
					mItemsAdapter = ScrollAdapter.getAdapterInstance(
							Gallery.this, mList_other, width_screen,
							height_screen);
					mContainer.setSaAdapter(mItemsAdapter);
					mContainer.notifyRefresh();// 刷新数据
					new Handler().postDelayed(new Runnable() {

						@Override
						public void run() {
							mContainer.showEdit(false);
							mContainer.showEdit(true);
						}
					}, 500);// 此处的延时是为了显示删除后其他模板的移动效果

				} else if (intent.getAction().equals("scrollupdata")) {
					getDataFromCache();
					mItemsAdapter = ScrollAdapter.getAdapterInstance(
							Gallery.this, mList_other, width_screen,
							height_screen);
					mContainer.setSaAdapter(mItemsAdapter);
					mContainer.refreView();// 通知刷新页面
				}
			}

		}

	}

	private void createSharePreference() {
		SharedPreferences settings = this.getSharedPreferences("FirstFlag", 0);
		SharedPreferences.Editor localEditor = settings.edit();
		localEditor.putBoolean("first", false);
		localEditor.commit();
	}

	private boolean getFirstUse() {
		SharedPreferences settings = this.getSharedPreferences("FirstFlag", 0);
		return settings.getBoolean("first", true);
	}

	public int getOrientation() {
		return mOrientationCompensation;
	}

	public void startShowcaseWelcome() {
		if (SettingsStorage.getAppSetting(this, KEY_SHOWCASE_WELCOME, "0")
				.equals("0")) {
			SettingsStorage.storeAppSetting(this, KEY_SHOWCASE_WELCOME, "1");
			ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
			co.hideOnClickOutside = true;
			mShowcaseView = ShowcaseView.insertShowcaseView(mSideBar, this,
					getString(R.string.showcase_welcome_1_title),
					getString(R.string.showcase_welcome_1_body), co);

			// Animate gesture
			Point size = new Point();
			getWindowManager().getDefaultDisplay().getSize(size);

			mShowcaseView.animateGesture(size.x / 2, size.y * 2.0f / 3.0f,
					size.x / 2, size.y / 2.0f);
			mShowcaseView.setOnShowcaseEventListener(this);
			mShowcaseIndex = SHOWCASE_INDEX_WELCOME_1;
		}
	}

	public void startShowcasePanorama() {
		if (SettingsStorage.getAppSetting(this, KEY_SHOWCASE_PANORAMA, "0")
				.equals("0")) {
			SettingsStorage.storeAppSetting(this, KEY_SHOWCASE_PANORAMA, "1");
			ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
			co.hideOnClickOutside = true;
			Point size = new Point();
			getWindowManager().getDefaultDisplay().getSize(size);
			mShowcaseView = ShowcaseView.insertShowcaseView(size.x / 2, size.y
					- Util.dpToPx(this, 16), this,
					getString(R.string.showcase_panorama_title),
					getString(R.string.showcase_panorama_body), co);

			mShowcaseIndex = SHOWCASE_INDEX_PANORAMA;
		}
	}

	public void startShowcasePicSphere() {
		if (SettingsStorage.getAppSetting(this, KEY_SHOWCASE_PICSPHERE, "0")
				.equals("0")) {
			SettingsStorage.storeAppSetting(this, KEY_SHOWCASE_PICSPHERE, "1");
			ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
			co.hideOnClickOutside = true;
			Point size = new Point();
			getWindowManager().getDefaultDisplay().getSize(size);

			mShowcaseView = ShowcaseView.insertShowcaseView(size.x / 2, size.y
					- Util.dpToPx(this, 16), this,
					getString(R.string.showcase_picsphere_title),
					getString(R.string.showcase_picsphere_body), co);

			mShowcaseIndex = SHOWCASE_INDEX_PICSPHERE;

			mShowcaseView.notifyOrientationChanged(mOrientationCompensation);
		}
	}

	protected void onPause_Camera() {
		// Pause the camera preview
		mPaused = true;

		if (mCamManager != null) {
			mCamManager.pause();
		}

		if (mSnapshotManager != null) {
			mSnapshotManager.onPause();
		}

		if (mOrientationListener != null) {
			mOrientationListener.disable();
		}

		if (mPicSphereManager != null) {
			mPicSphereManager.onPause();
		}

		if (SoftwareHdrCapture.isServiceBound()) {
			try {
				unbindService(SoftwareHdrCapture.getServiceConnection());
			} catch (IllegalArgumentException e) {
				// Do nothing
			}
		}

		// Reset capture transformers on pause, if we are in
		// PicSphere mode
		if (mCameraMode == CAMERA_MODE_PICSPHERE) {
			mCaptureTransformer = null;
		}
		if (cameraButtonWindow != null && cameraButtonWindow.isShowing()) {
			cameraButtonWindow.dismiss();
		}
		// super.onPause();
	}

	protected void onResume_Camera() {
		// Restore the camera preview
		mPaused = false;

		if (mCamManager != null) {
			mCamManager.resume();
		}

		// super.onResume();
		if (mSnapshotManager != null) {
			mSnapshotManager.onResume();
		}

		if (mPicSphereManager != null) {
			mPicSphereManager.onResume();
		}

		mOrientationListener.enable();

		// mReviewDrawer.close();
	}

	public void onBackPressed_Camera() {
		// if (mReviewDrawer.isOpen()) {
		// mReviewDrawer.close();
		// } else {
		// // super.onBackPressed();
		// }

	}

	/**
	 * Returns the mode of the activity See Gallery.CAMERA_MODE_*
	 * 
	 * @return int
	 */
	public static int getCameraMode() {
		return mCameraMode;
	}

	/**
	 * Notify, like a toast, but orientation aware
	 * 
	 * @param text
	 *            The text to show
	 * @param lengthMs
	 *            The duration
	 */
	public static void notify(String text, int lengthMs) {
		// mNotifier.notify(text, lengthMs);
	}

	/**
	 * Notify, like a toast, but orientation aware at the specified position
	 * 
	 * @param text
	 *            The text to show
	 * @param lengthMs
	 *            The duration
	 * 
	 */
	public static void notify(String text, int lengthMs, float x, float y) {
		// mNotifier.notify(text, lengthMs, x, y);
	}

	/**
	 * @return The Panorama Progress Bar view
	 */
	public PanoProgressBar getPanoProgressBar() {
		return mPanoProgressBar;
	}

	public void displayOverlayBitmap(Bitmap bmp) {
		final ImageView iv = (ImageView) findViewById(R.id.camera_preview_overlay);
		iv.setImageBitmap(bmp);
		iv.setAlpha(1.0f);
		iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
		Util.fadeIn(iv);
		iv.setVisibility(View.VISIBLE);
	}

	public void hideOverlayBitmap() {
		final ImageView iv = (ImageView) findViewById(R.id.camera_preview_overlay);
		Util.fadeOut(iv);
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				iv.setVisibility(View.GONE);
			}
		}, 300);
	}

	/**
	 * Sets the mode of the activity See Gallery.CAMERA_MODE_*
	 * 
	 * @param newMode
	 */
	public void setCameraMode(final int newMode) {
		if (mCameraMode == newMode) {
			return;
		}

		if (mCamManager.getParameters() == null) {
			mHandler.post(new Runnable() {
				public void run() {
					setCameraMode(newMode);
				}
			});
		}

		if (mCamPreviewListener != null) {
			mCamPreviewListener.onPreviewPause();
		}

		// Reset PicSphere 3D renderer if we were in PS mode
		if (mCameraMode == CAMERA_MODE_PICSPHERE) {
			resetPicSphere();
		} else if (mCameraMode == CAMERA_MODE_PANO) {
			// resetPanorama();
			startActivity(new Intent(Gallery.this, Gallery.class));
		} else if (mCameraMode == CAMERA_MODE_VIDEO) {
			// must release the camera
			// to reset internals - at least on find5
			mCamManager.pause();
			mCamManager.resume();
		}

		mCameraMode = newMode;

		// Reset any capture transformer
		mCaptureTransformer = null;

		if (newMode == CAMERA_MODE_PHOTO) {
			mShutterButton.setImageDrawable(getResources().getDrawable(
					R.drawable.btn_shutter_photo));
			mCamManager.setStabilization(false);
		} else if (newMode == CAMERA_MODE_VIDEO) {
			mShutterButton.setImageDrawable(getResources().getDrawable(
					R.drawable.btn_shutter_video));
			mCamManager.setStabilization(true);
			// mNotifier.notify(getString(R.string.double_tap_to_snapshot),
			// 2500);
		} else if (newMode == CAMERA_MODE_PICSPHERE) {
			initializePicSphere();
			mShutterButton.setImageDrawable(getResources().getDrawable(
					R.drawable.btn_shutter_photo));
			startShowcasePicSphere();
		} else if (newMode == CAMERA_MODE_PANO) {
			mShutterButton.setImageDrawable(getResources().getDrawable(
					R.drawable.btn_shutter_photo));
		}

		mCamManager.setCameraMode(mCameraMode);

		if (newMode == CAMERA_MODE_PANO) {
			initializePanorama();
			startShowcasePanorama();
		}

		// Reload pictures in the ReviewDrawer
		// mReviewDrawer.updateFromGallery(newMode != CAMERA_MODE_VIDEO, 0);
		mHandler.post(new Runnable() {
			public void run() {
				updateCapabilities();
			}
		});
	}

	/**
	 * Sets the active capture transformer. See {@link CaptureTransformer} for
	 * more details on what's a capture transformer.
	 * 
	 * @param transformer
	 *            The new transformer to apply
	 */
	public void setCaptureTransformer(CaptureTransformer transformer) {
		if (mCaptureTransformer != null) {
			mSnapshotManager.removeListener(mCaptureTransformer);
		}
		mCaptureTransformer = transformer;

		if (mCaptureTransformer != null && mSnapshotManager != null) {
			mSnapshotManager.addListener(transformer);
		}
	}

	/**
	 * Updates the orientation of the whole UI (in place) based on the
	 * calculations given by the orientation listener
	 */
	public void updateInterfaceOrientation() {
		setViewRotation(mShutterButton, mOrientationCompensation);
		setViewRotation(mPanoProgressBar, mOrientationCompensation);
		setViewRotation(mPicSphereUndo, mOrientationCompensation);
		// mNotifier.notifyOrientationChanged(mOrientationCompensation);
		mSideBar.notifyOrientationChanged(mOrientationCompensation);
		// mWidgetRenderer.notifyOrientationChanged(mOrientationCompensation);
		// mSwitchRingPad.notifyOrientationChanged(mOrientationCompensation);
		// mSavePinger.notifyOrientationChanged(mOrientationCompensation);
		// mReviewDrawer.notifyOrientationChanged(mOrientationCompensation);
	}

	public void updateCapabilities() {
		// Populate the sidebar buttons a little later (so we have camera
		// parameters)
		mHandler.post(new Runnable() {
			public void run() {
				Camera.Parameters params = mCamManager.getParameters();

				// We don't have the camera parameters yet, retry later
				if (params == null) {
					if (!mPaused) {
						mHandler.postDelayed(this, 100);
					}
				} else {
					mCamManager.startParametersBatch();

					// Close all widgets
					// mWidgetRenderer.closeAllWidgets();

					// Update focus/exposure ring support
					updateRingsVisibility();

					// Update sidebar
					mSideBar.checkCapabilities(Gallery.this,
							(ViewGroup) findViewById(R.id.widgets_container));

					// Set orientation
					updateInterfaceOrientation();

					mCamManager.stopParametersBatch();
				}
			}
		});
	}

	public void updateRingsVisibility() {
		// Rings logic:
		// * PicSphere and panorama don't need it (infinity focus when possible)
		// * Show focus all the time otherwise in photo and video
		// * Show exposure ring in photo and video, if it's not toggled off
		// * Fullscreen shutter hides all the rings
		if ((mCameraMode == CAMERA_MODE_PHOTO && !mIsFullscreenShutter)
				|| mCameraMode == CAMERA_MODE_VIDEO) {
			mFocusHudRing
					.setVisibility(mCamManager.isFocusAreaSupported() ? View.VISIBLE
							: View.GONE);
			mExposureHudRing
					.setVisibility(mCamManager.isExposureAreaSupported()
							&& mUserWantsExposureRing ? View.VISIBLE
							: View.GONE);
		} else {
			mFocusHudRing.setVisibility(View.GONE);
			mExposureHudRing.setVisibility(View.GONE);
		}
	}

	public boolean isExposureRingVisible() {
		return (mExposureHudRing.getVisibility() == View.VISIBLE);
	}

	public void setExposureRingVisible(boolean visible) {
		mUserWantsExposureRing = visible;
		updateRingsVisibility();

		// Internally reset the position of the exposure ring, while still
		// leaving it at its position so that if the user toggles it back
		// on, it will appear at its previous location
		mCamManager.setExposurePoint(0, 0);
	}

	public void startTimerCountdown(int timeMs) {
		mTimerView.animate().alpha(1.0f).setDuration(300).start();
		mTimerView.setIntervalTime(timeMs);
		mTimerView.startIntervalAnimation();
	}

	public void hideTimerCountdown() {
		mTimerView.animate().alpha(0.0f).setDuration(300).start();
	}

	protected void setupCamera() {
		// Setup the Camera hardware and preview
		mCamManager = new CameraManager(this);
		((LauncherApplication) getApplication()).setCameraManager(mCamManager);

		setGLRenderer(mCamManager.getRenderer());

		mCamPreviewListener = new CameraPreviewListener();
		mCamManager.setPreviewPauseListener(mCamPreviewListener);
		mCamManager.setCameraReadyListener(this);

		mCamManager.open(Camera.CameraInfo.CAMERA_FACING_BACK);
	}

	@Override
	public void onCameraReady() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Profiler.getDefault().start("OnCameraReady");
				Camera.Parameters params = mCamManager.getParameters();

				if (params == null) {
					// Are we too fast? Let's try again.
					mHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							onCameraReady();
						}
					}, 20);
					return;
				}

				mCamManager.updateDisplayOrientation();
				Camera.Size picSize = params.getPictureSize();

				Camera.Size sz = Util.getOptimalPreviewSize(Gallery.this,
						params.getSupportedPreviewSizes(),
						((float) picSize.width / (float) picSize.height));
				if (sz == null) {
					Log.e(TAG,
							"No preview size!! Something terribly wrong with camera!");
					return;
				}
				// mCamManager.setPreviewSize(sz.width, sz.height);

				if (mIsCamSwitching) {
					mCamManager.restartPreviewIfNeeded();
					mIsCamSwitching = false;
				}

				if (mFocusManager == null) {
					mFocusManager = new FocusManager(mCamManager);
					mFocusManager.setListener(new MainFocusListener());
				}

				mFocusHudRing.setManagers(mCamManager, mFocusManager);

				if (mSnapshotManager == null) {
					mSnapshotManager = new SnapshotManager(mCamManager,
							mFocusManager, Gallery.this);
					mSnapshotListener = new MainSnapshotListener();
					mSnapshotManager.addListener(mSnapshotListener);
				}

				// Hide sidebar after start
				mCancelSideBarClose = false;
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						if (!mCancelSideBarClose) {
							mSideBar.slideClose();
							// mWidgetRenderer.notifySidebarSlideClose();
						}
					}
				}, 1500);

				Profiler.getDefault().start("OnCameraReady-updateCapa");
				updateCapabilities();
				Profiler.getDefault().logProfile("OnCameraReady-updateCapa");

				// mSavePinger.stopSaving();
				Profiler.getDefault().logProfile("OnCameraReady");
			}
		});
	}

	public void onCameraFailed() {
		Log.e(TAG, "Could not open camera HAL");
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(Gallery.this,
						getResources().getString(R.string.cannot_connect_hal),
						Toast.LENGTH_LONG).show();
				CameraViewOpen();
				isCanCameraUse = false;
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		switch (keyCode) {
		case KeyEvent.KEYCODE_FOCUS:
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			// Use the volume down button as focus button
			// bill add begin
			if (mCamManager == null) {
				return false;
			}
			

	
			// bill add end
			if (!mIsFocusButtonDown) {
				mCamManager.doAutofocus(mFocusManager);
				mCamManager.setLockSetup(true);
				mIsFocusButtonDown = true;
			}
			return true;
		case KeyEvent.KEYCODE_CAMERA:
		case KeyEvent.KEYCODE_VOLUME_UP:
			// Use the volume up button as shutter button (or snapshot button in
			// video mode)
			// bill add begin
	
			if (mCamManager == null) {
				return false;
			}
			// bill add end
			if (!mIsShutterButtonDown) {
				if (mCameraMode == CAMERA_MODE_VIDEO) {
					mSnapshotManager.queueSnapshot(true, 0);
				} else {
					mShutterButton.performClick();
				}
				mIsShutterButtonDown = true;
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_FOCUS:
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			// bill add begin
			if (mCamManager == null) {
				return false;
			}
			// bill add end
			mIsFocusButtonDown = false;
			mCamManager.setLockSetup(false);
			break;

		case KeyEvent.KEYCODE_CAMERA:
		case KeyEvent.KEYCODE_VOLUME_UP:
			if (mCamManager == null) {
				return false;
			}
			// bill add end
			mIsShutterButtonDown = false;
			break;
		}

		return super.onKeyUp(keyCode, event);
	}

	public CameraManager getCamManager() {
		return mCamManager;
	}

	public SnapshotManager getSnapManager() {
		return mSnapshotManager;
	}

	public PicSphereManager getPicSphereManager() {
		return mPicSphereManager;
	}

	// public ReviewDrawer getReviewDrawer() {
	// return mReviewDrawer;
	// }

	public void initializePicSphere() {
		// Check if device has a gyroscope and GLES2 support
		// XXX: Should we make a fallback for super super old devices?
		final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		final ConfigurationInfo configurationInfo = activityManager
				.getDeviceConfigurationInfo();
		final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

		if (!supportsEs2) {
			// mNotifier.notify(getString(R.string.no_gles20_support), 4000);
			return;
		}
		// Close widgets and slide sidebar to make room and focus on the sphere
		mSideBar.slideClose();
		// mWidgetRenderer.closeAllWidgets();

		// Setup the 3D rendering
		if (mPicSphereManager == null) {
			mPicSphereManager = new PicSphereManager(this, mSnapshotManager);
		}
		setGLRenderer(mPicSphereManager.getRenderer());

		// Setup the capture transformer
		final PicSphereCaptureTransformer transformer = new PicSphereCaptureTransformer(
				this);
		setCaptureTransformer(transformer);

		mPicSphereUndo.setVisibility(View.VISIBLE);
		mPicSphereUndo.setAlpha(0.0f);
		mPicSphereUndo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				transformer.removeLastPicture();
			}
		});
	}

	/**
	 * Tear down the PicSphere mode and set the default renderer back on the
	 * preview GL surface.
	 */
	public void resetPicSphere() {
		// Reset the normal renderer
		setGLRenderer(mCamManager.getRenderer());

		// Tear down PicSphere capture system
		if (mPicSphereManager != null) {
			mPicSphereManager.tearDown();
		}
		setCaptureTransformer(null);

		if (mPicSphereUndo != null) {
			mPicSphereUndo.setVisibility(View.GONE);
		}
	}

	/**
	 * Initializes the panorama (mosaic) subsystem
	 */
	public void initializePanorama() {
		mMosaicProxy = new MosaicProxy(this);
		setCaptureTransformer(mMosaicProxy);
		mCamManager.setRenderToTexture(null);
		updateRingsVisibility();
	}

	/**
	 * Turns off the panorama (mosaic) subsystem
	 */
	public void resetPanorama() {
		if (mMosaicProxy != null) {
			mMosaicProxy.tearDown();
		}
		setGLRenderer(mCamManager.getRenderer());
	}

	public void setGLRenderer(GLSurfaceView.Renderer renderer) {
		final ViewGroup container = ((ViewGroup) findViewById(R.id.gl_renderer_container));
		// Delete the previous GL Surface View (if any)
		if (mGLSurfaceView != null) {
			container.removeView(mGLSurfaceView);
			mGLSurfaceView = null;
		}

		// Make a new GL view using the provided renderer
		mGLSurfaceView = new GLSurfaceView(this);
		mGLSurfaceView.setEGLContextClientVersion(2);
		mGLSurfaceView.setRenderer(renderer);

		container.addView(mGLSurfaceView);
	}

	/**
	 * Toggles the fullscreen shutter that lets user take pictures by tapping on
	 * the screen
	 */
	public void toggleFullscreenShutter() {
		if (mIsFullscreenShutter) {
			mIsFullscreenShutter = false;
			createCameraBar();//
			// mShutterButton.animate().translationY(0).setDuration(400).start();
			// mLayout_Album.animate().translationY(0).setDuration(400).start();
			// mLayout_Conversion.animate().translationY(0).setDuration(400)
			// .start();
		} else {
			mIsFullscreenShutter = true;
			if (cameraButtonWindow != null && cameraButtonWindow.isShowing()) {
				cameraButtonWindow.dismiss();
			}
			// mShutterButton.animate().translationY(mShutterButton.getHeight())
			// .setDuration(400).start();
			// mLayout_Album.animate().translationY(mLayout_Album.getHeight())
			// .setDuration(400).start();
			// mLayout_Conversion.animate()
			// .translationY(mLayout_Conversion.getHeight())
			// .setDuration(400).start();
			notify(getString(R.string.fullscreen_shutter_info), 2000);
		}
		updateRingsVisibility();
	}

	public void setPicSphereUndoVisible(final boolean visible) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (visible) {
					mPicSphereUndo.setVisibility(View.VISIBLE);
					mPicSphereUndo.setAlpha(1.0f);
				} else {
					mPicSphereUndo.animate().alpha(0.0f).setDuration(200)
							.start();
				}
			}
		});
	}

	/**
	 * Recursively rotates the Views of ViewGroups
	 * 
	 * @param vg
	 *            the root ViewGroup
	 * @param rotation
	 *            the angle to which rotate the views
	 */
	public static void setViewGroupRotation(ViewGroup vg, float rotation) {
		final int childCount = vg.getChildCount();

		for (int i = 0; i < childCount; i++) {
			View child = vg.getChildAt(i);

			if (child instanceof ViewGroup) {
				setViewGroupRotation((ViewGroup) child, rotation);
			} else {
				setViewRotation(child, rotation);
			}
		}
	}

	public static void setViewRotation(View v, float rotation) {
		v.animate().rotation(rotation).setDuration(200)
				.setInterpolator(new DecelerateInterpolator()).start();
	}

	@Override
	public void onShowcaseViewHide(ShowcaseView showcaseView) {
		switch (mShowcaseIndex) {
		case SHOWCASE_INDEX_WELCOME_1:
			mShowcaseIndex = SHOWCASE_INDEX_WELCOME_2;

			Point size = new Point();
			getWindowManager().getDefaultDisplay().getSize(size);

			ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
			co.hideOnClickOutside = true;
			mShowcaseView = ShowcaseView.insertShowcaseView(size.x / 2, size.y
					- Util.dpToPx(this, 16), this,
					getString(R.string.showcase_welcome_2_title),
					getString(R.string.showcase_welcome_2_body), co);

			// animate gesture
			mShowcaseView.animateGesture(size.x / 2,
					size.y - Util.dpToPx(this, 16), size.x / 2, size.y / 2);
			mShowcaseView.setOnShowcaseEventListener(this);

			// ping the button
			// mSwitchRingPad.animateHint();

			break;
		}
	}

	@Override
	public void onShowcaseViewShow(ShowcaseView showcaseView) {
		// Do nothing here
	}

	/**
	 * Listener that is called when the preview pauses or resumes
	 */
	private class CameraPreviewListener implements
			CameraManager.PreviewPauseListener {
		@Override
		public void onPreviewPause() {
			// XXX: Do a little animation
		}

		@Override
		public void onPreviewResume() {
		}
	}

	/**
	 * Listener that is called when a ring pad button is activated (finger
	 * release above)
	 */
	private class MainRingPadListener implements SwitchRingPad.RingPadListener {
		@Override
		public void onButtonActivated(int eventId) {
			switch (eventId) {
			case SwitchRingPad.BUTTON_CAMERA:
				setCameraMode(CAMERA_MODE_PHOTO);
				break;
			case SwitchRingPad.BUTTON_PANO:
				setCameraMode(CAMERA_MODE_PANO);
				break;
			case SwitchRingPad.BUTTON_VIDEO:
				// setCameraMode(CAMERA_MODE_VIDEO);
				break;
			case SwitchRingPad.BUTTON_PICSPHERE:
				// setCameraMode(CAMERA_MODE_PICSPHERE);
				break;
			case SwitchRingPad.BUTTON_SWITCHCAM:
				mIsCamSwitching = true;
				if (mCamManager.getCurrentFacing() == Camera.CameraInfo.CAMERA_FACING_FRONT) {
					mCamManager.open(Camera.CameraInfo.CAMERA_FACING_BACK);
				} else {
					mCamManager.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
				}

				break;
			}
		}
	}

	/**
	 * Listener that is called when shutter button is slided, to open ring pad
	 * view
	 */
	private class MainShutterSlideListener implements
			ShutterButton.ShutterSlideListener {
		@Override
		public void onSlideOpen() {
			// mSwitchRingPad.animateOpen();

			// Tapping the shutter button locked exposure/WB, so we unlock it if
			// we slide our finger
			mCamManager.setLockSetup(false);

			// Cancel long-press action
			mIsShutterLongClicked = false;
		}

		@Override
		public void onSlideClose() {
			// mSwitchRingPad.animateClose();
		}

		@Override
		public boolean onMotionEvent(MotionEvent ev) {
			// mSwitchRingPad.onTouchEvent(ev);
			return false;
		}

		@Override
		public void onShutterButtonPressed() {
			// Animate the ring pad
			// mSwitchRingPad.animateHint();

			// Make the review drawer super translucent if it is open
			// mReviewDrawer.setTemporaryHide(true);

			// Lock automatic settings
			mCamManager.setLockSetup(true);

			// Turn on stabilization
			mCamManager.setStabilization(true);
		}
	}

	/**
	 * When the shutter button is pressed
	 */
	public class MainShutterClickListener implements OnClickListener,
			View.OnLongClickListener, View.OnTouchListener {

		@Override
		public void onClick(View v) {
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					// mReviewDrawer.setTemporaryHide(false);
				}
			}, 500);

			if (mSnapshotManager == null)
				return;

			// If we have a capture transformer, apply it, otherwise use the
			// default
			// behavior.
			if (mCaptureTransformer != null) {
				mCaptureTransformer.onShutterButtonClicked(mShutterButton);
			} else if (Gallery.getCameraMode() == Gallery.CAMERA_MODE_PHOTO) {
				mSnapshotManager.queueSnapshot(true, 0);
			} else if (Gallery.getCameraMode() == Gallery.CAMERA_MODE_VIDEO) {
				mShutterButton.setImageDrawable(getResources().getDrawable(
						R.drawable.btn_shutter_video));
			} else {
				Log.e(TAG, "Unknown Camera Mode: " + mCameraMode
						+ " ; No capture transformer");
			}
		}

		@Override
		public boolean onLongClick(View view) {
			if (mCaptureTransformer != null) {
				mCaptureTransformer.onShutterButtonLongPressed(mShutterButton);
			} else {
				mIsShutterLongClicked = true;
				if (mFocusManager != null) {
					mFocusManager.checkFocus();
				}
			}
			return true;
		}

		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			// If we long-press the shutter button and no capture transformer
			// handles it, we
			// will just have nothing happening. We register the long click
			// event in here, and
			// trigger a snapshot once it's released.
			if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP
					&& mIsShutterLongClicked) {
				mIsShutterLongClicked = false;
				onClick(view);
			}

			return view.onTouchEvent(motionEvent);
		}
	}

	/**
	 * Focus listener to animate the focus HUD ring from FocusManager events
	 */
	private class MainFocusListener implements FocusManager.FocusListener {
		@Override
		public void onFocusStart(final boolean smallAdjust) {
			mIsFocusing = true;
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mFocusHudRing.animateWorking(smallAdjust ? 200 : 1500);
				}
			});
		}

		@Override
		public void onFocusReturns(final boolean smallAdjust,
				final boolean success) {
			mIsFocusing = false;
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mFocusHudRing.animatePressUp();

					if (!smallAdjust) {
						mFocusHudRing.setFocusImage(success);
					} else {
						mFocusHudRing.setFocusImage(true);
					}
				}
			});
		}
	}

	/**
	 * Snapshot listener for when snapshots are taken, in SnapshotManager
	 */
	private class MainSnapshotListener implements
			SnapshotManager.SnapshotListener {
		private long mRecordingStartTimestamp;
		private TextView mTimerTv;
		private boolean mIsRecording;

		private Runnable mUpdateTimer = new Runnable() {
			@Override
			public void run() {
				long recordingDurationMs = System.currentTimeMillis()
						- mRecordingStartTimestamp;
				int minutes = (int) Math.floor(recordingDurationMs / 60000.0);
				int seconds = (int) recordingDurationMs / 1000 - minutes * 60;

				mTimerTv.setText(String.format("%02d:%02d", minutes, seconds));

				// Loop infinitely until recording stops
				if (mIsRecording) {
					mHandler.postDelayed(this, 500);
				}
			}
		};

		@Override
		public void onSnapshotShutter(final SnapshotManager.SnapshotInfo info) {
			final FrameLayout layout = (FrameLayout) findViewById(R.id.thumb_flinger_container);

			// Fling the preview
			final ThumbnailFlinger flinger = new ThumbnailFlinger(Gallery.this);
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					layout.addView(flinger);
					flinger.setRotation(90);
					flinger.setImageBitmap(info.mThumbnail);
					flinger.doAnimation();
				}
			});

			// Unlock camera auto settings
			mCamManager.setLockSetup(false);
			mCamManager.setStabilization(false);
		}

		@Override
		public void onSnapshotPreview(SnapshotManager.SnapshotInfo info) {
			// Do nothing here
		}

		@Override
		public void onSnapshotProcessing(SnapshotManager.SnapshotInfo info) {
			runOnUiThread(new Runnable() {
				public void run() {
					// if (mSavePinger != null) {
					// mSavePinger.setPingMode(SavePinger.PING_MODE_ENHANCER);
					// mSavePinger.startSaving();
					// }
				}
			});
		}

		@Override
		public void onSnapshotSaved(SnapshotManager.SnapshotInfo info) {
			String uriStr = info.mUri.toString();

			// Add the new image to the gallery and the review drawer
			int originalImageId = Integer.parseInt(uriStr.substring(
					uriStr.lastIndexOf("/") + 1, uriStr.length()));
			Log.v(TAG, "Adding snapshot to gallery: " + originalImageId);
			// mReviewDrawer.addImageToList(originalImageId);
			// mReviewDrawer.scrollToLatestImage();
		}

		@Override
		public void onMediaSavingStart() {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// mSavePinger.setPingMode(SavePinger.PING_MODE_SAVE);
					// mSavePinger.startSaving();
				}
			});
		}

		@Override
		public void onMediaSavingDone() {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// mSavePinger.stopSaving();
				}
			});
		}

	}

	/**
	 * Handles the orientation changes without turning the actual activity
	 */
	private class CameraOrientationEventListener extends
			OrientationEventListener {
		public CameraOrientationEventListener(Context context) {
			super(context);
		}

		@Override
		public void onOrientationChanged(int orientation) {
			// We keep the last known orientation. So if the user first orient
			// the camera then point the camera to floor or sky, we still have
			// the correct orientation.
			if (orientation == ORIENTATION_UNKNOWN) {
				return;
			}
			mOrientation = Util.roundOrientation(orientation, mOrientation);

			// Notify camera of the raw orientation
			mCamManager.setOrientation(mOrientation);

			// Adjust orientationCompensation for the native orientation of the
			// device.
			Configuration config = getResources().getConfiguration();
			int rotation = getWindowManager().getDefaultDisplay().getRotation();
			Util.getDisplayRotation(Gallery.this);

			boolean nativeLandscape = false;

			if (((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) && config.orientation == Configuration.ORIENTATION_LANDSCAPE)
					|| ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) && config.orientation == Configuration.ORIENTATION_PORTRAIT)) {
				nativeLandscape = true;
			}

			int orientationCompensation = mOrientation; // + (nativeLandscape ?
														// 0 : 90);
			if (orientationCompensation == 90) {
				orientationCompensation += 180;
			} else if (orientationCompensation == 270) {
				orientationCompensation -= 180;
			}

			// Avoid turning all around
			float angleDelta = orientationCompensation
					- mOrientationCompensation;
			if (angleDelta >= 270) {
				orientationCompensation -= 360;
			}

			if (mOrientationCompensation != orientationCompensation) {
				mOrientationCompensation = orientationCompensation;
				updateInterfaceOrientation();
			}
		}
	}

	/**
	 * Handles the swipe and tap gestures on the lower layer of the screen (ie.
	 * the preview surface)
	 * 
	 * @note Remember that the default orientation of the screen is landscape,
	 *       thus the side bar is at the BOTTOM of the screen, and is swiped
	 *       UP/DOWN.
	 */
	public class GestureListener extends
			GestureDetector.SimpleOnGestureListener {
		private static final int SWIPE_MIN_DISTANCE = 10;
		private final float DRAG_MIN_DISTANCE = Util.dpToPx(Gallery.this, 5.0f);
		private static final int SWIPE_MAX_OFF_PATH = 80;
		private static final int SWIPE_THRESHOLD_VELOCITY = 800;

		// Allow to drag the side bar up to half of the screen
		private static final int SIDEBAR_THRESHOLD_FACTOR = 1;// 此值为2是左半屏有效

		private boolean mCancelSwipe = false;

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			if (mPaused)
				return false;

			// A single tap equals to touch-to-focus in photo/video
			if ((mCameraMode == CAMERA_MODE_PHOTO && !mIsFullscreenShutter)
					|| mCameraMode == CAMERA_MODE_VIDEO) {
				// if (mFocusManager != null) {//菜单的显示与隐藏
				// mFocusHudRing.setPosition(e.getRawX(), e.getRawY());
				// mFocusManager.refocus();
				// }
				toggleFullscreenShutter();
			} else if (mCameraMode == CAMERA_MODE_PHOTO && mIsFullscreenShutter) {
				// We are in fullscreen shutter mode, so just take a picture
				// mSnapshotManager.queueSnapshot(true, 0);//拍照
				toggleFullscreenShutter();
			}

			return super.onSingleTapConfirmed(e);
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			// In VIDEO mode, a double tap snapshots (or volume up)
			if (mCameraMode == CAMERA_MODE_VIDEO) {
				mSnapshotManager.queueSnapshot(true, 0);
			} else if (mCameraMode == CAMERA_MODE_PHOTO) {
				// Toggle fullscreen shutter
				// toggleFullscreenShutter();
				CameraViewOpen();
			}

			return super.onDoubleTap(e);
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {// distanceX从左向右滑时是负值，从右向左滑时时正直
			if (e1 == null || e2 == null) {
				return false;
			}

			// Detect drag of the side bar or review drawer
			if (Math.abs(e1.getY() - e2.getY()) < SWIPE_MAX_OFF_PATH) {
				if (e1.getRawX() < Util.getScreenSize(Gallery.this).x
						/ SIDEBAR_THRESHOLD_FACTOR) {
					if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
							|| e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE) {
						// mSideBar.slide(-distanceX);
						// 相机
						// mWidgetRenderer.notifySidebarSlideStatus(-distanceX);
						mCancelSwipe = true;
						mCancelSideBarClose = true;
					}

					return true;
				}
			} else if (Math.abs(e1.getY() - e2.getY()) > DRAG_MIN_DISTANCE) {
				// mReviewDrawer.slide(-distanceY);
			}

			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			try {
				if (Math.abs(e1.getY() - e2.getY()) < SWIPE_MAX_OFF_PATH) {
					// swipes to open/close the sidebar and/or hide/restore the
					// widgets
					if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
							&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
						// if (mWidgetRenderer.isHidden()
						// && mWidgetRenderer.getWidgetsCount() > 0) {
						// mWidgetRenderer.restoreWidgets();
						// } else {
						// // mSideBar.slideOpen();//侧边菜单栏打开
						// // mWidgetRenderer.notifySidebarSlideOpen();
						// // mCancelSideBarClose = true;
						// }
					} else if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
							&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
						// if (mSideBar.isOpen()) {
						// // mSideBar.slideClose();侧边菜单栏关闭
						// // mWidgetRenderer.notifySidebarSlideClose();
						// // mCancelSideBarClose = true;
						// }
						// else if (!mWidgetRenderer.isHidden()
						// && mWidgetRenderer.getWidgetsCount() > 0
						// && !mCancelSwipe) {
						// mWidgetRenderer.hideWidgets();
						// }
						Log.e("scroll", "切换");
						mIsCamSwitching = true;
						if (mCamManager.getCurrentFacing() == Camera.CameraInfo.CAMERA_FACING_FRONT) {
							mCamManager
									.open(Camera.CameraInfo.CAMERA_FACING_BACK);
						} else {
							mCamManager
									.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
						}
					}
				}

				if (Math.abs(e1.getX() - e2.getX()) < SWIPE_MAX_OFF_PATH) {
					// swipes up/down to open/close the review drawer
					if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE
							&& Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
						// mReviewDrawer.close();
					} else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE
							&& Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
						// mReviewDrawer.open();
					}
				}
			} catch (Exception e) {
				// Do nothing here
			}

			mCancelSwipe = false;
			return true;
		}
	}

	/**
	 * Handles the pinch-to-zoom gesture
	 */
	private class ZoomGestureListener extends
			ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			Camera.Parameters params = mCamManager.getParameters();

			if (params == null)
				return false;

			if (!mIsFocusing) {
				if (detector.getScaleFactor() > 1.0f) {
					params.setZoom(Math.min(params.getZoom() + 1,
							params.getMaxZoom()));
				} else if (detector.getScaleFactor() < 1.0f) {
					params.setZoom(Math.max(params.getZoom() - 1, 0));
				} else {
					return false;
				}

				mHasPinchZoomed = true;
				mCamManager.setParameters(params);
			}

			return true;
		}
	}

	public void CammeraStart() {
		mPaused = false;
		mIsCamSwitching = false;

		getWindow().getDecorView().setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_LOW_PROFILE);

		mUserWantsExposureRing = true;
		mIsFullscreenShutter = false;

		mSideBar = (SideBar) findViewById(R.id.sidebar_scroller);
		// mWidgetRenderer = (WidgetRenderer)
		// findViewById(R.id.widgets_container);
		// mSavePinger = (SavePinger) findViewById(R.id.save_pinger);
		// mTimerView = (CircleTimerView) findViewById(R.id.timer_view);
		mPicSphereUndo = (Button) findViewById(R.id.btn_picsphere_undo);

		// mSwitchRingPad = (SwitchRingPad) findViewById(R.id.switch_ring_pad);
		// mSwitchRingPad.setListener(new MainRingPadListener());

		mPanoProgressBar = (PanoProgressBar) findViewById(R.id.panorama_progress_bar);
		// mNotifier = (Notifier) findViewById(R.id.notifier_container);

		// mReviewDrawer = (ReviewDrawer) findViewById(R.id.review_drawer);

		// Create orientation listener. This should be done first because it
		// takes some time to get first orientation.
		mOrientationListener = new CameraOrientationEventListener(this);
		mOrientationListener.enable();

		mHandler = new Handler();

		// Setup the camera hardware and preview
		setupCamera();

		SoundManager.getSingleton().preload(this);

		// Setup HUDs
		mFocusHudRing = (FocusHudRing) findViewById(R.id.hud_ring_focus);

		mExposureHudRing = (ExposureHudRing) findViewById(R.id.hud_ring_exposure);
		mExposureHudRing.setManagers(mCamManager);

		// Setup shutter button

		// Setup gesture detection
		mGestureDetector = new GestureDetector(this, new GestureListener());
		mZoomGestureDetector = new ScaleGestureDetector(this,
				new ZoomGestureListener());

		findViewById(R.id.gl_renderer_container).setOnTouchListener(
				mPreviewTouchListener);

		// Use SavePinger to animate a bit while we open the camera device
		// mSavePinger.setPingMode(SavePinger.PING_MODE_SIMPLE);
		// mSavePinger.startSaving();

		// Hack because review drawer size might not be measured yet
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// mReviewDrawer.open();
				// mReviewDrawer.close();
			}
		}, 300);

		// startShowcaseWelcome();
	}

	public void CameraViewOpen() {
		if (!isCanCameraUse)
			return;
		galleryCamera.setVisibility(View.VISIBLE);

		Log.e("scroll", "open");
		if (!flag_CameraOpen) {
			// ((SurfaceView)
			// findViewById(R.id.gl_root_view)).setZOrderOnTop(true);
			setPhotoPageMenuBarVisibility(false);
			Toast.makeText(this, "相机打开", Toast.LENGTH_SHORT).show();
			findViewById(R.id.glviewbackground).setBackgroundResource(
					R.drawable.nothing);
			PhotoView.getPhotoViewIntance().setVisibility(View.GONE);
			flag_CameraOpen = true;
			if (flag_FirstLogin) {
				flag_FirstLogin = false;
				CammeraStart();

			} else {
				// mSnapshotManager.removemSnapshotsQueue();
			}
			createCameraBar();
			// mShutterButton.setVisibility(View.VISIBLE);
			// mLayout_Album.setVisibility(View.VISIBLE);
			// mLayout_Conversion.setVisibility(View.VISIBLE);
			onResume_Camera();
			Log.e("scroll", "open");
		} else {
			// ((SurfaceView)
			// findViewById(R.id.gl_root_view)).setZOrderOnTop(false);
			Toast.makeText(this, "相机关闭", Toast.LENGTH_SHORT).show();
			findViewById(R.id.glviewbackground).setBackgroundColor(Color.BLACK);
			// mShutterButton.setVisibility(View.GONE);
			// mLayout_Album.setVisibility(View.GONE);
			// mLayout_Conversion.setVisibility(View.GONE);
			if (cameraButtonWindow != null && cameraButtonWindow.isShowing()) {
				cameraButtonWindow.dismiss();
			}
			flag_CameraOpen = false;
			Log.e("scroll", "vis");
			PhotoView.getPhotoViewIntance().setVisibility(View.VISIBLE);
			Log.e("scroll", "vised");
			onPause_Camera();
			onBackPressed_Camera();
		}

	}

	public void createCameraBar() {
		if (null != cameraButtonWindow && cameraButtonWindow.isShowing()) {
			cameraButtonWindow.dismiss();
		}
		if (cameraButtonWindow == null) {
			initCameraButtonView();
			cameraButtonWindow = new PopupWindow(cameraButtonView, screenwidth,
					(int) (110 * displayMetrics.density));
			cameraButtonWindow.setFocusable(false);
			cameraButtonWindow.setOutsideTouchable(false);
			cameraButtonWindow.update();
			cameraButtonWindow.setBackgroundDrawable(new BitmapDrawable());
		}
		cameraButtonWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
	}

	public void createDialogPhotoBar() {
		if (isAlbumReturn) {
			dialogPhotoBar = null;
		}
		if (null != dialogPhotoBar && dialogPhotoBar.isShowing()) {
			dialogPhotoBar.dismiss();
		}
		// dialogPhotoBar.show();

		if (dialogPhotoBar == null) {
			InitPhotoView();
			w = PolicyManager.makeNewWindow(this);
			photoWindow = w;
			w.setWindowManager(window, null, null);
			w.setGravity(Gravity.TOP);
			dialogPhotoBar = new PopupWindow(photoBar, screenwidth,
					(int) (50 * displayMetrics.density));
			dialogPhotoBar.setFocusable(false);
			dialogPhotoBar.setOutsideTouchable(false);
			dialogPhotoBar.update();
			dialogPhotoBar.setAnimationStyle(R.style.MoreDialog);
			dialogPhotoBar.setBackgroundDrawable(new BitmapDrawable());
		}

		if (isPhotoBarShow) {
			return;
		}
		if (!flag_PhotoBar) {
			dialogPhotoBar.showAtLocation(photoWindow.getDecorView(),
					Gravity.TOP, 0, 0);
			// window.addView(photoBar, layoutParams);
			flag_PhotoBar = true;
		}
		isAlbumReturn = false;
	}

	// bill add begin 20150115 调整壁纸预览布局，添加设置布局按钮
	public void createDialogSettingWallPaper() {
		if (isAlbumReturn) {
			dialogWallPaper = null;
		}
		if (null != dialogWallPaper && dialogWallPaper.isShowing()) {
			dialogWallPaper.dismiss();
		}

		if (dialogWallPaper == null) {
			initSettingWallpaper();
			w = PolicyManager.makeNewWindow(this);
			setWallpaperWindow = w;
			w.setWindowManager(window, null, null);
			w.setGravity(Gravity.CENTER);
			dialogWallPaper = new PopupWindow(settingWallpaper,
					(int) (screenwidth), (int) (49 * displayMetrics.density));
			// 设置壁纸调整为显示宽度为屏幕宽度
			// (int) (114 * displayMetrics.density),
			// (int) (45 * displayMetrics.density));
			dialogWallPaper.setFocusable(false);
			dialogWallPaper.setOutsideTouchable(false);
			dialogWallPaper.update();
			dialogWallPaper.setAnimationStyle(R.style.MoreDialog);
			dialogWallPaper.setBackgroundDrawable(new BitmapDrawable());
		}

		dialogWallPaper.showAtLocation(setWallpaperWindow.getDecorView(),
				Gravity.TOP, 0, (int) screenHeight);
		// (int) (screenHeight - (83 * displayMetrics.density)));
		flag_PhotoBar = true;
		isAlbumReturn = false;
	}

	public void initSettingWallpaper() {
		settingWallpaper = (RelativeLayout) RelativeLayout.inflate(this,
				R.layout.settingwallpaper, null);
		// LayoutParams layoutParams = new
		// LayoutParams(LayoutParams.FILL_PARENT,
		// (int) (81 * displayMetrics.density));
		// layoutParams.height = (int) (81 * displayMetrics.density);
		// layoutParams.width = 114 * 3;
		// settingWallpaper.setLayoutParams(layoutParams);
		setWallpaper = (TextView) settingWallpaper
				.findViewById(R.id.settingwallpaper);
		setWallpaper.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				MobclickAgent.onEvent(Gallery.this, "SetWallPaper"); // 友盟统计
				handler.sendEmptyMessage(1);
		
				Thread thread = new Thread() {
					public void run() {
						WallpaperManager wallpaperManager = WallpaperManager
								.getInstance(getAndroidContext());
						try {
							mRealtimeWallpaper.realtimeSetWallPaper(handler,
									(float) screenwidth, (float) screenHeight);
							// path = PhotoView.pathString();
							// // bill add begin 在android4.4上能够屏幕设置单屏幕壁纸
							// BitmapFactory.Options options = new
							// BitmapFactory.Options();
							// // options.inSampleSize = 2;
							// Bitmap bmp = BitmapFactory
							// .decodeFile(path, options);
							// wallpaperManager.suggestDesiredDimensions(
							// screenwidth, screenHeight);
							// wallpaperManager.setBitmap(bmp);
							// bmp.recycle();
							// Toast.makeText(getAndroidContext(),
							// R.string.setSuccessful,
							// Toast.LENGTH_SHORT).show();
							// String spKey = "WallpaperCropActivity";
							// SharedPreferences sp =
							// getSharedPreferences(spKey,
							// Context.MODE_PRIVATE);
							// SharedPreferences.Editor editor = sp.edit();
							// editor.putInt(WALLPAPER_WIDTH_KEY, screenwidth);
							// editor.putInt(WALLPAPER_HEIGHT_KEY,
							// screenHeight);
							// editor.commit();
							// // bill add end
							// // dialog.dismiss();
							// handler.sendEmptyMessage(2);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};
				thread.start();
			}
		});
	}

	// bill add end 20150115 调整壁纸预览布局，添加设置布局按钮
	public void InitPhotoView() {
		photoBar = (RelativeLayout) RelativeLayout.inflate(this,
				R.layout.photobarforgallery, null);
		setListener();
	}

	public void initCameraButtonView() {
		Log.d("scroll", "CameraButton");
		cameraButtonView = (RelativeLayout) RelativeLayout.inflate(this,
				R.layout.gallery_camera_button, null);
		mShutterButton = (ShutterButton) cameraButtonView
				.findViewById(R.id.btn_shutter);
		mAlbumButton = (ImageView) cameraButtonView
				.findViewById(R.id.btn_shutter_album);
		mConversionButton = (ImageView) cameraButtonView
				.findViewById(R.id.btn_shutter_conversion);
		mLayout_Album = (FrameLayout) findViewById(R.id.layout_album);
		mLayout_Conversion = (FrameLayout) findViewById(R.id.layout_conversion);
		MainShutterClickListener shutterClickListener = new MainShutterClickListener();
		mShutterButton.setOnClickListener(shutterClickListener);
		// mShutterButton.setOnLongClickListener(shutterClickListener);
		// mShutterButton.setOnTouchListener(shutterClickListener);
		// mShutterButton.setSlideListener(new MainShutterSlideListener());
		mAlbumButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// bill add begin 进入相册
				String cameraPath = "";
				String path = ((GalleryApp) getApplication()).getDataManager()
						.getTopSetPath(DataManager.INCLUDE_IMAGE);
				mAlbumSetMediaSet = ((GalleryApp) getApplication())
						.getDataManager().getMediaSet(path);
				for (int i = 0; i < mAlbumSetMediaSet.getSubMediaSetCount(); i++) {
					if (mAlbumSetMediaSet.getSubMediaSet(i).getName()
							.equals("Camera")) {
						cameraPath = mAlbumSetMediaSet.getSubMediaSet(i)
								.getPath().toString();
						break;
					}
				}
				Intent intent = new Intent();
				intent.putExtra("path", cameraPath);
				intent.setClass(getApplication(), AlbumCameraActivity.class);
				startActivity(intent);
				isAlbumReturn = true;
				// bill add end 进入相册
				// Toast.makeText(Gallery.this, "进入相册",
				// Toast.LENGTH_SHORT).show();
			}
		});
		mConversionButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Toast.makeText(Gallery.this, "转换摄像头",
				// Toast.LENGTH_SHORT).show();
				mIsCamSwitching = true;
				if (mCamManager.getCurrentFacing() == Camera.CameraInfo.CAMERA_FACING_FRONT) {
					mCamManager.open(Camera.CameraInfo.CAMERA_FACING_BACK);
				} else {
					mCamManager.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
				}
			}
		});
	}

	// yongnan add over

	// lianglei begin
	public void deleteCurrentPhoto() {
		Log.v("lianglei", "del");
		MediaItem current = PhotoView.mModell.getCurrentMediaItem();
		Path path = current.getPath();
		mSelectionManager.deSelectAll();
		mSelectionManager.toggle(path);
		mMenuExecutor.startAction(R.id.action_confirm_delete, R.string.delete,
				listener);
		Toast.makeText(getAndroidContext(), R.string.delSuccessful,
				Toast.LENGTH_SHORT).show();
	}

	public void addFav() {
		MobclickAgent.onEvent(this, "AddFav"); // 友盟统计
		mCollectFolder = new CollectFolder();
		mCollectFolder.addImageToFolder(PhotoView.pathString(),
				getAndroidContext());
	}

	// lianglei end
	// bill add 1218 begin
	@Override
	public void onChangeCollectStutas(Object object) {
		// TODO Auto-generated method stub
		absolutePath = (String) object;
		if (absolutePath != null) {
			if (CollectFolder.isExistInCollect(absolutePath)) {
				collect.setImageResource(R.drawable.ty_ic_havebeenin_normal);
			} else {
				collect.setImageResource(R.drawable.ty_ic_collection_normal);
			}
		}
	}

	public void changeCollectStatus() {
		collect.setImageResource(R.drawable.ty_ic_havebeenin_normal);
	}

	// bill add 1218 end
	public boolean dialogPhotoBarIsShowing() {
		if (dialogPhotoBar != null) {
			return dialogPhotoBar.isShowing();
		}
		return false;
	}

	class StatusListener implements OnTouchListener {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			if (v.getId() == R.id.collect) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					collect.setImageResource(R.drawable.ty_ic_collection_press);
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					if (absolutePath != null) {
						if (CollectFolder.isExistInCollect(absolutePath)) {
							CollectFolder.deleteCollectImage(absolutePath,
									getApplication());
							collect.setImageResource(R.drawable.ty_ic_collection_normal);
							Toast.makeText(getApplication(),
									R.string.canclecollect, Toast.LENGTH_SHORT)
									.show();
							// Toast.makeText(getApplication(),
							// R.string.notrepeatFav,
							// Toast.LENGTH_SHORT).show();
						} else {
							collect.setImageResource(R.drawable.ty_ic_collection_normal);
							addFav();
						}
					}
				}
			}
			return true;
		}
	}

	public void setRealtimeWallpaper(RealtimeWallpaper realtimeWallpaper) {
		mRealtimeWallpaper = realtimeWallpaper;
	}
	// bill add end 1218
}
