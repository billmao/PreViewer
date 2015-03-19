package com.ktouch.kdc.launcher4.app;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.Keyframe;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ktouch.kdc.launcher4.R;
import com.ktouch.kdc.launcher4.adapter.ScrollAdapter;
import com.ktouch.kdc.launcher4.model.MoveItem;
import com.ktouch.kdc.launcher4.ui.PhotoView;
import com.ktouch.kdc.launcher4.ui.ScrollLayout;
import com.ktouch.kdc.launcher4.ui.ScrollLayout.OnAddOrDeletePage;
import com.ktouch.kdc.launcher4.ui.ScrollLayout.OnEditModeListener;
import com.ktouch.kdc.launcher4.ui.ScrollLayout.OnPageChangedListener;
import com.ktouch.kdc.launcher4.util.DBUtils_openHelper;
import com.ktouch.kdc.launcher4.util.FileUtil;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.umeng.analytics.MobclickAgent;

/**
 * 
 * @author 朱永男
 * 
 */
public class TemplateActivity extends Activity implements OnAddOrDeletePage,
		OnPageChangedListener, OnEditModeListener, OnClickListener {

	// 滑动控件的容器Container
	private ScrollLayout mContainer;
	Button vButton;
	// Container的Adapter
	private ScrollAdapter mItemsAdapter;
	// Container中滑动控件列表
	private List<MoveItem> mList_other;

	// xUtils中操纵SQLite的助手类
	private DbUtils mDbUtils;

	public static int height_screen, width_screen;

	private ImageView back;
	private ImageView itemDelete;

	Button edit;

	public TextView text_head;
	private TextView tv_delete;
	// Layout添加或删除动画
	Animator customAppearingAnim, customDisappearingAnim;
	Animator customChangingAppearingAnim, customChangingDisappearingAnim;
	private RelativeLayout layoutDelete;
	private ScrollLayoutReceiver scrollLayoutReceiver;
	private Gallery gallery;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		height_screen = displayMetrics.heightPixels;
		width_screen = displayMetrics.widthPixels;
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.template);
		// 从缓存中初始化滑动控件列表
		getDataFromCache();
		// 初始化控件
		initView();
		// 初始化容器Adapter
		loadBackground();

		scrollLayoutReceiver = new ScrollLayoutReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("scrollupdata");
		filter.addAction("tempdrag");
		filter.addAction("deletetemp");
		registerReceiver(scrollLayoutReceiver, filter);
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
		if (mContainer.isEditting()) {
			// mContainer.showEdit(false);
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (null != scrollLayoutReceiver) {
			unregisterReceiver(scrollLayoutReceiver);
		}
		super.onDestroy();
	}

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

	private void getData1() {
		if (mList_other == null || mList_other.size() == 0) {
			mList_other = new ArrayList<MoveItem>();
			// createMoveItem("assets/img/choice_nomap_min.png",
			// "assets/img/choice_nomap_min.png", R.drawable.choice_nomap,
			// 1, 1, getString(R.string.template_kt_none), false, false,
			// false, true);
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

	private void initView() {
		gallery = Gallery.getGalleryIntance();
		back = (ImageView) findViewById(R.id.img_header_view_back_key);
		edit = (Button) findViewById(R.id.header_view_update_bt);
		edit.setText(getResources().getString(R.string.templatedelete));
		edit.setVisibility(View.VISIBLE);
		edit.setOnClickListener(this);
		back.setOnClickListener(this);
		text_head = (TextView) findViewById(R.id.tv_titlename);
		text_head.setText(getResources().getString(R.string.templatetitlename));
		itemDelete = (ImageView) findViewById(R.id.template_delete);
		itemDelete.setOnClickListener(this);
		tv_delete = (TextView) findViewById(R.id.tv_delete);
		layoutDelete = (RelativeLayout) findViewById(R.id.layout_delete);
		mContainer = (ScrollLayout) findViewById(R.id.container);
		mContainer.setLeftPadding(width_screen / 37);
		mContainer.setRightPadding(width_screen / 37);
		// 初始化动画
		// final LayoutTransition transitioner = new LayoutTransition();
		// createCustomAnimations(transitioner);
		// setupTransition(transitioner);
		// mContainer.setLayoutTransition(transitioner);
		// 如果数据库中没有数据重新加载数据
		getData1();
		// 初始化Container的Adapter
		mItemsAdapter = ScrollAdapter.getAdapterInstance(this, mList_other,
				width_screen, height_screen);
		// 设置Container添加删除Item的回调
		mContainer.setOnAddPage(this);
		// 设置Container页面换转的回调，比如自第一页滑动第二页
		mContainer.setOnPageChangedListener(this);
		// 设置Container编辑模式的回调，长按进入修改模式
		mContainer.setOnEditModeListener(this);
		// 设置Adapter
		mContainer.setSaAdapter(mItemsAdapter);
		// 动态设置Container每页的列数为3列
		mContainer.setColCount(3);
		// 动态设置Container每页的行数为3行
		mContainer.setRowCount(3);
		// 调用refreView绘制所有的Item
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				mContainer.refreView();
			}
		}, 20);
	}

	// 设置Container滑动背景图片
	private void loadBackground() {
		// Options options = new Options();
		// options.inSampleSize = 2;
		// mContainer.setBackGroud(BitmapFactory.decodeResource(getResources(),
		// R.drawable.main_background, options));
	}

	private int getDrawableId(String name) {
		return getResources().getIdentifier(name, "drawable",
				"com.k_touch.imageviewer");
	}

	private void foregroundHight(TextView tv) {
		// 创建一个 SpannableString对象
		SpannableString sp = new SpannableString(
				getString(R.string.templatedelete));
		// 设置背景颜色
		sp.setSpan(new ForegroundColorSpan(Color.parseColor("#ff6600")), 0, 2,
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		// sp.setSpan(new ForegroundColorSpan(Color.YELLOW), 3
		// ,5,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		tv.setText(sp);
		// 设置TextView可点击
		tv.setMovementMethod(LinkMovementMethod.getInstance());
	}

	@Override
	public void onBackPressed() {
		// back键监听，如果在编辑模式，则取消编辑模式
		if (edit.getText().toString().equals(getString(R.string.templatedone))) {
			// mContainer.showEdit(false);
			edit.setText(R.string.templatedelete);
			layoutDelete.setVisibility(View.GONE);
			mContainer.setDeleting(false);
			mContainer.changView();
			mContainer.notifyRefresh(0);
			mContainer.refreshTitleString(false);
			mContainer.getChildAt(mContainer.getChildCount() - 1)
					.setVisibility(View.VISIBLE);
			return;
		} else {
			Intent intent = getIntent();
			if (intent.getBooleanExtra("close", false)) {
				gallery.finish();
			} else {
				MoveItem itemAdd = createNoneMoveItem(
						"assets/img/choice_addto_normal_min.png",
						"assets/img/choice_addto_normal_min.png",
						R.drawable.choice_addto_normal, 4, 4,
						getString(R.string.template_kt_add), true, false,
						false, true, false);
				mContainer.getShowImgList().add(itemAdd);

				PhotoView.setShowList(mContainer.getShowImgList());
			}
			try {
				// 退出APP前，保存当前的Items，记得所有item的位置
				mDbUtils = DBUtils_openHelper.getInstance(this).getDb();
				List<MoveItem> list = mContainer.getAllMoveItems();
				mDbUtils.deleteAll(MoveItem.class);
				mDbUtils.saveAll(list);
			} catch (DbException e) {
				e.printStackTrace();
			}
			super.onBackPressed();
		}
	}

	@Override
	public void onEdit() {
	}

	@Override
	public void onPage2Other(int former, int current) {
	}

	public void onAddOrDeletePage(int page, boolean isAdd) {
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

	private void firstStart() {
		File file = new File(FileUtil.RESOURCE_IMAGE);
		if (getFirstUse()) {
			if (!file.exists()) {// 如果文件夹不存在，直接将图片写入SD卡
				initData();
				createSharePreference();
				sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
						Uri.parse("file://"
								+ Environment.getExternalStorageDirectory())));
			} else {
				initData();
				createSharePreference();
				sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
						Uri.parse("file://"
								+ Environment.getExternalStorageDirectory())));
			}
		} else {
			// 判断用户是否把无和添加的图片也给删了
			if (!file.exists()) {
				FileUtil.writeImage(BitmapFactory.decodeResource(
						getResources(), R.drawable.choice_addto_normal),
						FileUtil.RESOURCE_IMAGE + "/kt_add.png", 100);
				FileUtil.writeImage(BitmapFactory.decodeResource(
						getResources(), R.drawable.choice_nomap),
						FileUtil.RESOURCE_IMAGE + "/kt_none.jpg", 100);
			}
		}

	}

	private void initData() {
		// BitmapFactory.Options options = new BitmapFactory.Options();
		// options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		// FileUtil.writeImage(BitmapFactory.decodeResource(getResources(),
		// R.drawable.choice_addto_normal, options),
		// FileUtil.RESOURCE_IMAGE + "/choice_addto_normal.png", 100);
		// FileUtil.writeImage(BitmapFactory.decodeResource(getResources(),
		// R.drawable.choice_addto_press, options),
		// FileUtil.RESOURCE_IMAGE + "/choice_addto_press.png", 100);
		// FileUtil.writeImage(BitmapFactory.decodeResource(getResources(),
		// R.drawable.kt_lock_time, options), FileUtil.RESOURCE_IMAGE
		// + "/kt_lock_time.png", 100);
		// FileUtil.writeImage(BitmapFactory.decodeResource(getResources(),
		// R.drawable.kt_noemaltime, options), FileUtil.RESOURCE_IMAGE
		// + "kt_noemaltime.png", 100);
		// FileUtil.writeImage(BitmapFactory.decodeResource(getResources(),
		// R.drawable.choice_nomap, options), FileUtil.RESOURCE_IMAGE
		// + "/choice_nomap.png", 100);
		// FileUtil.writeImage(BitmapFactory.decodeResource(getResources(),
		// R.drawable.ty_preview_wallpaper, options),
		// FileUtil.RESOURCE_IMAGE + "/kt_normal.png", 100);
		// FileUtil.writeImage(BitmapFactory.decodeResource(getResources(),
		// R.drawable.ty_preview_lockscreen, options),
		// FileUtil.RESOURCE_IMAGE + "/kta_lock_normal.png", 100);
	}

	private void setupTransition(LayoutTransition transition) {
		// transition.setAnimator(LayoutTransition.APPEARING,customAppearingAnim);
		transition.setAnimator(LayoutTransition.DISAPPEARING,
				customDisappearingAnim);
		// transition.setAnimator(LayoutTransition.CHANGE_APPEARING,customChangingAppearingAnim);
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

	public void setImageDelete(int id, int color) {
		itemDelete.setImageResource(id);
		tv_delete.setTextColor(getResources().getColor(color));
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

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.header_view_update_bt:

			// if (mContainer.isEditting()) {
			// mContainer.showEdit(false);
			// } else {
			// mContainer.showEdit(true);
			// }

			if (edit.getText().toString()
					.equals(getString(R.string.templatedelete))) {
				// mContainer.showEdit(true);
				edit.setText(R.string.templatedone);
				int i = mContainer.getShowImgList().size();
				layoutDelete.setVisibility(View.VISIBLE);
				mContainer.setDeleting(true);
				mContainer.changView();
				mContainer.notifyRefresh(0);
				mContainer.refreshTitleString(true);
				mContainer.getChildAt(mContainer.getChildCount() - 1)
						.setVisibility(View.GONE);
			} else if (edit.getText().toString()
					.equals(getString(R.string.templatedone))) {
				// mContainer.showEdit(false);
				edit.setText(R.string.templatedelete);
				layoutDelete.setVisibility(View.GONE);
				mContainer.setDeleting(false);
				mContainer.changView();
				mContainer.notifyRefresh(0);
				mContainer.refreshTitleString(false);
				mContainer.getChildAt(mContainer.getChildCount() - 1)
						.setVisibility(View.VISIBLE);
			}

			break;
		case R.id.img_header_view_back_key:
			// back键监听，如果在编辑模式，则取消编辑模式
			if (edit.getText().toString()
					.equals(getString(R.string.templatedone))) {
				// mContainer.showEdit(false);
				edit.setText(R.string.templatedelete);
				layoutDelete.setVisibility(View.GONE);
				mContainer.setDeleting(false);
				mContainer.changView();
				mContainer.notifyRefresh(0);
				mContainer.refreshTitleString(false);
				mContainer.getChildAt(mContainer.getChildCount() - 1)
				.setVisibility(View.VISIBLE);
				return;
			} else {
				if (mContainer.getShowImgList().size() == 0) {
					Intent intent = getIntent();
					if (intent.getBooleanExtra("close", false)) {
						gallery.finish();
					}
				} else {
					MoveItem itemAdd = createNoneMoveItem(
							"assets/img/choice_addto_normal_min.png",
							"assets/img/choice_addto_normal_min.png",
							R.drawable.choice_addto_normal, 4, 4,
							getString(R.string.template_kt_add), true, false,
							false, true, false);
					mContainer.getShowImgList().add(itemAdd);
					PhotoView.setShowList(mContainer.getShowImgList());
				}
				try {
					// 退出APP前，保存当前的Items，记得所有item的位置
					mDbUtils = DBUtils_openHelper.getInstance(this).getDb();
					List<MoveItem> list = mContainer.getAllMoveItems();
					mDbUtils.deleteAll(MoveItem.class);
					mDbUtils.saveAll(list);

				} catch (DbException e) {
					e.printStackTrace();
				}
			}
			finish();
			break;
		case R.id.template_delete:
			mContainer.deleteItem();
			break;
		default:
			break;
		}
	}

	class ScrollLayoutReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (null != mContainer) {
				if (intent.getAction().equals("tempdrag")) {
					getDataFromCache();
					mItemsAdapter = ScrollAdapter.getAdapterInstance(
							TemplateActivity.this, mList_other, width_screen,
							height_screen);
					mContainer.setSaAdapter(mItemsAdapter);
					mContainer.refreView();// 通知刷新
					mContainer.cancelAnimations();
				} else if (intent.getAction().equals("deletetemp")) {
					getDataFromCache();
					mItemsAdapter = ScrollAdapter.getAdapterInstance(
							TemplateActivity.this, mList_other, width_screen,
							height_screen);
					mContainer.setSaAdapter(mItemsAdapter);
					mContainer.notifyRefresh();// 刷新数据
					// new Handler().postDelayed(new Runnable() {
					//
					// @Override
					// public void run() {
					// mContainer.showEdit(false);
					// mContainer.showEdit(true);
					// }
					// }, 500);// 此处的延时是为了显示删除后其他模板的移动效果

				} else if (intent.getAction().equals("scrollupdata")) {
					getDataFromCache();
					mItemsAdapter = ScrollAdapter.getAdapterInstance(
							TemplateActivity.this, mList_other, width_screen,
							height_screen);
					mContainer.setSaAdapter(mItemsAdapter);
					mContainer.notifyRefresh();// 刷新数据
				}
			}

		}

	}

}
