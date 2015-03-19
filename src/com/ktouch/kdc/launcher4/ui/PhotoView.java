/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.ktouch.kdc.launcher4.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.R.integer;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.Toast;

import com.ktouch.kdc.launcher4.R;
import com.ktouch.kdc.launcher4.app.CollectFolder;
import com.ktouch.kdc.launcher4.app.Gallery;
import com.ktouch.kdc.launcher4.app.Gallery.RealtimeWallpaper;
import com.ktouch.kdc.launcher4.app.GalleryActivity;
import com.ktouch.kdc.launcher4.app.PhotoPage;
import com.ktouch.kdc.launcher4.app.TemplateActivity;
import com.ktouch.kdc.launcher4.data.Path;
import com.ktouch.kdc.launcher4.launcher2.Launcher;
import com.ktouch.kdc.launcher4.launcher2.Launcher.ImageSaveSucess;
import com.ktouch.kdc.launcher4.model.MoveItem;
import com.ktouch.kdc.launcher4.ui.PositionRepository.Position;
import com.ktouch.kdc.launcher4.util.DBUtils_openHelper;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;

public class PhotoView extends GLView implements ImageSaveSucess,RealtimeWallpaper {
	// private static final String TAG = "PhotoView";

	// lianglei begin
	private float imageH, imageW; // 图片宽高
	private float y1, y2;// 下拉时第一点坐标和下滑距离点坐标的y值
	private boolean flagMoveY = false;// 判断滑动方向
	private boolean photoMoveUP = false;
	private boolean photoMoveDown = false;
	private boolean photoMoveStay = false;
	private boolean flagZoomed = false;// 判断是否在放大状态
	private String slideText;// 滑动的时候显示的文字
	private ResourceTexture BTdownSlide;// 用来设置下拉时的图片
	private ResourceTexture BTupSlide;// 用来设置上滑时的图片
	private CollectFolder mCollectFolder;// 服务于下拉收藏功能
	private boolean firstOnScroll = true;// 用来进行onScroll方法计数 在onUp处重置
	public static PhotoPage.Model mModell;
	private int mCount = 0;// 控制上下滑动时模板切换的角标
	public static int showPosition;
	// lianglei end

	public static final int INVALID_SIZE = -1;

	private static final int MSG_TRANSITION_COMPLETE = 1;
	private static final int MSG_SHOW_LOADING = 2;

	private static final long DELAY_SHOW_LOADING = 250; // 250ms;

	private static final int TRANS_NONE = 0;
	private static final int TRANS_SWITCH_NEXT = 3;
	private static final int TRANS_SWITCH_PREVIOUS = 4;

	public static final int TRANS_SLIDE_IN_RIGHT = 1;
	public static final int TRANS_SLIDE_IN_LEFT = 2;
	public static final int TRANS_OPEN_ANIMATION = 5;

	private static final int LOADING_INIT = 0;
	private static final int LOADING_TIMEOUT = 1;
	private static final int LOADING_COMPLETE = 2;
	private static final int LOADING_FAIL = 3;

	private static final int ENTRY_PREVIOUS = 0;
	private static final int ENTRY_NEXT = 1;

	private static final int IMAGE_GAP = 96;
	private static final int SWITCH_THRESHOLD = 256;
	private static final float SWIPE_THRESHOLD = 300f;

	private static final float DEFAULT_TEXT_SIZE = 20;

	// private Path mPhotoViewPath;

	public interface PhotoTapListener {
		public void onSingleTapUp(int x, int y);
	}

	// the previous/next image entries
	private final ScreenNailEntry mScreenNails[] = new ScreenNailEntry[2];

	private final ScaleGestureDetector mScaleDetector;
	private final GestureDetector mGestureDetector;
	private final DownUpDetector mDownUpDetector;

	private PhotoTapListener mPhotoTapListener;

	private final PositionController mPositionController;

	private Model mModel;
	private StringTexture mLoadingText;
	private StringTexture mNoThumbnailText;
	private int mTransitionMode = TRANS_NONE;
	private final TileImageView mTileView;
	private EdgeView mEdgeView;
	private Texture mVideoPlayIcon;

	private boolean mShowVideoPlayIcon;
	private ProgressSpinner mLoadingSpinner;

	private SynchronizedHandler mHandler;

	private int mLoadingState = LOADING_COMPLETE;

	private int mImageRotation;

