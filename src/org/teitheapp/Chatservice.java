package org.teitheapp;

import java.net.URI;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.teitheapp.utils.Net;
import org.teitheapp.utils.Trace;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Chatservice extends Activity {
	
	
	private EditText editext;
	private TextView chatext;
	private Button btnsend;
	
	private ProgressDialog dialog;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chatservice);
		
		editext = (EditText)findViewById(R.id.editText1);
		chatext = (TextView)findViewById(R.id.txtchat);
		btnsend = (Button)findViewById(R.id.buttonsend);
		dialog = new ProgressDialog(this);
		
		GetChatRows gcr = new GetChatRows();
		gcr.execute();
		
		btnsend.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				SendChatRow scr = new SendChatRow();
				scr.execute();
			}
		});
		
	}
	
	
	
	private class GetChatRows extends AsyncTask<Void, Void, JSONArray> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			if (!dialog.isShowing()) {
			
				dialog = ProgressDialog.show(Chatservice.this, "", getResources().getString(R.string.reading_data), true);
			}
		}

		protected JSONArray doInBackground(Void... params) {
			JSONArray chatrows = null;

			try {
				HttpGet get = new HttpGet(new URI(String.format("%s/%s",
						Constants.API_URL, "?action=chat")));

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
			
			chatext.setText(Html.fromHtml(""));
			
			for (int i = 0; i < chatrows.length(); i++) {
				try {
					JSONObject curChatRow = chatrows.getJSONObject(i);

					String chatrow = curChatRow.getString("text");
					String name = curChatRow.getString("student_name");
					Date updateTime = new Date((long)curChatRow.getInt("update_time") * 1000);
					
					String strUpdateTime = String.format("[%d/%d %d:%02d]", updateTime.getDay(), updateTime.getMonth(), updateTime.getHours(), updateTime.getMinutes());
					
					String line = String.format(
							"%s &lt;%s&gt; %s",
							strUpdateTime,
							name, chatrow);

					
					chatext.append(Html.fromHtml(line + "<br />"));
					

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			dialog.dismiss();
			//dialog = null;
		}
	}
	
	private class SendChatRow extends AsyncTask<Void, Void, JSONArray> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			if (!dialog.isShowing()) {
			
				dialog = ProgressDialog.show(Chatservice.this, "", getResources().getString(R.string.reading_data), true);
			}
		}

		protected JSONArray doInBackground(Void... params) {
			JSONArray chatrows = null;

			try {
				HttpGet get = new HttpGet(new URI(String.format("%s?action=chat&mode=add&student_name=%s&text=%s",
						Constants.API_URL, "arxidakas", java.net.URLEncoder.encode(editext.getEditableText().toString(), "UTF-8"))));

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
			
			

			dialog.dismiss();
			//dialog = null;
		}
	}
	

}
