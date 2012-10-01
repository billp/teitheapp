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
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class Buservice extends Activity {

	private SeekBar sb;
	private TextView tvprogress;
	private ImageView imgRefresh;
	private RadioButton rbNSS, rbTEI;
	private Button btnSend;

	private int startingPoint;
	private JSONObject lastBusLineUpdate;
	
	private ProgressDialog dialog;
	SharedPreferences preferences;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.busline);

		preferences = PreferenceManager.getDefaultSharedPreferences(Buservice.this);
		
	

		sb = (SeekBar) findViewById(R.id.seekBar1);
		tvprogress = (TextView) findViewById(R.id.txtPercent);
		imgRefresh = (ImageView) findViewById(R.id.imgRefresh);
		rbNSS = (RadioButton) findViewById(R.id.rbNSS);
		rbTEI = (RadioButton) findViewById(R.id.rbTEI);
		btnSend = (Button) findViewById(R.id.btnSetProgress);
		dialog = new ProgressDialog(this);


		sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			int stepSize = 5;

			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				progress = ((int) Math.round(progress / stepSize)) * stepSize;
				seekBar.setProgress(progress);
				tvprogress.setText(progress + "%");
				// TODO Auto-generated method stub

			}
		});

		imgRefresh.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				GetBusLines dl = new GetBusLines();
				dl.execute();
			}
		});
		
		rbNSS.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startingPoint = 1;
				btnSend.setEnabled(true);
			}
		});
		
		rbTEI.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startingPoint = 2;
				btnSend.setEnabled(true);
			}
		});
		
		btnSend.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				

				
				long lastBusInsertion = preferences.getLong("lastBusInsertionTime", 0);
				long now = new Date().getTime();
				
				Trace.i("lala", now - lastBusInsertion + "");
				
				if (now - lastBusInsertion < 2000) {
					return;
				}
				
				String url = null;
				if (now - lastBusInsertion > 60 * 60 * 15 * 1000) {
					url = String.format("%s?action=bus_line&mode=add&starting_point=%d&progress=%d",
							Constants.API_URL, startingPoint, sb.getProgress());
				 
				 
					SetBusProgress bp = new SetBusProgress();
					bp.execute(new String[]{url});
				} else {
					
					int busLineId = preferences.getInt("lastBusInsertionId", -1);
					
					url = String.format("%s?action=bus_line&mode=update&starting_point=%d&progress=%d&id=%d",
							Constants.API_URL, startingPoint, sb.getProgress(), busLineId);
				 
					SetBusProgress bp = new SetBusProgress();
					bp.execute(new String[]{url});					
				}
			}
		});
		
		GetBusLines dl = new GetBusLines();
		dl.execute();
	}

	private class GetBusLines extends AsyncTask<Void, Void, JSONArray> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			if (!dialog.isShowing()) {
			
				dialog = ProgressDialog.show(Buservice.this, "", getResources().getString(R.string.reading_data), true);
			}
		}

		protected JSONArray doInBackground(Void... params) {
			JSONArray busLines = null;

			try {
				HttpGet get = new HttpGet(new URI(String.format("%s/%s",
						Constants.API_URL, "?action=bus_line")));

				DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
				HttpResponse response = defaultHttpClient.execute(get);

				String data = Net.readStringFromInputStream(response
						.getEntity().getContent(), "utf-8");

				Trace.i("data", data);

				busLines = new JSONArray(data);

				Trace.i("number", busLines.length() + "");

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return busLines;
		}

		protected void onPostExecute(JSONArray busLines) {
			TextView tvHistory = (TextView) findViewById(R.id.txtHistory);
			ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar1);
			tvHistory.setText("");

			for (int i = 0; i < busLines.length(); i++) {
				try {
					JSONObject curBusLine = busLines.getJSONObject(i);

					String from = curBusLine.getString("starting_point");

					if (from.equals("1")) {
						from = "ΝΣΣ";
					} else {
						from = "ΤΕΙ";
					}

					String line = String.format(
							"Πλ.: %s%% Ενημέρωση: %s Από: %s",
							curBusLine.getString("progress"),
							curBusLine.getString("update_time"), from);

					tvHistory.append(line + "\n");

					if (i == 0) {
						pb.setProgress(curBusLine.getInt("progress"));
					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			dialog.dismiss();
			//dialog = null;
		}
	}
	
	private class SetBusProgress extends AsyncTask<String, Void, JSONObject> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			dialog = ProgressDialog.show(Buservice.this, "", getResources()
					.getString(R.string.updating_bus_line), true);
		}

		protected JSONObject doInBackground(String... params) {
			JSONObject jsonResponse = null;

			try {
				HttpGet get = new HttpGet(new URI(params[0]));

				DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
				HttpResponse response = defaultHttpClient.execute(get);

				String data = Net.readStringFromInputStream(response
						.getEntity().getContent(), "utf-8");

				Trace.i("data", data);

				jsonResponse = new JSONObject(data);

				//Trace.i("number", busLines.length() + "");

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return jsonResponse;
		}

		protected void onPostExecute(JSONObject jsonResponse) {
			try {
				lastBusLineUpdate = jsonResponse;
				int busUpdateId = jsonResponse.getInt("id");
				
				Editor pEditor = preferences.edit();
				pEditor.putLong("lastBusInsertionTime", new Date().getTime());
				pEditor.putInt("lastBusInsertionId", busUpdateId);
				
				pEditor.commit();
				
				GetBusLines dl = new GetBusLines();
				dl.execute();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//dialog.hide();
		}
	}

}