	private Path mOpenedItemPath;
	private GalleryActivity mActivity;
	public static PhotoView photoView = null;
	public static List<MoveItem> showImg = new ArrayList<MoveItem>();
	private DbUtils dbUtils;
    //bill 20150126 begin 设置壁纸所见即所得
	protected static final String WALLPAPER_WIDTH_KEY = "wallpaper.width";
	protected static final String WALLPAPER_HEIGHT_KEY = "wallpaper.height";
	public int pmCurrentX =0;
	public int pmCurrentY =0;
	public float pmCurrentScale =0;
	public float mScaleMin =0;
	public float mScreenScale =0;
	public static int mOffsetX=0;
	public static int mOffsety=0;
    private static final int DEFAULT_COMPRESS_QUALITY = 90;
	//bill 20150126 end
	public PhotoView(GalleryActivity activity) {
		photoView = this;
		mActivity = activity;
		mTileView = new TileImageView(activity);
		addComponent(mTileView);
		Context context = activity.getAndroidContext();
		mEdgeView = new EdgeView(context);
		addComponent(mEdgeView);
		mLoadingSpinner = new ProgressSpinner(context);
		mLoadingText = StringTexture.newInstance(
				context.getString(R.string.loading), DEFAULT_TEXT_SIZE,
				Color.WHITE);
		mNoThumbnailText = StringTexture.newInstance(
				context.getString(R.string.no_thumbnail), DEFAULT_TEXT_SIZE,
				Color.WHITE);

		mHandler = new SynchronizedHandler(activity.getGLRoot()) {
			@Override
			public void handleMessage(Message message) {
				switch (message.what) {
				case MSG_TRANSITION_COMPLETE: {
					onTransitionComplete();
					break;
				}
				case MSG_SHOW_LOADING: {
					if (mLoadingState == LOADING_INIT) {
						// We don't need the opening animation
						mOpenedItemPath = null;

						mLoadingSpinner.startAnimation();
						mLoadingState = LOADING_TIMEOUT;
						invalidate();
					}
					break;
				}
				default:
					throw new AssertionError(message.what);
				}
			}
		};

		mGestureDetector = new GestureDetector(context,
				new MyGestureListener(), null, true /* ignoreMultitouch */);
		mScaleDetector = new ScaleGestureDetector(context,
				new MyScaleListener());
		mDownUpDetector = new DownUpDetector(new MyDownUpListener());

		for (int i = 0, n = mScreenNails.length; i < n; ++i) {
			mScreenNails[i] = new ScreenNailEntry();
		}

		mPositionController = new PositionController(this, context, mEdgeView);
		mVideoPlayIcon = new ResourceTexture(context,
				R.drawable.ic_control_play);
		// lianglei begin
		BTupSlide = new ResourceTexture(mActivity.getAndroidContext(),
				R.drawable.ty_preview_wallpaper);
		BTdownSlide = new ResourceTexture(mActivity.getAndroidContext(),
				R.drawable.ty_preview_wallpaper);
		// bill add 1027 解决内存溢出点击图片预览多次崩溃，将图片压缩后图片显示变小问题，这些图片设置标记不进行压缩
		BTupSlide.setInSamleFlag(false);
		BTdownSlide.setInSamleFlag(false);
		// lianglei end
		Launcher.setFreshListener(this);
		// yongnan add
		showImg.clear();
		if(showImg==null||showImg.size()==0){
		MoveItem itemNone = createNoneMoveItem("assets/img/choice_nomap_min.png",
				"assets/img/choice_nomap_min.png", R.drawable.nothing, 1, 1,
				context.getString(R.string.template_kt_none), false, false,
				false, true, false);
		MoveItem itemAdd = createNoneMoveItem("assets/img/choice_addto_normal_min.png",
				"assets/img/choice_addto_normal_min.png",
				R.drawable.choice_addto_normal, 4, 4,
				context.getString(R.string.template_kt_add), true, false, false,
				true, false);
		
		showImg.add(itemNone);
		dbUtils = DBUtils_openHelper.getInstance(context).getDb();
		try {
			List<MoveItem> seclectImg = dbUtils.findAll(Selector.from(MoveItem.class).where(
					"flag_selected", "=", true));
			showImg.addAll(seclectImg);
		} catch (DbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		showImg.add(itemAdd);
		}
		Gallery.getGalleryIntance().setRealtimeWallpaper(this);
	}

	public void setModel(Model model) {
		if (mModel == model)
			return;
		mModel = model;
		mTileView.setModel(model);
		if (model != null)
			notifyOnNewImage();
	}

	// lianglei begin
	public void setModelNew(PhotoPage.Model model) {
		mModell = model;
	}

	// lianglei end

	public void setPhotoTapListener(PhotoTapListener listener) {
		mPhotoTapListener = listener;
	}

	private boolean setTileViewPosition(int centerX, int centerY, float scale) {
		// lianglei begin 修改主要是获取当前图片长宽
		// int inverseX = mPositionController.getImageWidth() - centerX;
		// int inverseY = mPositionController.getImageHeight() - centerY;
		imageH = mPositionController.getImageHeight();
		imageW = mPositionController.getImageWidth();
		int inverseX = (int) (imageW - centerX);
		int inverseY = (int) (imageH - centerY);
		// lianglei end
		TileImageView t = mTileView;
		int rotation = mImageRotation;
		// lianglei begin 让横屏图片横屏显示
		// if (imageH<imageW) {
		// return t.setPosition(centerX, centerY, getWidth()/imageH, 270);
		// }else {
		// lianglei end
		switch (rotation) {
		case 0:
			return t.setPosition(centerX, centerY, scale, 0);
		case 90:
			return t.setPosition(centerY, inverseX, scale, 90);
		case 180:
			return t.setPosition(inverseX, inverseY, scale, 180);
		case 270:
			return t.setPosition(inverseY, centerX, scale, 270);
		default:
			throw new IllegalArgumentException(String.valueOf(rotation));
		}
		// }
	}

	public void setPosition(int centerX, int centerY, float scale) {
		if (setTileViewPosition(centerX, centerY, scale)) {
			layoutScreenNails();
		}
	}

	private void updateScreenNailEntry(int which, ImageData data) {
		if (mTransitionMode == TRANS_SWITCH_NEXT
				|| mTransitionMode == TRANS_SWITCH_PREVIOUS) {
			// ignore screen nail updating during switching
			return;
		}
		ScreenNailEntry entry = mScreenNails[which];
		if (data == null) {
			entry.set(false, null, 0);
		} else {
			entry.set(true, data.bitmap, data.rotation);
		}
	}

	// -1 previous, 0 current, 1 next
	public void notifyImageInvalidated(int which) {
		switch (which) {
		case -1: {
			updateScreenNailEntry(ENTRY_PREVIOUS, mModel.getPreviousImage());
			layoutScreenNails();
			invalidate();
			break;
		}
		case 1: {
			updateScreenNailEntry(ENTRY_NEXT, mModel.getNextImage());
			layoutScreenNails();
			invalidate();
			break;
		}
		case 0: {
			// mImageWidth and mImageHeight will get updated
			mTileView.notifyModelInvalidated();
			mImageRotation = mModel.getImageRotation();
			if (((mImageRotation / 90) & 1) == 0) {
				mPositionController.setImageSize(mTileView.mImageWidth,
						mTileView.mImageHeight);
			} else {
				mPositionController.setImageSize(mTileView.mImageHeight,
						mTileView.mImageWidth);
			}
			updateLoadingState();
			break;
		}
		}
	}

	private void updateLoadingState() {
		// Possible transitions of mLoadingState:
		// INIT --> TIMEOUT, COMPLETE, FAIL
		// TIMEOUT --> COMPLETE, FAIL, INIT
		// COMPLETE --> INIT
		// FAIL --> INIT
		if (mModel.getLevelCount() != 0 || mModel.getBackupImage() != null) {
			mHandler.removeMessages(MSG_SHOW_LOADING);
			mLoadingState = LOADING_COMPLETE;
		} else if (mModel.isFailedToLoad()) {
			mHandler.removeMessages(MSG_SHOW_LOADING);
			mLoadingState = LOADING_FAIL;
		} else if (mLoadingState != LOADING_INIT) {
			mLoadingState = LOADING_INIT;
			mHandler.removeMessages(MSG_SHOW_LOADING);
			mHandler.sendEmptyMessageDelayed(MSG_SHOW_LOADING,
					DELAY_SHOW_LOADING);
		}
	}

	public void notifyModelInvalidated() {
		if (mModel == null) {
			updateScreenNailEntry(ENTRY_PREVIOUS, null);
			updateScreenNailEntry(ENTRY_NEXT, null);
		} else {
			updateScreenNailEntry(ENTRY_PREVIOUS, mModel.getPreviousImage());
			updateScreenNailEntry(ENTRY_NEXT, mModel.getNextImage());
		}
		layoutScreenNails();

		if (mModel == null) {
			mTileView.notifyModelInvalidated();
			mImageRotation = 0;
			mPositionController.setImageSize(0, 0);
			updateLoadingState();
		} else {
			notifyImageInvalidated(0);
		}
	}

	@Override
	protected boolean onTouch(MotionEvent event) {
		// if (event.getPointerCount() > 1) {
		mScaleDetector.onTouchEvent(event);
		// } else {
		mGestureDetector.onTouchEvent(event);
		mDownUpDetector.onTouchEvent(event);
		// }
		return true;
	}

	@Override
	protected void onLayout(boolean changeSize, int left, int top, int right,
			int bottom) {
		mTileView.layout(left, top, right, bottom);
		mEdgeView.layout(left, top, right, bottom);
		if (changeSize) {
			mPositionController.setViewSize(getWidth(), getHeight());
			for (ScreenNailEntry entry : mScreenNails) {
				entry.updateDrawingSize();
			}
		}
	}

	private static int gapToSide(int imageWidth, int viewWidth) {
		return Math.max(0, (viewWidth - imageWidth) / 2);
	}

	/*
	 * Here is how we layout the screen nails
	 * 
	 * previous current next ___________ ________________ __________ | _______ |
	 * | __________ | | ______ | | | | | | | right->| | | | | | | |
	 * |<-------->|<--left | | | | | | | |_______| | | | |__________| | |
	 * |______| | |___________| | |________________| |__________| | <-->
	 * gapToSide() | IMAGE_GAP + Max(previous.gapToSide(), current.gapToSide)
	 */
	private void layoutScreenNails() {
		int width = getWidth();
		// int height = getHeight();

		// Use the image width in AC, since we may fake the size if the
		// image is unavailable
		RectF bounds = mPositionController.getImageBounds();
		int left = Math.round(bounds.left);
		int right = Math.round(bounds.right);
		int gap = gapToSide(right - left, width);

		// layout the previous image
		ScreenNailEntry entry = mScreenNails[ENTRY_PREVIOUS];

		if (entry.isEnabled()) {
			entry.layoutRightEdgeAt(left
					- (IMAGE_GAP + Math.max(gap, entry.gapToSide())));
		}

		// layout the next image
		entry = mScreenNails[ENTRY_NEXT];
		if (entry.isEnabled()) {
			entry.layoutLeftEdgeAt(right
					+ (IMAGE_GAP + Math.max(gap, entry.gapToSide())));
		}
	}

	@Override
	protected void render(GLCanvas canvas) {
		// PositionController p = mPositionController;

		// Draw the current photo
		if (mLoadingState == LOADING_COMPLETE) {
			super.render(canvas);
		}

		// Draw the previous and the next photo
		if (mTransitionMode != TRANS_SLIDE_IN_LEFT
				&& mTransitionMode != TRANS_SLIDE_IN_RIGHT
				&& mTransitionMode != TRANS_OPEN_ANIMATION) {
			ScreenNailEntry prevNail = mScreenNails[ENTRY_PREVIOUS];
			ScreenNailEntry nextNail = mScreenNails[ENTRY_NEXT];

			if (prevNail.mVisible)
				prevNail.draw(canvas);
			if (nextNail.mVisible)
				nextNail.draw(canvas);
		}

		// Draw the progress spinner and the text below it
		//
		// (x, y) is where we put the center of the spinner.
		// s is the size of the video play icon, and we use s to layout text
		// because we want to keep the text at the same place when the video
		// play icon is shown instead of the spinner.
		int h = getHeight();
		int w = getWidth();
		int x = Math.round(mPositionController.getImageBounds().centerX());
		int y = h / 2;
		int s = Math.min(getWidth(), getHeight()) / 6;
		// lianglei begin
		// if (photoMoveUP) {
		// if (y2 > 50) {// 向下滑动
		// BTdownSlide.draw(canvas, 0, (int) y2 - h, w, h);//内存的解决后,用这句话实现动画
		// } else if (y2 < -50) {// 向上滑动
		// BTupSlide.draw(canvas, 0, h - (int) y2, w, h);//内存的解决后,用这句话实现动画
		// }
		// }
		// lianglei end

		if (mLoadingState == LOADING_TIMEOUT) {
			StringTexture m = mLoadingText;
			ProgressSpinner r = mLoadingSpinner;
			r.draw(canvas, x - r.getWidth() / 2, y - r.getHeight() / 2);
			m.draw(canvas, x - m.getWidth() / 2, y + s / 2 + 5);
			invalidate(); // we need to keep the spinner rotating
		} else if (mLoadingState == LOADING_FAIL) {
			StringTexture m = mNoThumbnailText;
			m.draw(canvas, x - m.getWidth() / 2, y + s / 2 + 5);
		}

		// Draw the video play icon (in the place where the spinner was)
		if (mShowVideoPlayIcon && mLoadingState != LOADING_INIT
				&& mLoadingState != LOADING_TIMEOUT) {
			mVideoPlayIcon.draw(canvas, x - s / 2, y - s / 2, s, s);
		}

		if (mPositionController.advanceAnimation())
			invalidate();
	}

	private void stopCurrentSwipingIfNeeded() {
		// Enable fast sweeping
		if (mTransitionMode == TRANS_SWITCH_NEXT) {
			mTransitionMode = TRANS_NONE;
			mPositionController.stopAnimation();
			switchToNextImage();
		} else if (mTransitionMode == TRANS_SWITCH_PREVIOUS) {
			mTransitionMode = TRANS_NONE;
			mPositionController.stopAnimation();
			switchToPreviousImage();
		}
	}

	private boolean swipeImages(float velocity) {
		if (mTransitionMode != TRANS_NONE
				&& mTransitionMode != TRANS_SWITCH_NEXT
				&& mTransitionMode != TRANS_SWITCH_PREVIOUS)
			return false;

		ScreenNailEntry next = mScreenNails[ENTRY_NEXT];
		ScreenNailEntry prev = mScreenNails[ENTRY_PREVIOUS];

		int width = getWidth();

		// If we are at the edge of the current photo and the sweeping velocity
		// exceeds the threshold, switch to next / previous image.
		PositionController controller = mPositionController;
		boolean isMinimal = controller.isAtMinimalScale();

		if (velocity < -SWIPE_THRESHOLD
				&& (isMinimal || controller.isAtRightEdge())) {
			stopCurrentSwipingIfNeeded();
			if (next.isEnabled()) {
				mTransitionMode = TRANS_SWITCH_NEXT;
				controller.startHorizontalSlide(next.mOffsetX - width / 2);
				return true;
			}
		} else if (velocity > SWIPE_THRESHOLD
				&& (isMinimal || controller.isAtLeftEdge())) {
			stopCurrentSwipingIfNeeded();
			if (prev.isEnabled()) {
				mTransitionMode = TRANS_SWITCH_PREVIOUS;
				controller.startHorizontalSlide(prev.mOffsetX - width / 2);
				return true;
			}
		}

		return false;
	}

	public boolean snapToNeighborImage() {
		if (mTransitionMode != TRANS_NONE)
			return false;

		ScreenNailEntry next = mScreenNails[ENTRY_NEXT];
		ScreenNailEntry prev = mScreenNails[ENTRY_PREVIOUS];

		int width = getWidth();
		PositionController controller = mPositionController;

		RectF bounds = controller.getImageBounds();
		int left = Math.round(bounds.left);
		int right = Math.round(bounds.right);
		int threshold = SWITCH_THRESHOLD + gapToSide(right - left, width);

		// If we have moved the picture a lot, switching.
		if (next.isEnabled() && threshold < width - right) {
			mTransitionMode = TRANS_SWITCH_NEXT;
			controller.startHorizontalSlide(next.mOffsetX - width / 2);
			return true;
		}
		if (prev.isEnabled() && threshold < left) {
			mTransitionMode = TRANS_SWITCH_PREVIOUS;
			controller.startHorizontalSlide(prev.mOffsetX - width / 2);
			return true;
		}

		return false;
	}

	private boolean mIgnoreUpEvent = false;

	private class MyGestureListener extends
			GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx,
				float dy) {// lianglei
			if (e2.getPointerCount() == 1) {// 单指操作的时候才进行如下语句
				// 需要判断的变量有四个,firstOnScroll,flagDown,flagMoveY,flagZoomed
				// 分别是判断第一次执行滑动,按下,Y轴移动,放大四个条件
				// 只有第一次滑动的时候才会对其他变量进行赋值
				if (firstOnScroll) {
					if (3 * Math.abs(dx) > Math.abs(dy)) {// 进行Y轴移动的判断
						flagMoveY = false;// 为X轴移动
					} else {
						flagMoveY = true;// 为Y轴移动
					}

					if (mPositionController.fling(e1.getX(), e1.getY())) {// 进行是否为zoomed判断
						flagZoomed = false;// 为放大状态
					} else {
						flagZoomed = true;// 为正常状态
					}

					if (flagMoveY && flagZoomed && showImg != null) {// y轴移动且不是放大状态
						y1 = e1.getY();
						y2 = (e2.getY() - y1);
						if ((e2.getY() - y1) > 0) {// 向下移动
							mCount++;
							if (mCount >= showImg.size()-1) {
								mCount = 0;
							}
						} else if ((e2.getY() - y1) < 0) {// 向上移动
							mCount--;
							if (mCount < 0) {
								mCount = showImg.size() - 2;
							}
						}
					}
				}
				if (flagMoveY && flagZoomed) {// y轴移动且不是放大状态
					if (null != showImg) {
						y1 = e1.getY();
						y2 = (e2.getY() - y1);
						if ((e2.getY() - y1) > 50) {// 向下移动
							showPosition = mCount;
							photoMoveUP = false;
							photoMoveDown = true;
							photoMoveStay = false;
							if (showImg == null || showImg.size() <= 1) {
								// do nothing
							} else {
								if (showImg.get(mCount).isFlag_resouce()) {
									PhotoPage.setImageview(showImg.get(mCount)
											.getImg_normal_int());
								} else {
									PhotoPage.setImageview(showImg.get(mCount)
											.getImgurl());
								}
							}
						} else if ((e2.getY() - y1) < -50) {// 向上移动
							showPosition = mCount;
							photoMoveUP = true;
							photoMoveDown = false;
							photoMoveStay = false;
							if (showImg == null || showImg.size() <= 1) {
								// do nothing
							} else {
								if (showImg.get(mCount).isFlag_resouce()) {
									PhotoPage.setImageview(showImg.get(mCount)
											.getImg_normal_int());
								} else {
									PhotoPage.setImageview(showImg.get(mCount)
											.getImgurl());
								}
							}
						} else {// 未到可移动的阈值
							photoMoveUP = false;
							photoMoveDown = false;
							photoMoveStay = true;
						}
					}
				} else {// x轴移动,或为缩放状态
					if (mTransitionMode != TRANS_NONE)
						return true;
					ScreenNailEntry next = mScreenNails[ENTRY_NEXT];
					ScreenNailEntry prev = mScreenNails[ENTRY_PREVIOUS];
					mPositionController.startScroll(dx, dy, next.isEnabled(),
							prev.isEnabled());
				}
				firstOnScroll = false;
				return true;
			} else {
				return true;
			}
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			if (mPhotoTapListener != null) {
				mPhotoTapListener.onSingleTapUp((int) e.getX(), (int) e.getY());
			}
			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			if (Math.abs(velocityX) < Math.abs(velocityY)) {
				// do nothing
			} else {
				if (swipeImages(velocityX)) {
					mIgnoreUpEvent = true;
				} else if (mTransitionMode != TRANS_NONE) {
					// do nothing
				} else if (mPositionController.fling(velocityX, velocityY)) {
					mIgnoreUpEvent = true;
				}
			}
			return true;
		}

