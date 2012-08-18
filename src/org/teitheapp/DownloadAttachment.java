package org.teitheapp;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.widget.EditText;

public class DownloadAttachment extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.select_path);
		
		EditText path = (EditText)findViewById(R.id.attachment_path);
		path.setText(Environment.getExternalStorageDirectory() + "/downloads");
	}
}
