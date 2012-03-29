package org.teitheapp;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

public class TeitheApp extends TabActivity {
	private TabHost tabHost;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		setTabs();
	}

	private void setTabs() {
		tabHost = getTabHost();

		Intent emptyIntent = new Intent(this, Empty.class);
		
		
		addTab(R.string.tab_home_text, R.drawable.tab_ic_home, new Intent(this, Home.class));
		addTab(R.string.tab_info_text, R.drawable.tab_ic_info, new Intent(this, InformationsLauncher.class));
		addTab(R.string.tab_services_text, R.drawable.tab_ic_services, emptyIntent);
		addTab(R.string.tab_extra_services_text, R.drawable.tab_ic_services_extra, emptyIntent);
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
}