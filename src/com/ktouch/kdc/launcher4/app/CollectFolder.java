package com.ktouch.kdc.launcher4.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import com.ktouch.kdc.launcher4.R;
import com.ktouch.kdc.launcher4.util.FileUtil;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

//bill create
public class CollectFolder {
	private File collectFolder;
	private String collectPath ="我的收藏";
	//在sd卡内创建文件夹
	public CollectFolder(){
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED)) {
			collectPath = Environment.getExternalStorageDirectory().getPath()+"/"+collectPath;
		}
		else{
			return;
		}
		collectFolder = new File(collectPath);
		if(!collectFolder.exists()){
			collectFolder.mkdir();
		}
	}	
	//创建图片,fileName是图片的绝对路径
	public void addImageToFolder(String fileName,Context context){
		FileOutputStream outputStream = null;
		FileInputStream inputStream = null;
		int index=fileName.lastIndexOf("/");
		String name=fileName.substring(index+1);
		String imageName = collectPath+"/"+name;
		File sourceFile = new File(fileName);
		File imageFile = new File(imageName);
	    byte[] buffer = null;
		//获取剩余空间是否够用
		//long avilaleSize = getAvailaleSize();
	    long avilaleSize=FileUtil.getSDCardFreeSize();
		//进行提示空间不足
		if(avilaleSize < 5){
			Toast.makeText(context, R.string.sdcard_Lackofspace, Toast.LENGTH_SHORT).show();
			return;
		}
		try {
			outputStream = new FileOutputStream(imageFile);
			inputStream = new FileInputStream(fileName);
			buffer = new byte[(int)sourceFile.length()];
			inputStream.read(buffer);
			outputStream.write(buffer);

			inputStream.close();
			outputStream.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e("bill collectFolder exception", e.getMessage());
			e.printStackTrace();
		}
		//文件拷贝成功
		if(sourceFile.length()==imageFile.length()){
			Toast.makeText(context, R.string.addFavSuccessful, Toast.LENGTH_SHORT).show();
			Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);  
	        scanIntent.setData(Uri.fromFile(new File(imageName)));  
	        context.sendBroadcast(scanIntent);  
	        Gallery.gInstance.changeCollectStatus();//改变收藏状态
		}
		else{
			imageFile.delete();
			Toast.makeText(context, R.string.addFavFailed, Toast.LENGTH_SHORT).show();
		}
	}
	//文件是否已经被收藏
	//fileName 是图片绝对路径
	public static boolean isExistInCollect(String fileName){
		String path="";
		int index=fileName.lastIndexOf("/");
		String name=fileName.substring(index+1);
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED)) {
			path = Environment.getExternalStorageDirectory().getPath()+"/"+"我的收藏";
		}
		String imageName = path+"/"+name;
		File wallPaper = new File(imageName);
		if(!wallPaper.exists()){
			return false;
		}
		return true;
	}
	//删除收藏的壁纸
	public static void deleteCollectImage(String fileName,Context context){
		String path="";
		int index=fileName.lastIndexOf("/");
		String name=fileName.substring(index+1);
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED)) {
			path = Environment.getExternalStorageDirectory().getPath()+"/"+"我的收藏";
		}
		String imageName = path+"/"+name;
		File wallPaper = new File(imageName);
		if(wallPaper.exists()){
			wallPaper.delete();
		}
		Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);  
        scanIntent.setData(Uri.fromFile(new File(imageName)));  
        context.sendBroadcast(scanIntent);  
	}
}
