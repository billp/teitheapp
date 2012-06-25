package org.teitheapp.utils;

import java.util.Arrays;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import org.teitheapp.classes.*;

public class DatabaseManager extends SQLiteOpenHelper {

	static final String dbName="teitheapp_db";
	static final String tableSettingsName = "settings";
	
	public DatabaseManager(Context context) {
		super(context, dbName, null, 2); 
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL("CREATE TABLE " + tableSettingsName + " (id integer primary key autoincrement, name text not null, data text not null)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

		db.execSQL("drop table if exists " + tableSettingsName);
		onCreate(db);
	}
	
	public void insertSetting(Setting s) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		
		cv.put("name", s.getName());
		cv.put("data", s.getText());
	
		db.insert(tableSettingsName, null, cv);
		//db.execSQL(@"insert into settings(name, text) values (")
	}
	
	public Setting getSetting(String name) {
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor c = db.query(tableSettingsName, new String[] {"*"}, "name=?", new String[] {name}, null, null, null);
		
		if (c.getCount() == 0) return null;
		
		c.moveToFirst();
		
		Setting s = new Setting(c.getInt(0), c.getString(1), c.getString(2));
		
		return s;
	}
	
	public void deleteSetting(String name) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		db.execSQL("delete from " + tableSettingsName + " where name = '" + name + "'");
	}
}
