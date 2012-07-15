package org.teitheapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.teitheapp.classes.Setting;
import org.teitheapp.utils.DatabaseManager;
import org.teitheapp.utils.Trace;


public class ServicesLauncher extends Activity implements OnItemClickListener {

	//orizw final metavlites gia tis apaithseis pou exei to ka8e service
	final static int HYDRA_LOGIN_REQUIRED = 1;
	final static int PITHIA_LOGIN_REQUIRED = 2;
	
	final static int ITEM_MY_INFO = 0;
	final static int ITEM_MY_STATEMENT = 1;
	final static int ITEM_MY_GRADES = 2;
	final static int ITEM_NUMBER_OF_COURSES_LEFT = 3;
	final static int ITEM_HYDRA_ANNOUNCEMENTS = 4;
	final static int ITEM_TEACHER_INFO = 5;

	private SharedPreferences preferences;
	
	// references to our images
	private TypedArray icons = null;
	private String[] iconsDesc =  null;
	private int[] requirements = {PITHIA_LOGIN_REQUIRED, PITHIA_LOGIN_REQUIRED, PITHIA_LOGIN_REQUIRED, PITHIA_LOGIN_REQUIRED, HYDRA_LOGIN_REQUIRED, HYDRA_LOGIN_REQUIRED};
	private Setting hydraStudent, pithiaStudent;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.launcher_grid);


		
		// Initialize the launcher icons
		icons = getResources().obtainTypedArray(R.array.services_icons);
		iconsDesc = getResources().getStringArray(R.array.services_icons_desc);
	
		GridView gridview = (GridView) findViewById(R.id.gridview);
		gridview.setAdapter(new CustomAdapted(this));
		gridview.setOnItemClickListener(this);
		
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		

	}

	public class CustomAdapted extends BaseAdapter {
		private Context mContext;

		public CustomAdapted(Context c) {
			mContext = c;
		}

		public int getCount() {
			return icons.length();
		}

		public Object getItem(int position) {
			return icons.getResourceId(position, 0);
		}

		public long getItemId(int position) {
			return position;
		}

		// create a new item for each item referenced by the Adapter
		public View getView(int position, View convertView, ViewGroup parent) {
			View itemView = null;

			if (convertView == null) { // if it's not recycled, initialize some
										// attributes
				LayoutInflater inflater = getLayoutInflater();
				itemView = (View) inflater.inflate(R.layout.launcher_item,
						parent, false);
			} else {
				itemView = (View) convertView;
			}

			ImageView imagePart = (ImageView) itemView
					.findViewById(R.id.imagepart);
			TextView textView = (TextView) itemView.findViewById(R.id.textpart);

			imagePart.setImageResource(icons.getResourceId(position, 0));
			textView.setText(iconsDesc[position]);

			return itemView;
		}
	}
	
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		// TODO Auto-generated method stub
		DatabaseManager dbManager = new DatabaseManager(this);
		hydraStudent = dbManager.getSetting("hydra_student");
		pithiaStudent = dbManager.getSetting("pithia_student");
		
		if (requirements[position] == HYDRA_LOGIN_REQUIRED && (hydraStudent == null || preferences.getString("hydra_login", "").equals("") || preferences.getString("hydra_pass", "").equals("")) ) {
			Intent intent = new Intent();
        	intent.setClass(this, InfoDialog.class);
        	intent.putExtra("stringRes", R.string.info_hydra_login);
            startActivity(intent);
		}
		else if (requirements[position] == PITHIA_LOGIN_REQUIRED && (pithiaStudent == null || preferences.getString("pithia_login", "").equals("") || preferences.getString("pithia_pass", "").equals(""))) {
			Intent intent = new Intent();
        	intent.setClass(this, InfoDialog.class);
        	intent.putExtra("stringRes", R.string.info_pithia_login);
            startActivity(intent);
		} else {
			startService(position);
		}
	}
	
	private void startService(int pos) {
		Intent intent = new Intent();
		
		switch (pos) {
			case ITEM_NUMBER_OF_COURSES_LEFT: 
				intent.setClass(this, DiplomaNumber.class);
	        	startActivity(intent);
				break;
			case ITEM_MY_GRADES:
	        	intent.setClass(this, MyGrades.class);
	        	startActivity(intent);
				
				break;
			default:
				Toast.makeText(ServicesLauncher.this,
						"Έκανες κλικ στο '" + iconsDesc[pos] + "'",
						Toast.LENGTH_SHORT).show();
				break;
		}
	}
}
