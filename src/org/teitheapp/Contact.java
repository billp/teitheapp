package org.teitheapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class Contact extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	
		StringBuilder html = new StringBuilder("");
		String mime = "text/html";
		String encoding = "utf-8";
		String line = "";
		
		BufferedReader br = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.contact)));
		
		try {
			while((line = br.readLine()) != null) {
				html.append(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		setContentView(R.layout.contact);
		WebView wvContact = (WebView)findViewById(R.id.contact);	
		wvContact.setBackgroundColor(0);
		wvContact.setBackgroundResource(R.drawable.backrepeat4);
		wvContact.loadDataWithBaseURL(null, html.toString(), mime, encoding, null);
	}

}
