package org.teitheapp;

import java.util.Date;

import org.teitheapp.classes.LoginService;
import org.teitheapp.classes.LoginServiceDelegate;
import org.teitheapp.classes.Setting;
import org.teitheapp.utils.DatabaseManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Login extends Activity implements OnClickListener,
		LoginServiceDelegate {

	public final static int LOGIN_MODE_HYDRA = 1;
	public final static int LOGIN_MODE_PITHIA = 2;

	private TextView tvDialogTitle;
	private EditText editLogin, editPass;
	private Button btnLogin, btnCancel;

	private SharedPreferences preferences;
	private ProgressDialog dialog;
	private DatabaseManager dbManager;

	public int LOGIN_MODE;

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.alert_dialog_text_entry);

		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		//dbManager = new DatabaseManager(this);

		tvDialogTitle = (TextView) findViewById(R.id.login_dialog_title);
		editLogin = (EditText) findViewById(R.id.username_edit);
		editPass = (EditText) findViewById(R.id.password_edit);
		btnLogin = (Button) findViewById(R.id.login_login_button);
		btnCancel = (Button) findViewById(R.id.login_cancel_button);

		btnLogin.setOnClickListener(this);
		btnCancel.setOnClickListener(this);

		Bundle extras = getIntent().getExtras();
		LOGIN_MODE = extras.getInt("login_mode");

		if (LOGIN_MODE == LOGIN_MODE_HYDRA) {

			tvDialogTitle.setText(R.string.pref_hydra);
			editLogin.setText(preferences.getString("hydra_login", ""));
			editPass.setText(preferences.getString("hydra_pass", ""));

		}

		else if (LOGIN_MODE == LOGIN_MODE_PITHIA) {
			tvDialogTitle.setText(R.string.pref_pithia);
			editLogin.setText(preferences.getString("pithia_login", ""));
			editPass.setText(preferences.getString("pithia_pass", ""));
		}

	}

	public void onClick(View v) {
		// TODO Auto-generated method stub

		switch (v.getId()) {
		case R.id.login_login_button:

			SharedPreferences.Editor editor = preferences.edit();

			if (editLogin.getText().toString().equals(""))
				return;
			if (editPass.getText().toString().equals(""))
				return;

			if (LOGIN_MODE == LOGIN_MODE_HYDRA) {
				editor.putString("hydra_login", editLogin.getText().toString());
				editor.putString("hydra_pass", editPass.getText().toString());
				editor.commit();

				// DownloadWebPageTask task = new DownloadWebPageTask();
				// task.execute();
				LoginService ls = new LoginService(
						LoginService.LOGIN_MODE_HYDRA, editLogin.getText()
								.toString(), editPass.getText().toString(),
						this);

				ls.login();
				dialog = ProgressDialog.show(Login.this, "", getResources()
						.getString(R.string.login_loading), true);

			} else if (LOGIN_MODE == LOGIN_MODE_PITHIA) {
				editor.putString("pithia_login", editLogin.getText().toString());
				editor.putString("pithia_pass", editPass.getText().toString());
				editor.commit();

				LoginService ls = new LoginService(
						LoginService.LOGIN_MODE_PITHIA, editLogin.getText()
								.toString(), editPass.getText().toString(),
						this);

				ls.login();

				dialog = ProgressDialog.show(Login.this, "", getResources()
						.getString(R.string.login_loading), true);
			}
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(editLogin.getWindowToken(), 0);
			imm.hideSoftInputFromWindow(editPass.getWindowToken(), 0);

			break;
		case R.id.login_cancel_button:
			finish();
			break;

		}
	}

	public void onDestroy() {
		super.onDestroy();

	}

	public void loginSuccess(String cookie, String am, String surname,
			String name, int loginMode) {
		dialog.dismiss();
		// Trace.i("cookie", cookie);

		Toast.makeText(
				Login.this,
				getResources().getString(R.string.login_success) + " " + surname
						+ " " + name, Toast.LENGTH_LONG).show();

		finish();
	}

	public void loginFailed(int status, int loginMode) {
		dialog.dismiss();
		if (status == LoginService.RESPONSE_BADUSERPASS) {
			String studentColumnName = (loginMode == LoginService.LOGIN_MODE_HYDRA ? "hydra_student" : "pithia_student");
			String cookieColumnName = (loginMode == LoginService.LOGIN_MODE_HYDRA ? "hydra_cookie" : "pithia_cookie");
			
			Toast.makeText(getBaseContext(), R.string.wrong_user_pass,
					Toast.LENGTH_SHORT).show();
			
			DatabaseManager dbManager = new DatabaseManager(this);
			dbManager.deleteSetting(studentColumnName);
			dbManager.deleteSetting(cookieColumnName);
			
		} else if (status == LoginService.RESPONSE_TIMEOUT) {

			Toast.makeText(getBaseContext(), R.string.net_timeout,
					Toast.LENGTH_SHORT).show();
		} else if (status == LoginService.RESPONSE_SERVICEUNAVAILABLE) {
			Toast.makeText(getBaseContext(),
					getResources().getString(R.string.pithia_down),
					Toast.LENGTH_SHORT).show();
		}
	}
}
