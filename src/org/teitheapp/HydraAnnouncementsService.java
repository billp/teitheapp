package org.teitheapp;

import java.util.Date;

import org.teitheapp.utils.Trace;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

public class HydraAnnouncementsService extends Service {

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		showNotification();
		
		final Handler handler = new Handler();
		final Runnable r = new Runnable()
		{
		    public void run() 
		    {
		        Trace.i("service", "Hello World");
		        handler.postDelayed(this, 1000);
		    }
		};

		handler.postDelayed(r, 1000);

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
        return null;
	}
	
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        //CharSequence text = getText(R.string.remote_service_started);

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.arrow_down, "service started",
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, HydraAnnouncements.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, "Hydra",
                       "service_Started", contentIntent);

        // Send the notification.
        // We use a string id because it is a unique number.  We use it later to cancel.
        NotificationManager mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mNM.notify(R.string.app_name, notification);
    }

}
