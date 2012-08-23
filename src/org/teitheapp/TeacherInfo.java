package org.teitheapp;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.teitheapp.classes.LoginService;
import org.teitheapp.classes.LoginServiceDelegate;
import org.teitheapp.classes.Teacher;
import org.teitheapp.utils.DatabaseManager;
import org.teitheapp.utils.Net;
import org.teitheapp.utils.Trace;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TeacherInfo extends Activity implements LoginServiceDelegate {
	private ProgressDialog dialog;
	private DatabaseManager dbManager;
	private ArrayList<Teacher> teachers = new ArrayList<Teacher>();
	ArrayList<Teacher> teachersClone;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		dbManager = new DatabaseManager(this);
	    
		Long time = Long.parseLong(dbManager.getSetting("hydra_cookie").getText().split("\\s")[1]);
		int minutesElapsed = (int)(TimeUnit.MILLISECONDS.toSeconds(new java.util.Date().getTime())-TimeUnit.MILLISECONDS.toSeconds(time))/60;
	
		Trace.i("time", minutesElapsed+"");
		
		//Re-login if required
		if (minutesElapsed > Constants.HYDRA_LOGIN_TIMEOUT) {
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
			
			LoginService ls = new LoginService(LoginService.LOGIN_MODE_HYDRA, preferences.getString("hydra_login", null),  preferences.getString("hydra_pass", null), this);
			ls.login();
			
			dialog = ProgressDialog.show(this, "", getResources()
					.getString(R.string.login_loading), true);
		
		} else {
			new DownloadWebPageTask().execute();
			dialog = ProgressDialog.show(this, "", getResources()
					.getString(R.string.reading_data), true);
		}
		
	}
	
	private class DownloadWebPageTask extends AsyncTask<Void, Void, Void> {
		
		protected Void doInBackground(Void... params) {
		
			Bundle bundle = new Bundle();
			String cookie = dbManager.getSetting("hydra_cookie").getText().split("\\s")[0];
			
			Trace.i("Cookie", cookie);
			
			try {
				HttpGet get = new HttpGet(new URI(Constants.URL_HYDRA_TEACHER_INFO));
				
				get.addHeader("Cookie", cookie);

				DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
				HttpResponse response = defaultHttpClient.execute(get);
				
				String data = Net.readStringFromInputStream(response.getEntity().getContent(), "utf8");
				
				Trace.i("data", data);

				Document doc = Jsoup.parse(data);
				
				
				Elements tableRows = doc.getElementsByClass("data");
				
				
				
				for (int i = 3; i < tableRows.size(); i++) {
					Element curRow = tableRows.get(i);
					
					Elements children = curRow.getElementsByTag("td");
					
					String name, surname, role, phone, email;
					
					//Trace.i("row", curRow.html());					
					surname = children.get(0).text();
					name = children.get(1).text();
					role = children.get(2).text();
					phone = children.get(3).text();
					email = children.get(4).text();

					teachers.add(new Teacher(surname, name, role, phone, email));
					
					Trace.i("teacher: ", "name: " + name + ", surname: " + surname + ", role: " + role + ", phone: " + phone + ", email: " + email);
				}
				
				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			dialog.dismiss();
			
			return null;
		}
		
		protected void onPostExecute(Void v) {
			setContentView(R.layout.teacher_info);
			
			ListView list = (ListView)findViewById(R.id.teacher_list);
			
			final MySimpleArrayAdapter adapter = new MySimpleArrayAdapter(TeacherInfo.this);
			list.setAdapter(adapter);
			
			EditText filterText = (EditText)findViewById(R.id.filterText);

			filterText.addTextChangedListener(new TextWatcher() {
				public void onTextChanged(CharSequence s, int start, int before,
			      int count) {
					adapter.filter();
					adapter.notifyDataSetChanged();
			    }
			 
			    // @Override
			     public void beforeTextChanged(CharSequence s, int start, int count,
			      int after) {
			     }
			 
			     //@Override
			     public void afterTextChanged(Editable s) {
			     }
			});
			
			list.setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
					
					Intent intent = new Intent(TeacherInfo.this, ViewTeacher.class);
					intent.putExtra("teacher", (Serializable)teachersClone.get(position));
					startActivity(intent);
				}
			}); 
		}
	}

	public void loginSuccess(String cookie, String am, String surname,
		String name, int loginMode) {
		// TODO Auto-generated method stub
		
		//dialog.dismiss();

		Trace.i("relogin", "true");
		new DownloadWebPageTask().execute();
		dialog.setMessage( getResources()
				.getString(R.string.reading_data));
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
	
	public class MySimpleArrayAdapter extends ArrayAdapter<String> {
		private final Context context;


		public void filter() {
			teachersClone = new ArrayList<Teacher>();
			EditText filterText = (EditText)findViewById(R.id.filterText);
			
			for (Teacher curTeacher: teachers) {
				String fullName = curTeacher.getSurname() + " " + curTeacher.getName();
				
				String regex = "^" + filterText.getText().toString() + ".*";
				
				Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
				Pattern pattern2 = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
				
				Matcher m = pattern.matcher(curTeacher.getSurname());
				Matcher m2 = pattern2.matcher(curTeacher.getName());
				
				if (m.matches() || m2.matches()) {
					teachersClone.add(curTeacher);
				}
			}
		}
		
		
		public MySimpleArrayAdapter(Context context) {
			super(context, R.layout.teacher_row);
			filter();
			this.context = context;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.teacher_row, parent, false);
			
			TextView textView = (TextView) rowView.findViewById(R.id.teacher_row_txt1);
			TextView textView2 = (TextView) rowView.findViewById(R.id.teacher_row_txt2);
			
			String role = (teachersClone.get(position).getRole().equals("") ? "n/a" : teachersClone.get(position).getRole());
			
			textView.setText(teachersClone.get(position).getSurname() + " " + teachersClone.get(position).getName());
			textView2.setText(role);
		
			return rowView;
		}
		
		@Override
		public int getCount() {
		    return teachersClone.size();
		}
	}
	
	public void netError(String errMsg) {
		// TODO Auto-generated method stub
		Toast.makeText(getBaseContext(), getResources().getString(R.string.net_error), Toast.LENGTH_LONG).show();
	}
}