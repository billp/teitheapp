package org.teitheapp.utils;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Net extends Activity {


	
	public static String readStringFromInputStream(InputStream is, String charset) {
		StringBuilder strData = new StringBuilder();
		
		try {
			char[] buff = new char[512];
			InputStreamReader isr = new InputStreamReader(is, charset);
			int charsReaded = 0;
			while ((charsReaded = isr.read(buff)) != -1) {
				strData.append(buff, 0, charsReaded);
	
			}
		} catch (Exception e) {
			
		}
		return strData.toString();
	}
	
	public static String readStringFromInputStream(InputStreamReader isr, int length) {
		String strData = null;
		
		try {
			char[] buff = new char[length];

			int charsReaded = 0;
			charsReaded = isr.read(buff);
			
			if (charsReaded > 0) {
				strData = String.copyValueOf(buff, 0, charsReaded);
			}

		} catch (Exception e) {
			//Trace.i("neterror", "unable to get data (no network?)");
		}
		return strData;
	}
	
	public static String writeDataToFile(InputStream is, String file) {
		StringBuilder strData = new StringBuilder();
		
		try {
			byte[] buff = new byte[512];
			//InputStreamReader isr = new InputStreamReader(is, charset);
			FileOutputStream fos = new FileOutputStream(file);
			
			int charsReaded = 0;
			while ((charsReaded = is.read(buff)) != -1) {
				fos.write(buff, 0, charsReaded);
				//strData.append(buff, 0, charsReaded);
			}
		} catch (Exception e) {
			Trace.i("write error:", e.toString());
		}
		return strData.toString();
	}
	
	public static String getLocalIpAddress() {
	    try {
	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	            NetworkInterface intf = en.nextElement();
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
	                InetAddress inetAddress = enumIpAddr.nextElement();
	                if (!inetAddress.isLoopbackAddress()) {
	                    return inetAddress.getHostAddress().toString();
	                }
	            }
	        }
	    } catch (SocketException ex) {
	        Trace.i("err", ex.toString());
	    }
	    return null;
	}

	

}
