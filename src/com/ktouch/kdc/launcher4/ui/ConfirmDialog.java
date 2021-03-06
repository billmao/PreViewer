package com.ktouch.kdc.launcher4.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.ktouch.kdc.launcher4.R;
import com.ktouch.kdc.launcher4.app.AlbumActivityDataAdapter.SelectModeChange;
import com.ktouch.kdc.launcher4.app.Gallery;
import com.ktouch.kdc.launcher4.app.GalleryActivity;
import com.ktouch.kdc.launcher4.app.GalleryApp;
import com.ktouch.kdc.launcher4.app.PhotoPage;
import com.ktouch.kdc.launcher4.app.TemplateActivity;
import com.ktouch.kdc.launcher4.data.DataManager;
import com.ktouch.kdc.launcher4.data.MediaItem;
import com.ktouch.kdc.launcher4.data.Path;
import com.ktouch.kdc.launcher4.model.MoveItem;
import com.umeng.analytics.MobclickAgent;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/*
 * 调用dialog用于且不局限于删除图片用
 * 调用需要提供参数:
 * 1.Context context; 不解释
 * 2.Int theme 建议使用-->R.style.shareDialogTheme<--
 * 3.String title 用于显示需要确认的文本,如"是否确认删除?"
 * 4.boolean isSingle 用来标记是否为单张图片操作 主要是用来删除图片
 * 5.GalleryActivity mGalleryActivity 主要是用来删除图片
 * 本dialog用于其他用处,需要按需修改或添加构造函数,确认按钮的监听
 */
public class ConfirmDialog extends Dialog implements OnClickListener {
	// 设置布局
	private LinearLayout mLinearLayout;
	private LinearLayout mLinearLayout_child;
	private TextView mTextView;
	private Button Confirm, Cancel;
	private String title = "";
	private final int TEXTSIZE = 20;
	private final String TEXTCOLOR = "#434343";
	private final int SCREENWIDTH = getContext().getResources()
			.getDisplayMetrics().widthPixels;
	private final int SCREENHEIGHT = getContext().getResources()
			.getDisplayMetrics().heightPixels;
	private final int CONFIRM = 1;
	private final int CANCEL = 0;
	private LinearLayout.LayoutParams paramsButton, params;
	// 删除图片
	private GalleryActivity mGalleryActivity;
	private SelectionManager mSelectionManager;
	private MenuExecutor mMenuExecutor;
	private MenuExecutor.ProgressListener listener;
	private boolean isSingle, wantToDel;
	private Set<Path> mList = null;
	private GalleryApp app;
	private SelectModeChange mSelectModeChange;

	private String[] items;
	private boolean isListViewStyle;
	private ArrayList<MoveItem> itemList;

	public ConfirmDialog(Context context, int theme, String title) {// 非删除操作请new此构造方法
		super(context, theme);
		this.title = title;
		this.wantToDel = false;
	}

	public ConfirmDialog(Context context, GalleryActivity mGalleryActivity,
			int theme, String title, boolean isSingle) {// 删除图片请new此构造方法
		super(context, theme);
		this.title = title;
		this.mGalleryActivity = mGalleryActivity;
		this.isSingle = isSingle;
		this.wantToDel = true;
		this.isListViewStyle = false;
	}

	// bill add begin 20150122
	public ConfirmDialog(Context context, GalleryApp app, int theme,
			String title, boolean isSingle, Set<Path> list) {// 删除多张图片请new此构造方法
		super(context, theme);
		this.title = title;
		this.mGalleryActivity = null;
		this.isSingle = isSingle;
		this.wantToDel = true;
		this.mList = list;
		this.app = app;
		this.isListViewStyle = false;
	}

	// bill add end 20150122

