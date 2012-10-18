package org.teitheapp;

import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
import android.widget.ArrayAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MyCourseDeclaration extends Activity implements
		LoginServiceDelegate {
	private ProgressDialog dialog;
	private DatabaseManager dbManager;
	private String cookie;
	private ExpandableListAdapter mAdapter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		dbManager = new DatabaseManager(this);

		Long time = Long.parseLong(dbManager.getSetting("pithia_cookie")
				.getText().split("\\s")[1]);
		int minutesElapsed = (int) (TimeUnit.MILLISECONDS
				.toSeconds(new java.util.Date().getTime()) - TimeUnit.MILLISECONDS
				.toSeconds(time)) / 60;

		Trace.i("time", minutesElapsed + "");

		// Re-login if required
		if (minutesElapsed > Constants.PITHIA_LOGIN_TIMEOUT) {
			SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(this);

			LoginService ls = new LoginService(LoginService.LOGIN_MODE_PITHIA,
					preferences.getString("pithia_login", null),
					preferences.getString("pithia_pass", null), this);
			ls.login();

			dialog = ProgressDialog.show(this, "",
					getResources().getString(R.string.login_loading), true);

		} else {
			new DownloadWebPageTask().execute();
			dialog = ProgressDialog.show(this, "",
					getResources().getString(R.string.reading_data), true);
		}

		/*
		 * String[] values = new String[] { "Android", "iPhone",
		 * "WindowsMobile", "Blackberry", "WebOS", "Ubuntu", "Windows7",
		 * "Max OS X", "Linux", "OS/2" }; ArrayAdapter<String> adapter = new
		 * ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
		 * values); setListAdapter(adapter);
		 */

	}

	private class DownloadWebPageTask extends
			AsyncTask<Void, Void, ArrayList<String>> {

		protected ArrayList<String> doInBackground(Void... params) {

			ArrayList<String> arrCourses = new ArrayList<String>();
			String cookie = dbManager.getSetting("pithia_cookie").getText()
					.split("\\s")[0];

			try {
				HttpGet get = new HttpGet(new URI(
						Constants.URL_PITHIA_MY_DECLARATION));

				get.addHeader("Cookie", "login=True; " + cookie);

				DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
				HttpResponse response = defaultHttpClient.execute(get);

				String data = Net.readStringFromInputStream(response
						.getEntity().getContent(), "windows-1253");

				Document doc = Jsoup.parse(data);

				Elements courses = doc.getElementsByAttributeValueStarting(
						"onmouseover", "underline");

				for (int i = 0; i < courses.size(); i++) {
					arrCourses.add(courses.get(i).text());
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			dialog.dismiss();
			return arrCourses;

		}

		protected void onPostExecute(ArrayList<String> arrCourses) {
			setContentView(R.layout.my_declaration);
			ListView listView = (ListView) findViewById(R.id.my_declaration_listview);

			// First paramenter - Context
			// Second parameter - Layout for the row
			// Third parameter - ID of the TextView to which the data is written
			// Forth - the Array of data
			//arrCourses.toArray(new String[arrCourses.size()]
			Trace.i("rows", arrCourses.size() + "");
			
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(MyCourseDeclaration.this, android.R.layout.simple_list_item_1, android.R.id.text1, arrCourses.toArray(new String[arrCourses.size()]));
			listView.setAdapter(adapter);
			
		}
	}

	public void loginSuccess(String cookie, String am, String surname,
			String name, int loginMode) {
		// TODO Auto-generated method stub

		// dialog.dismiss();

		Trace.i("relogin", "true");
		new DownloadWebPageTask().execute();
		dialog.setMessage(getResources().getString(R.string.reading_data));
		// dialog = ProgressDialog.show(this, "", getResources()
		// .getString(R.string.reading_data), true);
	}

	public void loginFailed(int status, int loginMode) {
		// TODO Auto-generated method stub

		dialog.dismiss();
		if (status == LoginService.RESPONSE_BADUSERPASS) {
			String studentColumnName = (loginMode == LoginService.LOGIN_MODE_HYDRA ? "hydra_student"
					: "pithia_student");
			String cookieColumnName = (loginMode == LoginService.LOGIN_MODE_HYDRA ? "hydra_cookie"
					: "pithia_cookie");

			Toast.makeText(getBaseContext(), R.string.wrong_user_pass,
					Toast.LENGTH_SHORT).show();

			DatabaseManager dbManager = new DatabaseManager(this);
			dbManager.deleteSetting(studentColumnName);
			dbManager.deleteSetting(cookieColumnName);

		} else if (status == LoginService.RESPONSE_TIMEOUT) {

			Toast.makeText(getBaseContext(), R.string.net_timeout,
					Toast.LENGTH_SHORT).show();
		} else if (status == LoginService.RESPONSE_SERVICEUNAVAILABLE) {
			Toast.makeText(getBaseContext(),
					getResources().getString(R.string.pithia_down),
					Toast.LENGTH_SHORT).show();
		}

		finish();
	}

	public void netError(String errMsg) {
		// TODO Auto-generated method stub
		Toast.makeText(getBaseContext(), getResources().getString(R.string.net_error), Toast.LENGTH_LONG).show();
		finish();
	}
}