package com.ktouch.kdc.launcher4.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AlbumFilterDatabase extends SQLiteOpenHelper {
	public SQLiteDatabase database;

	// 建立数据库
	public AlbumFilterDatabase(Context context) {
		super(context, "AlbumFilter.db", null, 1);
	}

	// 建立数据表
	@Override
	public void onCreate(SQLiteDatabase arg0) {
		String sql = "create table AlbumNameList(_id Integer primary key autoincrement,AlbumName TEXT,AlbumSize TEXT,Filt TEXT)";
		arg0.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
	}

	// 打开数据库
	public void openMyDataBase() {
		database = getWritableDatabase();
	}

	// 关闭数据库
	public void closeMyDataBase() {
		if (database != null) {
			database.close();
		}
	}

	// 增
	// 新建一条默认不隐藏的数据
	public void insertAlbumName(String AlbumName) {
		String sql = "insert into AlbumNameList(AlbumName,Filt)values('"
				+ AlbumName + "','N')";
		database.execSQL(sql);
	}

	// 新建一条指定隐藏属性的数据
	public void insertAlbumName(String AlbumName, String AlbumSize, String Filt) {
		String sql = "insert into AlbumNameList(AlbumName,AlbumSize,Filt)values('"
				+ AlbumName + "' ,'" + AlbumSize + "' , '" + Filt + "' )";
		database.execSQL(sql);
	}

	// 删
	// 根据name删除
	public void delByAlbumName(String AlbumName) {
		String sql = "delete from AlbumNameList where AlbumName= '" + AlbumName
				+ "' ";
		database.execSQL(sql);
	}

	// 清空表
	public void delAll() {
		String sql = "delete from AlbumNameList";
		database.execSQL(sql);
	}

	// 改
	// 根据name改变隐藏属性
	public void updataFiltByName(String name, String Filt) {
		String sql = "update AlbumNameList SET Filt = '" + Filt
				+ "' WHERE AlbumName= '" + name + "' ";
		database.execSQL(sql);
	}

	// 查
	// 查询数据库行数
	public int queryRaw() {
		Cursor cursor = null;
		String sql = "select * from AlbumNameList";
		cursor = database.rawQuery(sql, null);
		return cursor.getCount();
	}

	// 查询所有数据
	public Cursor queryAll() {
		Cursor cursor = null;
		String sql = "select * from AlbumNameList";
		cursor = database.rawQuery(sql, null);
		return cursor;
	}

	// 根据name查询隐藏属性
	public boolean queryFiltByAlbumName(String AlbumName) {
		Cursor cursor = null;
		String filter = "";
		String sql = "select * from AlbumNameList where AlbumName =  '"
				+ AlbumName + "' ";
		cursor = database.rawQuery(sql, null);
		cursor.moveToPrevious();
		if (cursor.moveToNext()) {
			filter = cursor.getString(cursor.getColumnIndex("Filt"));
		}

		if (filter.equals("Y")) {
			return true;
		} else {
			return false;
		}
	}

	// 查询是/否隐藏的数量
	public int queryAlbumNumByFilt(String Filt) {
		Cursor cursor = null;
		String sql = "select * from AlbumNameList where Filt =  '" + Filt
				+ "' ";
		cursor = database.rawQuery(sql, null);
		int count = cursor.getCount();
		cursor.close();
		return count;
	}

	// 查询name相册是否存在
	public boolean queryAlbumNameByAlbumName(String AlbumName, String Filt) {
		Cursor cursor = null;
		String sql = "select * from AlbumNameList where AlbumName =  '"
				+ AlbumName + "' and Filt =  '" + Filt + "' ";
		cursor = database.rawQuery(sql, null);
		int count = 0;
		count = cursor.getCount();
		cursor.close();
		if (count != 0) {
			return true;
		} else {
			return false;
		}
	}

	// bill add begin
	// 查询所有数据
	public Cursor queryAllHideFolder(String Filt) {
		Cursor cursor = null;
		String sql = "select * from AlbumNameList where Filt =  '" + Filt
				+ "' ";
		cursor = database.rawQuery(sql, null);
		return cursor;
	}
	// bill add end
}
