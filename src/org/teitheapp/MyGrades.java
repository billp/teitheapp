package org.teitheapp;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
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
import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Toast;

public class MyGrades extends Activity implements LoginServiceDelegate {
	private ProgressDialog dialog;
	private DatabaseManager dbManager;
	private String cookie;
	private ExpandableListAdapter mAdapter;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		dbManager = new DatabaseManager(this);
	    
		
		Long time = Long.parseLong(dbManager.getSetting("pithia_cookie").getText().split("\\s")[1]);
		int minutesElapsed = (int)(TimeUnit.MILLISECONDS.toSeconds(new java.util.Date().getTime())-TimeUnit.MILLISECONDS.toSeconds(time))/60;
	
		
		Trace.i("time", minutesElapsed+"");
		
		//Re-login if required
		if (minutesElapsed > Constants.PITHIA_LOGIN_TIMEOUT) {
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
			
			LoginService ls = new LoginService(LoginService.LOGIN_MODE_PITHIA, preferences.getString("pithia_login", null),  preferences.getString("pithia_pass", null), this);
			ls.login();
			
			dialog = ProgressDialog.show(this, "", getResources()
					.getString(R.string.login_loading), true);
		
		} else {
			new DownloadWebPageTask().execute();
			dialog = ProgressDialog.show(this, "", getResources()
					.getString(R.string.reading_data), true);
		}
		
	}
	
	private class DownloadWebPageTask extends AsyncTask<Void, Void, Object[]> {
		 
		protected Object[] doInBackground(Void... params) {
		
			 List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();
		     List<List<Map<String, String>>> childData = new ArrayList<List<Map<String, String>>>();
		     List<Map<String, String>> children = new ArrayList<Map<String, String>>();
		   
			
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("Cookie", cookie));
			
			String cookie = dbManager.getSetting("pithia_cookie").getText().split("\\s")[0];
			
			try {
				HttpGet get = new HttpGet(new URI(Constants.URL_PITHIA_MYGRADES));
				
				get.addHeader("Cookie", "login=True; " + cookie);
				
				DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
				HttpResponse response = defaultHttpClient.execute(get);
				
				String data = Net.readStringFromInputStream(response.getEntity().getContent(), "windows-1253");
				

				Document doc = Jsoup.parse(data);
				Elements tables = doc.getElementsByTag("table").get(12).getElementsByTag("td");
				

				  
				for (Element row : tables) {
					
					
					
					if (row.attr("class").equals("groupHeader")) {
					
						Trace.i("row", row.text());
						Map<String, String> curGroupMap = new HashMap<String, String>();
			           
						if (groupData.size() > 0) {
							//Trace.i("childData", children.size() + "");
							childData.add(children);
							children = new ArrayList<Map<String, String>>();
						}
						
						groupData.add(curGroupMap);
			            curGroupMap.put("NAME", row.text());
						
						
					}
					if (row.text().matches("\\(.+\\)\\s+.+")) {
		                Map<String, String> curChildMap = new HashMap<String, String>();
		                children.add(curChildMap);
						
						
						//Trace.i("row", row.text() + " -> " + row.siblingElements().get(row.siblingIndex()+4).text());
					
		                curChildMap.put("NAME", row.text());
		                curChildMap.put("GRADE", getResources().getString(R.string.grade) + ": " + row.siblingElements().get(row.siblingIndex()+4).text());
		                //children = new ArrayList<Map<String, String>>();
		                
		                
					}
					


					
					
					
					/*for (Element math : row.parent()) {
						Trace.i("row", math.text());
					}*/
					

				}
				
				//Add last children to last group
				childData.add(children);


				Elements averageTableRows = doc.getElementsByClass("subHeaderBack");
				
				Elements averageTableColumns = averageTableRows.get(9).getElementsByClass("error");
				Map<String, String> curGroupMap = new HashMap<String, String>();
				Map<String, String> curChildMap = new HashMap<String, String>();
				
				curGroupMap.put("NAME", getResources().getString(R.string.grades_average));
				groupData.add(curGroupMap);
				
				children = new ArrayList<Map<String, String>>();
				curChildMap.put("NAME", averageTableColumns.get(0).text().replace("-", ""));
				children.add(curChildMap);
				childData.add(children);
				//Trace.i("mo", averageTableColumns.get(0).text());
				
				
				
				
				//Trace.i("data", tables.size() + "");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			dialog.dismiss();
			
			//Trace.i("childData", childData.size() + "");
			
			return new Object[]{groupData, childData};
		}
		
		protected void onPostExecute(Object[] result) {
			setContentView(R.layout.my_grades);
			
			ExpandableListView exListView = (ExpandableListView)findViewById(R.id.my_grades_listview);
			
	        // Set up our adapter
			//mAdapter = new SimpleExpandableListAdapter(context, groupData, expandedGroupLayout, collapsedGroupLayout, groupFrom, groupTo, childData, childLayout, lastChildLayout, childFrom, childTo)
	        mAdapter = new SimpleExpandableListAdapter(
	                MyGrades.this,
	                (List<Map<String, String>>)result[0], 
	                R.layout.expanded_list_item1,
	                new String[] { "NAME" },
	                new int[] { R.id.explist_grouptext },
	                (List<List<Map<String, String>>>)result[1],
	                R.layout.expanded_list_item2,
	                new String[] { "NAME", "GRADE" },
	                new int[] { R.id.explist_text1, R.id.explist_text2 }
	                );
	        exListView.setAdapter(mAdapter);
		}
	}

	public void loginSuccess(String cookie, String am, String surname,
		String name, int loginMode) {
		// TODO Auto-generated method stub
		
		//dialog.dismiss();

		Trace.i("relogin", "true");
		new DownloadWebPageTask().execute();
		dialog.setMessage(getResources()
				.getString(R.string.reading_data));
		//dialog = ProgressDialog.show(this, "", getResources()
		//		.getString(R.string.reading_data), true);
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
		Toast.makeText(getBaseContext(), getResources().getString(R.string.net_error), Toast.LENGTH_LONG).show();
	}
	
}