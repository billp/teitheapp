package org.teitheapp;

import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.teitheapp.utils.Net;
import org.teitheapp.utils.Trace;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Login extends Activity implements OnClickListener {

	public final static int LOGIN_MODE_HYDRA = 1;
	public final static int LOGIN_MODE_PITHIA = 2;

	private TextView tvDialogTitle;
	private EditText editLogin, editPass;
	private Button btnLogin, btnCancel;

	private SharedPreferences preferences;
	private ProgressDialog dialog;

	public int LOGIN_MODE;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alert_dialog_text_entry);

		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		tvDialogTitle = (TextView) findViewById(R.id.login_dialog_title);
		editLogin = (EditText) findViewById(R.id.username_edit);
		editPass = (EditText) findViewById(R.id.password_edit);
		btnLogin = (Button) findViewById(R.id.login_login_button);
		btnCancel = (Button) findViewById(R.id.login_cancel_button);

		btnLogin.setOnClickListener(this);
		btnCancel.setOnClickListener(this);

		Bundle extras = getIntent().getExtras();
		LOGIN_MODE = extras.getInt("login_mode");

		if (LOGIN_MODE == LOGIN_MODE_HYDRA) {
			tvDialogTitle.setText(R.string.pref_hydra);
			editLogin.setText(preferences.getString("hydra_login", ""));
			editPass.setText(preferences.getString("hydra_pass", ""));

		}

		else if (LOGIN_MODE == LOGIN_MODE_PITHIA) {
			tvDialogTitle.setText(R.string.pref_pithia);
			editLogin.setText(preferences.getString("pithia_login", ""));
			editPass.setText(preferences.getString("pithia_pass", ""));
		}
		


	}

	public void onClick(View v) {
		// TODO Auto-generated method stub

		switch (v.getId()) {
		case R.id.login_login_button:

			SharedPreferences.Editor editor = preferences.edit();

			if (editLogin.getText().toString().equals("")) return;
			if (editPass.getText().toString().equals("")) return;
			
			if (LOGIN_MODE == LOGIN_MODE_HYDRA) {
				editor.putString("hydra_login", editLogin.getText().toString());
				editor.putString("hydra_pass", editPass.getText().toString());
				editor.commit();

				DownloadWebPageTask task = new DownloadWebPageTask();
				task.execute();
				dialog = ProgressDialog.show(Login.this, "", getResources()
						.getString(R.string.login_loading), true);

			} else if (LOGIN_MODE == LOGIN_MODE_PITHIA) {
				editor.putString("pithia_login", editLogin.getText().toString());
				editor.putString("pithia_pass", editPass.getText().toString());
				editor.commit();

				DownloadWebPageTask task = new DownloadWebPageTask();
				task.execute();
				dialog = ProgressDialog.show(Login.this, "", getResources()
						.getString(R.string.login_loading), true);
			}
			InputMethodManager imm = (InputMethodManager)getSystemService(
				      Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(editLogin.getWindowToken(), 0);
				imm.hideSoftInputFromWindow(editPass.getWindowToken(), 0);

			break;
		case R.id.login_cancel_button:
			finish();
			break;
			
		}
	}

	private class DownloadWebPageTask extends AsyncTask<Void, Void, String> {
		protected String doInBackground(Void... params) {
			String strResponse = null;

			try {
				HttpPost post = null;

				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				String encoding = "utf-8";

				if (LOGIN_MODE == LOGIN_MODE_HYDRA) {
					post = new HttpPost(new URI(Constants.URL_HYDRA_LOGIN));

					nameValuePairs.add(new BasicNameValuePair("am", editLogin
							.getText().toString()));
					nameValuePairs.add(new BasicNameValuePair("pass", editPass
							.getText().toString()));
					nameValuePairs
							.add(new BasicNameValuePair("login", "Login"));
				}

				else if (LOGIN_MODE == LOGIN_MODE_PITHIA) {
					encoding = "windows-1253";
					
					HttpGet get = new HttpGet(new URI(
							Constants.URL_PITHIA_LOGIN));

					post = new HttpPost(new URI(Constants.URL_PITHIA_LOGIN));

					DefaultHttpClient defaultHttpClient = new DefaultHttpClient();

					HttpResponse response = defaultHttpClient.execute(get);

					if (Net.readStringFromInputStream(response.getEntity().getContent(), encoding).contains("Ανακοίνωση")) {
						return "service unavailable";
					}

					Trace.i("headers", Arrays.toString(response.getAllHeaders()));
					
					post.addHeader("Host", "pithia.teithe.gr");
					post.addHeader("Content-Type",
							"application/x-www-form-urlencoded");
					post.addHeader("Cookie",
							response.getFirstHeader("Set-Cookie").getValue()
									.split(";")[0]);

					nameValuePairs.add(new BasicNameValuePair("userName",
							editLogin.getText().toString()));
					nameValuePairs.add(new BasicNameValuePair("pwd", editPass
							.getText().toString()));
					nameValuePairs.add(new BasicNameValuePair("submit1",
							"Είσοδος"));
					nameValuePairs.add(new BasicNameValuePair("loginTrue",
							"login"));

					encoding = "windows-1253";
				}

				HttpParams httpParameters = new BasicHttpParams();
				// Set the timeout in milliseconds until a connection is established.
				// The default value is zero, that means the timeout is not used. 
				int timeoutConnection = 5000;
				HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
				// Set the default socket timeout (SO_TIMEOUT) 
				// in milliseconds which is the timeout for waiting for data.
				int timeoutSocket = 15000;
				HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
				
				
			      
				
				DefaultHttpClient defaultHttpClient = new DefaultHttpClient(httpParameters);
				post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				Trace.i("headers", Arrays.toString(post.getAllHeaders()));
				HttpResponse response = defaultHttpClient.execute(post);

				InputStream data = response.getEntity().getContent();

				strResponse = Net.readStringFromInputStream(data, encoding);
			

				/*
				 * byte[] buffer = new byte[512]; int bytesReaded = 0; while
				 * ((bytesReaded = data.read(buffer)) != -1) {
				 * content.write(buffer, 0, bytesReaded);
				 * 
				 * Trace.i("http", new String(buffer));
				 * 
				 * }
				 */

			} catch (SocketTimeoutException e) {
				return "timeout";
			} catch (Exception e) {
				Trace.e("error", e.toString());
			}
			return strResponse;
		}

		protected void onPostExecute(String result) {

			dialog.dismiss();

			if (result.equals("timeout")) {
				Toast.makeText(Login.this, R.string.net_timeout,
						Toast.LENGTH_SHORT).show();
				return;
			}
			
			if (LOGIN_MODE == LOGIN_MODE_HYDRA) {

				if (result.contains("Bad username/password")) {
					Toast.makeText(Login.this, R.string.wrong_user_pass,
							Toast.LENGTH_SHORT).show();
				} else {

					String pattern = "<div class=\"txt\">([^<]+)<\\/div>";

					// Create a Pattern object
					Pattern r = Pattern.compile(pattern);

					// Now create matcher object.
					Matcher m = r.matcher(result);

					String am, name, surName, fatherName;

					m.find();
					am = m.group(1).trim();
					m.find();
					String namePart = m.group(1).trim().replace(" of ", " ");
					String[] parts = namePart.split("\\W");

					name = parts[0];
					surName = parts[1];
					fatherName = parts[2];

					Toast.makeText(
							Login.this,
							getResources().getString(R.string.login_success)
									+ " " + name + " " + surName,
							Toast.LENGTH_LONG).show();

					finish();

				}
			}

			else if (LOGIN_MODE == LOGIN_MODE_PITHIA) {
				String msg = null;
				if (result.equals("service unavailable")) {
					msg = getResources().getString(R.string.pithia_down);
				}
				
				else if (result.contains("Λάθος όνομα χρήστη")
						|| result.contains("Λάθος κωδικός πρόσβασης")) {
					msg = getResources().getString(R.string.wrong_user_pass);

				} else {
					msg = "Logged in";
				}
				
				Toast.makeText(Login.this, msg,
						Toast.LENGTH_SHORT).show();

			}

		}
	}

}
