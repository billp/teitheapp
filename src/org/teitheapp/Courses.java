package org.teitheapp;

import android.app.ExpandableListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;

/**
 * Demonstrates expandable lists using a custom {@link ExpandableListAdapter}
 * from {@link BaseExpandableListAdapter}.
 */
public class Courses extends ExpandableListActivity {

	ExpandableListAdapter mAdapter;
	private static final String LOG_TAG = "ExpListMine";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set up our adapter
		mAdapter = new MyExpandableListAdapter();
		setListAdapter(mAdapter);
		registerForContextMenu(getExpandableListView());
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// menu.setHeaderTitle("Sample menu");
		// menu.add(0, 0, 0, R.string.expandable_list_sample_action);
	}

	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {

		Log.d(LOG_TAG, "onChildClick: " + childPosition);

		String strCourseId = null;
		
		if (groupPosition == 7 && childPosition == 8) {
			strCourseId = "4880";
		} else if (groupPosition == 7 && childPosition == 9) {
			strCourseId = "4881";
		} else {
			strCourseId = String.format("4%d0%d", groupPosition+1, childPosition+1);
		}
		
		Intent intent = new Intent();
    	intent.setClass(this, ViewCourse.class);
    	intent.putExtra("sem_id", groupPosition + 1);
    	intent.putExtra("course_id", Integer.parseInt(strCourseId));
        startActivity(intent);
		
		//Toast.makeText(this, "Re! Me patise sto x",
			//	Toast.LENGTH_SHORT).show();

		return false;
	}

	/*@Override
	public boolean onContextItemSelected(MenuItem item) {
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item
				.getMenuInfo();

		String title = ((TextView) info.targetView).getText().toString();

		int type = ExpandableListView
				.getPackedPositionType(info.packedPosition);
		if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
			int groupPos = ExpandableListView
					.getPackedPositionGroup(info.packedPosition);
			int childPos = ExpandableListView
					.getPackedPositionChild(info.packedPosition);
			Toast.makeText(
					this,
					title + ": Child " + childPos + " clicked in group "
							+ groupPos, Toast.LENGTH_SHORT).show();
			return true;
		} else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
			int groupPos = ExpandableListView
					.getPackedPositionGroup(info.packedPosition);
			Toast.makeText(this, title + ": Group " + groupPos + " clicked",
					Toast.LENGTH_SHORT).show();
			return true;
		}

		return false;
	}*/


	/**
	 * A simple adapter which maintains an ArrayList of photo resource Ids. Each
	 * photo is displayed as an image. This adapter supports clearing the list
	 * of photos and adding a new photo.
	 * 
	 */
	public class MyExpandableListAdapter extends BaseExpandableListAdapter {
		// Sample data set. children[i] contains the children (String[]) for
		// groups[i].
		private String[] groups = { "Α' Εξάμηνο", "Β' Εξάμηνο", "Γ' Εξάμηνο",
				"Δ' Εξάμηνο", "Ε' Εξάμηνο", "ΣΤ' Εξάμηνο", "Ζ' Εξάμηνο",
				"Η' Εξάμηνο" };
		private String[][] children = {
				{ "Εισαγωγή στη Πληροφορική",
						"Αλγοριθμική και Προγραμματισμός", "Ψηφιακά Συστήματα",
						"Μαθηματική Ανάλυση",
						"Δεξιότητες Επικοινωνίας/Κοινωνικά Δίκτυα" },
				{ "Αντικειμενοστραφής Προγραμματισμός",
						"Εισαγωγή στα Λειτουργικά Συστήματα",
						"Διακριτά Μαθηματικά", "Γλώσσες και Τεχνολογίες Ιστού",
						"Πληροφοριακά Συστήματα Ι" },
				{
						"Αριθμητική Ανάλυση & Προγραμματισμός Επιστημονικών Εφαρμογών",
						"Δομές Δεδομένων και Ανάλυση Αλγορίθμων",
						"Οργάνωση και Αρχιτεκτονική Υπολογιστικών Συστημάτων",
						"Αλληλεπίδραση Ανθρώπου-Μηχανής & Ανάπτυξη Διεπιφανειών Χρήστη",
						"Συστήματα Διαχείρισης Βάσεων Δεδομένων" },
				{ "Μεθοδολογίες Προγραμματισμού",
						"Τεχνητή Νοημοσύνη:Γλώσσες και Τεχνικές",
						"Τηλεπικοινωνίες και Δίκτυα Υπολογιστών",
						"Θεωρία Λειτουργικών Συστημάτων",
						"Θεωρία Πιθανοτήτων και Στατιστική" },
				{ "Πληροφοριακά Συστήματα ΙΙ", "Μηχανική Λογισμικού Ι",
						"Δίκτυα Υπολογιστών",
						"Ανάπτυξη Διαδικτυακών Συστημάτων & Εφαρμογών",
						"Επιχειρησιακή Έρευνα" },
				{ "Ασφάλεια Πληροφοριακών Συστημάτων", "Μηχανική Μάθηση",
						"Τεχνολογία Βάσεων Δεδομένων",
						"Μηχανική Λογισμικού ΙI", "Μάθημα Επιλογής 1" },
				{
						"Ανάπτυξη και Διαχείριση Ολοκληρωμένων Πλ. Συστημάτων & Εφαρμογών",
						"Τεχνολογία Πολυμέσων", "Μάθημα Επιλογής 2",
						"Μάθημα Επιλογής 3", "Μάθημα Επιλογής 4" },
				{
						"Ευφυή Συστήματα",
						"Προηγμένες Αρχιτεκτονικές Υπολογιστών και Παράλληλα Συστήματα",
						"Οργάνωση Δεδομένων και Εξόρυξη Πληροφορίας",
						"Ειδικά Θέματα Δικτύων Ι", "Ειδικά Θέματα Δικτύων ΙI",
						"Διαδικτυακές Υπηρεσίες Προστιθέμενης Αξίας",
						"Ασύρματα και Κινητά Δίκτυα Επικοινωνιών",
						"Γραφικά Υπολογιστών", "Πτυχιακή Eργασία",
						"Πρακτική Ασκηση" } };

		public Object getChild(int groupPosition, int childPosition) {
			return children[groupPosition][childPosition];
		}

		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		public int getChildrenCount(int groupPosition) {
			return children[groupPosition].length;
		}

		public TextView getGenericView() {
			// Layout parameters for the ExpandableListView
			AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
					ViewGroup.LayoutParams.FILL_PARENT, 64);

			TextView textView = new TextView(Courses.this);
			textView.setLayoutParams(lp);
			// Center the text vertically
			textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
			// Set the text starting position
			textView.setPadding(66, 0, 0, 0);
			return textView;
		}

		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			TextView textView = getGenericView();
			textView.setText(getChild(groupPosition, childPosition).toString());
			return textView;
		}

		public Object getGroup(int groupPosition) {
			return groups[groupPosition];
		}

		public int getGroupCount() {
			return groups.length;
		}

		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			TextView textView = getGenericView();
			textView.setText(getGroup(groupPosition).toString());
			return textView;
		}

		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

		public boolean hasStableIds() {
			return true;
		}

	}
}