	public ConfirmDialog(Context context, int theme,List<MoveItem> itemList) {// ListView样式请new此构造方法
		super(context, theme);
		this.itemList = (ArrayList<MoveItem>) itemList;
		this.wantToDel = false;
		this.isListViewStyle = true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (isListViewStyle) {
			init(getContext(), itemList);
		} else {
			init(getContext(), title);// 初始化布局
		}

		setContentView(mLinearLayout);// 加载布局

		if (isSingle) {// 删除单张图片所需资源
			mSelectionManager = new SelectionManager(mGalleryActivity, false);
			mMenuExecutor = new MenuExecutor(mGalleryActivity,
					mSelectionManager);
		}
	}

	private void init(Context context, final ArrayList<MoveItem> items) {// ListView样式初始化布局
		mLinearLayout = new LinearLayout(context);
		mLinearLayout.setOrientation(LinearLayout.VERTICAL);
		params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		params.width = (int) (SCREENWIDTH * 0.8);
		if(items.size()>7)
		params.height = (int) (SCREENHEIGHT * 0.6);
		mLinearLayout.setLayoutParams(params);
		mLinearLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));

		ListView mListView = new ListView(getContext());
		ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(
				getContext(), R.layout.textview_layout);
		for (int i = 0; i < items.size(); i++) {
			mArrayAdapter.add(items.get(i).getText_describe());
		}
		mListView.setAdapter(mArrayAdapter);
		mListView.setDivider(new ColorDrawable(Color.parseColor("#c2c1c1")));
		mListView.setDividerHeight(1);
		mListView.setSelector(new ColorDrawable(Color.parseColor("#eaeaea")));
		mListView.setLayoutParams(params);

		mLinearLayout_child = new LinearLayout(context);
		mLinearLayout_child.setOrientation(LinearLayout.HORIZONTAL);
		mLinearLayout_child.setLayoutParams(params);
		mLinearLayout_child.setBackgroundColor(Color.parseColor("#FFFFFF"));

		paramsButton = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		paramsButton.width = (int) (SCREENWIDTH * 0.8);
		paramsButton.height = (int) (SCREENHEIGHT * 0.15 / 2);

		Cancel = new Button(context);
		Cancel.setId(CANCEL);
//		Cancel.setGravity(Gravity.CENTER);
		Cancel.setLayoutParams(paramsButton);
		Cancel.setBackgroundResource(R.drawable.listview_confirm_dialog_cancel);
		Cancel.setTextSize(TEXTSIZE);
		Cancel.setTextColor(Color.parseColor(TEXTCOLOR));
		Cancel.setText("取消");

//		mLinearLayout_child.addView(Cancel);

		mLinearLayout.addView(mListView);
		mLinearLayout.addView(Cancel);

		Cancel.setOnClickListener(this);

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				HashMap<String, String> map = new HashMap<String, String>();// 友盟统计
				map.put("点击", items.get(position).getText_describe());// 友盟统计
				MobclickAgent.onEvent(getContext(), "LongClickDialog", map);// 友盟统计
				if(items.get(position).isFlag_add()){
					Intent mIntentTemplate = new Intent(Gallery
							.getGalleryIntance(), TemplateActivity.class);
					Gallery.getGalleryIntance().startActivityForResult(
							mIntentTemplate, 300);
				}else{
					if (items.get(position).isFlag_resouce()) {
						PhotoPage.setImageview(items.get(position).getImg_normal_int());
					} else {
						PhotoPage.setImageview(items.get(position).getImgurl());
					}
					PhotoView.showPosition = position;
				}
					

