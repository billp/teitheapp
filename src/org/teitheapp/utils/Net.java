package org.teitheapp.utils;

import java.io.InputStream;
import java.io.InputStreamReader;
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
import org.teitheapp.Login;
import org.teitheapp.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Net extends Activity {


	
	public static String readStringFromInputStream(InputStream is, String charset) {
		StringBuilder strData = new StringBuilder();
		
		try {
			char[] buff = new char[512];
			InputStreamReader isr = new InputStreamReader(is, charset);
			int charsReaded = 0;
			while ((charsReaded = isr.read(buff)) != -1) {
				strData.append(buff, 0, charsReaded);
			}
		} catch (Exception e) {
			
		}
		return strData.toString();
	}
	
	
}