		// lianglei begin
		@Override
		public void onLongPress(MotionEvent e) {
			if (mPositionController.fling(e.getX(), e.getY())) {
				// do nothing
			} else {
				Dialog(mActivity.getAndroidContext());
			}
			super.onLongPress(e);
		}

		// 长按弹出的Dialog
		public void Dialog(Context context) {
//			final String[] arrayFruit = new String[] {
//					mActivity.getAndroidContext().getResources()
//							.getString(R.string.template_kt_none),
//					mActivity.getAndroidContext().getResources()
//							.getString(R.string.template_kt_lock_normal),
//					mActivity.getAndroidContext().getResources()
//							.getString(R.string.template_kt_normal),
//					mActivity.getAndroidContext().getResources()
//							.getString(R.string.template_kt_icon),
//					mActivity.getAndroidContext().getResources()
//							.getString(R.string.template_kt_add) };
			ConfirmDialog mDialog = new ConfirmDialog(context,
					R.style.shareDialogTheme, showImg);
			mDialog.show();
		}

		// lianglei end

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			// lianglei begin
			// if (mTransitionMode != TRANS_NONE)
			// return true;
			// lianglei end
			Gallery.getGalleryIntance().CameraViewOpen();
			// PositionController controller = mPositionController;
			// float scale = controller.getCurrentScale();
			// // onDoubleTap happened on the second ACTION_DOWN.
			// // We need to ignore the next UP event.
			// mIgnoreUpEvent = true;
			//
			// // lianglei add
			//
			// // if (scale <= 1.0f || controller.isAtMinimalScale()) {
			// // controller.zoomIn(e.getX(), e.getY(),
			// // Math.max(1.5f, scale * 1.5f));
			// // } else {
			// // controller.resetToFullView();
			// // }
			//
			// if (imageW / imageH <= 1) {
			// if (scale <= 1.0f || controller.isAtMinimalScale()) {
			// controller.zoomIn(e.getX(), e.getY(),
			// Math.max(1.5f, scale * 1.5f));
			// } else {
			// controller.resetToFullView();
			// }
			// } else {
			// if (scale <= 0.90f || controller.isAtMinimalScale()) {
			// controller.zoomIn(e.getX(), e.getY(), getHeight() / imageH);
			// } else {
			// controller.resetToFullView();
			// }
			// }
			// lianglei End
			return true;
		}
	}

	private class MyScaleListener extends
			ScaleGestureDetector.SimpleOnScaleGestureListener {

		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			float scale = detector.getScaleFactor();
			if (Float.isNaN(scale) || Float.isInfinite(scale)
					|| mTransitionMode != TRANS_NONE)
				return true;
			mPositionController.scaleBy(scale, detector.getFocusX(),
					detector.getFocusY());
			return true;
		}

		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			if (mTransitionMode != TRANS_NONE)
				return false;

			mPositionController.beginScale(detector.getFocusX(),
					detector.getFocusY());
			return true;
		}

		@Override
		public void onScaleEnd(ScaleGestureDetector detector) {
			mPositionController.endScale();
			snapToNeighborImage();
		}
	}

	public boolean jumpTo(int index) {
		if (mTransitionMode != TRANS_NONE)
			return false;
		mModel.jumpTo(index);
		return true;
	}

	public void notifyOnNewImage() {
		mPositionController.setImageSize(0, 0);
	}

	public void startSlideInAnimation(int direction) {
		PositionController a = mPositionController;
		a.stopAnimation();
		switch (direction) {
		case TRANS_SLIDE_IN_LEFT:
		case TRANS_SLIDE_IN_RIGHT: {
			mTransitionMode = direction;
			a.startSlideInAnimation(direction);
			break;
		}
		default:
			throw new IllegalArgumentException(String.valueOf(direction));
		}
	}

	private class MyDownUpListener implements DownUpDetector.DownUpListener {
		public void onDown(MotionEvent e) {
		}

		public void onUp(MotionEvent e) {
			firstOnScroll = true;// lianglei 重置
			photoMoveUP = false;// lianglei 重置
			photoMoveDown = false;// lianglei 重置
			photoMoveStay = false;// lianglei 重置

			mEdgeView.onRelease();

			if (mIgnoreUpEvent) {
				mIgnoreUpEvent = false;
				return;
			}
			if (!snapToNeighborImage() && mTransitionMode == TRANS_NONE) {
				mPositionController.up();
			}

			// lianglei begin
			if (e.getPointerCount() != 1) {
				// do nothing
			} else {
				flagMoveY = false;
				try {
					if (slideText.equals(mActivity.getAndroidContext()
							.getResources().getString(R.string.releaseToDel))) {
					} else if (slideText
							.equals(mActivity.getAndroidContext()
									.getResources()
									.getString(R.string.releaseToAddFav))) {
						mCollectFolder = new CollectFolder();
						mCollectFolder.addImageToFolder(pathString(),
								mActivity.getAndroidContext());
						setPosition((int) imageW / 2, (int) imageH / 2,
								mPositionController.getCurrentScale());
						// bill delete
						// Toast.makeText(mActivity.getAndroidContext(),
						// R.string.addFavSuccessful, Toast.LENGTH_SHORT)
						// .show();
					} else {
						setPosition((int) imageW / 2, (int) imageH / 2,
								mPositionController.getCurrentScale());
					}
				} catch (Exception e1) {

				}
			}
			// lianglei end

		}
	}

	private void switchToNextImage() {
		// We update the texture here directly to prevent texture uploading.
		ScreenNailEntry prevNail = mScreenNails[ENTRY_PREVIOUS];
		ScreenNailEntry nextNail = mScreenNails[ENTRY_NEXT];
		mTileView.invalidateTiles();
		if (prevNail.mTexture != null)
			prevNail.mTexture.recycle();
		prevNail.mTexture = mTileView.mBackupImage;
		mTileView.mBackupImage = nextNail.mTexture;
		nextNail.mTexture = null;
		mModel.next();
	}

	private void switchToPreviousImage() {
		// We update the texture here directly to prevent texture uploading.
		ScreenNailEntry prevNail = mScreenNails[ENTRY_PREVIOUS];
		ScreenNailEntry nextNail = mScreenNails[ENTRY_NEXT];
		mTileView.invalidateTiles();
		if (nextNail.mTexture != null)
			nextNail.mTexture.recycle();
		nextNail.mTexture = mTileView.mBackupImage;
		mTileView.mBackupImage = prevNail.mTexture;
		nextNail.mTexture = null;
		mModel.previous();
	}

	public void notifyTransitionComplete() {
		mHandler.sendEmptyMessage(MSG_TRANSITION_COMPLETE);
	}

	private void onTransitionComplete() {
		int mode = mTransitionMode;
		mTransitionMode = TRANS_NONE;

		if (mModel == null)
			return;
		if (mode == TRANS_SWITCH_NEXT) {
			switchToNextImage();
		} else if (mode == TRANS_SWITCH_PREVIOUS) {
			switchToPreviousImage();
		}
	}

	public boolean isDown() {
		return mDownUpDetector.isDown();
	}

	public static interface Model extends TileImageView.Model {
		public void next();

		public void previous();

		public void jumpTo(int index);

		public int getImageRotation();

		// Return null if the specified image is unavailable.
		public ImageData getNextImage();

		public ImageData getPreviousImage();
	}

	public static class ImageData {
		public int rotation;
		public Bitmap bitmap;

		public ImageData(Bitmap bitmap, int rotation) {
			this.bitmap = bitmap;
			this.rotation = rotation;
		}
	}

	private static int getRotated(int degree, int original, int theother) {
		return ((degree / 90) & 1) == 0 ? original : theother;
	}

	private class ScreenNailEntry {
		private boolean mVisible;
		private boolean mEnabled;

		private int mRotation;
		private int mDrawWidth;
		private int mDrawHeight;
		private int mOffsetX;

		private BitmapTexture mTexture;

		public void set(boolean enabled, Bitmap bitmap, int rotation) {
			mEnabled = enabled;
			mRotation = rotation;
			if (bitmap == null) {
				if (mTexture != null)
					mTexture.recycle();
				mTexture = null;
			} else {
				if (mTexture != null) {
					if (mTexture.getBitmap() != bitmap) {
						mTexture.recycle();
						mTexture = new BitmapTexture(bitmap);
					}
				} else {
					mTexture = new BitmapTexture(bitmap);
				}
				updateDrawingSize();
			}
		}

		public void layoutRightEdgeAt(int x) {
			mVisible = x > 0;
			mOffsetX = x - getRotated(mRotation, mDrawWidth, mDrawHeight) / 2;
		}

		public void layoutLeftEdgeAt(int x) {
			mVisible = x < getWidth();
			mOffsetX = x + getRotated(mRotation, mDrawWidth, mDrawHeight) / 2;
		}

		public int gapToSide() {
			return ((mRotation / 90) & 1) != 0 ? PhotoView.gapToSide(
					mDrawHeight, getWidth()) : PhotoView.gapToSide(mDrawWidth,
					getWidth());
		}

		public void updateDrawingSize() {
			if (mTexture == null)
				return;

			int width = mTexture.getWidth();
			int height = mTexture.getHeight();

			// Calculate the initial scale that will used by PositionController
			// (usually fit-to-screen)
			float s = ((mRotation / 90) & 0x01) == 0 ? mPositionController
					.getMinimalScale(width, height) : mPositionController
					.getMinimalScale(height, width);

			mDrawWidth = Math.round(width * s);
			mDrawHeight = Math.round(height * s);
		}

		public boolean isEnabled() {
			return mEnabled;
		}

		public void draw(GLCanvas canvas) {
			int x = mOffsetX;
			int y = getHeight() / 2;

			if (mTexture != null) {
				if (mRotation != 0) {
					canvas.save(GLCanvas.SAVE_FLAG_MATRIX);
					canvas.translate(x, y, 0);
					canvas.rotate(mRotation, 0, 0, 1); // mRotation
					canvas.translate(-x, -y, 0);
				}
				mTexture.draw(canvas, x - mDrawWidth / 2, y - mDrawHeight / 2,
						mDrawWidth, mDrawHeight);
				if (mRotation != 0) {
					canvas.restore();
				}
			}
		}
	}

	public void pause() {
		mPositionController.skipAnimation();
		mTransitionMode = TRANS_NONE;
		mTileView.freeTextures();
		for (ScreenNailEntry entry : mScreenNails) {
			entry.set(false, null, 0);
		}
	}

	public void resume() {
		photoView = this;
		mTileView.prepareTextures();
	}

	public void setOpenedItem(Path itemPath) {
		mOpenedItemPath = itemPath;
	}

	public void showVideoPlayIcon(boolean show) {
		mShowVideoPlayIcon = show;
	}

	// Returns the position saved by the previous page.
	public Position retrieveSavedPosition() {
		if (mOpenedItemPath != null) {
			Position position = PositionRepository.getInstance(mActivity).get(
					Long.valueOf(System.identityHashCode(mOpenedItemPath)));
			mOpenedItemPath = null;
			return position;
		}
		return null;
	}

	public void openAnimationStarted() {
		mTransitionMode = TRANS_OPEN_ANIMATION;
	}

	public boolean isInTransition() {
		return mTransitionMode != TRANS_NONE;
	}

	public static String pathString() {// lianglei 为Gallery类的更多按钮服务
		return mModell
				.getCurrentMediaItem()
				.getDetails()
				.getDetail(
						com.ktouch.kdc.launcher4.data.MediaDetails.INDEX_PATH)
				.toString();
	}

	@Override
	public void refreshImage() {
		DbUtils utils = DBUtils_openHelper.getInstance(
				mActivity.getAndroidContext()).getDb();
		try {
			MoveItem moveItem = utils.findFirst(Selector.from(MoveItem.class)
					.where("text_describe",
							"=",
							mActivity.getAndroidContext().getString(
									R.string.template_kt_normaltime)));
			moveItem.setFlag_resouce(true);
			moveItem.setImgdown("/data/data/com.ktouch.kdc.launcher4/files/screenShot.png");
			moveItem.setImgurl("assets/img/ty_preview_desktop_time.png");
			utils.saveOrUpdate(moveItem);
		} catch (DbException e) {
			e.printStackTrace();
		}
		Intent intent = new Intent();
		intent.setAction("scrollupdata");
		mActivity.getAndroidContext().sendBroadcast(intent);
		PhotoPage
				.setImageview("data/data/com.ktouch.kdc.launcher4/files/screenShot.png");
	}

	private MoveItem createNoneMoveItem(String imgdown, String Imgurl, int url,
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
		return other;
	}

	public static PhotoView getPhotoViewIntance() {
		return photoView;
	}

	public static void setShowList(ArrayList<MoveItem> list) {
		showPosition = 0;
		if (null != list && list.size() > 0) {
			showImg = list;
			if (list.get(0).isFlag_resouce()) {
				PhotoPage.setImageview(list.get(0).getImg_normal_int());
			} else {
				PhotoPage.setImageview(list.get(0).getImgurl());
			}

		}

	}

	public static void setImageView() {
		if (showImg.get(showPosition).isFlag_resouce()) {
			PhotoPage.preview.setImageId(showImg.get(showPosition)
					.getImg_normal_int());
		} else {
			PhotoPage.preview.setImagePath(showImg.get(showPosition)
					.getImgurl());
		}
	}
    //bill add begin 20150126  设置壁纸所见即所得
	@Override
	public void realtimeSetWallPaper(Handler handler,float screenwidth,float screenHeight) {
		// TODO Auto-generated method stub
		boolean originalRecyleFlag = false;
		boolean scaleImageRecyleFlag = false;
		boolean isOriginalImage = false;
		Matrix m = new Matrix();
		m.reset();
		// 将图片按总缩放比例进行缩放
		float width =mModel.getBackupImage().getWidth();
		float height =mModel.getBackupImage().getHeight();
		//对于相册内有些图片getImageWidth()宽高和备份的宽高不一致，但是pmCurrentScale确实相对于getImageWidth()宽高
		//来说的
		String path = PhotoView.pathString();
		if(width*pmCurrentScale == screenwidth){
			isOriginalImage = true;
		}
		float mNewScale=1;
		//以下代码是利用小图设置的
		if(mPositionController.getImageWidth()>width){
			pmCurrentScale = mPositionController.getImageWidth()*pmCurrentScale/width;
		}
		//原图未缩放
		if(pmCurrentScale==0){
			mNewScale = mScaleMin;
			{
				m.postScale(mScaleMin,mScaleMin);
			}
		}
		else{
			//缩放后未达到屏幕高度
			if(height *pmCurrentScale<=screenHeight){
				mNewScale = pmCurrentScale;	
				m.postScale(pmCurrentScale,pmCurrentScale);
			}
			else{
				if(pmCurrentScale<3){
					mNewScale = pmCurrentScale*mScaleMin;
					m.postScale(pmCurrentScale*mScaleMin,pmCurrentScale*mScaleMin);
				}
				else{
					mNewScale = pmCurrentScale;
					m.postScale(pmCurrentScale,pmCurrentScale);
				}
			}
		}
		//以下是改为清晰的大图显示比例，在小图的基础上修改
		m.reset();
		m.postScale(mNewScale, mNewScale);
		int newwidth = 0;
		int newheight =0;

		try{
			newwidth=(int) (width*mNewScale);
			newheight=(int) (height*mNewScale);
		}
		catch (java.lang.OutOfMemoryError e) {
		     System.gc();
		     return;
		}
		//根据放大后不同情况对偏移值进行设置
		if(PhotoView.mOffsetX+screenwidth>newwidth && newwidth>screenwidth){
			PhotoView.mOffsetX = (int) (newwidth-screenwidth);
		}
		if(PhotoView.mOffsety+screenHeight>newheight && newheight>=screenHeight){
			if(newheight>=screenHeight){
				PhotoView.mOffsety = (int) (newheight-screenHeight);
			}
			else{
				PhotoView.mOffsety= 0;
			}
		}
		if(newheight<=screenHeight){
			PhotoView.mOffsety = 0;
		}
		Bitmap bitmap1 = null;
		int cropwidth =0;
		int cropheight = 0;
		//设置裁剪的尺寸
		if(newheight<=screenHeight){
			if(newwidth<=screenwidth){
				PhotoView.mOffsetX = 0;
				cropwidth = newwidth;
				cropheight = newheight;
			}
			else{
				cropwidth = (int) screenwidth;
				cropheight = newheight;
			}
		}
		else{
			cropwidth = (int) screenwidth;
			cropheight = (int)screenHeight;
		}
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		int imageWidthScale =(int) (options.outWidth/screenwidth);

		BitmapFactory.Options options1 = new BitmapFactory.Options();
		//对宽度大于屏幕2倍的大图的处理
		if(imageWidthScale>2){
			options1.inSampleSize =4;
			//根据屏幕中图像放大的倍数设置inSampleSize，放大率越大，inSampleSize越小，目的是让图片保持清晰
			if((float)((float)newwidth/(float)(options.outWidth))>=1){

				options1.inSampleSize =1;
			}
			else if((float)((float)newwidth/(float)(options.outWidth))>=0.5){

				options1.inSampleSize =2;
			}
		}
		float fullScale =1;
		//未进行缩放，设置壁纸采用原图
		if(pmCurrentScale==0){
			options1.inSampleSize =1;		
		}
		Bitmap bmp111 = BitmapFactory.decodeFile(path, options1);
			fullScale =(float)newwidth/(float)bmp111.getWidth();
		//进行放大后执行
		if(pmCurrentScale!=0){
			PhotoView.mOffsetX = (int) (PhotoView.mOffsetX/fullScale);
			PhotoView.mOffsety = (int) (PhotoView.mOffsety/fullScale);

			bitmap1=Bitmap.createBitmap(bmp111, PhotoView.mOffsetX, PhotoView.mOffsety, (int)(cropwidth/fullScale),(int)(cropheight/fullScale));				
		}
		else{
			bitmap1 = bmp111;   //将原图直接设置壁纸
		}
		String newFileName = Environment.getExternalStorageDirectory().getPath()+"/"+"temp.png";
		File file = new File(newFileName);
		if (file.exists()) {
			try {
				file.delete();
				file.createNewFile();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
		}
		try {
			WallpaperManager wallpaperManager = WallpaperManager
					.getInstance(mActivity.getAndroidContext());
				// bill add begin 在android4.4上能够屏幕设置单屏幕壁纸
				try {
					wallpaperManager.suggestDesiredDimensions(
							(int)screenwidth, (int)screenHeight);
				    ByteArrayOutputStream tmpOut = new ByteArrayOutputStream(2048);
				    bitmap1.compress(Bitmap.CompressFormat.PNG,DEFAULT_COMPRESS_QUALITY, tmpOut);
					wallpaperManager.setStream(new ByteArrayInputStream(tmpOut
                            .toByteArray()));
					//wallpaperManager.setBitmap(bitmap1);
					String spKey = "WallpaperCropActivity";
					SharedPreferences sp = mActivity.getAndroidContext().getSharedPreferences(spKey,
							Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = sp.edit();
					editor.putInt(WALLPAPER_WIDTH_KEY, (int)screenwidth);
					editor.putInt(WALLPAPER_HEIGHT_KEY, (int)screenHeight);
					editor.commit();
					// bill add end
					// dialog.dismiss();
					handler.sendEmptyMessage(2);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			FileOutputStream fos = new FileOutputStream(file);
			boolean result = bitmap1.compress(Bitmap.CompressFormat.PNG, 100, fos);
			if (result) {
				//Toast.makeText(mActivity.getAndroidContext(), "已保存图片", Toast.LENGTH_SHORT).show();
			}
			bmp111.recycle();
			bitmap1.recycle();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
    //bill add begin 20150204  设置壁纸所见即所得
}
