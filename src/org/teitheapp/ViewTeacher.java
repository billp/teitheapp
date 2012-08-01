package org.teitheapp;

import org.teitheapp.classes.Teacher;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;


public class ViewTeacher extends Activity {
	private Teacher teacher;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_teacher);
		
		if (savedInstanceState == null) {
		    
			Bundle extras = getIntent().getExtras();
			
		    if(extras == null) {
		        teacher= null;
		    } else {
		        teacher = (Teacher) extras.getSerializable("teacher");
		    }
		} else {
		    teacher= (Teacher) savedInstanceState.getSerializable("teacher");
		}
		
		Button btnClose = (Button)findViewById(R.id.btn_close);
		
		btnClose.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		TextView name = (TextView)findViewById(R.id.teacher_info_name);
		TextView surname = (TextView)findViewById(R.id.teacher_info_surname);
		TextView role = (TextView)findViewById(R.id.teacher_info_role);
		TextView phone = (TextView)findViewById(R.id.teacher_info_phone);
		TextView email = (TextView)findViewById(R.id.teacher_info_email);
		
		name.setText(teacher.getName());
		surname.setText(teacher.getSurname());
		role.setText(teacher.getRole());
		phone.setText(teacher.getPhone());
		email.setText(teacher.getEmail());
		
	
	}
}