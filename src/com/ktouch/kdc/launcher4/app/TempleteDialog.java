package com.ktouch.kdc.launcher4.app;

import android.app.Dialog;
import android.content.Context;
//bill create
public class TempleteDialog extends Dialog {

	public TempleteDialog(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
	}
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		//从首页右上角进入模板编辑返回时直接退出gallery
		if(Gallery.gInstance.isMainEnterTemplete){
			Gallery.gInstance.finish();
			Gallery.gInstance.isMainEnterTemplete = false;
		}
		super.onBackPressed();

	}
	
}
