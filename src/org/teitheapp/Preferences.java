package org.teitheapp;

import org.teitheapp.utils.Trace;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);
		
		// Get the custom preference
        Preference hydra_login = (Preference) findPreference("hydra_login");
        Preference pithia_login = (Preference) findPreference("pithia_login");
        
        
        hydra_login.setOnPreferenceClickListener(new OnPreferenceClickListener() {

        	public boolean onPreferenceClick(Preference preference) {
                                	
                Intent intent = new Intent();
                intent.setClass(Preferences.this, Login.class);
                intent.putExtra("login_mode", Login.LOGIN_MODE_HYDRA);
                startActivity(intent);
                                	
                return true;
            }
        });
        
        pithia_login.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference preference) {
            	
            	Intent intent = new Intent();
            	intent.setClass(Preferences.this, Login.class);
            	intent.putExtra("login_mode", Login.LOGIN_MODE_PITHIA);
                startActivity(intent);
            	
                return true;
            }
        });
		
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Trace.i("sdsds", key + " changed");
	}
}
