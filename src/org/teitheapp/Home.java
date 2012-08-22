package org.teitheapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.webkit.WebView;

public class Home extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);

		WebView webview = (WebView) findViewById(R.id.txtHomeArticle);
		
		String sampleText = getString(R.string.sample_text);
		
		sampleText = sampleText.replaceAll("(https?:\\/\\/[^\\s]+)", "<a href=\"$1\">$1</a>");
		
		String summary = String.format("<html><body style=\"text-align:justify;margin:0;padding:0 0 0 8px;word-wrap:break-word;\">%s</body></html>", sampleText);
		webview.getSettings().setDefaultTextEncodingName("utf-8"); 
		webview.loadData(summary, "text/html", "utf8");

		 //Set background color for webview to transparent
		
		int pixel=this.getWindowManager().getDefaultDisplay().getWidth();
		int dp = (int)(pixel/getResources().getDisplayMetrics().density);
		
		webview.setBackgroundColor(0x00000000);
		
		 
	}
}
