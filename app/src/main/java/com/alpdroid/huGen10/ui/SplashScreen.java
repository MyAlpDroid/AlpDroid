package com.alpdroid.huGen10.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;

import androidx.preference.PreferenceManager;

import com.alpdroid.huGen10.AlpdroidApplication;
import com.alpdroid.huGen10.ListenerService;
import com.alpdroid.huGen10.R;

import in.rmkrishna.mlog.MLog;

@SuppressLint("CustomSplashScreen")
public class SplashScreen extends Activity {

  private AlertDialog alertDialog;
  private AlpdroidApplication application;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

      Throwable throwable = new Throwable("OK Begin");
      MLog.Companion.d("Splashcreen::",throwable);

   application = (AlpdroidApplication) getApplication();

   StrictMode.ThreadPolicy oldPolicy;

   super.onCreate(savedInstanceState);


   setContentView(R.layout.activity_splash_screen);

   oldPolicy = StrictMode.getThreadPolicy();
  StrictMode.allowThreadDiskReads();
      try {
          // Do reads here
       //   PreferenceManager.setDefaultValues(this, R.xml.pref_notification, false);
          PreferenceManager.setDefaultValues(this, R.xml.pref_players, false);
      } finally {
          StrictMode.setThreadPolicy(oldPolicy);
      }


     enableNotificationAccess();

      MLog.Companion.d("Splashcreen::",(new Throwable("OK end")));
  }

  @Override
  protected void onResume() {
    super.onResume();
      application = (AlpdroidApplication) getApplication();
      enableNotificationAccess();
  }

  private void enableNotificationAccess() {

      StrictMode.ThreadPolicy oldPolicy;

      if (alertDialog != null) {
          alertDialog.dismiss();
      }

      if (!ListenerService.isNotificationAccessEnabled(this)) {
          alertDialog =
                  new AlertDialog.Builder(this)
                          .setTitle(R.string.splash_notification_access)
                          .setMessage(R.string.splash_notification_access_text)
                          .setPositiveButton(
                                  android.R.string.ok,
                                  (dialogInterface, i) -> {
                                      String action;
                                      if (Build.VERSION.SDK_INT >= 22) {
                                          action = Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS;
                                      } else {
                                          action = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
                                      }
                                      startActivity(new Intent(action));
                                  })
                          .show();

          return;
      }

      Intent intent = new Intent(this, MainActivity.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      oldPolicy= StrictMode.getThreadPolicy();
      StrictMode.allowThreadDiskReads();
      try {
          // Do reads here
          startActivity(intent);

      } finally {
         StrictMode.setThreadPolicy(oldPolicy);
      }

      finish();
  }

}
