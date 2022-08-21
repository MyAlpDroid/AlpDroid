package com.alpdroid.huGen10.ui;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import androidx.preference.PreferenceFragmentCompat;

import com.alpdroid.huGen10.R;

/**
 * A {@link PreferenceActivity} which implements and proxies the necessary calls
 * to be used with AppCompat.
 */
public class AppCompatPreferenceActivity extends PreferenceFragmentCompat {

  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    setPreferencesFromResource(R.xml.pref_notification, rootKey);
  }

}
