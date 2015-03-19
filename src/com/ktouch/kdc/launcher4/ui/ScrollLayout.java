package com.ktouch.kdc.launcher4.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.security.auth.PrivateCredentialPermission;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.Selection;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.Scroller;
import android.widget.Toast;

import com.ktouch.kdc.launcher4.R;
import com.ktouch.kdc.launcher4.R.color;
import com.ktouch.kdc.launcher4.adapter.ScrollAdapter;
import com.ktouch.kdc.launcher4.adapter.ScrollAdapter.OnDataChangeListener;
import com.ktouch.kdc.launcher4.app.ChooseTemplateActivity;
import com.ktouch.kdc.launcher4.app.Log;
import com.ktouch.kdc.launcher4.app.PhotoPage;
import com.ktouch.kdc.launcher4.app.Gallery;
import com.ktouch.kdc.launcher4.app.TemplateActivity;
import com.ktouch.kdc.launcher4.launcher2.Launcher;
import com.ktouch.kdc.launcher4.launcher2.Launcher.ImageSaveSucess;
import com.ktouch.kdc.launcher4.model.MoveItem;
import com.ktouch.kdc.launcher4.util.DBUtils_openHelper;
import com.ktouch.kdc.launcher4.util.DensityUtil;
import com.ktouch.kdc.launcher4.util.FileUtil;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;

/**
 * 
 * @author 朱永男
 * 
 */
