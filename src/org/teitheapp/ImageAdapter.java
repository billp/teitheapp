package org.teitheapp;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {
	private Context mContext;
	
	// Keep all Images in array
	public Integer[] mThumbIds = {
			R.drawable.picture_0, R.drawable.picture_1,
			R.drawable.picture_2, R.drawable.picture_3,
			R.drawable.picture_4, R.drawable.picture_5,
			R.drawable.picture_6, R.drawable.picture_7,
			R.drawable.picture_8, R.drawable.picture_9,
			R.drawable.picture_10, R.drawable.picture_11,
			R.drawable.picture_12, R.drawable.picture_13
	};
	
	// Constructor
	public ImageAdapter(Context c){
		mContext = c;
	}

	public int getCount() {
		return mThumbIds.length;
	}

	public Object getItem(int position) {
		return mThumbIds[position];
	}

	
	public long getItemId(int position) {
		return 0;
	}

	
	public View getView(int position, View convertView, ViewGroup parent) {			
		ImageView imageView = new ImageView(mContext);
        imageView.setImageResource(mThumbIds[position]);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(new GridView.LayoutParams(70, 70));
        return imageView;
	}

}