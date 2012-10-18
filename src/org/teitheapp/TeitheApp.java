package org.teitheapp;

import org.teitheapp.classes.Setting;
import org.teitheapp.utils.DatabaseManager;
import org.teitheapp.utils.Trace;

import android.R.bool;
import android.app.ActivityManager;
import android.app.TabActivity;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

public class TeitheApp extends TabActivity {
	private TabHost tabHost;
	private SharedPreferences preferences;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		setContentView(R.layout.main);
		setTabs();

		// prepare mimetype table
		/*
		 * Thread thread = new Thread(new Runnable() { public void run() { //
		 * TODO Auto-generated method stub DatabaseManager dbManager = new
		 * DatabaseManager(TeitheApp.this);
		 * 
		 * if (dbManager.getNumberOfMimeTypes() == 0) {
		 * dbManager.insertMimeTypesFromInputStream
		 * (getResources().openRawResource(R.raw.mime)); } } }); thread.start();
		 */

		// startService(new Intent(this, HydraAnnouncementsService.class));
	}

	private void setTabs() {
		tabHost = getTabHost();

		Intent emptyIntent = new Intent(this, Empty.class);

		addTab(R.string.tab_home_text, R.drawable.tab_ic_home, new Intent(this,
				Home.class));
		addTab(R.string.tab_info_text, R.drawable.tab_ic_info, new Intent(this,
				InformationsLauncher.class));
		addTab(R.string.tab_services_text, R.drawable.tab_ic_services,
				new Intent(this, ServicesLauncher.class));
		addTab(R.string.tab_extra_services_text,
				R.drawable.tab_ic_services_extra, new Intent(this,
						ExtraLauncher.class));
	}

	private void addTab(int labelId, int drawableId, Intent intent) {
		TabHost.TabSpec spec = tabHost.newTabSpec("tab" + labelId);

		View tabIndicator = LayoutInflater.from(this).inflate(
				R.layout.tab_indicator, getTabWidget(), false);

		TextView title = (TextView) tabIndicator.findViewById(R.id.title);
		title.setText(labelId);
		ImageView icon = (ImageView) tabIndicator.findViewById(R.id.icon);
		icon.setImageResource(drawableId);

		spec.setIndicator(tabIndicator);
		spec.setContent(intent);
		tabHost.addTab(spec);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Hold on to this
		// mMenu = menu;

		// Inflate the currently selected menu XML resource.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.title_icon, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// For "Title only": Examples of matching an ID with one assigned in
		// the XML
		case R.id.menu_settings:
			Intent intent = new Intent();
			intent.setClass(this, Preferences.class);
			startActivity(intent);
			return true;

		case R.id.menu_about:
			Toast.makeText(this, "Dive into the water!", Toast.LENGTH_SHORT)
					.show();
			return true;
		}

		return false;
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		boolean hydraNotificationsEnabled = preferences.getBoolean("hydra_notifications_enabled", false);
		Intent serviceIntent = new Intent(this, HydraAnnouncementsService.class);
		
		Trace.i("hydra_notifications_enabled", hydraNotificationsEnabled + "");
		
		DatabaseManager dbManager = new DatabaseManager(this);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		Setting hydraCookie = dbManager.getSetting("hydra_cookie");
		
		if (hydraCookie == null) {
			Trace.i("hydra_service", "no login");
		}

		
		if (hydraNotificationsEnabled) {
			if (!isServiceRunning() && hydraCookie != null && isOnline()) {
				startService(serviceIntent);
			//	Trace.i("hydra_notifications_enabled", "service_started");
			}
		} else {
			if (isServiceRunning()) {
				stopService(serviceIntent);
			//	Trace.i("hydra_notifications_enabled", "service_stopped");
			}
		}
	}
	
	private boolean isServiceRunning() {
	    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	    	Trace.i("service name", service.service.getClassName());
	        if ("org.teitheapp.HydraAnnouncementsService".equals(service.service.getClassName())) {
	        	 
	        	Trace.i("service", "is running");
	            return true;
	        }
	    }
	    Trace.i("service", "is not running");
	    return false;
	}

	public boolean isOnline() {
	    ConnectivityManager cm =
	        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}

	
}