@SuppressLint("UseSparseArrays")
public class ScrollLayout extends ViewGroup implements OnDataChangeListener,
		ImageSaveSucess {

	// 容器的Adapter
	private ScrollAdapter mAdapter;
	// 左边距
	private int leftPadding = 0;
	// 右边距
	private int rightPadding = 0;
	// 上边距
	private int topPadding = 0;
	// 下边距
	private int bottomPadding = 0;

	// 每个Item图片宽度的半长，用于松下手指时的动画
	private int halfBitmapWidth;
	// 同上
	private int halfBitmapHeight;

	// 动态设置行数
	private int rowCount = 1;
	// 动态设置列数
	private int colCount = 1;
	// 每一页的Item总数
	private int itemPerPage = 1;

	// 行间距
	private int rowSpace = 0;
	// 列间距
	private int colSpace = 0;

	// item的宽度
	private int childWidth = 0;
	// item的高度
	private int childHeight = 0;

	// 手机屏幕宽度
	private int screenWidth = 0;
	// 手机屏幕高度
	private int screenHeight = 0;

	// 总Item数
	private int totalItem = 0;
	// 总页数
	private int totalPage = 0;
	// 当前屏数
	private int mCurScreen;
	// 默认屏数为0，即第一屏
	private int mDefaultScreen = 0;

	// 上次位移滑动到的X坐标位置
	private float mLastMotionX;
	// 上次位移滑动到的Y坐标位置
	private float mLastMotionY;

	// 拖动点的X坐标（加上当前屏数 * screenWidth）
	private int dragPointX;
	// 拖动点的Y坐标
	private int dragPointY;
	// X坐标偏移量
	private int dragOffsetX;
	// Y坐标偏移量
	private int dragOffsetY;

	// 拖拽点的位置编号，每个Item对应一个位置编号，自增
	private int dragPosition = -1;

	// 临时交换位置的编号
	private int temChangPosition = -1;

	// window管理器，负责随手势显示拖拽View
	private WindowManager windowManager;
	private WindowManager.LayoutParams windowParams;

	// 拖拽Item的子View
	private ImageView dragImageView;
	// 拖拽View对应的位图
	private Bitmap dragBitmap;

	// 页面滚动的Scroll管理器
	private Scroller mScroller;

	// 三种滑动状态，默认为静止状态
	private int Mode_Free = 0; // 静止状态
	private int Mode_Drag = 1; // 当前页面下，拖动状态
	private int Mode_Scroll = 2; // 跨页面滚动状态
	private int Mode = Mode_Free;

	// 手势落下的X坐标
	private int startX = 0;
	private int startY = 0;

	// 编辑状态标识
	private boolean isEditting;

	private Context mContext;

	// Container的背景
	private Bitmap background;
	// 背景绘制的Paint
	private Paint paint = new Paint();

	// 系列动画执行完成标识的集合
	private HashMap<Integer, Boolean> animationMap = new HashMap<Integer, Boolean>();

	// 用来判断滑动到哪一个item的位置
	private Rect frame;

	// 页面滑动的监听
	private OnPageChangedListener pageChangedListener;
	// 删除或增加页面的监听
	private OnAddOrDeletePage onAddPage;
	// Container编辑模式的监听
	private OnEditModeListener onEditModeListener;
	// 被拖拽的视图
	private View viewDrag;
	// 判断是否是点击
	private boolean isClick;
	// 图片路径或id
	private String imgPath;
	private int imgId;
	// 实时截屏路径
	private static final String IMGPATHTIME = "/data/data/com.ktouch.kdc.launcher4/files/screenShot.png";
	private MoveItem moveItemSave;
	private static int moveItemPosition;
	private DbUtils utils;
	// Dragging
	protected int dragged = -1, lastX = -1, lastY = -1, lastTarget = -1;
	protected boolean enabled = true, touching = false, deleting = false;
	private Paint paintLine;
	private int scroll = 0;
	private ArrayList<MoveItem> showImgList;
	private ArrayList<View> showList;
	private ArrayList<View> removeList;
	private ArrayList<MoveItem> removeItemList;

	public ScrollLayout(Context context) {
		super(context);
		init(context);
	}

	public ScrollLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public ScrollLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	// 初始化成员变量，同时设置OnClick监听
	private void init(Context context) {
		this.mContext = context;
		paintLine = new Paint();
		paintLine.setColor(mContext.getResources().getColor(R.color.line));
		paintLine.setStrokeWidth(2.0f); 
		paint.setColor(Color.WHITE);
		this.mScroller = new Scroller(context);

		this.mCurScreen = mDefaultScreen;

		this.rightPadding = DensityUtil.dip2px(mContext, 1);// 70
		this.leftPadding = DensityUtil.dip2px(mContext, 1);// 10
		this.topPadding = DensityUtil.dip2px(mContext, 5);// 30
		this.bottomPadding = DensityUtil.dip2px(mContext, 5);// 30
		this.colSpace = 0;// DensityUtil.dip2px(mContext, 5)-2;// 15
		this.rowSpace = DensityUtil.dip2px(mContext, 5);// 15
		if (mAdapter != null)
			refreView();

		// this.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// showEdit(false);
		// }
		// });
		showImgList = new ArrayList<MoveItem>();
		MoveItem none = createNoneMoveItem(
				"assets/img/choice_nomap_min.png",
				"assets/img/choice_nomap_min.png",
				R.drawable.nothing, 1, 1,
				context.getString(R.string.template_kt_none), false, false,
				false, true, false);
		showImgList.add(none);
		showList = new ArrayList<View>();
		removeList = new ArrayList<View>();
		removeItemList = new ArrayList<MoveItem>();
		// bill add
		Launcher.setFreshListener(this);
		utils = DBUtils_openHelper.getInstance(mContext).getDb();
	}

	// 添加一个item
	public void addItemView(MoveItem item) {
		mAdapter.add(item);
		this.addView(getView(mAdapter.getCount() - 1));
		// showEdit(isEditting);
		requestLayout();
	}

	@Override
	public void addView(View child, int index, LayoutParams params) {

		child.setClickable(true);
		try {
			if (child.getVisibility() != View.VISIBLE)
				child.setVisibility(View.VISIBLE);
		} catch (Exception e) {
			e.printStackTrace();
		}

		super.addView(child, index, params);
		int pages = (int) Math.ceil(getChildCount() * 1.0 / itemPerPage);
		if (pages > totalPage) {
			if (this.onAddPage != null)
				onAddPage.onAddOrDeletePage(totalPage, true);
			totalPage = pages;
		}
	}

	// 绘制Container所有item
	public void refreView() {
		removeAllViews();
		showImgList = new ArrayList<MoveItem>();
		MoveItem none = createNoneMoveItem(
				"assets/img/choice_nomap_min.png",
				"assets/img/choice_nomap_min.png",
				R.drawable.nothing, 1, 1,
				mContext.getString(R.string.template_kt_none), false, false,
				false, true, false);
		showImgList.add(none);
		removeList = new ArrayList<View>();
		showList = new ArrayList<View>();
		removeItemList = new ArrayList<MoveItem>();
		for (int i = 0; i < mAdapter.getCount(); i++) {
			this.addView(getView(i));
		}
		totalPage = (int) Math.ceil(getChildCount() * 1.0 / itemPerPage);
		if(!isDeleting()){
			refreshTitleString(false);
		}
		requestLayout();
	}

	@Override
	public void removeView(View view) {
		super.removeView(view);
		int pages = (int) Math.ceil(getChildCount() * 1.0 / itemPerPage);
		if (pages < totalPage) {
			if (this.onAddPage != null)
				onAddPage.onAddOrDeletePage(totalPage, false);
			totalPage = pages;
		}
	}

	@Override
	public void removeViewAt(int index) {
		super.removeViewAt(index);
		int pages = (int) Math.ceil(getChildCount() * 1.0 / itemPerPage);
		if (pages < totalPage) {
			totalPage = pages;
			if (this.onAddPage != null)
				onAddPage.onAddOrDeletePage(totalPage, false);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		final float x = ev.getX();
		final float y = ev.getY();
		int thresholdX = DensityUtil.dip2px(mContext, 8);
		Log.e("scroll", "i donnot kown " + thresholdX);
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			startX = (int) x;
			startY = (int) y;
			if (mScroller.isFinished()) {
				if (!mScroller.isFinished()) {
					mScroller.abortAnimation();
				}
				temChangPosition = dragPosition = pointToPosition((int) x,
						(int) y);
				dragOffsetX = (int) (ev.getRawX() - x);
				dragOffsetY = (int) (ev.getRawY() - y);

				mLastMotionX = x;
				mLastMotionY = y;
				startX = (int) x;
				startY = (int) y;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			int deltaX = (int) (mLastMotionX - x);
			int deltaY = (int) (mLastMotionY - y);
			if (IsCanMove(deltaY) && Math.abs(deltaY) > thresholdX
					&& Mode != Mode_Drag) {
				// if (Mode != Mode_Drag) {
				mLastMotionY = y;
				scrollBy(0, deltaY);
				Mode = Mode_Scroll;
			    Log.e("scroll", "move---"+getHeight());
			}
			
			if(IsCanMove(deltaY)){
				if (getScrollY() <= 0 && deltaY < 0) {
					snapToDistance(0);
					snapToDestination(true);
				}
				if (getScrollY() >= getMaxScroll() && deltaY > 0) {
					snapToDistance(0);
					snapToDestination(false);
				}
			}

			if (Mode == Mode_Drag) {
				onDrag((int) x, (int) y);
			}
			break;
		case MotionEvent.ACTION_UP:
			float distance = ev.getRawY() - startY;
			float distance_click = ev.getY() - startY;
			if (distance_click > screenHeight / 8
					|| distance_click < -screenHeight / 8) {
				isClick = false;
			} else {
				isClick = true;
			}
			// Log.e("scroll",
			// "isclick   " + isClick + ev.getRawY() + "   " + ev.getY());
			if (distance > screenHeight / 6 && mCurScreen > 0
					&& Mode != Mode_Drag) {
				// snapToScreen(mCurScreen - 1);
			} else if (distance < -screenHeight / 6
					&& mCurScreen < totalPage - 1 && Mode != Mode_Drag) {
				// snapToScreen(mCurScreen + 1);
			} else if (Mode != Mode_Drag) {
				// snapToDistance(0);
				// snapToDestination();
				// Log.e("scroll", "not drag");
			}
			if (Mode == Mode_Drag) {
				stopDrag();
				// showEdit(false);
				// showEdit(true);
			}
			if (dragImageView != null) {
				animationMap.clear();
				showDropAnimation((int) x, (int) y);
			}
			startY = 0;
			break;
		case MotionEvent.ACTION_CANCEL:
			// showEdit(false);
		}
		super.dispatchTouchEvent(ev);
		return true;
	}

	// 开始拖动
	private void startDrag(Bitmap bm, int x, int y, View itemView) {
		dragPointX = x - itemView.getLeft() + mCurScreen * screenWidth;
		dragPointY = y - itemView.getTop();
		windowParams = new WindowManager.LayoutParams();

		windowParams.gravity = Gravity.TOP | Gravity.LEFT;
		windowParams.x = x - dragPointX + dragOffsetX;
		windowParams.y = y - dragPointY + dragOffsetY;
		windowParams.height = LayoutParams.WRAP_CONTENT;
		windowParams.width = LayoutParams.WRAP_CONTENT;
		windowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

		windowParams.format = PixelFormat.TRANSLUCENT;// 半透明格式
		windowParams.windowAnimations = 0;
		windowParams.alpha = 0.8f;

		ImageView iv = new ImageView(getContext());
		iv.setImageBitmap(bm);
		dragBitmap = bm;
		windowManager = (WindowManager) getContext().getSystemService(
				Context.WINDOW_SERVICE);
		windowManager.addView(iv, windowParams);
		dragImageView = iv;
		Mode = Mode_Drag;

		halfBitmapWidth = bm.getWidth() / 2;
		halfBitmapHeight = bm.getHeight() / 2;

		for (int i = 0; i < getChildCount(); i++) {
			getChildAt(i).getBackground().setAlpha((int) (0.8f * 255));
		}
	}

	// 停止拖动
	private void stopDrag() {
		recoverChildren();
		if (Mode == Mode_Drag) {
			if ((dragPosition == 0)
					|| (dragPosition == mAdapter.getCount() - 1)) {
				getChildAt(temChangPosition).setVisibility(View.VISIBLE);
			}
			if (getChildAt(dragPosition).getVisibility() != View.VISIBLE)
				getChildAt(dragPosition).setVisibility(View.VISIBLE);
			Mode = Mode_Free;
			try {
				// 退出APP前，保存当前的Items，记得所有item的位置
				DbUtils mDbUtils = DBUtils_openHelper.getInstance(mContext)
						.getDb();
				List<MoveItem> list = getAllMoveItems();
				mDbUtils.deleteAll(MoveItem.class);
				mDbUtils.saveAll(list);
			} catch (DbException e) {
				e.printStackTrace();
			}
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					cancelAnimations();
					mContext.sendBroadcast(new Intent("tempdrag"));
				}
			}, 150);
		}
	}

	// 使用Map集合记录，防止动画执行混乱
	private class NotifyDataSetListener implements AnimationListener {
		private int movedPosition;

		public NotifyDataSetListener(int primaryPosition) {
			this.movedPosition = primaryPosition;
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			if (animationMap.containsKey(movedPosition)) {
				// remove from map when end
				animationMap.remove(movedPosition);
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationStart(Animation animation) {
			// put into map when start
			animationMap.put(movedPosition, true);
		}
	}

	// 返回滑动的位移动画，比较复杂，有兴趣的可以看看
	private Animation animationPositionToPosition(int oldP, int newP,
			boolean isCrossScreen, boolean isForward) {
		PointF oldPF = positionToPoint2(oldP);
		PointF newPF = positionToPoint2(newP);

		TranslateAnimation animation = null;

		// when moving forward across pages,the first item of the new page moves
		// backward
		if (oldP != 0 && (oldP + 1) % itemPerPage == 0 && isForward) {
			animation = new TranslateAnimation(screenWidth - oldPF.x, 0,
					DensityUtil.dip2px(mContext, 25) - screenHeight, 0);
			animation.setDuration(800);
		}
		// when moving backward across pages,the last item of the new page moves
		// forward
		else if (oldP != 0 && oldP % itemPerPage == 0 && isCrossScreen
				&& !isForward) {
			animation = new TranslateAnimation(newPF.x - screenWidth, 0,
					screenHeight - DensityUtil.dip2px(mContext, 25), 0);
			animation.setDuration(800);
		}
		// regular animation between two neighbor items
		else {
			animation = new TranslateAnimation(newPF.x - oldPF.x, 0, newPF.y
					- oldPF.y, 0);
			animation.setDuration(500);
		}
		animation.setFillAfter(true);
		animation.setAnimationListener(new NotifyDataSetListener(oldP));

		return animation;
	}

	// 滑动合法性的判断，防止滑动到空白区域
	private boolean IsCanMove(int deltaY) {
		// Log.e("scroll", "move" + getScaleX() + "  y " + getScrollY());
		if (getScrollY() <= 0 && deltaY < 0) {
			snapToDistance(0);
			snapToDestination(true);
			return false;
		}
		if (getScrollY() >= getMaxScroll() && deltaY > 0) {
			snapToDistance(0);
			snapToDestination(false);
			return false;
		}
		return true;
	}

	// 判断滑动的一系列动画是否有冲突
	private boolean isMovingFastConflict(int moveNum) {
		int itemsMoveNum = Math.abs(moveNum);
		int temp = dragPosition;
		for (int i = 0; i < itemsMoveNum; i++) {
			int holdPosition = moveNum > 0 ? temp + 1 : temp - 1;
			if (animationMap.containsKey(holdPosition)) {
				return true;
			}
			temp = holdPosition;
		}
		return false;
	}

	// 执行位置动画
	private void movePostionAnimation(int oldP, int newP) {
		int moveNum = newP - oldP;
		boolean isCrossScreen = false;
		boolean isForward = false;
		if (moveNum != 0 && !isMovingFastConflict(moveNum)) {
			int absMoveNum = Math.abs(moveNum);
			for (int i = Math.min(oldP, newP) + 1; i <= Math.max(oldP, newP); i++) {
				if (i % 8 == 0) {
					isCrossScreen = true;
				}
			}
			if (isCrossScreen) {
				isForward = moveNum < 0 ? false : true;
			}
			for (int i = 0; i < absMoveNum; i++) {
				int holdPosition = (moveNum > 0) ? oldP + 1 : oldP - 1;
				View view = getChildAt(holdPosition);
				if (view != null) {
					view.clearAnimation();
					view.startAnimation(animationPositionToPosition(oldP,
							holdPosition, isCrossScreen, isForward));
				}
				oldP = holdPosition;
			}
		}
	}

	// 滑动过程中，使所有的item暗掉
	private void fadeChildren() {
		final int count = getChildCount() - 1;
		for (int i = count; i >= 0; i--) {
			View child = getChildAt(i);
			child.getBackground().setAlpha(80);// 180
		}
	}

	// 滑动停止后，恢复item的透明度
	private void recoverChildren() {
		final int count = getChildCount() - 1;
		for (int i = count; i >= 0; i--) {
			final View child = getChildAt(i);
			// child.setAlpha(1.0f);
			child.getBackground().setAlpha(255);
			Drawable drawable = child.getBackground();
			if (drawable != null) {
				child.getBackground().setAlpha(255);
			}
		}
	}

	public int getChildIndex(View view) {
		if (view != null && view.getParent() instanceof ScrollLayout) {
			final int childCount = ((ScrollLayout) view.getParent())
					.getChildCount();
			for (int i = 0; i < childCount; i++) {
				if (view == ((ScrollLayout) view.getParent()).getChildAt(i)) {
					return i;
				}
			}
		}
		return -1;
	}

	// 获取特定position下的item View
	private View getView(final int position) {
		View view = null;
		MoveItem moveItem = null;
		if (mAdapter != null && !isDeleting()) {
			view = mAdapter.getView(position);
			moveItem = mAdapter.getMoveItem(position);
			final ImageView iv = (ImageView) view.findViewById(R.id.delete_iv);
			if (moveItem.isFlag_selected()) {
				iv.setVisibility(View.VISIBLE);
				showImgList.add(moveItem);
				showList.add(view);
			}
			view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (isClick) {
						if (mAdapter.getMoveItem(position).isFlag_add()) {
							if (isDeleting()) {
								return;
							}
							Intent intent = new Intent(mContext,
									ChooseTemplateActivity.class);
							((TemplateActivity) mContext)
									.startActivityForResult(intent, 10);

							// MoveItem item = (MoveItem) v.getTag();
							try {
								// utils.delete(MoveItem.class,
								// WhereBuilder.b("mid", "=", item.getMid()));
								List<MoveItem> list = getAllMoveItems();
								utils.deleteAll(MoveItem.class);
								utils.saveAll(list);
							} catch (DbException e) {
								e.printStackTrace();
							}
						} else {
							if (isDeleting()) {
								if (iv.getVisibility() == View.VISIBLE
										&& mAdapter.getMoveItem(position)
												.isFlag_delete()) {
									iv.setVisibility(View.GONE);
									removeList.remove(v);
									removeItemList.remove(mAdapter
											.getMoveItem(position));
									if (removeList.size() == 0) {
										((TemplateActivity) mContext)
												.setImageDelete(
														R.drawable.btn_bg_delete_press,
														R.color.deletetext);
									}
								} else if (iv.getVisibility() == View.GONE
										&& mAdapter.getMoveItem(position)
												.isFlag_delete()) {
									iv.setVisibility(View.VISIBLE);
									removeList.add(v);
									removeItemList.add(mAdapter
											.getMoveItem(position));
									if (removeList.size() == 1) {
										((TemplateActivity) mContext)
												.setImageDelete(
														R.drawable.button_selector_delete,
														R.color.white);
									}
								} else {
									Toast.makeText(
											mContext,
											mContext.getString(R.string.template_delete_hint),
											Toast.LENGTH_SHORT).show();
								}
							} else {
								if (iv.getVisibility() == View.VISIBLE) {
									iv.setVisibility(View.GONE);
									mAdapter.getMoveItem(position)
											.setFlag_selected(false);
									showList.remove(v);
									showImgList.remove(mAdapter
											.getMoveItem(position));
								} else {
									iv.setVisibility(View.VISIBLE);
									mAdapter.getMoveItem(position)
											.setFlag_selected(true);
									showList.add(v);
									showImgList.add(mAdapter
											.getMoveItem(position));
								}
								refreshTitleString(isDeleting());
							}
						}
					}

				}
			});
			// view.setOnLongClickListener(new OnLongClickListener() {
			// @Override
			// public boolean onLongClick(View v) {
			// return onItemLongClick(v, mAdapter.getMoveItem(position)
			// .isFlag_drag(), position);
			// }
			// });
			// view.setOnClickListener(new OnClickListener() {
			//
			// @Override
			// public void onClick(View v) {
			// if (!isClick) {
			// return;
			// }
			// if (mAdapter.getMoveItem(position).isFlag_add()) {
			// Intent intent = new Intent(mContext,
			// ChooseTemplateActivity.class);
			// ((Gallery) mContext).startActivityForResult(intent, 10);
			//
			// // MoveItem item = (MoveItem) v.getTag();
			// try {
			// // utils.delete(MoveItem.class,
			// // WhereBuilder.b("mid", "=", item.getMid()));
			// List<MoveItem> list = getAllMoveItems();
			// utils.deleteAll(MoveItem.class);
			// utils.saveAll(list);
			// } catch (DbException e) {
			// e.printStackTrace();
			// }
			//
			// } else {
			// try {
			// // 退出APP前，保存当前的Items，记得所有item的位置
			// List<MoveItem> list = getAllMoveItems();
			// utils.deleteAll(MoveItem.class);
			// utils.saveAll(list);
			// } catch (DbException e) {
			// e.printStackTrace();
			// }
			// if (mAdapter
			// .getMoveItem(position)
			// .getText_describe()
			// .equals(mContext
			// .getString(R.string.template_kt_normaltime))) {
			// moveItemSave = mAdapter.getMoveItem(position);
			// moveItemPosition = position;
			// Intent mIntent = new Intent(
			// "android.intent.action.IMAGEPREVIER_SCREEN_SHOT");
			// mContext.sendBroadcast(mIntent);
			// ((Gallery) mContext).dialogTemplte.dismiss();
			// return;
			//
			// }
			// if (mAdapter
			// .getMoveItem(position)
			// .getText_describe()
			// .equals(mContext
			// .getString(R.string.template_kt_none))) {
			// PhotoPage
			// .setImageview(R.drawable.ty_preview_wallpaper_none);
			// ((Gallery) mContext).dialogTemplte.dismiss();
			// return;
			//
			// }
			// if
			// (mAdapter.getMoveItem(position).isFlag_delete()||(!mAdapter.getMoveItem(position).isFlag_resouce()))
			// {
			// imgPath = mAdapter.getMoveItem(position)
			// .getImgurl();
			// if(FileUtil.isFileExist(imgPath)){
			// PhotoPage.setImageview(imgPath);
			// }else{
			// Toast.makeText(mContext,
			// mContext.getString(R.string.template_img_notexist),
			// Toast.LENGTH_SHORT).show();
			// }
			//
			// } else {
			// imgId = mAdapter.getMoveItem(position)
			// .getImg_normal_int();
			// PhotoPage.setImageview(imgId);
			// }
			//
			// ((Gallery) mContext).dialogTemplte.dismiss();
			// }
			// }
			// });

		}
		return view;
	}

	public void addRefreView() {
		this.addView(getView(mAdapter.getCount() - 2));
		this.addView(getView(mAdapter.getCount() - 1));
		totalPage = (int) Math.ceil(getChildCount() * 1.0 / itemPerPage);
		requestLayout();
	}

	public void addNewCustomView(String path, String describe, int setFlag) {
		mAdapter.delete(mAdapter.getCount() - 1);
		removeView(getChildAt(getChildCount() - 1));
		MoveItem itemOther = new MoveItem();
		itemOther.setImgdown(path);
		itemOther.setImgurl(path);
		itemOther.setOrderId(mAdapter.getCount() + 1);
		itemOther.setMid(mAdapter.getCount() + 1);
		itemOther.setFlag_drag(true);
		itemOther.setText_describe(describe);
		itemOther.setSetTitle(setFlag);
		itemOther.setFlag_delete(true);
		itemOther.setFlag_resouce(false);
		MoveItem itemOtheradd = new MoveItem();
		itemOtheradd.setImg_pressed_int(R.drawable.choice_addto_press);
		itemOtheradd.setImg_normal_int(R.drawable.choice_addto_normal);
		itemOtheradd.setImgdown("assets/img/choice_addto_normal_min.png");
		itemOtheradd.setImgurl("assets/img/choice_addto_normal_min.png");
		itemOtheradd.setOrderId(mAdapter.getCount() + 2);
		itemOtheradd.setMid(mAdapter.getCount() + 2);
		itemOtheradd.setText_describe(mContext.getResources().getString(
				R.string.templateaddtitlename));
		itemOtheradd.setFlag_add(true);
		itemOtheradd.setFlag_drag(false);
		itemOtheradd.setSetTitle(0);
		itemOtheradd.setFlag_delete(false);
		itemOtheradd.setFlag_resouce(true);
		mAdapter.add(itemOther);
		mAdapter.add(itemOtheradd);
//		showImgList.clear();
		refreshTitleString(isDeleting());
		addRefreView();
		// refreView();
		// 如果添加后多了一屏，则移动到后一屏，并进行页面刷新
		if (getChildCount() % itemPerPage == 1) {
			// snapToScreen(mCurScreen + 1);
		}
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();
		}
	}

	// Container背景滑动的实现，下面是计算公式
	// int w = x * ((width - n) / (totalPage - 1)) / getWidth();
	@Override
	protected void dispatchDraw(Canvas canvas) {
		if (this.background != null) {
			int width = this.background.getWidth();
			int height = this.background.getHeight();
			int x = getScrollX();
			int n = height * getWidth() / getHeight();
			int w;
			if (totalPage == 1) {
				w = x * (width - n) / 1 / getWidth();
			} else {
				w = x * ((width - n) / (totalPage - 1)) / getWidth();
			}
			canvas.drawBitmap(this.background, new Rect(w, 0, n + w, height),
					new Rect(x, 0, x + getWidth(), getHeight()), this.paint);
		}
		final int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			final View childView = getChildAt(i);
			if (i > 2 && i % 3 == 0) {
				childWidth = childView.getMeasuredWidth();
				childHeight = childView.getMeasuredHeight();
				int page = i / itemPerPage;
				int row = i / colCount;
				int col = i % colCount;
				int left = leftPadding + col * (colSpace + childWidth);
				int top = topPadding + row * (rowSpace + childHeight);
				childView.layout(left, top, left + childWidth,
						top + childView.getMeasuredHeight());
				canvas.drawLine(0, top - rowSpace, getWidth(), top
						- rowSpace, paintLine);
			}
		}

		super.dispatchDraw(canvas);
	}

	// 获取当前Container状态下，所有的item
	public List<MoveItem> getAllMoveItems() {
		List<MoveItem> items = new ArrayList<MoveItem>();
		int count = getChildCount();
		MoveItem item = null;
		for (int i = 0; i < count; i++) {
			item = (MoveItem) getChildAt(i).getTag();
			items.add(item);
		}
		return items;
	}

	public Bitmap getBg() {
		return background;
	}

	public int getBottomPadding() {
		return bottomPadding;
	}

	public int getColCount() {
		return colCount;
	}

	public int getColSpace() {
		return colSpace;
	}

	public int getCurrentPage() {
		return mCurScreen;
	}

	public int getLeftPadding() {
		return leftPadding;
	}

	public OnAddOrDeletePage getOnCaculatePage() {
		return onAddPage;
	}

	public OnEditModeListener getOnEditModeListener() {
		return onEditModeListener;
	}

	public OnPageChangedListener getOnPageChangedListener() {
		return pageChangedListener;
	}

	public int getRightPadding() {
		return rightPadding;
	}

	public int getRowCount() {
		return rowCount;
	}

	public int getRowSpace() {
		return rowSpace;
	}

	public ScrollAdapter getSaAdapter() {
		return mAdapter;
	}

	public int getTopPadding() {
		return topPadding;
	}

	public int getTotalItem() {
		return totalItem;
	}

	public int getTotalPage() {
		return totalPage;
	}

	@Override
	public void ondataChange() {
		refreView();
	}

	// 根据手势绘制不断变化位置的dragView
	private void onDrag(int x, int y) {
		if (dragImageView != null) {
			windowParams.alpha = 0.8f;
			windowParams.x = x - dragPointX + dragOffsetX;
			windowParams.y = y - dragPointY + dragOffsetY;
			windowManager.updateViewLayout(dragImageView, windowParams);
		}
		int tempPosition = pointToPosition(x, y);
		if (tempPosition != -1) {
			dragPosition = tempPosition;
		}
		View view = getChildAt(temChangPosition);
		if (view == null) {
			stopDrag();
			return;
		}
		view.setVisibility(View.INVISIBLE);
		if (temChangPosition != dragPosition && (dragPosition != 0)
				&& (dragPosition != mAdapter.getCount() - 1)) {
			View dragView = getChildAt(temChangPosition);
			// View dragView = mAdapter.getView(temChangPosition);
			movePostionAnimation(temChangPosition, dragPosition);
			removeViewAt(temChangPosition);
			addView(dragView, dragPosition);
			getChildAt(dragPosition).setVisibility(View.INVISIBLE);
			this.getSaAdapter().exchange(temChangPosition, dragPosition);
			temChangPosition = dragPosition;
		}

		if (x > getRight() - DensityUtil.dip2px(mContext, 25)
				&& mCurScreen < totalPage - 1 && mScroller.isFinished()
				&& x - startX > 10) {
			snapToScreen(mCurScreen + 1, false);
		} else if (x - getLeft() < DensityUtil.dip2px(mContext, 35)
				&& mCurScreen > 0 && mScroller.isFinished() && x - startX < -10) {
			snapToScreen(mCurScreen - 1, false);
		}

	}

	public boolean onItemLongClick(View v, boolean drag, int position) {
		viewDrag = v;
		if (mScroller.isFinished()) {
			v.destroyDrawingCache();
			v.setDrawingCacheEnabled(true);
			fadeChildren();
			if (onEditModeListener != null)
				onEditModeListener.onEdit();
			Bitmap bm = Bitmap.createBitmap(v.getDrawingCache());

			if (drag) {
				// showEdit(true, temChangPosition);
				if (isEditting) {
					showEdit(false);
				}
				v.setVisibility(View.GONE);
				startDrag(bm, (int) (mLastMotionX), (int) (mLastMotionY), v);
			}
			return true;
		}
		return false;

	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			final View childView = getChildAt(i);
			if (childView.getVisibility() != View.GONE) {
				childWidth = childView.getMeasuredWidth();
				childHeight = childView.getMeasuredHeight();
				int page = i / itemPerPage;
				int row = i / colCount;
				int col = i % colCount;
				int left = leftPadding + col * (colSpace + childWidth);
				int top = topPadding + row * (rowSpace + childHeight);
				childView.layout(left, top, left + childWidth,
						top + childView.getMeasuredHeight());
			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final int width = MeasureSpec.getSize(widthMeasureSpec);
		MeasureSpec.getMode(widthMeasureSpec);
		final int height = MeasureSpec.getSize(heightMeasureSpec);
		MeasureSpec.getMode(heightMeasureSpec);

		screenWidth = width;
		screenHeight = height;
		int usedWidth = width - leftPadding - rightPadding - (colCount - 1)
				* colSpace;
		int usedheight = ((height - topPadding - bottomPadding - (rowCount - 1)
				* rowSpace));
		int childWidth = usedWidth / colCount;
		int childHeight = (int) (0.583 * width);// usedheight / rowCount;
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			int childWidthSpec = getChildMeasureSpec(
					MeasureSpec
							.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY),
					20, childWidth);
			int childHeightSpec = getChildMeasureSpec(
					MeasureSpec.makeMeasureSpec(childHeight,
							MeasureSpec.EXACTLY), 20, childHeight);
			child.measure(childWidthSpec, childHeightSpec);
		}
		// scrollTo(0, mCurScreen * height);
	}

	// 根据坐标，判断当前item所属的位置，即编号
	public int pointToPosition(int x, int y) {
		int locX = x + mCurScreen * getWidth();

		if (frame == null)
			frame = new Rect();
		final int count = getChildCount();
		for (int i = count - 1; i >= 0; i--) {
			final View child = getChildAt(i);
			child.getHitRect(frame);
			if (frame.contains(locX, y)) {
				return i;
			}
		}
		return -1;
	}

	// item编号对应的左上角坐标
	public PointF positionToPoint1(int position) {
		PointF point = new PointF();

		int page = position / itemPerPage;
		int row = position / colCount % rowCount;
		int col = position % colCount;
		int left = leftPadding + page * screenWidth + col
				* (colSpace + childWidth);
		int top = topPadding + row * (rowSpace + childHeight);

		point.x = left;
		point.y = top;
		return point;

	}

	public PointF positionToPoint2(int position) {
		PointF point = new PointF();

		int row = position / colCount % rowCount;
		int col = position % colCount;
		int left = leftPadding + col * (colSpace + childWidth);
		int top = topPadding + row * (rowSpace + childHeight);

		point.x = left;
		point.y = top;
		return point;

	}

	public void setBackGroud(Bitmap paramBitmap) {
		this.background = paramBitmap;
		this.paint.setFilterBitmap(true);
	}

	public void setBottomPadding(int bottomPadding) {
		this.bottomPadding = bottomPadding;
	}

	public void setColCount(int colCount) {
		this.colCount = colCount;
		this.itemPerPage = this.colCount * this.rowCount;
	}

	public boolean isEditting() {
		return isEditting;
	}

	public void setColSpace(int colSpace) {
		this.colSpace = colSpace;
	}

	public void setLeftPadding(int leftPadding) {
		this.leftPadding = leftPadding;
	}

	public void setOnAddPage(OnAddOrDeletePage onAddPage) {
		this.onAddPage = onAddPage;
	}

	public void setOnEditModeListener(OnEditModeListener onEditModeListener) {
		this.onEditModeListener = onEditModeListener;
	}

	public void setOnPageChangedListener(
			OnPageChangedListener pageChangedListener) {
		this.pageChangedListener = pageChangedListener;
	}

	public void setRightPadding(int rightPadding) {
		this.rightPadding = rightPadding;
	}

	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
		this.itemPerPage = this.colCount * this.rowCount;
	}

	public void setRowSpace(int rowSpace) {
		this.rowSpace = rowSpace;
	}

	public void setSaAdapter(ScrollAdapter SaAdapter) {
		this.mAdapter = SaAdapter;
		this.mAdapter.setOnDataChangeListener(this);
	}

	public void setTopPadding(int topPadding) {
		this.topPadding = topPadding;
	}

	public void setTotalItem(int totalItem) {
		this.totalItem = totalItem;
	}

	// 执行松手动画
	private void showDropAnimation(int x, int y) {
		ViewGroup moveView = (ViewGroup) getChildAt(dragPosition);
		TranslateAnimation animation = new TranslateAnimation(x
				- halfBitmapWidth - moveView.getLeft(), 0, y - halfBitmapHeight
				- moveView.getTop(), 0);
		animation.setFillAfter(false);
		animation.setDuration(300);
		moveView.setAnimation(animation);
		windowManager.removeView(dragImageView);
		dragImageView = null;

		if (dragBitmap != null) {
			dragBitmap = null;
		}
		cancelAnimations();
	}

	public void showEdit(boolean isEdit, int position) {
		showEdit(false);
		// cancelAnimations();
		isEditting = isEdit;
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			if (i == 0 || i == count - 1 || i == position) {
				continue;
			}
			View child = getChildAt(i);
			ImageView iv = (ImageView) child.findViewById(R.id.delete_iv);
			iv.setTag(child.getTag());
			iv.setVisibility(isEdit == true ? View.VISIBLE : View.GONE);
			if (isEdit) {
				iv.setOnClickListener(new DelItemClick(i));
				// animateMoveItems(child);
			} else {
				// child.clearAnimation();
			}
		}

		if (isEdit == false) {
			int pages = (int) Math.ceil(getChildCount() * 1.0 / itemPerPage);
			if (pages < totalPage) {
				totalPage = pages;
				if (this.onAddPage != null)
					onAddPage.onAddOrDeletePage(totalPage + 1, false);
			}
		}
	}

	public void showEdit(boolean isEdit) {
		isEditting = isEdit;
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			if (((MoveItem) getChildAt(i).getTag()).isFlag_resouce()) {
				if (isEdit) {
					animateMoveItems(child);
				} else {
					child.clearAnimation();
				}
				continue;
			}
			// ImageView iv = (ImageView) child.findViewById(R.id.delete_iv);
			// iv.setTag(child.getTag());
			// iv.setVisibility(isEdit == true ? View.VISIBLE : View.GONE);
			if (isEdit) {
				// iv.setOnClickListener(new DelItemClick(i));
				animateMoveItems(child);
			} else {
				child.clearAnimation();
			}
		}

		if (isEdit == false) {
			int pages = (int) Math.ceil(getChildCount() * 1.0 / itemPerPage);
			if (pages < totalPage) {
				totalPage = pages;
				if (this.onAddPage != null)
					onAddPage.onAddOrDeletePage(totalPage + 1, false);
			}
		}
	}

	// 滚屏
	public void snapToDestination(boolean flag) {
		final int screenWidth = getHeight();
		final int destScreen = (getScrollY() + screenWidth / 2) / screenWidth;
		if (destScreen >= 0 && destScreen < totalPage) {
			if(flag)
			snapToScreen(destScreen);
			else
			snapToScreenBottom(destScreen, true);
		}
	}

	public void snapToScreen(int whichScreen) {
		snapToScreen(whichScreen, true);
	}

	public void snapToDistance(int distance) {
		requestLayout();
		invalidate();
	}

	public void snapToScreen(int whichScreen, boolean isFast) {
		// get the valid layout page
		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
		if (getScrollY() != (whichScreen * getHeight())) {

			final int delta = whichScreen * getHeight() - getScrollY();

			if (pageChangedListener != null)
				pageChangedListener.onPage2Other(mCurScreen, whichScreen);

			if (!isFast)
				mScroller.startScroll(0, getScrollY(), 0, delta, 800);
			else
				mScroller.startScroll(0, getScrollY(), 0, delta, 500);
			mCurScreen = whichScreen;
			requestLayout();
			invalidate(); // Redraw the layout
		}
	}
	
	public void snapToScreenBottom(int whichScreen, boolean isFast) {
		// get the valid layout page
		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
		if (getScrollY() != (whichScreen * getHeight())) {

			final int delta = whichScreen * getHeight() - getScrollY();

			if (pageChangedListener != null)
				pageChangedListener.onPage2Other(mCurScreen, whichScreen);

			if (!isFast)
				mScroller.startScroll(0, getScrollY(), 0, 0, 800);
			else
				mScroller.startScroll(0, getMaxScroll(), 0, 0, 500);
			mCurScreen = whichScreen;
			requestLayout();
			invalidate(); // Redraw the layout
		}
	}

	private void animateMoveAllItems() {
		final Animation rotateAnimation = createFastRotateAnimation();
		Handler handler = new Handler();
		Random random = new Random();
		int time = 10;
		for (int i = 0; i < getChildCount(); i++) {
			if (i == 0 || i == getChildCount() - 1) {
				continue;
			}
			final View child = getChildAt(i);
			time = random.nextInt(300);
			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					child.startAnimation(rotateAnimation);
				}
			}, time);

		}
	}

	private void animateMoveItems(final View v) {
		final Animation rotateAnimation = createFastRotateAnimation();
		Handler handler = new Handler();
		Random random = new Random();
		int time = 10;
		time = random.nextInt(300);

		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				v.startAnimation(rotateAnimation);
			}
		}, time);

	}

	public void cancelAnimations() {

		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			child.clearAnimation();
		}
	}

	private Animation createFastRotateAnimation() {
		Animation rotate = new RotateAnimation(-1.0f, 1.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);

		rotate.setRepeatMode(Animation.REVERSE);
		rotate.setRepeatCount(Animation.INFINITE);
		rotate.setDuration(125);
		rotate.setInterpolator(new AccelerateDecelerateInterpolator());

		return rotate;
	}

	/**
	 * 删除按钮的功能处理
	 * 
	 */
	private final class DelItemClick implements OnClickListener {
		int deletePostion;

		public DelItemClick(int deletePostion) {
			this.deletePostion = deletePostion;
		}

		@Override
		public void onClick(View v) {
			if (v.getParent() != null) {
				if (mCurScreen < totalPage - 1) {

				}
				// showEdit(false);
				((ViewGroup) (v.getParent().getParent())).clearAnimation();// 把动画删除
				movePostionAnimation(deletePostion, getChildCount() - 1);
				removeView((ViewGroup) (v.getParent().getParent()));
				mAdapter.delete(deletePostion);
				DbUtils utils = DBUtils_openHelper.getInstance(mContext)
						.getDb();
				try {
					List<MoveItem> list = getAllMoveItems();
					utils.deleteAll(MoveItem.class);
					utils.saveAll(list);
				} catch (DbException e) {
					e.printStackTrace();
				}
				mContext.sendBroadcast(new Intent("deletetemp"));
				// 如果删除后少了一屏，则移动到前一屏，并进行页面刷新
				if (getChildCount() % itemPerPage == 0) {
					snapToScreen(totalPage - 1);
				}
			}
		}
	}

	public void notifyRefresh() {
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			setListener(i, getChildAt(i));
		}
	}

	public void notifyRefresh(int flag) {
		showImgList = new ArrayList<MoveItem>();
		MoveItem none = createNoneMoveItem(
				"assets/img/choice_nomap_min.png",
				"assets/img/choice_nomap_min.png",
				R.drawable.nothing, 1, 1,
				mContext.getString(R.string.template_kt_none), false, false,
				false, true, false);
		showImgList.add(none);
		showList = new ArrayList<View>();
		removeList = new ArrayList<View>();
		removeItemList = new ArrayList<MoveItem>();
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			setListener(i, getChildAt(i));
		}
	}

	public void setListener(final int position, View view) {
		MoveItem moveItem = null;
		final ImageView iv = (ImageView) view.findViewById(R.id.delete_iv);
		if (mAdapter != null && !isDeleting()) {
			moveItem = mAdapter.getMoveItem(position);
			if (moveItem.isFlag_selected()) {
				iv.setVisibility(View.VISIBLE);
				showImgList.add(moveItem);
				showList.add(view);
			}
		}
		
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isClick) {
					if (mAdapter.getMoveItem(position).isFlag_add()) {
						if (isDeleting()) {
							return;
						}
						Intent intent = new Intent(mContext,
								ChooseTemplateActivity.class);
						((TemplateActivity) mContext).startActivityForResult(
								intent, 10);

						// MoveItem item = (MoveItem) v.getTag();
						try {
							// utils.delete(MoveItem.class,
							// WhereBuilder.b("mid", "=", item.getMid()));
							List<MoveItem> list = getAllMoveItems();
							utils.deleteAll(MoveItem.class);
							utils.saveAll(list);
						} catch (DbException e) {
							e.printStackTrace();
						}
					} else {
						if (isDeleting()) {
							if (iv.getVisibility() == View.VISIBLE
									&& mAdapter.getMoveItem(position)
											.isFlag_delete()) {
								iv.setVisibility(View.GONE);
								removeList.remove(v);
								removeItemList.remove(mAdapter
										.getMoveItem(position));
								if (removeList.size() == 0) {
									((TemplateActivity) mContext)
											.setImageDelete(
													R.drawable.btn_bg_delete_press,
													R.color.deletetext);
								}
							} else if (iv.getVisibility() == View.GONE
									&& mAdapter.getMoveItem(position)
											.isFlag_delete()) {
								iv.setVisibility(View.VISIBLE);
								removeList.add(v);
								removeItemList.add(mAdapter
										.getMoveItem(position));
								if (removeList.size() == 1) {
									((TemplateActivity) mContext)
											.setImageDelete(
													R.drawable.button_selector_delete,
													R.color.white);
								}
							} else {
								Toast.makeText(
										mContext,
										mContext.getString(R.string.template_delete_hint),
										Toast.LENGTH_SHORT).show();
							}
						} else {
							if (iv.getVisibility() == View.VISIBLE) {
								iv.setVisibility(View.GONE);
								mAdapter.getMoveItem(position)
										.setFlag_selected(false);
								showList.remove(v);
								showImgList.remove(mAdapter
										.getMoveItem(position));
							} else {
								iv.setVisibility(View.VISIBLE);
								mAdapter.getMoveItem(position)
										.setFlag_selected(true);
								showList.add(v);
								showImgList.add(mAdapter.getMoveItem(position));
							}
							refreshTitleString(isDeleting());
						}
					}
				}

			}
		});
		// view.setOnLongClickListener(new OnLongClickListener() {
		// @Override
		// public boolean onLongClick(View v) {
		// return onItemLongClick(v, mAdapter.getMoveItem(position)
		// .isFlag_drag(), position);
		// }
		// });
		// view.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// if (!isClick) {
		// return;
		// }
		// if (mAdapter.getMoveItem(position).isFlag_add()) {
		// Intent intent = new Intent(mContext,
		// ChooseTemplateActivity.class);
		// ((Gallery) mContext).startActivityForResult(intent, 10);
		//
		// // MoveItem item = (MoveItem) v.getTag();
		// try {
		// // utils.delete(MoveItem.class,
		// // WhereBuilder.b("mid", "=", item.getMid()));
		// List<MoveItem> list = getAllMoveItems();
		// utils.deleteAll(MoveItem.class);
		// utils.saveAll(list);
		// } catch (DbException e) {
		// e.printStackTrace();
		// }
		//
		// } else {
		// try {
		// // 退出APP前，保存当前的Items，记得所有item的位置
		// List<MoveItem> list = getAllMoveItems();
		// utils.deleteAll(MoveItem.class);
		// utils.saveAll(list);
		// } catch (DbException e) {
		// e.printStackTrace();
		// }
		// if (mAdapter
		// .getMoveItem(position)
		// .getText_describe()
		// .equals(mContext
		// .getString(R.string.template_kt_normaltime))) {
		// moveItemSave = mAdapter.getMoveItem(position);
		// moveItemPosition = position;
		// Intent mIntent = new Intent(
		// "android.intent.action.IMAGEPREVIER_SCREEN_SHOT");
		// mContext.sendBroadcast(mIntent);
		// ((Gallery) mContext).dialogTemplte.dismiss();
		// return;
		//
		// }
		// if (mAdapter
		// .getMoveItem(position)
		// .getText_describe()
		// .equals(mContext
		// .getString(R.string.template_kt_none))) {
		// PhotoPage
		// .setImageview(R.drawable.ty_preview_wallpaper_none);
		// ((Gallery) mContext).dialogTemplte.dismiss();
		// return;
		//
		// }
		// if (mAdapter.getMoveItem(position).isFlag_delete()) {
		// imgPath = mAdapter.getMoveItem(position).getImgurl();
		// PhotoPage.setImageview(imgPath);
		// } else {
		// imgId = mAdapter.getMoveItem(position)
		// .getImg_normal_int();
		// PhotoPage.setImageview(imgId);
		// }
		//
		// ((Gallery) mContext).dialogTemplte.dismiss();
		// }
		// }
		// });

	}

	public interface SAdapter {
		void exchange(int oldPosition, int newPositon);

		int getCount();

		View getView(int position);
	}

	public interface OnAddOrDeletePage {
		void onAddOrDeletePage(int page, boolean isAdd);
	}

	public interface OnEditModeListener {
		void onEdit();
	}

	public interface OnPageChangedListener {
		void onPage2Other(int n1, int n2);
	}

	@Override
	public void refreshImage() {
		DbUtils utils = DBUtils_openHelper.getInstance(mContext).getDb();
		try {
			moveItemSave = utils.findFirst(Selector.from(MoveItem.class).where(
					"text_describe", "=", "实时桌面"));
			moveItemSave.setFlag_resouce(true);
			moveItemSave.setImgdown(IMGPATHTIME);
			moveItemSave.setImgurl("assets/img/ty_preview_desktop_time.png");
			utils.saveOrUpdate(moveItemSave);
		} catch (DbException e) {
			e.printStackTrace();
		}
		Intent intent = new Intent();
		intent.setAction("scrollupdata");
		mContext.sendBroadcast(intent);
		PhotoPage.setImageview(IMGPATHTIME);
	}

	/**
	 * Get the maximum scroll needed for drawing all the children correctly
	 * 
	 * @return
	 */
	protected int getMaxScroll() {
		int rowCount = getChildCount() / colCount;
		if (isDeleting()) {
			if (getChildCount() % colCount == 0) {
				return topPadding + rowCount * (rowSpace + childHeight)
						+ bottomPadding - getHeight() + 140;
			} else {
				return topPadding + rowCount * (rowSpace + childHeight)
						+ bottomPadding - getHeight() + childHeight + 140;
			}
		} else {
			if (getChildCount() % colCount == 0) {
				return topPadding + rowCount * (rowSpace + childHeight)
						+ bottomPadding - getHeight();
			} else {
				return topPadding + rowCount * (rowSpace + childHeight)
						+ bottomPadding - getHeight() + childHeight;
			}
		}

	}

	public ArrayList<MoveItem> getShowImgList() {
		return showImgList;
	}

	public void setShowImgList(ArrayList<MoveItem> showImgList) {
		this.showImgList = showImgList;
	}

	public boolean isDeleting() {
		return deleting;
	}

	public void setDeleting(boolean deleting) {
		this.deleting = deleting;
	}

	public void refreshTitleString(boolean isDelete) {
		if (isDelete) {
			((TemplateActivity) mContext).text_head.setText(mContext
					.getString(R.string.templatetitlename));
		} else {
			((TemplateActivity) mContext).text_head.setText(mContext
					.getString(R.string.templatetitlename)
					+ "("
					+ (showImgList.size()-1)
					+ "/"
					+ (mAdapter.getCount() - 1)
					+ ")");
		}

	}

	public void deleteItem() {

		if (removeList.size() == 0) {
			Toast.makeText(mContext,
					mContext.getString(R.string.template_delete_null),
					Toast.LENGTH_SHORT).show();
			return;
		}

		for (int i = 0; i < removeList.size(); i++) {
			((ViewGroup) (removeList.get(i))).clearAnimation();
			removeView((ViewGroup) (removeList.get(i)));
		}
		mAdapter.getmList().removeAll(removeItemList);
		removeItemList.clear();
		removeList.clear();
		((TemplateActivity) mContext).setImageDelete(
				R.drawable.btn_bg_delete_press, R.color.deletetext);
		DbUtils utils = DBUtils_openHelper.getInstance(mContext).getDb();
		try {
			List<MoveItem> list = getAllMoveItems();
			utils.deleteAll(MoveItem.class);
			utils.saveAll(list);
		} catch (DbException e) {
			e.printStackTrace();
		}
		mContext.sendBroadcast(new Intent("deletetemp"));
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

	public void changView() {
		if (isDeleting()) {
			for (View v : showList) {
				v.findViewById(R.id.delete_iv).setVisibility(View.GONE);
			}
		} else {
			for (View v : removeList) {
				v.findViewById(R.id.delete_iv).setVisibility(View.GONE);
			}
		}
	}

}
