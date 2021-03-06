package org.teitheapp;

import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import android.text.Html;
import android.widget.ExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class DiplomaNumber extends Activity implements LoginServiceDelegate {
	private ProgressDialog dialog;
	private DatabaseManager dbManager;
	private String cookie;
	private ExpandableListAdapter mAdapter;
	
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
	
	private class DownloadWebPageTask extends AsyncTask<Void, Void,Integer> {
		
		
		protected Integer doInBackground(Void... params) {
		
			Integer coursesLeft = null;
			String cookie = dbManager.getSetting("pithia_cookie").getText().split("\\s")[0];
			
			try {
				HttpGet get = new HttpGet(new URI(Constants.URL_PITHIA_MYGRADES));
				
				get.addHeader("Cookie", "login=True; " + cookie);
				
				DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
				HttpResponse response = defaultHttpClient.execute(get);
				
				String data = Net.readStringFromInputStream(response.getEntity().getContent(), "windows-1253");
				

				Document doc = Jsoup.parse(data);
				
				Elements averageTableRows = doc.getElementsByClass("subHeaderBack");
				
				Element averageTableColumn = null;
				//averageTableColumn = doc.getElementsMatchingText("ΓΕΝΙΚΑ ΣΥΝΟΛΑ ΠΕΡΑΣΜΕΝΩΝ ΜΑΘΗΜΑΤΩΝ").get(0);
				
				for (Element el:averageTableRows) {
					if (el.text().contains("ΓΕΝΙΚΑ ΣΥΝΟΛΑ ΠΕΡΑΣΜΕΝΩΝ ΜΑΘΗΜΑΤΩΝ")) {
						averageTableColumn = el;
						break;
					}
				}
				
				//Element averageTableColumn = averageTableRows.get(9);
				
				//Get number of courses
				Pattern pattern = Pattern.compile(".*:\\s([^\\s]+)\\s");
				Matcher matcher = pattern.matcher(averageTableColumn.text());
				
				matcher.find();
				
				coursesLeft = 35 - Integer.parseInt(matcher.group(1));
			
				Trace.i("courses", coursesLeft + "");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			dialog.dismiss();
			
			
			return coursesLeft;
		}
		
		protected void onPostExecute(Integer result) {
			setContentView(R.layout.diploma);
			TextView tv = (TextView)findViewById(R.id.diploma_text);
			Trace.i("courses", result + "");
			
			//hresult = 25;
			
			if (result > 1) {
				tv.setText(Html.fromHtml(getResources().getString(R.string.diploma_number_text1) + " <font color='green'><b>" + result + "</b></font> " + getResources().getString(R.string.diploma_number_text4)));
			}
			else if (result == 1) {
				tv.setText(Html.fromHtml(getResources().getString(R.string.diploma_number_text2) + "  <font color='green'><b>" + result + "</b></font> " + getResources().getString(R.string.diploma_number_text3)));
			}
			else {
				tv.setText(Html.fromHtml("<font color='green'>" + getResources().getString(R.string.diploma_number_text5) + "</font>"));
			}
		}
	}

	public void loginSuccess(String cookie, String am, String surname,
		String name, int loginMode) {
		// TODO Auto-generated method stub
		
		//dialog.dismiss();

		Trace.i("relogin", "true");
		new DownloadWebPageTask().execute();
		dialog.setMessage( getResources()
				.getString(R.string.reading_data));
		//dialog = ProgressDialog.show(this, "", getResources()
				//.getString(R.string.reading_data), true);
	}

	public void loginFailed(int status, int loginMode) {
		// TODO Auto-generated method stub
		
		dialog.dismiss();
		if (status == LoginService.RESPONSE_BADUSERPASS) {
			String studentColumnName = (loginMode == LoginService.LOGIN_MODE_HYDRA ? "hydra_student" : "pithia_student");
			String cookieColumnName = (loginMode == LoginService.LOGIN_MODE_HYDRA ? "hydra_cookie" : "pithia_cookie");
			
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