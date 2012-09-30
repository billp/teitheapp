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
import org.teitheapp.classes.Setting;
import org.teitheapp.utils.DatabaseManager;
import org.teitheapp.utils.Net;
import org.teitheapp.utils.Trace;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class HydraAnnouncementsService extends Service implements
		LoginServiceDelegate {
	private DatabaseManager dbManager;
	private SharedPreferences preferences;
	private ArrayList<Announcement> announcements;
	private String cookie;
	private Thread thread;
	private Runnable runnable;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		updateAnnouncements();
		//showNotification();

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		if (thread != null) {
			thread.interrupt();
			Trace.i("service", "stopped");
		}
		
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	private void showNotification(int number) {
		// In this sample, we'll use the same text for the ticker and the
		// expanded notification
		// CharSequence text = getText(R.string.remote_service_started);
		String strNotification = "";
		if (number == 1) {
			strNotification = String.format("%d %s", number, getResources().getString(R.string.notification_new_announcemnt));
		} else {
			strNotification = String.format("%d %s", number, getResources().getString(R.string.notification_new_announcemnts));
		}
		
		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(R.drawable.hydra_announcement,
				strNotification, System.currentTimeMillis());

		// The PendingIntent to launch our activity if the user selects this
		// notification
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, HydraAnnouncements.class), 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(this, getResources().getString(R.string.pref_hydra_announcements), strNotification,
				contentIntent);
		
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		//notification.flags |= Notification.DEFAULT_SOUND;
		//notification.flags |= Notification.DEFAULT_VIBRATE;
		//notification.flags |= Notification.DEFAULT_LIGHTS;
		
		//long[] vibrate = {0,400,500};
		//notification.vibrate = vibrate;
		
		// Send the notification.
		// We use a string id because it is a unique number. We use it later to
		// cancel.
		NotificationManager mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mNM.notify(R.string.app_name, notification);
		
		MediaPlayer mPlayer = MediaPlayer.create(this, R.raw.notification);
		mPlayer.start();
		
		// Get instance of Vibrator from current Context
		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		// Vibrate for 300 milliseconds
		v.vibrate(1000);
		
		//PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "tag");
		//wl.acquire();
}

	public void updateAnnouncements() {
		
		if (timeOutExceded()) {
			 SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(this);
			 LoginService ls = new LoginService(LoginService.LOGIN_MODE_HYDRA,
					preferences.getString("hydra_login", null),
					preferences.getString("hydra_pass", null), this);
			ls.login();
			return;
		}
		
		if (thread != null) {
			thread.interrupt();
		}
		
		runnable = new Runnable() {
			public void run() {
				
				//handler = new Handler();
				
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("Cookie", cookie));

				String cookie = dbManager.getSetting("hydra_cookie").getText()
						.split("\\s")[0];

				int count = 0;

				try {
					HttpGet get = new HttpGet(new URI(
							Constants.URL_HYDRA_ANNOUNCEMENTS));

					get.addHeader("Cookie", cookie);

					DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
					HttpResponse response = defaultHttpClient.execute(get);

					String curString;
					StringBuffer data = new StringBuffer();

					InputStream is = response.getEntity().getContent();
					InputStreamReader isr = new InputStreamReader(is, "utf8");

					Integer totalLength = 0;

					while ((curString = Net.readStringFromInputStream(isr, 512)) != null) {

						totalLength += 512;

						if (totalLength > 1024 * 50) {
							break;
						}

						Trace.i("512 bytes: ", curString);

						data.append(curString);
					}

					isr.close();

					String name = dbManager.getSetting("hydra_student").getText().split(";")[1];
					
					//Check for server login timeout (hack)
					if (!data.toString().contains(name)) {
						//thread.stop();
						thread = new Thread(runnable);
						thread.start();
						return;
					}
					
					// String data = Net.readStringFromInputStream(response
					// .getEntity().getContent(), "utf8");

					Document doc = Jsoup.parse(data.toString());

					Elements rows = doc.getElementsByClass("data")
							.tagName("tr");

					String announcementTitle, announcementBody, announcementCategory, announcementAuthor, announcementAttachmentLink, announcementDate;

					announcements = new ArrayList<Announcement>();

					int step = 1;
					int order = 0;

					if (dbManager.getNumberOfAnnouncements() > 0) {
						order = dbManager.getAnnouncementMinimumOrder() - 1;
						step = -1;
					}

					for (int i = 4; i < rows.size() - 1; i++) {

						Element el = rows.get(i);

						announcementBody = el.attr("onmouseover");

						String pattern = "return overlib\\(\'(.+?)\',TEXTCOLOR.+";
						announcementBody = announcementBody.replaceAll(pattern,
								"$1");

						announcementTitle = announcementBody.replaceAll(
								"<div class=\"title\">([^>]+)</div>.*", "$1");
						announcementTitle = announcementTitle
								.replace("\\r", "");
						announcementTitle = announcementTitle.replace("&amp;",
								"&");
						announcementTitle = announcementTitle.replace("&quot;",
								"\"");
						announcementTitle = announcementTitle.replace("\\'",
								"\'");

						announcementBody = announcementBody.replaceAll(
								"<div class=\"title\">[^>]+</div>(.*)", "$1");
						announcementBody = announcementBody.replace("\\r", "");
						announcementBody = announcementBody
								.replace("\\'", "\'");
						announcementBody = announcementBody.replace("&quot;",
								"\"");
						announcementBody = announcementBody.replace("&amp;",
								"&");

						// Replace all links with 'link'
						Pattern p = Pattern
								.compile("(https?://[^ <)]+)([ <)])");

						Matcher m = p.matcher(announcementBody);

						while (m.find()) {
							String url = m.group(1);
							announcementBody = announcementBody.replace(url,
									"<a href=\"" + url + "\">link</a>");
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

						announcementAttachmentLink = announcementAttachmentLink
								.replace("&amp;", "&");

						Announcement newAnnouncement = new Announcement(
								announcementBody, announcementCategory,
								announcementAuthor, announcementTitle,
								announcementAttachmentLink, announcementDate,
								order);

						order += step;

						announcements.add(newAnnouncement);
					}

					for (int i = 0; i < announcements.size(); i++) {
						Announcement thisAnnouncement = announcements.get(i);

						if (dbManager.announcementExists(thisAnnouncement)) {
							break;
						}

						count++;

						dbManager.insertAnnouncement(thisAnnouncement);
					}
					
					Trace.i("number of announcements: ", count + "");

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (thread.isInterrupted()) {
					Trace.i("hydra_service", "interrupted");
					return;
				}
				Trace.i("hydra_service", "not interrupted");
				
				//Show notification if there are new announcements
				if (count > 0) {
					showNotification(count);
				}
				
				int interval = Integer.parseInt(preferences.getString("hydra_notifications_interval", ""));
				Trace.i("interval", interval + "");
				try {
					Thread.sleep(interval);
					thread = new Thread(runnable);
					thread.start();
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			}
		};

		thread = new Thread(runnable);
		thread.start();
	}
	
	public void loginSuccess(String cookie, String am, String surname,
			String name, int loginMode) {
		// TODO Auto-generated method stub

		// dialog.dismiss();
		dbManager.close();

		Trace.i("relogin", "true");
		updateAnnouncements();
		// dialog = ProgressDialog.show(this, "", getResources()
		// .getString(R.string.reading_data), true);
	}

	public void loginFailed(int status, int loginMode) {
		// TODO Auto-generated method stub

		//dialog.dismiss();
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

		//finish();
	}
	
	
	public void netError(String errMsg) {
		try {
			Thread.sleep(60000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		updateAnnouncements();
	}
	
	public boolean timeOutExceded() {
		dbManager = new DatabaseManager(this);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		Setting hydraCookie = dbManager.getSetting("hydra_cookie");
		
		/*if (hydraCookie == null) {
			Trace.i("hydra_service", "no login");
			stopSelf();
			return false;
		}*/
		
		Long time = Long.parseLong(hydraCookie.getText().split("\\s")[1]);
		int minutesElapsed = (int) (TimeUnit.MILLISECONDS
				.toSeconds(new java.util.Date().getTime()) - TimeUnit.MILLISECONDS
				.toSeconds(time)) / 60;

		Trace.i("time", minutesElapsed + "");

		// Re-login if required
		if (minutesElapsed > Constants.HYDRA_LOGIN_TIMEOUT
				|| !dbManager.getSetting("last_ip").getText()
						.equals(Net.getLocalIpAddress())) {
			

			return true;
			
			/*
			 * SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(this);
			 * LoginService ls = new LoginService(LoginService.LOGIN_MODE_HYDRA,
					preferences.getString("hydra_login", null),
					preferences.getString("hydra_pass", null), this);
			ls.login();*/

		} else {
			
			return false;
			
			/*updateAnnouncements();
			dbManager.close();*/
		}
	}
}
