package org.teitheapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class Esupport extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	
		StringBuilder html = new StringBuilder("");
		String mime = "text/html";
		String encoding = "utf-8";
		String line = "";
		
		BufferedReader br = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.esupport)));
		
		try {
			while((line = br.readLine()) != null) {
				html.append(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		setContentView(R.layout.esupport);
		WebView wvEsupport = (WebView)findViewById(R.id.esupport);	
		wvEsupport.setBackgroundColor(0);
		wvEsupport.setBackgroundResource(R.drawable.backrepeat4);
		wvEsupport.loadDataWithBaseURL(null, html.toString(), mime, encoding, null);
	
	
	}

}
