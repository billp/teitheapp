package org.teitheapp;

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.teitheapp.utils.Net;
import org.teitheapp.utils.Trace;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class Buservice extends Activity {

	private SeekBar sb;
	private TextView tvprogress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.busline);
		
		DownloadWebPageTask dl = new DownloadWebPageTask();
		dl.execute();
		
		sb = (SeekBar) findViewById(R.id.seekBar1);
		tvprogress = (TextView) findViewById(R.id.txtPercent);
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
	}
	
	private class DownloadWebPageTask extends AsyncTask<Void, Void, JSONArray> {
		
		
		protected JSONArray doInBackground(Void... params) {
			JSONArray busLines = null;
			
			try {
				HttpGet get = new HttpGet(new URI(String.format("%s/%s", Constants.API_URL, "?action=bus_line")));
				
				DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
				HttpResponse response = defaultHttpClient.execute(get);
				
				String data = Net.readStringFromInputStream(response.getEntity().getContent(), "utf-8");
				
				Trace.i("data", data);

				busLines = new JSONArray(data);
				
				Trace.i("number", busLines.length()+"");

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return busLines;
		}
		
		protected void onPostExecute(JSONArray busLines) {
			TextView tvHistory = (TextView)findViewById(R.id.txtHistory);
			ProgressBar pb = (ProgressBar)findViewById(R.id.progressBar1);
			tvHistory.setText("");
			
			
			StringBuffer strLines = new StringBuffer();
			
			for (int i = 0; i < busLines.length(); i++) {
				try {
					JSONObject curBusLine = busLines.getJSONObject(i);
					
					String from = curBusLine.getString("starting_point");
					
					if (from.equals("1")) {
						from = "ΝΣΣ";
					} else {
						from = "ΤΕΙ";
					}
					
					String line = String.format("Πλ.:%s%% Ενημέρωση:%s Από:%s",
							curBusLine.getString("progress"),
							curBusLine.getString("update_time"),
							from
							);
					
					tvHistory.append(line + "\n");
					
					if (i == 0) {
						pb.setProgress(curBusLine.getInt("progress"));
					}
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}
		}
	}
}
