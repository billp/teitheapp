package org.teitheapp;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.teitheapp.classes.Setting;
import org.teitheapp.utils.DatabaseManager;
import org.teitheapp.utils.Net;
import org.teitheapp.utils.Trace;

import android.R.bool;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothClass.Device;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.text.Html;
import android.text.format.Time;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class Chatservice extends Activity {

	private EditText editext;
	private TextView chatext;
	private Button btnsend;
	private ScrollView scrollView;
	private ProgressDialog dialog;
	String lastJsonHash = null;
	private String android_id;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chatservice);

		editext = (EditText) findViewById(R.id.editText1);
		chatext = (TextView) findViewById(R.id.txtchat);
		btnsend = (Button) findViewById(R.id.buttonsend);
		scrollView = (ScrollView) findViewById(R.id.scrollView1);
		dialog = new ProgressDialog(this);

		// GetChatRows gcr = new GetChatRows();
		// gcr.execute();

		if (!dialog.isShowing()) {
			dialog = ProgressDialog.show(Chatservice.this, "", getResources()
					.getString(R.string.reading_data), true);
		}

		final Handler handler = new Handler();

		final Runnable runn = new Runnable() {

			public void run() {
				// TODO Auto-generated method stub
				new GetChatRows().execute();
				handler.postDelayed(this, 2000);
			}
		};
		handler.postDelayed(runn, 0);

		btnsend.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!editext.getText().toString().equals("")) {
					SendChatRow scr = new SendChatRow();
					scr.execute();
				}
			}
		});

		android_id = Secure.getString(getBaseContext().getContentResolver(),
	            Secure.ANDROID_ID);
	}

	private class GetChatRows extends AsyncTask<Void, Void, JSONArray> {


		protected JSONArray doInBackground(Void... params) {
			JSONArray chatrows = null;

			try {
				HttpGet get = new HttpGet(new URI(String.format("%s?action=chat&devid=%s",
						Constants.API_URL, android_id)));

				DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
				HttpResponse response = defaultHttpClient.execute(get);

				String data = Net.readStringFromInputStream(response
						.getEntity().getContent(), "utf-8");

				Trace.i("data", data);

				chatrows = new JSONArray(data);

				Trace.i("number", chatrows.length() + "");

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return chatrows;
		}

		protected void onPostExecute(JSONArray chatrows) {
			StringBuilder str = new StringBuilder();

			for (int i = 0; i < chatrows.length(); i++) {
				try {
					JSONObject curChatRow = chatrows.getJSONObject(i);

					// Calendar mydate = Calendar.getInstance();
					// mydate.setTimeInMillis((long)curChatRow.getLong("update_time")*1000);

					String chatrow = curChatRow.getString("text");
					String name = curChatRow.getString("student_name");
					Date updateTime = new java.util.Date(
							(long) curChatRow.getLong("update_time") * 1000);

					Format dateFormat = new SimpleDateFormat("[dd/MM HH:mm]");

					String strUpdateTime = String.format("[%d/%d %d:%02d]",
							updateTime.getDay(), updateTime.getMonth(),
							updateTime.getHours(), updateTime.getMinutes());
					// String strUpdateTime = String.format("[%d/%d %d:%02d]",
					// mydate.DAY_OF_MONTH, mydate.MONTH, mydate.HOUR,
					// mydate.MINUTE);

					String line = String.format("%s &lt;%s&gt; %s",
							dateFormat.format(updateTime),
							String.format("<b>%s</b>", name), chatrow);

					str.append(line + "<br /><br />");

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			
			if (lastJsonHash == null 
					|| !lastJsonHash.equals(MD5(chatrows.toString()))) {

				chatext.setText(Html.fromHtml(str.toString()));

				scrollView.post(new Runnable() {
					public void run() {
						scrollView.fullScroll(View.FOCUS_DOWN);
					}
				});

			}

			lastJsonHash = MD5(chatrows.toString());


			dialog.dismiss();

			// dialog = null;
		}
	}

	private class SendChatRow extends AsyncTask<Void, Void, JSONArray> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			if (!dialog.isShowing()) {
				dialog = ProgressDialog.show(Chatservice.this, "",
						getResources().getString(R.string.reading_data), true);
			}
		}

		protected JSONArray doInBackground(Void... params) {
			JSONArray chatrows = null;

			try {
				DatabaseManager dbManager = new DatabaseManager(
						Chatservice.this);
				Setting pithiaStudent = dbManager.getSetting("pithia_student");
				SharedPreferences preferences = PreferenceManager
						.getDefaultSharedPreferences(Chatservice.this);
				boolean showName = preferences.getBoolean("chat_show_name",
						false);

				String name = "Anonymous";

				if (pithiaStudent != null && showName) {
					name = String.format("%s %s", pithiaStudent.getText()
							.split(";")[1],
							pithiaStudent.getText().split(";")[2]);
				}

				HttpGet get = new HttpGet(new URI(String.format(
						"%s?action=chat&mode=add&student_name=%s&text=%s&devid=%s",
						Constants.API_URL, java.net.URLEncoder.encode(name),
						java.net.URLEncoder.encode(editext.getEditableText()
								.toString(), "UTF-8"), android_id)));

				DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
				HttpResponse response = defaultHttpClient.execute(get);

				String data = Net.readStringFromInputStream(response
						.getEntity().getContent(), "utf-8");

				Trace.i("data", data);

				chatrows = new JSONArray(data);

				Trace.i("number", chatrows.length() + "");

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return chatrows;
		}

		protected void onPostExecute(JSONArray chatrows) {
			GetChatRows gcr = new GetChatRows();
			gcr.execute();

			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(editext.getWindowToken(), 0);

			editext.setText("");

			dialog.dismiss();
			// dialog = null;
		}
		
	}

	private static String convertToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9))
					buf.append((char) ('0' + halfbyte));
				else
					buf.append((char) ('a' + (halfbyte - 10)));
				halfbyte = data[i] & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}

	public static String MD5(String text)  {
		MessageDigest md;
		try {
		
		md = MessageDigest.getInstance("MD5");
		byte[] md5hash = new byte[32];
		md.update(text.getBytes("iso-8859-1"), 0, text.length());
		md5hash = md.digest();
		return convertToHex(md5hash);
		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}
	}

}
