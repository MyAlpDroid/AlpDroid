package com.alpdroid.huGen10.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.alpdroid.huGen10.R
import androidx.preference.*



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
class SettingsDisplay : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    lateinit var global_background:Drawable

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Display the divider
        setDivider(ColorDrawable(Color.WHITE))
        setDividerHeight(0)

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        // Load the preferences from an XML resource

        val oldPolicy = StrictMode.getThreadPolicy()

        val root = preferenceManager.context
        val screen = this.preferenceManager

        val packageManager : PackageManager = root.applicationContext.packageManager

        var applicationInfo: ApplicationInfo
        val sharedPreferences = this.preferenceManager.sharedPreferences
        val prefKeys: Set<String> = sharedPreferences!!.all.keys
        val playerPrefKeys: MutableList<String> = ArrayList()

        StrictMode.allowThreadDiskReads()
        try {
           this.preferenceManager.setStorageDefault()
            addPreferencesFromResource(R.xml.pref_players)

        } finally {
            StrictMode.setThreadPolicy(oldPolicy);
        }

        val preference: SwitchPreferenceCompat = SwitchPreferenceCompat(screen.context)

        val new_bg=sharedPreferences.getString("key_background","background")!!

        val  resourceId = resources.getIdentifier(new_bg, "drawable", root.getPackageName())


        view?.setBackground(resources.getDrawable(resourceId,root.getTheme()))


        for (key in prefKeys) {
            if (key.startsWith("player.")) {
                playerPrefKeys.add(key)
            }
        }

        for (key in playerPrefKeys) {

            val packageName = key.substring(7)


            try {
                applicationInfo = packageManager.getApplicationInfo(
                    packageName, 0
                    //PackageManager.ApplicationInfoFlags.of(0)
                )


            } catch (e: PackageManager.NameNotFoundException) {
                val editor = sharedPreferences.edit()
                editor.remove(key)
                editor.apply()
                continue
            }

            preference.title = packageManager.getApplicationLabel(applicationInfo)
            preference.key = key
            preference.icon = packageManager.getApplicationIcon(applicationInfo)
            screen.preferenceScreen.addPreference(preference)
        }

        
        preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)

        val clickableTextViewPref = findPreference<ClickableTextViewPreference>("arduino_update")
        if (clickableTextViewPref != null) {
            clickableTextViewPref.setOnTextViewClickListener {
                // Handle click event here
                onSharedPreferenceChanged(preferenceManager.sharedPreferences,"arduino_update")
            }
        }
    }


    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {


        Log.d("Settings", key.toString())

        if (key.equals("Choix")) {
            // Broadcast the change to the mainactivity
            val intent = Intent("change_background")
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)

        }

        if (key.equals("Langue"))
        {
            // Broadcast the change to the mainactivity
            val intent = Intent("change_language")
            Log.d("Settings", "intent change_language")
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)

        }

        if (key.equals(getString(/* resId = */ R.string.arduino_update))) {

                preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
                sharedPreferences!!.edit().putBoolean(getString(R.string.arduino_update), true)
                    .apply()
                // Broadcast the change to the mainactivity
         /*       val intent = Intent(getString(R.string.arduino_update))
                Log.d("Settings", "intent arduino update")
                LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent) */

        }

    }





}