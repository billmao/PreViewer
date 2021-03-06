package com.ktouch.kdc.launcher4.launcher2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
//bill create
public class ScreenShotReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action =intent.getAction();
        if(action.equals("android.intent.action.IMAGEPREVIER_SCREEN_SHOT")){
        	
        	if(Launcher.getInstance()==null){
        		startLauncher(context);
        	}
        	else if(Launcher.getInstance().getLoadFlag()==true)
        	{
        		startLauncher(context);
        	}
        	else{
        		Launcher.getInstance().getDeskBitmap();
        	}
       }
        else if(action.equals("android.intent.action.IMAGEPREVIER_GETDESKIMAGE")){
        	if(Launcher.getInstance()!=null && Launcher.getInstance().getFinishFlag()==false){
        		Launcher.getInstance().getDeskBitmap();
        	}
        }
	}
	public void startLauncher(Context context){
  		Intent intent1 =new Intent();
		intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent1.setClass(context, Launcher.class);
		intent1.putExtra("action", "android.intent.action.IMAGEPREVIER_SCREEN_SHOT");
		context.startActivity(intent1);
	}
}
