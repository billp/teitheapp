package org.teitheapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.teitheapp.classes.LoginService;
import org.teitheapp.classes.LoginServiceDelegate;
import org.teitheapp.utils.DatabaseManager;
import org.teitheapp.utils.Net;
import org.teitheapp.utils.Trace;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class DownloadAttachment extends Activity implements
		LoginServiceDelegate {
	private DatabaseManager dbManager;
	private ProgressDialog dialog;
	private String path, url;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.select_path);
		
		// get the url
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		url = bundle.getString("url");

		dbManager = new DatabaseManager(this);
		
		Button btnDownload = (Button) findViewById(R.id.download_attachment);
		
		btnDownload.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				//dbManager = new DatabaseManager(DownloadAttachment.this);
				EditText etPath = (EditText)findViewById(R.id.attachment_path);
				path = etPath.getText().toString();
				
				Long time = Long.parseLong(dbManager.getSetting("hydra_cookie").getText().split("\\s")[1]);
				int minutesElapsed = (int)(TimeUnit.MILLISECONDS.toSeconds(new java.util.Date().getTime())-TimeUnit.MILLISECONDS.toSeconds(time))/60;
			
				Trace.i("time", minutesElapsed+"");
				
				//Re-login if required
				if (minutesElapsed > Constants.HYDRA_LOGIN_TIMEOUT) {
					SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(DownloadAttachment.this);
					
					LoginService ls = new LoginService(LoginService.LOGIN_MODE_HYDRA, preferences.getString("hydra_login", null),  preferences.getString("hydra_pass", null), DownloadAttachment.this);
					ls.login();
					
					dialog = ProgressDialog.show(DownloadAttachment.this, "", getResources()
							.getString(R.string.login_loading), true);
				
				} else {
					new DownloadWebPageTask().execute(url);
					dialog = ProgressDialog.show(DownloadAttachment.this, "", getResources()
							.getString(R.string.downloading_attachment), true);
				}
				
			}
		});

		EditText path = (EditText) findViewById(R.id.attachment_path);
		path.setText(Environment.getExternalStorageDirectory() + "/downloads");
	}

	private class DownloadWebPageTask extends AsyncTask<String, Void, Void> {
		
		private String filename, fullPath;
		
		protected Void doInBackground(String... params) {

			String cookie = dbManager.getSetting("hydra_cookie").getText()
					.split("\\s")[0];

			try {
				HttpGet get = new HttpGet(new URI(Constants.URL_HYDRA
						+ params[0]));

				get.addHeader("Cookie", cookie);

				DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
				HttpResponse response = defaultHttpClient.execute(get);

				Trace.i("url", Constants.URL_HYDRA + params[0]);
				

				filename = response.getFirstHeader("Content-Disposition").toString().split(";")[1].replaceAll("filename=\"([^\"]+)\"", "$1");
				filename = filename.replace("\\'", "'");
				filename = filename.replace("\\\"", "\"");
				
				fullPath = path + "/" + filename;
				
				Trace.i("headers", fullPath);

				Net.writeDataToFile(response.getEntity().getContent(), fullPath);
				
				// String data =
				// Net.readStringFromInputStream(response.getEntity().getContent(),
				// "windows-1253");

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			dialog.dismiss();

			return null;
		}

		protected void onPostExecute(Void v) {
			File attachment = new File(fullPath);
			Intent i = new Intent();
			
			
			String fileExtension = fullPath.replaceAll("[^.]+\\.(.+)", "$1");
			
			Trace.i("ext", getMimeType(fileExtension));
			
			String mimeType = getMimeType(fileExtension).trim();
			
			
			i.setAction(android.content.Intent.ACTION_VIEW);
			i.setDataAndType(Uri.fromFile(attachment), mimeType);
			startActivity(i);
			
			finish();
		}
	}

	public void loginSuccess(String cookie, String am, String surname,
			String name, int loginMode) {
			// TODO Auto-generated method stub
			
			//dialog.dismiss();

			Trace.i("relogin", "true");
			new DownloadWebPageTask().execute(url);
			dialog.setMessage( getResources()
					.getString(R.string.downloading_attachment));
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
		Toast.makeText(getBaseContext(),
				getResources().getString(R.string.net_error), Toast.LENGTH_LONG)
				.show();
		finish();
	}
	
	private String getMimeType(String ext) {
		BufferedReader br = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.mimetypes)));
		String line;
		
		Pattern pattern = Pattern.compile("(.*)\\t+(.*)");
		
		try {
			
			while ((line = br.readLine()) != null) {				
				Matcher m = pattern.matcher(line);
				
				m.find();
				
				String mime = m.group(1);
				
				String[] exts = m.group(2).split("\\s");
				
				for (String curExt : exts) {
					if (curExt.equals(ext)) {
						return mime;
					}
				}
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
}
