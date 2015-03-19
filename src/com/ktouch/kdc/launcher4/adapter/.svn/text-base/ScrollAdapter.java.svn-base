package com.ktouch.kdc.launcher4.adapter;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ktouch.kdc.launcher4.R;
import com.ktouch.kdc.launcher4.model.MoveItem;
import com.ktouch.kdc.launcher4.ui.ScrollLayout.SAdapter;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.core.BitmapSize;

/**
 * 
 * @author 朱永男
 *
 */

public class ScrollAdapter implements SAdapter {

	private Context mContext;
	private LayoutInflater mInflater;
	
	private List<MoveItem> mList;
	private HashMap<Object,SoftReference<Drawable>> mCache;
	private int w,h;
	private BitmapFactory.Options options;
	public static BitmapUtils bitmapUtils;
	private static ScrollAdapter scrollAdapter = null;
	public ScrollAdapter(Context context, List<MoveItem> list,int w,int h) {
		
		this.mContext = context;
		this.mInflater = LayoutInflater.from(context);
		
		this.mList = list;
	    this.mCache = new HashMap<Object, SoftReference<Drawable>>();
	    
	    this.w = w;
	    this.h = h;
	    options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		bitmapUtils = com.ktouch.kdc.launcher4.util.BitmapHelp.getBitmapUtils(mContext.getApplicationContext());
//        bitmapUtils.configDefaultLoadingImage(R.drawable.ic_launcher_gallery);
//        bitmapUtils.configDefaultLoadFailedImage(R.drawable.ic_launcher_gallery);
        bitmapUtils.configDefaultBitmapConfig(Bitmap.Config.RGB_565);
        bitmapUtils.configDefaultBitmapMaxSize(new BitmapSize((int) (w * 0.24f), (int) (w * 0.41f)));
	    
	}

	public static ScrollAdapter getAdapterInstance(Context context, List<MoveItem> list,int w,int h){
		if(scrollAdapter==null){
			scrollAdapter = new ScrollAdapter(context, list, w, h);
		}
		return scrollAdapter;
		
	}
	
	@Override
	public View getView(int position) {
		View view = null;
		if (position < mList.size()) {
			MoveItem MoveItem = mList.get(position);
			view = mInflater.inflate(R.layout.template_item, null);
			ImageView iv = (ImageView) view.findViewById(R.id.content_iv);
			ImageView iv_seclect = (ImageView) view.findViewById(R.id.delete_iv);
			iv.setScaleType(ScaleType.FIT_XY);
			TextView tv = (TextView) view.findViewById(R.id.content_text);
			iv.setLayoutParams(new RelativeLayout.LayoutParams((int) (w*0.235f), (int) (w*0.41f)));//动态设置模板的宽高
			tv.setText(MoveItem.getText_describe());
//			if(MoveItem.isFlag_selected()){
//				iv_seclect.setVisibility(View.VISIBLE);
//			}else{
//				iv_seclect.setVisibility(View.GONE);
//			}
			if(MoveItem.isFlag_delete()||(!MoveItem.isFlag_resouce())){
				String imgUrl = MoveItem.getImgurl();
				bitmapUtils.display(iv, imgUrl);
//				iv.setImageBitmap(BitmapFactory.decodeFile(imgUrl));
			}else{
				String imgUrl = MoveItem.getImgurl();
				bitmapUtils.display(iv, imgUrl);
//				iv.setImageResource(MoveItem.getImg_normal_int());
			}	
//			iv.setOnClickListener(new OnClickListener() {
//				
//				@Override
//				public void onClick(View v) {
//					// TODO Auto-generated method stub
//					iv_seclect.setVisibility(iv_seclect.getVisibility()==View.VISIBLE?View.GONE:View.VISIBLE);
//				}
//			});
			view.setTag(MoveItem);
		}
		return view;
	}
	
	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public void exchange(int oldPosition, int newPositon) {
		MoveItem item = mList.get(oldPosition);
		mList.remove(oldPosition);
		mList.add(newPositon, item);
	}

	private OnDataChangeListener dataChangeListener = null;

	public interface OnDataChangeListener {
		void ondataChange();

	}

	public OnDataChangeListener getOnDataChangeListener() {
		return dataChangeListener;
	}

	public void setOnDataChangeListener(OnDataChangeListener dataChangeListener) {
		this.dataChangeListener = dataChangeListener;
	}

	public void delete(int position) {
		if (position < getCount()) {
			mList.remove(position);
		}
	}

	public void add(MoveItem item) {
		mList.add(item);
	}

	public MoveItem getMoveItem(int position) {
		return mList.get(position);
	}
	
	
	
	
	public List<MoveItem> getmList() {
		return mList;
	}

	public void setmList(List<MoveItem> mList) {
		this.mList = mList;
	}

	public void recycleCache() {
		if (mCache != null) {
			Set<Object> keys = mCache.keySet();
			for (Iterator<Object> it = keys.iterator(); it.hasNext();) {
				Object key = it.next();
				SoftReference<Drawable> reference = mCache.get(key);
				if (reference != null) {
					reference.clear();
				}
			}
			mCache.clear();
			mCache = null;
		}
	}

}
