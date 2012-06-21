package org.teitheapp;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.teitheapp.utils.Trace;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
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

			break;
		case R.id.login_cancel_button:
			finish();
			break;
		}
	}

	private class DownloadWebPageTask extends AsyncTask<Void, Void, String> {
		protected String doInBackground(Void... params) {
			ByteArrayOutputStream content = new ByteArrayOutputStream();

			try {
				HttpPost post = null;

				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

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
					post = new HttpPost(new URI(Constants.URL_PITHIA_LOGIN));

					nameValuePairs.add(new BasicNameValuePair("userName",
							editLogin.getText().toString()));
					nameValuePairs.add(new BasicNameValuePair("pwd", editPass
							.getText().toString()));
					nameValuePairs.add(new BasicNameValuePair("submit1",
							"Είσοδος"));
					nameValuePairs.add(new BasicNameValuePair("loginTrue",
							"login"));
				}


				
				DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
	            
				HttpResponse response = defaultHttpClient.execute(post);				
				
				Trace.i("Das", response.getFirstHeader("Set-Cookie").getValue());
				
				post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				
				InputStream data = response.getEntity().getContent();

				byte[] buffer = new byte[512];

				int bytesReaded = 0;
				while ((bytesReaded = data.read(buffer)) != -1) {
					content.write(buffer, 0, bytesReaded);
					
					//Trace.i("http", new String(buffer));
					
				}
				
			} catch (Exception e) {
				Trace.e("neterror", e.toString());
			}
			return content.toString();
		}

		protected void onPostExecute(String result) {

			dialog.dismiss();

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
				if (result.contains("<div class=\"error\"")) {
					Toast.makeText(Login.this, R.string.wrong_user_pass,
							Toast.LENGTH_SHORT).show();

					Trace.i("err", "error");
				} else {
					
				}
			}

		}
	}

}
