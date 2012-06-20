package org.teitheapp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class InfoDialog extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.empty);
		
		Bundle extras=getIntent().getExtras();
		int strRes = extras.getInt("stringRes");
		
		TextView tv = (TextView)findViewById(R.id.infotext);
		
		tv.setText(getResources().getString(strRes));
		
	}
}
