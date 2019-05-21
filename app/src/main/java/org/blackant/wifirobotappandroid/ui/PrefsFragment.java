package org.blackant.wifirobotappandroid.ui;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.util.Log;

import org.blackant.wifirobotappandroid.R;


public class PrefsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    SharedPreferences sharedPreferences;
    PreferenceScreen prefScreen;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        prefScreen = getPreferenceScreen();
        sharedPreferences = prefScreen.getSharedPreferences();

        initSummary(getPreferenceScreen());
    }

    @Override
    public void onResume() {
        super.onResume();

        // Set up a listener whenever a key changes
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }




    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePrefSummary(findPreference(key));

        Log.i("Test", "PreferenceChanged");
        if (key.equals(getString(R.string.pref_key_router_url))) {
//            routerUrl = sharedPreferences.getString(getString(R.string.pref_key_router_url), getString(R.string.pref_key_router_url_default));
        } else if (key.equals(getString(R.string.pref_key_camera_url))) {
//            videoUrl = sharedPreferences.getString(getString(R.string.pref_key_camera_url), getString(R.string.pref_key_camera_url_default));
            Log.i("Test", "PreferenceChanged");
            // TODO: 19-4-26
            // reload the video view
//            mVideoView.reload(videoUrl, true);
        } else if (key.equals(getString(R.string.pref_key_test_enabled))) {
            sharedPreferences.getBoolean(getString(R.string.pref_key_test_enabled),getResources().getBoolean(R.bool.pref_key_test_enabled_default));
        } else if (key.equals(getString(R.string.pref_key_camera_url_test))) {
            sharedPreferences.getString(getString(R.string.pref_key_camera_url_test), getString(R.string.pref_key_camera_url_test_default));
        } else if (key.equals(getString(R.string.pref_key_router_url_test))) {
            sharedPreferences.getString(getString(R.string.pref_key_router_url_test), getString(R.string.pref_key_router_url_test_default));
        } else if (key.equals(getString(R.string.pref_key_left_motor_speed))) {
            sharedPreferences.getString(getString(R.string.pref_key_left_motor_speed), getString(R.string.pref_key_left_motor_speed_default));
        } else if (key.equals(getString(R.string.pref_key_right_motor_speed))) {
            sharedPreferences.getString(getString(R.string.pref_key_right_motor_speed), getString(R.string.pref_key_right_motor_speed_default));
        } else if (key.equals(getString(R.string.pref_key_len_on))) {
            sharedPreferences.getString(getString(R.string.pref_key_len_on), getString(R.string.pref_key_len_on_default));
        } else if (key.equals(getString(R.string.pref_key_len_off))) {
            sharedPreferences.getString(getString(R.string.pref_key_len_off), getString(R.string.pref_key_len_off_default));
        }
    }

    private void initSummary(Preference p) {
        if (p instanceof PreferenceGroup) {
            PreferenceGroup pGrp = (PreferenceGroup) p;
            for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
                initSummary(pGrp.getPreference(i));
            }
        } else {
            updatePrefSummary(p);
        }
    }

    private void updatePrefSummary(Preference p) {
        if (p instanceof EditTextPreference) {
            EditTextPreference editTextPref = (EditTextPreference) p;
            p.setSummary(editTextPref.getText());
        }
    }

}
