package org.teitheapp;

import java.util.concurrent.TimeUnit;

import org.teitheapp.utils.DatabaseManager;
import org.teitheapp.utils.Trace;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

public class MyGrades extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		DatabaseManager dbManager = new DatabaseManager(this);
		Long time = Long.parseLong(dbManager.getSetting("pithia_cookie").getText().split("\\s")[1]);
		int minutesElapsed = (int)(TimeUnit.MILLISECONDS.toSeconds(new java.util.Date().getTime())-TimeUnit.MILLISECONDS.toSeconds(time))/60;
		Trace.i("time", minutesElapsed+"");
		
	}
	
	private class DownloadWebPageTask extends AsyncTask<Void, Void, String[]> {
		protected String[] doInBackground(Void... params) {
		
			return null;
		}
		
		protected void onPostExecute(String[] result) {
			
		}
	}
}