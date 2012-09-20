package org.teitheapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class Stufflinks extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		StringBuilder html = new StringBuilder("");
		String mime = "text/html";
		String encoding = "utf-8";
		String line = "";
		
		BufferedReader br = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.stufflinks)));
		
		try {
			while((line = br.readLine()) != null) {
				html.append(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		setContentView(R.layout.stufflinks);
		WebView wvDsupport = (WebView)findViewById(R.id.stufflinks);	
		wvDsupport.setBackgroundColor(0);
		wvDsupport.setBackgroundResource(R.drawable.backg);
		wvDsupport.loadDataWithBaseURL(null, html.toString(), mime, encoding, null);
	}

}
