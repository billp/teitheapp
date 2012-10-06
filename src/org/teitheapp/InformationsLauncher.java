package org.teitheapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class InformationsLauncher extends Activity implements OnItemClickListener {

	// references to our images
	private TypedArray icons = null;
	private String[] iconsDesc = null;
	final static int ITEM_BASIC_INFO = 0;
	final static int ITEM_STUDY = 1;
	final static int ITEM_DEGREE = 2;
	final static int ITEM_D_SUPPORT = 3;
	final static int ITEM_E_SUPPORT = 4;
	final static int ITEM_STUDENT = 5;
	final static int ITEM_ORGANIZE = 6;
	final static int ITEM_COURSES = 7;
	final static int ITEM_LINKS = 8;
	final static int ITEM_STUFF_LINKS = 9;
	final static int ITEM_STUDENT_LINKS = 10;
	final static int ITEM_PHOTOS = 11;
	final static int ITEM_CONTACT = 12;
	
	

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.launcher_grid3);

		// Initialize the launcher icons
		icons = getResources().obtainTypedArray(R.array.info_icons);
		iconsDesc = getResources().getStringArray(R.array.info_icons_desc);

		GridView gridview = (GridView) findViewById(R.id.gridview);
		gridview.setAdapter(new CustomAdapted(this));

		//gridview.setOnClickListener(this);
		gridview.setOnItemClickListener(this);
		
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
		startService(position);
	}
	
	private void startService(int pos) {
		Intent intent = new Intent();
		
		switch (pos) {
			case ITEM_BASIC_INFO: 
				intent.setClass(this, BasicInfo.class);
				startActivity(intent);
				break;
			case ITEM_STUDY: 
				intent.setClass(this, Study.class);
				startActivity(intent);
				break;
			case ITEM_DEGREE: 
				intent.setClass(this, Degree.class);
				startActivity(intent);
				break;
		  	case ITEM_D_SUPPORT: 
				intent.setClass(this, Dsupport.class);
				startActivity(intent);
				break;
		    case ITEM_E_SUPPORT: 
				intent.setClass(this, Esupport.class);
				startActivity(intent);
				break;
			case ITEM_STUDENT: 
				intent.setClass(this, Student.class);
				startActivity(intent);
				break;
			case ITEM_ORGANIZE: 
				intent.setClass(this, Organize.class);
				startActivity(intent);
				break;
			case ITEM_COURSES: 
				intent.setClass(this, Courses.class);
				startActivity(intent);
				break;
			case ITEM_LINKS: 
				intent.setClass(this, Links.class);
				startActivity(intent);
				break;
			case ITEM_STUFF_LINKS: 
				intent.setClass(this, Stufflinks.class);
				startActivity(intent);
				break;	
			case ITEM_STUDENT_LINKS: 
				intent.setClass(this, Studentlinks.class);
				startActivity(intent);
				break;
			case ITEM_PHOTOS: 
				intent.setClass(this, AndroidGridLayoutActivity.class);
				startActivity(intent);
				break;
			case ITEM_CONTACT: 
				intent.setClass(this, Contact.class);
				startActivity(intent);
				break;
			
			default:
				Toast.makeText(InformationsLauncher.this,
						"Έκανες κλικ στο '" + iconsDesc[pos] + "'",
						Toast.LENGTH_SHORT).show();
				break;
		}
	}
	
}
