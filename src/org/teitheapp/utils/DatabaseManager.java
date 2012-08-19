package org.teitheapp.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.teitheapp.classes.Announcement;
import org.teitheapp.classes.MimeType;
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
	static final String tableMimeTypes = "mimetypes";

	public DatabaseManager(Context context) {
		super(context, dbName, null, 6);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL("CREATE TABLE " + tableSettingsName + " ('id' integer primary key autoincrement, 'name' text not null, 'data' text not null)");
		db.execSQL("CREATE TABLE " + tableAnnouncementsName + " ('id' integer primary key autoincrement, 'title' text not null, 'body' text not null, 'author' text not null, 'category' text not null, 'date' string, 'attachment_url' text not null, 'order' integer)");
		db.execSQL("CREATE TABLE " + tableMimeTypes + " ('id' integer primary key autoincrement, 'mime_type' text not null, 'ext' text not null)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

		db.execSQL("drop table if exists " + tableSettingsName);
		db.execSQL("drop table if exists " + tableAnnouncementsName);
		db.execSQL("drop table if exists " + tableMimeTypes);
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
		cv.put("'order'", ann.getOrder());
		
		db.insert(tableAnnouncementsName, null, cv);
		// db.execSQL(@"insert into settings(name, text) values (")
	}
	
	
	public ArrayList<Announcement> getAnnouncements() {
		ArrayList<Announcement> announcements = new ArrayList<Announcement>();
		SQLiteDatabase db = this.getWritableDatabase();
		
		Cursor c = db.query(tableAnnouncementsName, new String[] {"*"}, null,
				null, null, null, "order asc");

		String announcementTitle, announcementBody, announcementCategory, announcementAuthor, announcementAttachmentLink, announcementDate;
		Integer announcementOrder;
		
		while (c.moveToNext()) {
			announcementTitle = c.getString(c.getColumnIndex("title"));
			announcementBody = c.getString(c.getColumnIndex("body"));
			announcementAuthor = c.getString(c.getColumnIndex("author"));
			announcementCategory = c.getString(c.getColumnIndex("category"));
			announcementDate = c.getString(c.getColumnIndex("date"));
			announcementAttachmentLink = c.getString(c.getColumnIndex("attachment_url"));
			announcementOrder = c.getInt(c.getColumnIndex("order"));
			
			announcements.add(new Announcement(announcementBody, announcementCategory, announcementAuthor, announcementTitle, announcementAttachmentLink, announcementDate, announcementOrder));
		}
		
		return announcements;
	}

	
	public long getNumberOfAnnouncements() {
		SQLiteDatabase db = this.getWritableDatabase();
		
		String sql = "select count(*) from announcements";
		SQLiteStatement statement = db.compileStatement(sql);
		
		return statement.simpleQueryForLong();
	}
	
	public void removeAllAnnouncements() {
		SQLiteDatabase db = this.getWritableDatabase();
		
		String sql = "delete from " + tableAnnouncementsName;
		db.execSQL(sql);
	}
	
	public Announcement getAnnouncementAtIndex(int index) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		Cursor c = db.query(tableAnnouncementsName, new String[] {"*"}, null,
				null, null, null, "`order` asc");

		String announcementTitle, announcementBody, announcementCategory, announcementAuthor, announcementAttachmentLink, announcementDate;
		Integer announcementOrder;
		
		c.moveToPosition(index-1);
		
		announcementTitle = c.getString(c.getColumnIndex("title"));
		announcementBody = c.getString(c.getColumnIndex("body"));
		announcementAuthor = c.getString(c.getColumnIndex("author"));
		announcementCategory = c.getString(c.getColumnIndex("category"));
		announcementDate = c.getString(c.getColumnIndex("date"));
		announcementAttachmentLink = c.getString(c.getColumnIndex("attachment_url"));
		announcementOrder = c.getInt(c.getColumnIndex("order"));
			
		return new Announcement(announcementBody, announcementCategory, announcementAuthor, announcementTitle, announcementAttachmentLink, announcementDate, announcementOrder);
		
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
	
	public long getNumberOfMimeTypes() {
		SQLiteDatabase db = this.getWritableDatabase();
		
		String sql = "select count(*) from " + tableMimeTypes;
		SQLiteStatement statement = db.compileStatement(sql);
		
		//Check if the table isn't empty
		return statement.simpleQueryForLong();
	}
	
	public void insertMimeTypesFromInputStream(InputStream is) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		
		String curLine;
		
		try {
			while ((curLine = br.readLine()) != null) {
				db.execSQL(curLine);
				Trace.i("line", curLine);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public MimeType getMimeType(String ext) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		Cursor c = db.query(tableMimeTypes, new String[] {"*"}, "ext=?",
				new String[] {ext}, null, null, null);

		c.moveToFirst();
		
		MimeType mime = new MimeType(c.getString(c.getColumnIndex("mime_type")), (c.getString(c.getColumnIndex("ext"))));
		return mime;
	}
}