//				switch (position) {
//				case 0:
//					PhotoPage.setImageview(R.drawable.nothing);
//					break;
//				case 1:
//					PhotoPage.setImageview(R.drawable.ty_preview_lockscreen);
//					break;
//				case 2:
//					PhotoPage.setImageview(R.drawable.ty_preview_wallpaper);
//					break;
//				case 3:
//					// Intent mIntent = new Intent(
//					// "android.intent.action.IMAGEPREVIER_SCREEN_SHOT");
//					// getContext().sendBroadcast(mIntent);
//					break;
//				case 4:
//					
//					break;
//				}
				dismiss();
			}
		});
	}

	private void init(Context context, String title) {// 标准样式初始化布局
		mLinearLayout = new LinearLayout(context);
		mLinearLayout.setOrientation(LinearLayout.VERTICAL);
		params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		params.width = (int) (SCREENWIDTH * 0.8);
		mLinearLayout.setLayoutParams(params);
		mLinearLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));// 整体背景颜色

		mTextView = new TextView(context);
		mTextView.setGravity(Gravity.CENTER);
		mTextView.setLayoutParams(params);
		mTextView.setHeight((int) (SCREENHEIGHT * 0.15));
		mTextView.setTextSize(TEXTSIZE);
		mTextView.setTextColor(Color.parseColor(TEXTCOLOR));
		mTextView.setText(title);

		mLinearLayout_child = new LinearLayout(context);
		mLinearLayout_child.setOrientation(LinearLayout.HORIZONTAL);
		mLinearLayout_child.setLayoutParams(params);
		mLinearLayout_child.setBackgroundColor(Color.parseColor("#FFFFFF"));

		paramsButton = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		paramsButton.width = (int) (SCREENWIDTH * 0.4);
		paramsButton.height = (int) (SCREENHEIGHT * 0.15 / 2);

		Cancel = new Button(context);
		Cancel.setId(CANCEL);
		Cancel.setGravity(Gravity.CENTER);
		Cancel.setLayoutParams(paramsButton);
		Cancel.setBackgroundResource(R.drawable.del_confirm_dialog_selector_cancel);
		Cancel.setTextSize(TEXTSIZE);
		Cancel.setTextColor(Color.parseColor(TEXTCOLOR));
		Cancel.setText("取消");

		Confirm = new Button(context);
		Confirm.setId(CONFIRM);
		Confirm.setGravity(Gravity.CENTER);
		Confirm.setLayoutParams(paramsButton);
		Confirm.setBackgroundResource(R.drawable.del_confirm_dialog_selector_confirm);
		Confirm.setTextSize(TEXTSIZE);
		Confirm.setTextColor(Color.parseColor(TEXTCOLOR));
		Confirm.setText("确定");

		mLinearLayout_child.addView(Cancel);
		mLinearLayout_child.addView(Confirm);

		mLinearLayout.addView(mTextView);
		mLinearLayout.addView(mLinearLayout_child);

		Cancel.setOnClickListener(this);
		Confirm.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case CANCEL:
			dismiss();
			break;
		case CONFIRM:
			if (wantToDel && isSingle) {
				MobclickAgent.onEvent(getContext(), "DelSingle");
				deleteCurrentPhoto();
			} else if (wantToDel && !isSingle) {
				// 批量删除的方法请写在这里
				// bill add begin
				MobclickAgent.onEvent(getContext(), "DelMultiple");
				Iterator iterator = mList.iterator();
				while (iterator.hasNext()) {
					DataManager manager = (app).getDataManager();
					manager.delete((Path) iterator.next());
				}
				mSelectModeChange.onSelectModeChange();
			} else if (!wantToDel) {
				// 执行非删除操作的其他方法
			}
			dismiss();
			break;
		}
	}

	public void setSelectModeChange(SelectModeChange selectModeChange) {
		mSelectModeChange = selectModeChange;
	}

	// bill add end

	public void deleteCurrentPhoto() {// 删除当前图片
		MediaItem current = PhotoView.mModell.getCurrentMediaItem();
		Path path = current.getPath();
		mSelectionManager.deSelectAll();
		mSelectionManager.toggle(path);
		mMenuExecutor.startAction(R.id.action_confirm_delete, R.string.delete,
				listener);
		Toast.makeText(getContext(), R.string.delSuccessful, Toast.LENGTH_SHORT)
				.show();
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

}
