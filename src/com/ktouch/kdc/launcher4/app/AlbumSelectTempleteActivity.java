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
public class AlbumSelectTempleteActivity extends Activity {
	private static final String KEY_MEDIA_PATH = "media-path";
	private String titleName;
	private Path mMediaSetPath;
	private MediaSet mMediaSet;
	private StickyListHeadersListView albumModeList;
	private AlbumActivityDataAdapter albumAdapter = null;
	private ImageView albumback;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_selectmode_album);
        Intent intent = getIntent();
        titleName = intent.getStringExtra("bucket");
    	Bundle data = intent.getExtras();
        mMediaSetPath = Path.fromString(data.getString(KEY_MEDIA_PATH));
        mMediaSet = ((GalleryApp) getApplication()).getDataManager().getMediaSet(mMediaSetPath);
		getViews();
		
	}
	public void getViews(){
        if(titleName!=null){
        	albumback = (ImageView)findViewById(R.id.albumback);
    		albumModeList = (StickyListHeadersListView)findViewById(R.id.albummodelist);
		    albumAdapter = new AlbumActivityDataAdapter(AlbumSelectTempleteActivity.this,(GalleryApp) getApplication(),mMediaSet,mMediaSetPath,albumModeList,"AlbumSelectModeActivity");
		    albumModeList.setAdapter(albumAdapter);    
		    albumAdapter.setSelectTemplate(true);
		    albumAdapter.resume();
		    albumback.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					AlbumSelectTempleteActivity.this.finish();
					albumAdapter.pause();
				}
		    	
		    });
        }
	}
}
