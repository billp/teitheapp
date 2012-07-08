package org.teitheapp;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
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

public class MyGrades extends Activity implements LoginServiceDelegate {
	private ProgressDialog dialog;
	private DatabaseManager dbManager;
	private String cookie;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		dbManager = new DatabaseManager(this);
		
		Long time = Long.parseLong(dbManager.getSetting("pithia_cookie").getText().split("\\s")[1]);
		int minutesElapsed = (int)(TimeUnit.MILLISECONDS.toSeconds(new java.util.Date().getTime())-TimeUnit.MILLISECONDS.toSeconds(time))/60;
	
		
		Trace.i("time", minutesElapsed+"");
		
		//Re-login if required
		if (minutesElapsed > Constants.PITHIA_LOGIN_TIMEOUT) {
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
			
			LoginService ls = new LoginService(LoginService.LOGIN_MODE_PITHIA, preferences.getString("pithia_login", null),  preferences.getString("pithia_pass", null), this);
			ls.login();
			
			dialog = ProgressDialog.show(this, "", getResources()
					.getString(R.string.login_loading), true);
		
		} else {
			new DownloadWebPageTask().execute();
			dialog = ProgressDialog.show(this, "", getResources()
					.getString(R.string.reading_data), true);
		}
		
	}
	
	private class DownloadWebPageTask extends AsyncTask<Void, Void, String[]> {
		protected String[] doInBackground(Void... params) {
		
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("Cookie", cookie));
			
			String cookie = dbManager.getSetting("pithia_cookie").getText().split("\\s")[0];
			
			try {
				HttpGet get = new HttpGet(new URI(Constants.URL_PITHIA_MYGRADES));
				
				get.addHeader("Cookie", "login=True; " + cookie);
				
				DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
				HttpResponse response = defaultHttpClient.execute(get);
				
				String data = Net.readStringFromInputStream(response.getEntity().getContent(), "windows-1253");
				

				Document doc = Jsoup.parse(data);
				Elements tables = doc.getElementsByTag("table").get(12).getElementsByTag("td");
				

				for (Element row : tables) {
					
					if (row.attr("class").equals("groupHeader")) {
					
						Trace.i("row", row.text());
					}
					if (row.text().matches("\\(.+\\)\\s+.+")) {
						Trace.i("row", row.text() + " -> " + row.siblingElements().get(row.siblingIndex()+4).text());
					}

					
					
					
					/*for (Element math : row.parent()) {
						Trace.i("row", math.text());
					}*/
					

				}


				
				Trace.i("data", tables.size() + "");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			dialog.dismiss();
			
			return null;
		}
		
		protected void onPostExecute(String[] result) {
			
		}
	}

	public void loginSuccess(String cookie, String am, String surname,
			String name, int loginMode) {
		// TODO Auto-generated method stub
		
		dialog.dismiss();

		Trace.i("relogin", "true");
		new DownloadWebPageTask().execute();
		dialog = ProgressDialog.show(this, "", getResources()
				.getString(R.string.reading_data), true);
	}

	public void loginFailed(int status, int loginMode) {
		// TODO Auto-generated method stub
		
	}
	
}