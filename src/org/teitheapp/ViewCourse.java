package org.teitheapp;

import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.teitheapp.classes.LoginService;
import org.teitheapp.classes.LoginServiceDelegate;
import org.teitheapp.utils.DatabaseManager;
import org.teitheapp.utils.Net;
import org.teitheapp.utils.Trace;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ViewCourse extends Activity {
	private ProgressDialog dialog;
	private int semId, courseId;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		
		dialog = ProgressDialog.show(this, "",
				getResources().getString(R.string.reading_data), true);
		
		//semId = 6;
		//courseId = 4601;
		Bundle extras=getIntent().getExtras();
		
		semId = extras.getInt("sem_id");
		courseId = extras.getInt("course_id");
		
		new DownloadWebPageTask().execute();

	}

	private class DownloadWebPageTask extends
			AsyncTask<Void, Void, String> {

		StringBuilder strData = new StringBuilder();
		
		protected String doInBackground(Void... params) {

			try {
				HttpGet get = new HttpGet(new URI(Constants.URL_COURSES + "&semid=" + semId + "&cid=" + courseId));

				DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
				HttpResponse response = defaultHttpClient.execute(get);

				String data = Net.readStringFromInputStream(response
						.getEntity().getContent(), "utf-8");

				Document doc = Jsoup.parse(data);
				
				Elements tables = doc.getElementsByClass("courlist");
				
				
				for (int i = 3; i < tables.size(); i++) {
					Element curEl = tables.get(i);
					strData.append(curEl.html());
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
				return null;
				

			}

			return "<html><head></head><body>" + strData.toString() + "</body></html>";

		}

		protected void onPostExecute(String strData) {
			if (strData == null) {
				dialog.dismiss();
				Toast.makeText(getBaseContext(), getResources().getString(R.string.net_error), Toast.LENGTH_LONG).show();
				finish();
			}
			
			setContentView(R.layout.view_course);
			WebView wView = (WebView) findViewById(R.id.webView1);
			//wView.setBackgroundResource(R.drawable.backrepeat4);
			wView.setBackgroundColor(0);
			wView.loadDataWithBaseURL(null, strData, "text/html", "utf-8", null);

			dialog.dismiss();
		}
	}
}