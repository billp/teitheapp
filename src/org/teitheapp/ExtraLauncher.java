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

public class ExtraLauncher extends Activity {

	// references to our images
	private TypedArray icons = null;
	private String[] iconsDesc = null;
	final static int ITEM_BUSLINE = 0;
	final static int ITEM_CHAT = 1;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.launcher_grid);

		// Initialize the launcher icons
		icons = getResources().obtainTypedArray(R.array.extra_icons);
		iconsDesc = getResources().getStringArray(R.array.extra_icons_desc);

		GridView gridview = (GridView) findViewById(R.id.gridview);
		gridview.setAdapter(new CustomAdapted(this));

		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				
				startService(position);
				
			}
		});
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
	
	
	private void startService(int pos) {
		Intent intent = new Intent();
		
		switch (pos) {
			case ITEM_BUSLINE: 
				intent.setClass(this, Buservice.class);
				startActivity(intent);
				break;
			case ITEM_CHAT: 
				intent.setClass(this, Chatservice.class);
				startActivity(intent);
				break;
		
			
			default:
				Toast.makeText(ExtraLauncher.this,
						"Έκανες κλικ στο '" + iconsDesc[pos] + "'",
						Toast.LENGTH_SHORT).show();
				break;
		}
	}
}
