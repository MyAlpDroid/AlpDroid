package com.alpdroid.huGen10.ui

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.preference.*
import com.alpdroid.huGen10.R


/**
 * A [PreferenceActivity] that presents a set of application settings. On handset devices,
 * settings are presented as a single list. On tablets, settings are split by category, with
 * category headers shown to the left of the list of settings.
 *
 *
 * See [Android Design:
 * Settings](http://developer.android.com/design/patterns/settings.html) for design guidelines and the [Settings API Guide](http://developer.android.com/guide/topics/ui/settings.html) for more
 * information on developing a Settings UI.
 */
class SettingsDisplay : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Display the divider
        setDivider(ColorDrawable(Color.WHITE))
        setDividerHeight(0)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // Load the preferences from an XML resource

        val oldPolicy = StrictMode.getThreadPolicy()
        StrictMode.allowThreadDiskReads()
        try {        addPreferencesFromResource(R.xml.pref_players)
        } finally {
            StrictMode.setThreadPolicy(oldPolicy);
        }

        val sharedPreferences = this.preferenceManager.sharedPreferences
        val prefKeys: Set<String> = sharedPreferences!!.all.keys
        val playerPrefKeys: MutableList<String> = ArrayList()

        for (key in prefKeys) {
            if (key.startsWith("player.")) {
                playerPrefKeys.add(key)
            }
        }

        val root = preferenceManager.context
        val screen = preferenceManager.createPreferenceScreen(root)

        val packageManager : PackageManager? = root.applicationContext.packageManager

        for (key in playerPrefKeys) {
            val preference: SwitchPreferenceCompat = SwitchPreferenceCompat(root)
            val packageName = key.substring(7)
            var applicationInfo: ApplicationInfo

            Log.d("Settings","key : "+packageName)

            try {
                    applicationInfo= packageManager?.getApplicationInfo(packageName,PackageManager.ApplicationInfoFlags.of(0))!!
                Log.d("Settings","appliInfo : "+packageManager.getApplicationLabel(applicationInfo))
            } catch (e: PackageManager.NameNotFoundException) {
                val editor = sharedPreferences.edit()
                editor.remove(key)
                editor.apply()
                continue
            }

            preference.title = packageManager.getApplicationLabel(applicationInfo)
            preference.key = key
            preference.icon = packageManager.getApplicationIcon(applicationInfo)
            screen.addPreference(preference)
        }


    }

    private fun bindPreferenceSummaryToValue(preference: Preference) {
        preference.onPreferenceChangeListener = this

        onPreferenceChange(preference,
            PreferenceManager
                .getDefaultSharedPreferences(preference.context)
                .getString(preference.key, ""))
    }


    override fun onPreferenceChange(preference: Preference, value: Any?): Boolean {
        val stringValue = value.toString()

        if (preference is ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            val listPreference = preference
            val index = listPreference.findIndexOfValue(stringValue)

            // Set the summary to reflect the new value.
            preference.setSummary(if (index >= 0) listPreference.entries[index] else null)
        } else {
            // For all other preferences, set the summary to the value's
            // simple string representation.
            preference.summary = stringValue
        }
        return true
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        return when (preference.key) {
            getString(R.string.pref_title_new_players) -> {
                true
            }
            getString(R.string.pref_title_notification_light) -> {
                true
            }
            getString(R.string.pref_title_notification_sound) -> {
                true
            }
            else -> {
                super.onPreferenceTreeClick(preference)
            }
        }
    }



}