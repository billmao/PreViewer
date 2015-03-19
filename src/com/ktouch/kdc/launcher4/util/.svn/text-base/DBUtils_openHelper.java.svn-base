package com.ktouch.kdc.launcher4.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.DbUtils.DbUpgradeListener;
/**
 * 
 * @author 朱永男
 *
 */
public class DBUtils_openHelper implements DbUpgradeListener{

	private static final String DATABASENAME = "imageviewer.db"; 
	private static final int DATABASEVERSION = 1;	
	private DbUtils db;
	private static DBUtils_openHelper instance = null;
	
	private DBUtils_openHelper(Context context){
		db = DbUtils.create(context, DATABASENAME, DATABASEVERSION, this);
        db.configAllowTransaction(true);
        db.configDebug(true);
	}
	
	public DbUtils getDb(){
		return db;
	}
	
	public static DBUtils_openHelper getInstance(Context context){
		if(instance == null){
			instance = new DBUtils_openHelper(context);
		}
		return instance;
	}

	@Override
	public void onUpgrade(DbUtils arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}


}
