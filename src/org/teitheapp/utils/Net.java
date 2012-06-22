package org.teitheapp.utils;

import java.io.InputStream;
import java.io.InputStreamReader;

public class Net {
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
}
