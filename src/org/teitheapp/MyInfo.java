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

public class MyInfo extends Activity implements LoginServiceDelegate {
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
	
	private class DownloadWebPageTask extends AsyncTask<Void, Void, Bundle> {
		
		
		protected Bundle doInBackground(Void... params) {
		
			Bundle bundle = new Bundle();
			String cookie = dbManager.getSetting("pithia_cookie").getText().split("\\s")[0];
			
			
			try {
				HttpGet get = new HttpGet(new URI(Constants.URL_PITHIA_MYINFO));
				
				get.addHeader("Cookie", "login=True; " + cookie);
				
				DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
				HttpResponse response = defaultHttpClient.execute(get);
				
				String data = Net.readStringFromInputStream(response.getEntity().getContent(), "windows-1253");
				

				Document doc = Jsoup.parse(data);
				
				Elements tableRows = doc.getElementsByClass("tableBold");
				Elements tableRowsRegistration = doc.getElementsByAttributeValue("width", "80%").get(0).getElementsByClass("tablecell");
				
				String surname, name, aem, department, semester, program, registration_info;
				
				surname = tableRows.get(10).siblingElements().get(0).text();
				name = tableRows.get(11).siblingElements().get(0).text();
				aem = tableRows.get(12).siblingElements().get(0).text();
				department =  tableRows.get(13).siblingElements().get(0).text();
				semester = tableRows.get(14).siblingElements().get(0).text();
				program = tableRows.get(15).siblingElements().get(0).text();
		
				
				Trace.i("dsa", tableRowsRegistration.get(1).text());
				
				//registration_info = "";
				
				registration_info = "Ακάδ. έτος: " + tableRowsRegistration.get(0).text() + ", Περίοδος: " + tableRowsRegistration.get(1).text() + ", Εξάμηνο: " + tableRowsRegistration.get(2).text() ;
				
				bundle.putString("surname", surname);
				bundle.putString("name", name);
				bundle.putString("aem", aem);
				bundle.putString("department", department);
				bundle.putString("semester", semester);
				bundle.putString("program", program);
				bundle.putString("registration_info", registration_info);



				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			dialog.dismiss();
			return bundle;
			
			
		}
		
		protected void onPostExecute(Bundle bundle) {
			setContentView(R.layout.my_info);
			TextView tvSurname = (TextView)findViewById(R.id.my_info_surname);
			TextView tvName = (TextView)findViewById(R.id.my_info_name);
			TextView tvAem = (TextView)findViewById(R.id.my_info_aem);
			TextView tvDepartment = (TextView)findViewById(R.id.my_info_department);
			TextView tvSemester = (TextView)findViewById(R.id.my_info_semester);
			TextView tvProgram = (TextView)findViewById(R.id.my_info_program);
			TextView tvRegistrationInfo = (TextView)findViewById(R.id.my_info_resistration);
			
			tvSurname.setText(bundle.getString("surname"));
			tvName.setText(bundle.getString("name"));
			tvAem.setText(bundle.getString("aem"));
			tvDepartment.setText(bundle.getString("department"));
			tvSemester.setText(bundle.getString("semester"));
			tvProgram.setText(bundle.getString("program"));
			tvRegistrationInfo.setText(bundle.getString("registration_info"));
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
	
}