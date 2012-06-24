package org.teitheapp.classes;

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
import org.teitheapp.Constants;
import org.teitheapp.R;
import org.teitheapp.utils.Net;
import org.teitheapp.utils.Trace;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

public class LoginService extends Activity {
	public final static int LOGIN_MODE_HYDRA = 1;
	public final static int LOGIN_MODE_PITHIA = 2;
	
	//Response constants
	public final static int RESPONSE_BADUSERPASS = 3;
	public final static int RESPONSE_SERVICEUNAVAILABLE = 4;
	public final static int RESPONSE_TIMEOUT = 5;
	
	private LoginServiceDelegate delegate;
	
	private String user, pass;

	public int LOGIN_MODE;

	public LoginService(int loginMode, String user, String pass, LoginServiceDelegate delegate) {
		super();
		LOGIN_MODE = loginMode;
		this.user = user;
		this.pass = pass;
		this.delegate = delegate;
	}

	public void login() {
		new DownloadWebPageTask().execute();
	}

	private class DownloadWebPageTask extends AsyncTask<Void, Void, String[]> {
		protected String[] doInBackground(Void... params) {
			String strResponse = null;
			String strCookie =  null;
			
			try {
				HttpPost post = null;

				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				String encoding = "utf-8";
				

				if (LOGIN_MODE == LOGIN_MODE_HYDRA) {
					strCookie = new DefaultHttpClient().execute(new HttpGet(new URI(Constants.URL_HYDRA_LOGIN))).getFirstHeader("Set-Cookie").getValue().split(";")[0];
					
					
					post = new HttpPost(new URI(Constants.URL_HYDRA_LOGIN));

					nameValuePairs.add(new BasicNameValuePair("am", user));
					nameValuePairs.add(new BasicNameValuePair("pass", pass));
					nameValuePairs.add(new BasicNameValuePair("login", "Login"));
				}

				else if (LOGIN_MODE == LOGIN_MODE_PITHIA) {
					encoding = "windows-1253";
					
					HttpGet get = new HttpGet(new URI(Constants.URL_PITHIA_LOGIN));

					post = new HttpPost(new URI(Constants.URL_PITHIA_LOGIN));

					DefaultHttpClient defaultHttpClient = new DefaultHttpClient();

					HttpResponse response = defaultHttpClient.execute(get);

					if (Net.readStringFromInputStream(response.getEntity().getContent(), encoding).contains("Ανακοίνωση")) {
						return new String[] {null, "service unavailable"};
					}

					//Trace.i("headers", strCookie);
					strCookie = response.getFirstHeader("Set-Cookie").getValue().split(";")[0];
					post.addHeader("Host", "pithia.teithe.gr");
					post.addHeader("Content-Type",
							"application/x-www-form-urlencoded");
					post.addHeader("Cookie", strCookie);
					nameValuePairs.add(new BasicNameValuePair("userName",
							user));
					nameValuePairs.add(new BasicNameValuePair("pwd", pass));

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
				
				HttpResponse response = defaultHttpClient.execute(post);
				
				//strCookie = response.getFirstHeader("Set-Cookie").getValue()
				//		.split(";")[0];
				InputStream data = response.getEntity().getContent();
				//Trace.i("headers", strCookie);

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
				return new String[]{null, "timeout"};
			} catch (Exception e) {
				Trace.e("error", e.toString());
			}
			return new String[] {strCookie, strResponse};
		}

		protected void onPostExecute(String[] result) {

			//dialog.dismiss();

			if (result[1].equals("timeout")) {
				
				delegate.loginFailed(RESPONSE_TIMEOUT);
				return;
			}
			
			if (LOGIN_MODE == LOGIN_MODE_HYDRA) {

				if (result[1].contains("Bad username/password")) {
					delegate.loginFailed(RESPONSE_BADUSERPASS);
				} else {

					String pattern = "<div class=\"txt\">([^<]+)<\\/div>";

					// Create a Pattern object
					Pattern r = Pattern.compile(pattern);

					// Now create matcher object.
					Matcher m = r.matcher(result[1]);

					String am, name, surName, fatherName;

					m.find();
					am = m.group(1).trim();
					m.find();
					String namePart = m.group(1).trim().replace(" of ", " ");
					String[] parts = namePart.split("\\W");

					name = parts[0];
					surName = parts[1];
					fatherName = parts[2];

					//Toast.makeText(
					//		getBaseContext(),
					//		getResources().getString(R.string.login_success)
					//				+ " " + name + " " + surName,
					//		Toast.LENGTH_LONG).show();

					//finish();
					delegate.loginSuccess(result[0], am, surName, name, LOGIN_MODE_HYDRA);
				}
			}

			else if (LOGIN_MODE == LOGIN_MODE_PITHIA) {
				
				if (result[1].equals("service unavailable")) {
					delegate.loginFailed(RESPONSE_SERVICEUNAVAILABLE);
					return;
				}
				
				else if (result[1].contains("Λάθος όνομα χρήστη")
						|| result[1].contains("Λάθος κωδικός πρόσβασης")) {
					
					delegate.loginFailed(RESPONSE_BADUSERPASS);
					
				} else {
					delegate.loginSuccess(result[0], null, null, null, LOGIN_MODE_PITHIA);
				}
				
				//Toast.makeText(getBaseContext(), msg,
				//		Toast.LENGTH_SHORT).show();
			}
		}
	}
}
