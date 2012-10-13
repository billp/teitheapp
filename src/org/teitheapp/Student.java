package org.teitheapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class Student extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	
		StringBuilder html = new StringBuilder("");
		String mime = "text/html";
		String encoding = "utf-8";
		String line = "";
		
		BufferedReader br = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.student)));
		
		try {
			while((line = br.readLine()) != null) {
				html.append(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		setContentView(R.layout.student);
		WebView wvStudent = (WebView)findViewById(R.id.student);	
		wvStudent.setBackgroundColor(0);
		wvStudent.setBackgroundResource(R.drawable.backrepeat4);
		wvStudent.loadDataWithBaseURL(null, html.toString(), mime, encoding, null);
	
	}

}
