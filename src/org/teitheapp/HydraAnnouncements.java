package org.teitheapp;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
import org.teitheapp.classes.Announcement;
import org.teitheapp.classes.LoginService;
import org.teitheapp.classes.LoginServiceDelegate;
import org.teitheapp.utils.DatabaseManager;
import org.teitheapp.utils.Net;
import org.teitheapp.utils.Trace;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

public class HydraAnnouncements extends Activity implements
		LoginServiceDelegate {
	private ProgressDialog dialog;
	private DatabaseManager dbManager;
	private ArrayList<Announcement> announcements;
	private String cookie;
	private int selectedAnnouncementIndex = 0;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		dbManager = new DatabaseManager(this);

		
		if (dbManager.getNumberOfAnnouncements() > 0) {
			setContentView(R.layout.hydra_announcements_main);
			
			TextView txtTitle = (TextView)findViewById(R.id.txtHomeTitle);
			WebView wvHomeArticle = (WebView)findViewById(R.id.txtHomeArticle);
			
			announcements = dbManager.getAnnouncements();
			
			Announcement curAnnouncement = announcements.get(1);
			
			
			
			txtTitle.setText(curAnnouncement.getTitle());
			wvHomeArticle.loadDataWithBaseURL("fake://not/needed", curAnnouncement.getBody(), "text/html", "utf-8", "");
			
			return;
		}
		
		Long time = Long.parseLong(dbManager.getSetting("hydra_cookie")
				.getText().split("\\s")[1]);
		int minutesElapsed = (int) (TimeUnit.MILLISECONDS
				.toSeconds(new java.util.Date().getTime()) - TimeUnit.MILLISECONDS
				.toSeconds(time)) / 60;

		Trace.i("time", minutesElapsed + "");

		// Re-login if required
		if (minutesElapsed > Constants.HYDRA_LOGIN_TIMEOUT) {
			SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(this);

			LoginService ls = new LoginService(LoginService.LOGIN_MODE_HYDRA,
					preferences.getString("hydra_login", null),
					preferences.getString("hydra_pass", null), this);
			ls.login();

			dialog = ProgressDialog.show(this, "",
					getResources().getString(R.string.login_loading), true);

		} else {
			new DownloadWebPageTask().execute();
			dialog = ProgressDialog.show(this, "",
					getResources().getString(R.string.reading_data), true);

			dbManager.close();
		}

	}

	private class DownloadWebPageTask extends AsyncTask<Void, Void, Void> {

		protected Void doInBackground(Void... params) {

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("Cookie", cookie));

			String cookie = dbManager.getSetting("hydra_cookie").getText()
					.split("\\s")[0];

			try {
				HttpGet get = new HttpGet(new URI(
						Constants.URL_HYDRA_ANNOUNCEMENTS));

				get.addHeader("Cookie", "login=True; " + cookie);

				DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
				HttpResponse response = defaultHttpClient.execute(get);

				String data = Net.readStringFromInputStream(response
						.getEntity().getContent(), "utf8");

				Document doc = Jsoup.parse(data);

				Elements rows = doc.getElementsByClass("data").tagName("tr");

				String announcementTitle, announcementBody, announcementCategory, announcementAuthor, announcementAttachmentLink;
				Date announcementDate;

				for (int i = 4; i < rows.size(); i++) {

					Element el = rows.get(i);

					announcementBody = el.attr("onmouseover");

					String pattern = "(return overlib\\(\\')(.+?)(\\,TEXTCOLOR.+)";
					announcementBody = announcementBody.replaceAll(pattern,
							"$2");

					Trace.i("data", announcementBody);

					Elements children = el.getElementsByTag("td");

					announcementCategory = children.get(0).text();
					announcementAuthor = children.get(1).text();
					announcementDate = new Date(children.get(2).text());
					announcementTitle = children.get(3).text();

					
					Trace.i("ann", announcementCategory + " "
							+ announcementAuthor + " " + announcementTitle);

					// Export attachment link
					announcementAttachmentLink = children.get(4).html();

					pattern = ".* <a href=\"([^\"]+)\" class=\"veh-link\" target=\"_blank\">[^\\<]+</a>";

					announcementAttachmentLink = announcementAttachmentLink
							.replaceAll(pattern, "$1");

					Announcement newAnnouncement = new Announcement(announcementBody,
							announcementCategory, announcementAuthor,
							announcementTitle, announcementAttachmentLink,
							announcementDate);
					
					announcements.add(newAnnouncement);
				}
				
				//Trace.i("number", dbManager.getNumberOfAnnouncements() +"");
				
				//Add the required announcements to database
				int numberToAdd = announcements.size() - (int)dbManager.getNumberOfAnnouncements();
				
				for (int i = 0; i < numberToAdd; i++) {
					Announcement thisAnnouncement = announcements.get(i);
					dbManager.insertAnnouncement(thisAnnouncement);
				}
				Trace.i("announcements inserted to db:" , numberToAdd + "");

				// Trace.i("data", tables.size() + "");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			dialog.dismiss();

			// Trace.i("childData", childData.size() + "");

			return null;
		}

		protected void onPostExecute(Void v) {
			setContentView(R.layout.hydra_announcements_main);

		}
	}

	public void loginSuccess(String cookie, String am, String surname,
			String name, int loginMode) {
		// TODO Auto-generated method stub

		// dialog.dismiss();
		dbManager.close();

		Trace.i("relogin", "true");
		new DownloadWebPageTask().execute();
		dialog.setMessage(getResources().getString(R.string.reading_data));
		// dialog = ProgressDialog.show(this, "", getResources()
		// .getString(R.string.reading_data), true);
	}

	public void loginFailed(int status, int loginMode) {
		// TODO Auto-generated method stub

		dialog.dismiss();
		if (status == LoginService.RESPONSE_BADUSERPASS) {
			String studentColumnName = (loginMode == LoginService.LOGIN_MODE_HYDRA ? "hydra_student"
					: "pithia_student");
			String cookieColumnName = (loginMode == LoginService.LOGIN_MODE_HYDRA ? "hydra_cookie"
					: "pithia_cookie");

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
}
