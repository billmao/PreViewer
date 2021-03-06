package com.ktouch.kdc.launcher4.app;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import com.ktouch.kdc.launcher4.R;
import com.ktouch.kdc.launcher4.data.MediaSet;
import com.ktouch.kdc.launcher4.data.Path;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
//bill create
public class AlbumCameraActivity extends Activity {
	private static final String KEY_MEDIA_PATH = "media-path";
	private String path;
	private Path mMediaSetPath;
	private MediaSet mMediaSet;
	private StickyListHeadersListView albumModeList;
	private AlbumActivityDataAdapter albumAdapter = null;
	private LinearLayout backLayout;
	private TextView titlenum;
	private RelativeLayout cameraPropmptLayout;
	private int curSelectPos = 0;
	private int top = 0;
	private int albumsetSelectIndex = 0; 			// 上次选择相册的索引，图片浏览时返回时使用
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_album_camera);
		titlenum = (TextView)findViewById(R.id.littlenum);
        Intent intent = getIntent();
        path = intent.getStringExtra("path");
        if(path.equals("")){
        	titlenum.setText("("+0+")");
        	backLayout = (LinearLayout)findViewById(R.id.backlayout);
        	cameraPropmptLayout = (RelativeLayout)findViewById(R.id.camerapropmptlayout);
        	backLayout.setOnClickListener(new OnClickListener(){

 				@Override
 				public void onClick(View v) {
 					// TODO Auto-generated method stub
 					AlbumCameraActivity.this.finish();
 				}
 		    	
 		    });
        	cameraPropmptLayout.setVisibility(View.VISIBLE);
        	return;
        }
        mMediaSetPath = Path.fromString(path);
        mMediaSet = ((GalleryApp) getApplication()).getDataManager().getMediaSet(mMediaSetPath);
        titlenum.setText("("+mMediaSet.getTotalMediaItemCount()+")");
		getViews();
		
	}
	public void getViews(){
        if(path!=null){
        	backLayout = (LinearLayout)findViewById(R.id.backlayout);
    		albumModeList = (StickyListHeadersListView)findViewById(R.id.albummodelist);
		    albumAdapter = new AlbumActivityDataAdapter(AlbumCameraActivity.this,(GalleryApp) getApplication(),mMediaSet,mMediaSetPath,albumModeList,"AlbumCameraActivity");
		    albumModeList.setAdapter(albumAdapter);    
		    albumAdapter.setSelectTemplate(false);
		    albumAdapter.resume();
		    backLayout.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					AlbumCameraActivity.this.finish();
					albumAdapter.pause();
				}
		    	
		    });
        }
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(albumAdapter!=null){
			albumAdapter.pause();
			mMediaSet = ((GalleryApp) getApplication()).getDataManager().getMediaSet(mMediaSetPath);
		    albumAdapter = new AlbumActivityDataAdapter(AlbumCameraActivity.this,(GalleryApp) getApplication(),mMediaSet,mMediaSetPath,albumModeList,"AlbumCameraActivity");
		    albumModeList.setAdapter(albumAdapter);    
		    albumAdapter.setSelectTemplate(false);
			albumAdapter.resume();
			titlenum.setText("("+mMediaSet.getTotalMediaItemCount()+")");
			getSetSelectedPos();
		}
	}
	//获取上次显示位置
    public void getSetSelectedPos(){    
    	albumModeList.setSelectionFromTop(curSelectPos,top);
    }
    
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if(albumModeList!=null){
			curSelectPos = albumModeList.getFirstVisiblePosition();  
			View v=albumModeList.getListChildAt(0);
			top = (v == null) ? 0 : v.getTop(); 
		}
	}
}
