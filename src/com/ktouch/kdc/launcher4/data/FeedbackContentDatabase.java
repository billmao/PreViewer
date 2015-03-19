package com.ktouch.kdc.launcher4.data;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
//bill create
public class FeedbackContentDatabase extends SQLiteOpenHelper {
	private static final String TBL_NAME = "FeedbackInfo"; 
	private SQLiteDatabase db;
	public FeedbackContentDatabase(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String sql = "create table FeedbackInfo(_id Integer primary key autoincrement,content TEXT,time TEXT)";
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
	
	public void insert(ContentValues values) {  
        db = openDatabase();  
	    db.insert(TBL_NAME, null, values);  
	    closeDatabase();  
	}  
	
	public Cursor query() {  
		db = openDatabase();  
        Cursor c = db.query(TBL_NAME, null, null, null, null, null, null);  
        return c;  
	}  
	
	public SQLiteDatabase openDatabase() {  
		return this.getWritableDatabase();
    }
	
	public void closeDatabase() {  
		if (db != null)  
			db.close();  
    }  


}
