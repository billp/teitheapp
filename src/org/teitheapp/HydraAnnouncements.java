package org.teitheapp;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class HydraAnnouncements extends Activity implements
		LoginServiceDelegate {
	private ProgressDialog dialog;
	private DatabaseManager dbManager;
	private ArrayList<Announcement> announcements;
	private String cookie;
	private int selectedAnnouncementIndex = 1;
	private boolean updateInBackground = false;
	
	
	//views
	private TextView txtTitle, txtDate, txtAuthor, txtCurrent;
	private WebView wvHomeArticle;
	private ProgressBar progress;	
	private ImageView leftButton, rightButton;
	
	private DownloadWebPageTask announcementsDownloader;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		dbManager = new DatabaseManager(this);
		setContentView(R.layout.hydra_announcements_main);
		
		//Set onClick listeners (right/left buttons)
		txtTitle = (TextView)findViewById(R.id.txtHomeTitle);
		txtDate = (TextView)findViewById(R.id.txtDate);
		txtAuthor = (TextView)findViewById(R.id.txtAuthor);
		txtCurrent = (TextView)findViewById(R.id.txtCurrent);
		wvHomeArticle = (WebView)findViewById(R.id.txtHomeArticle);
		progress = (ProgressBar)findViewById(R.id.progress);
		
		wvHomeArticle.setBackgroundColor(0x00000000);
		
		leftButton = (ImageView)findViewById(R.id.imgArrowLeft);
		rightButton = (ImageView)findViewById(R.id.imgArrowRight);
		
		leftButton.setClickable(true);
		rightButton.setClickable(true);
		
		leftButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				moveToAnnouncementAtIndex(selectedAnnouncementIndex-1);
			}
		});
		
		rightButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				moveToAnnouncementAtIndex(selectedAnnouncementIndex+1);
			}
		});
		
		if (dbManager.getNumberOfAnnouncements() > 0) {
			//announcements = dbManager.getAnnouncements();
			
			moveToAnnouncementAtIndex(selectedAnnouncementIndex);
			progress.setVisibility(View.VISIBLE);
			updateInBackground = true;

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

			if (!updateInBackground) {
			
				dialog = ProgressDialog.show(this, "",
						getResources().getString(R.string.login_loading), true);
			}

		} else {
			announcementsDownloader = new DownloadWebPageTask();
			announcementsDownloader.execute();
			
			if (!updateInBackground) {
				dialog = ProgressDialog.show(this, "",
					getResources().getString(R.string.reading_data), true);
			}

			dbManager.close();
		}

	}
	
	private void moveToAnnouncementAtIndex(int index) {
		// TODO Auto-generated method stub
		
		selectedAnnouncementIndex = index;
		
		if (selectedAnnouncementIndex == 1) {
			leftButton.setVisibility(View.INVISIBLE);
		} else {
			leftButton.setVisibility(View.VISIBLE);
		}
		if (selectedAnnouncementIndex == dbManager.getNumberOfAnnouncements()) {
			rightButton.setVisibility(View.INVISIBLE);
		} else {
			rightButton.setVisibility(View.VISIBLE);
		}
		
		Announcement curAnnouncement = dbManager.getAnnouncementAtIndex(selectedAnnouncementIndex);
		
		Trace.i("ann", curAnnouncement.toString());
		
		txtTitle.setText(curAnnouncement.getTitle());
		wvHomeArticle.loadDataWithBaseURL("fake://fake", curAnnouncement.getBody(), "text/html", "utf-8", "");
		txtDate.setText(curAnnouncement.getDate());
		txtAuthor.setText(curAnnouncement.getAuthor());
		txtCurrent.setText(selectedAnnouncementIndex + "/" + dbManager.getNumberOfAnnouncements());
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		announcementsDownloader.cancel(false);
		super.onBackPressed();

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

				String curString;
				StringBuffer data = new StringBuffer();

				InputStream is = response.getEntity().getContent();
				InputStreamReader isr = new InputStreamReader(is, "utf8");
				while( (curString = Net.readStringFromInputStream(isr, 512)) != null ) {
					if (isCancelled()) {
						Trace.i("cancel", "cancelled!");
						return null;
					}
					data.append(curString);
				}
				
				
				//String data = Net.readStringFromInputStream(response
				//		.getEntity().getContent(), "utf8");

				Document doc = Jsoup.parse(data.toString());

				Elements rows = doc.getElementsByClass("data").tagName("tr");

				String announcementTitle, announcementBody, announcementCategory, announcementAuthor, announcementAttachmentLink, announcementDate;

				announcements = new ArrayList<Announcement>();
				
				for (int i = 4; i < rows.size(); i++) {

					Element el = rows.get(i);

					announcementBody = el.attr("onmouseover");

					String pattern = "return overlib\\(\'(.+?)\',TEXTCOLOR.+";
					announcementBody = announcementBody.replaceAll(pattern,
							"$1");
					
					announcementTitle = announcementBody.replaceAll("<div class=\"title\">([^>]+)</div>.*", "$1");
					announcementTitle = announcementTitle.replace("\\r", "");
					announcementTitle = announcementTitle.replace("&amp;", "&");
					announcementTitle = announcementTitle.replace("&quot;", "\"");
					announcementTitle = announcementTitle.replace("\\'", "\'");
					
					announcementBody = announcementBody.replaceAll("<div class=\"title\">[^>]+</div>(.*)", "$1");
					announcementBody = announcementBody.replace("\\r", "");
					announcementBody = announcementBody.replace("\\'", "\'");
					announcementBody = announcementBody.replace("&quot;", "\"");
					announcementBody = announcementBody.replace("&amp;", "&");
					
					
					//Replace all links with 'link'
					Pattern p = Pattern.compile("(https?://[^ <)]+)([ <)])");
					
					Matcher m = p.matcher(announcementBody);
					
					while (m.find()) {
						String url = m.group(1);
						announcementBody = announcementBody.replace(url, "<a href=\"" + url + "\">link</a>");
					}
					
					Trace.i("data", announcementBody);

					Elements children = el.getElementsByTag("td");

					announcementCategory = children.get(0).text();
					announcementAuthor = children.get(1).text();
					announcementDate = children.get(2).text();
					
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
				
				announcements = null;
				System.gc();
				Trace.i("announcements inserted to db:" , numberToAdd + "");
				


				// Trace.i("data", tables.size() + "");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}



			// Trace.i("childData", childData.size() + "");

			return null;
		}

		protected void onPostExecute(Void v) {
			if (!updateInBackground) {
				dialog.dismiss();
				
				//Move to first announcement
				moveToAnnouncementAtIndex(1);
			} else {
				progress.setVisibility(View.INVISIBLE);
			}
		}
	}

	public void loginSuccess(String cookie, String am, String surname,
			String name, int loginMode) {
		// TODO Auto-generated method stub

		// dialog.dismiss();
		dbManager.close();

		Trace.i("relogin", "true");
		announcementsDownloader = new DownloadWebPageTask();
		announcementsDownloader.execute();
		
		if (!updateInBackground) {
			dialog.setMessage(getResources().getString(R.string.reading_data));
		}
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
