package org.teitheapp;

import java.net.URI;
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
import android.widget.TextView;
import android.widget.Toast;

public class TeacherInfo extends Activity implements LoginServiceDelegate {
	private ProgressDialog dialog;
	private DatabaseManager dbManager;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		dbManager = new DatabaseManager(this);
	    
		Long time = Long.parseLong(dbManager.getSetting("hydra_cookie").getText().split("\\s")[1]);
		int minutesElapsed = (int)(TimeUnit.MILLISECONDS.toSeconds(new java.util.Date().getTime())-TimeUnit.MILLISECONDS.toSeconds(time))/60;
	
		Trace.i("time", minutesElapsed+"");
		
		//Re-login if required
		if (minutesElapsed > Constants.HYDRA_LOGIN_TIMEOUT) {
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
			
			LoginService ls = new LoginService(LoginService.LOGIN_MODE_HYDRA, preferences.getString("hydra_login", null),  preferences.getString("hydra_pass", null), this);
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
			String cookie = dbManager.getSetting("hydra_cookie").getText().split("\\s")[0];
			
			Trace.i("Cookie", cookie);
			
			try {
				HttpGet get = new HttpGet(new URI(Constants.URL_HYDRA_TEACHER_INFO));
				
				get.addHeader("Cookie", cookie);

				DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
				HttpResponse response = defaultHttpClient.execute(get);
				
				String data = Net.readStringFromInputStream(response.getEntity().getContent(), "utf8");
				
				Trace.i("data", data);

				Document doc = Jsoup.parse(data);
				
				Elements tableRows = doc.getElementsByClass("data");
				
				for (Element el: tableRows ) {
					Elements children = el.getElementsByTag("td");
					
					Trace.i("dsa", children.get(0).text());
				}
				
				

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