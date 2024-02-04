package com.alpdroid.huGen10.ui


import android.Manifest
import android.accounts.Account
import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.session.MediaSession
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.provider.Settings
import android.util.Log
import android.view.Window
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentPagerAdapter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.alpdroid.huGen10.*
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.common.collect.ImmutableList
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.ExperimentalTime


@OptIn(ExperimentalTime::class)
class MainActivity : FragmentActivity()  {

    private val TAG = "MainActivity"

    lateinit var application: AlpdroidApplication

    private var sharedPreferences: SharedPreferences? = null

    private lateinit var myRef : DatabaseReference
    private lateinit var database:FirebaseDatabase


    companion object {

        const val EXTRA_INITIAL_TAB = "initial_tab"
        const val TAB_NOW_PLAYING = 0
        const val TAB_ENGINE = 1

        var locationPermissionGranted = false

    }


    private lateinit var mSectionsPagerAdapter: SectionsPagerAdapter

    /** The [ViewPager] that will host the section contents.  */
    private lateinit var mViewPager: ViewPager2


    private val backgroundImagePreferenceChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateBackgroundImage()
        }
    }

    private val languagePreferenceChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            updateLanguage()

        }
    }


    private val updateArduinoChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            Log.d(TAG,"receive intent to update arduino")

        }
    }


    fun getGoogleAccountName(context: Context): String? {
        val accountManager = AccountManager.get(context)
        val googleAccountType = "com.google"

        val accounts = accountManager.getAccountsByType(googleAccountType)
        if (accounts.isNotEmpty()) {
            // Assuming the first Google account found is the one you want
            val googleAccount = accounts[0] as Account
            return googleAccount.name
        }

        return "not found"
    }

     private fun logAppInstallEvent(uniqueId: String) {


         val devicePolicyManager =
             this@MainActivity.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

         val androidId = Settings.Secure.getString(
             this.contentResolver,
            Settings.Secure.ANDROID_ID
         )


         val hardwareInfo = "Model: ${Build.MODEL}, Product: ${Build.PRODUCT}, Hardware: ${Build.HARDWARE}"

         val versionCode=BuildConfig.VERSION_CODE

         myRef = database.getReference("MyAlpDroid Installation - $versionCode")

         myRef.child("UniqueID-$androidId").child("Version").setValue(BuildConfig.VERSION_NAME)
         myRef.child("UniqueID-$androidId").child("hardwareKey").setValue(hardwareInfo)
         myRef.child("UniqueID-$androidId").child("dateformat").setValue(uniqueId)
         myRef.child("UniqueID-$androidId").child("accountName").setValue(getGoogleAccountName(application))

         myRef.child("UniqueID-$androidId").child("num_install").get().addOnSuccessListener {
             if (it.value!=null)
                myRef.child("UniqueID-$androidId").child("num_install").setValue(it.value.toString().toInt()+1)
             else
                 myRef.child("UniqueID-$androidId").child("num_install").setValue(1.toString())
         }.addOnFailureListener{
             Log.d(TAG, "Failure data Firebase")
         }



     }

    fun getCurrentDateFormatted(): String {
        val currentTimeMillis = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("ddMMyy", Locale.getDefault())
        val date = Date(currentTimeMillis)
        return dateFormat.format(date)
    }

    @SuppressLint("NoLoggedException")
    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)


        application=getApplication() as AlpdroidApplication

        AlpdroidApplication.setContext(application)

        FirebaseApp.initializeApp(this)

        database = FirebaseDatabase.getInstance()

        logAppInstallEvent(getCurrentDateFormatted())

        AlpdroidApplication.controller = MediaSession(application, TAG).controller

        application.startVehicleServices()
        application.alpdroidData = VehicleServices()
        application.startListenerService()

        val oldPolicy: ThreadPolicy
        oldPolicy = StrictMode.getThreadPolicy()
        StrictMode.allowThreadDiskReads()
        try {
            // Do reads here
           setContentView(R.layout.activity_main)
        } finally {
            StrictMode.setThreadPolicy(oldPolicy)
        }


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = SectionsPagerAdapter(this)

        // Set up the ViewPager2 with the sections adapter.
        mViewPager = findViewById(R.id.container)
        mViewPager.adapter=mSectionsPagerAdapter

        val tabLayout = findViewById<TabLayout>(R.id.tabs)
        TabLayoutMediator(tabLayout, mViewPager) { tab, position ->
            tab.text = mSectionsPagerAdapter.getPageTitle(position+1)
        }.attach()

        // Initial tab may have been specified in the intent.
        val initialTab = intent.getIntExtra(EXTRA_INITIAL_TAB, TAB_ENGINE)
        mViewPager.setCurrentItem(initialTab)

        try {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    101
                )
                locationPermissionGranted = true
            }
            locationPermissionGranted = true
        } catch (e: Exception) {
            locationPermissionGranted = true
            // e.printStackTrace();
        }

        updateBackgroundImage()



        StrictMode.allowThreadDiskReads()
        try {
              sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        } finally {
            StrictMode.setThreadPolicy(oldPolicy)
        }

        if (sharedPreferences!!.getBoolean(getString(R.string.isdownload_UNO),false))
        {
            Toast.makeText(this, "Arduino code updated",5.toInt()).show()
            sharedPreferences!!.edit().putBoolean(getString(R.string.isdownload_UNO),false).apply()
        }

        if (application.alpdroidServices.isArduinoOpen())
        {
            Toast.makeText(this, "Arduino is Open",20.toInt()).show()
        }
        else
            Toast.makeText(this, "Arduino is not Up",20.toInt()).show()

    }

    fun setLocale(languageCode: String) {

        val config = resources.configuration

        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            config.setLocale(locale)
        else
            config.locale = locale

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            createConfigurationContext(config)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun updateLanguage()
    {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        val languetoggle = sharedPreferences.getBoolean("Langue", false)

        if (languetoggle)
            setLocale("en")
        else
            setLocale("fr")

        val refresh = Intent(this, MainActivity::class.java)
        startActivity(refresh)

    }
    private fun updateBackgroundImage() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        var backgroundImage = "background"
        var taskbarColor = "#000000"

        val string = sharedPreferences.getString("Choix", "background,#031627")
        val targetChar = ','
        val targetIndex = string?.indexOf(targetChar)!!+1

        if (targetIndex>0)
     {
             backgroundImage = string.substring(0, targetIndex - 1)
             taskbarColor = (string.substring(targetIndex))
        }


        val backgroundImageResource = resources.getIdentifier(backgroundImage, "drawable", packageName)

        mViewPager.setBackgroundResource(backgroundImageResource)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.statusBarColor = Color.parseColor(taskbarColor)
        }
    }


    public override fun onRestart() {

   //     MLog.d(TAG, "Activity onRestart : Begin")
        application=getApplication() as AlpdroidApplication

        AlpdroidApplication.setContext(application)

        application.startVehicleServices()
        application.alpdroidData = VehicleServices()
        application.startListenerService()


        super.onRestart()

        //MLog.d(TAG, "Activity onRestart : End")

    }


    public override fun onResume() {
        val oldPolicy: ThreadPolicy
      //  MLog.d(TAG, "Activity onResume : Begin")
        super.onResume()


        oldPolicy = StrictMode.getThreadPolicy()
        StrictMode.allowThreadDiskReads()
        StrictMode.allowThreadDiskWrites()
        try {
            // Do reads here
            //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        } finally {
            StrictMode.setThreadPolicy(oldPolicy)
        }
        //MLog.d(TAG, "Activity onResume : End")
        LocalBroadcastManager.getInstance(this).registerReceiver(backgroundImagePreferenceChangedReceiver, IntentFilter("change_background"))
        LocalBroadcastManager.getInstance(this).registerReceiver(languagePreferenceChangedReceiver, IntentFilter("change_language"))

    }

    public override fun onPause() {
        val oldPolicy: ThreadPolicy
     //   MLog.d(TAG, "Activity onPause : Begin")
        super.onPause()

        LocalBroadcastManager.getInstance(this).unregisterReceiver(backgroundImagePreferenceChangedReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(languagePreferenceChangedReceiver)

        //  application.startVehicleServices();
        //  application.alpdroidData = new VehicleServices();
        oldPolicy = StrictMode.allowThreadDiskReads()
        try {
            // Do reads here
            //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        } finally {
            StrictMode.setThreadPolicy(oldPolicy)
        }
     //   MLog.d(TAG, "Activity onPause : End")
    }

    public override fun onDestroy() {
        //MLog.d(TAG, "Activity onDestroy : Begin")
        super.onDestroy()

    }


    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to one of the
     * sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentActivity?) :
        FragmentStateAdapter(fm!!) {
        @OptIn(ExperimentalStdlibApi::class, ExperimentalUnsignedTypes::class)
        var fragments: List<Fragment> = ImmutableList.of(
            NowPlayingFragment(),
            EngineDisplay(),
            ConfortDisplay(),
            ComputerDisplay(),
           BargrapheFragment(),
            RoadBook(),
            SettingsDisplay(),
            SimpleCounterDisplay(),
        )

        override fun createFragment(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            return fragments[position]
        }

        override fun getItemCount(): Int {
            return fragments.size
        }

        fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return getString(R.string.tab_now_playing)
                1 -> return getString(R.string.engine_display)
                2 -> return getString(R.string.confort_display)
                3 -> return getString(R.string.computer_display)
                4 -> return "Télémétrie"
                4 -> return "RoadBook"
                5 -> return getString(R.string.settings)
                6 -> return "Simple"
            }
            return null
        }
    }




}