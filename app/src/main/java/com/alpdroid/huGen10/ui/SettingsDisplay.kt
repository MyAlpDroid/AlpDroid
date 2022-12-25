package com.alpdroid.huGen10.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
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

        // Hide the divider
        setDivider(ColorDrawable(Color.TRANSPARENT))
        setDividerHeight(0)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.pref_players)

  /*      val sharedPreferences = this.preferenceManager.sharedPreferences
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


            try {
                applicationInfo= packageManager?.getApplicationInfo(packageName,0)!!
            } catch (e: PackageManager.NameNotFoundException) {
                val editor = sharedPreferences!!.edit()
                editor.remove(key)
                editor.apply()
                continue
            }

            preference.title = packageManager.getApplicationLabel(applicationInfo)
            preference.key = key
            preference.icon = packageManager.getApplicationIcon(applicationInfo)
            screen.addPreference(preference)
        }
*/

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
            val listPreference = preference
            val prefIndex = listPreference.findIndexOfValue(stringValue)
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.entries[prefIndex])
            }
        } else {
            preference?.summary = stringValue
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