package org.teitheapp.utils;

import java.util.ArrayList;
import java.util.Date;

import org.teitheapp.classes.Announcement;
import org.teitheapp.classes.Setting;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

public class DatabaseManager extends SQLiteOpenHelper {

	static final String dbName = "teitheapp_db";
	static final String tableSettingsName = "settings";
	static final String tableAnnouncementsName = "announcements";

	public DatabaseManager(Context context) {
		super(context, dbName, null, 3);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL("CREATE TABLE " + tableSettingsName + " (id integer primary key autoincrement, name text not null, data text not null)");
		db.execSQL("CREATE TABLE " + tableAnnouncementsName + " (id integer primary key autoincrement, title text not null, body text not null, author text not null, category text not null, date string, attachment_url text not null)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

		db.execSQL("drop table if exists " + tableSettingsName);
		db.execSQL("drop table if exists " + tableAnnouncementsName);
		onCreate(db);
	}

	public void insertSetting(Setting s) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();

		cv.put("name", s.getName());
		cv.put("data", s.getText());

		db.insert(tableSettingsName, null, cv);
		// db.execSQL(@"insert into settings(name, text) values (")
	}
	
	public void insertAnnouncement(Announcement ann) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();

		cv.put("title", ann.getTitle());
		cv.put("body", ann.getBody());
		cv.put("author", ann.getAuthor());
		cv.put("category", ann.getCategory());
		cv.put("date", ann.getDate());
		cv.put("attachment_url", ann.getAttachmentUrl());
		
		db.insert(tableAnnouncementsName, null, cv);
		// db.execSQL(@"insert into settings(name, text) values (")
	}
	
	
	public ArrayList<Announcement> getAnnouncements() {
		ArrayList<Announcement> announcements = new ArrayList<Announcement>();
		SQLiteDatabase db = this.getWritableDatabase();
		
		Cursor c = db.query(tableAnnouncementsName, new String[] {"*"}, null,
				null, null, null, null);

		String announcementTitle, announcementBody, announcementCategory, announcementAuthor, announcementAttachmentLink, announcementDate;
		
		while (c.moveToNext()) {
			announcementTitle = c.getString(c.getColumnIndex("title"));
			announcementBody = c.getString(c.getColumnIndex("body"));
			announcementAuthor = c.getString(c.getColumnIndex("author"));
			announcementCategory = c.getString(c.getColumnIndex("category"));
			announcementDate = c.getString(c.getColumnIndex("date"));
			announcementAttachmentLink = c.getString(c.getColumnIndex("attachment_url"));
			
			announcements.add(new Announcement(announcementBody, announcementCategory, announcementAuthor, announcementTitle, announcementAttachmentLink, announcementDate));
		}
		
		return announcements;
	}

	
	public long getNumberOfAnnouncements() {
		SQLiteDatabase db = this.getWritableDatabase();
		
		String sql = "select count(*) from announcements";
		SQLiteStatement statement = db.compileStatement(sql);
		
		return statement.simpleQueryForLong();
	}
	
	public Announcement getAnnouncementAtIndex(int index) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		Cursor c = db.query(tableAnnouncementsName, new String[] {"*"}, "id=?",
				new String[] { ""+ index }, null, null, null);

		String announcementTitle, announcementBody, announcementCategory, announcementAuthor, announcementAttachmentLink, announcementDate;
		
		c.moveToFirst();
		
		announcementTitle = c.getString(c.getColumnIndex("title"));
		announcementBody = c.getString(c.getColumnIndex("body"));
		announcementAuthor = c.getString(c.getColumnIndex("author"));
		announcementCategory = c.getString(c.getColumnIndex("category"));
		announcementDate = c.getString(c.getColumnIndex("date"));
		announcementAttachmentLink = c.getString(c.getColumnIndex("attachment_url"));
			
		return new Announcement(announcementBody, announcementCategory, announcementAuthor, announcementTitle, announcementAttachmentLink, announcementDate);
		
	}

	public Setting getSetting(String name) {
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor c = db.query(tableSettingsName, new String[] { "*" }, "name=?",
				new String[] { name }, null, null, null);

		if (c.getCount() == 0)
			return null;

		c.moveToFirst();

		Setting s = new Setting(c.getInt(0), c.getString(1), c.getString(2));

		return s;
	}

	public void deleteSetting(String name) {
		SQLiteDatabase db = this.getWritableDatabase();

		db.execSQL("delete from " + tableSettingsName + " where name = '"
				+ name + "'");
	}
}
