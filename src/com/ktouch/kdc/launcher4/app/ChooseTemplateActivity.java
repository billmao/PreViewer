package com.ktouch.kdc.launcher4.app;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ktouch.kdc.launcher4.R;
import com.ktouch.kdc.launcher4.ui.RoundedImageView;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.core.BitmapSize;
/**
 * 
 * @author 朱永男
 *
 */
public class ChooseTemplateActivity extends Activity implements OnClickListener {

	private ImageView back;
	private static ImageView template_Image;
	Button push;
	private TextView heatTitle;
	private EditText template_EditText;
	private String path = "";
	private int img_w;
	private int img_h;
	private int width_screen,height_screen;
	private UploadBroadReceiver uploadBroadReceiver;
	public  BitmapUtils bitmapUtils;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		height_screen = displayMetrics.heightPixels;
		width_screen = displayMetrics.widthPixels;
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choosetemplate);
		init();
	}

	public void init() {
		uploadBroadReceiver = new UploadBroadReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("upload");
		registerReceiver(uploadBroadReceiver, filter);
		back = (ImageView) findViewById(R.id.img_header_view_back_key);
		push = (Button) findViewById(R.id.header_view_update_bt);
		template_Image = (ImageView) findViewById(R.id.templateImage);
		template_EditText = (EditText) findViewById(R.id.templateName);
		heatTitle = (TextView) findViewById(R.id.tv_titlename);
		heatTitle.setText(getString(R.string.templateaddtitlename));
		push.setText(getResources().getString(R.string.templatedone));
		push.setVisibility(View.VISIBLE);
		img_w = (int) (width_screen*0.4);
		img_h = (int) (width_screen*0.7070);
//		template_Image.setLayoutParams(new LinearLayout.LayoutParams(img_w,img_h));
		template_Image.setImageResource(R.drawable.choose_img_button_selector);
//		template_Image.setPadding(0, 20, 0, 0);
		back.setOnClickListener(this);
		push.setOnClickListener(this);
		template_Image.setOnClickListener(this);
		bitmapUtils = com.ktouch.kdc.launcher4.util.BitmapHelp.getBitmapUtils(getApplicationContext());
        bitmapUtils.configDefaultBitmapConfig(Bitmap.Config.RGB_565);
        bitmapUtils.configDefaultBitmapMaxSize(new BitmapSize(img_w,img_h));
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}, 100);

	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:

			this.finish();

			break;

		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.header_view_update_bt:
			int setFlag = 0;
			if(path.equals("")){
				Toast.makeText(this, getString(R.string.chooseuploadhint),Toast.LENGTH_SHORT).show();
				return;
			}
			String templateName = template_EditText.getText().toString();
			if(templateName.trim().equals("")){
				setFlag = 1;
				templateName = getString(R.string.templateaddtitlecreate);
			}
			Intent intent = new Intent();
			intent.putExtra("describe", templateName);
			intent.putExtra("path", path);
			intent.putExtra("setfalg", setFlag);
			setResult(21, intent);
			this.finish();
			break;
		case R.id.img_header_view_back_key:
			this.finish();
			break;
		case R.id.templateImage:
			//bill modify begin 瑙ｅ喅浠庢枃浠跺す娴忚鍣ㄦ墦寮�鏃堕瑙堢晫闈笂闈㈡樉绀洪�夋嫨妯″紡闂
			//Intent i = new Intent(this,Gallery.class);;
			//startActivity(i);
			Intent mIntent = new Intent();
			mIntent.setClass(this, Gallery.class);
			mIntent.putExtra("albumsetpage", "AlbumSetPage");
			startActivity(mIntent);
			//bill modify end
			break;
		default:
			break;
		}

	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(null!=uploadBroadReceiver){
			unregisterReceiver(uploadBroadReceiver);
		}
	}
	private class UploadBroadReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("upload")){
            	if(null!=intent){
            		path = intent.getStringExtra("upload");
            		bitmapUtils.display(template_Image, path);  			
            	}
            }	
		}
		
	}

}
