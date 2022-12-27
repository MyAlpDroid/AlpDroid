package com.alpdroid.huGen10.ui

import `in`.rmkrishna.mlog.MLog
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.alpdroid.huGen10.*
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.common.collect.ImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.osmand.aidlapi.navigation.ADirectionInfo
import kotlin.time.Duration
import kotlin.time.ExperimentalTime


@OptIn(ExperimentalTime::class)
class MainActivity : FragmentActivity() {

    private val TAG = "MainActivity"
    val REQUEST_OSMAND_API = 1001
    val REQUEST_NAVIGATE_GPX_RAW_DATA = 1002
    val REQUEST_SHOW_GPX_RAW_DATA = 1003
    val REQUEST_NAVIGATE_GPX_URI = 1004
    val REQUEST_SHOW_GPX_URI = 1005
    val REQUEST_SHOW_GPX_RAW_DATA_AIDL = 1006
    val REQUEST_SHOW_GPX_URI_AIDL = 1007
    val REQUEST_NAVIGATE_GPX_RAW_DATA_AIDL = 1008
    val REQUEST_NAVIGATE_GPX_URI_AIDL = 1009
    val REQUEST_GET_GPX_BITMAP_URI_AIDL = 1010
    val REQUEST_COPY_FILE = 1011
    val REQUEST_IMPORT_FILE = 1012
    val AUTHORITY = "net.osmand.plus.fileprovider"
    val GPX_FILE_NAME = "test.gpx"
    val SQLDB_FILE_NAME = "test.sqlitedb"
    val MAP_LAYER_ID = "layer_1"
    val KEY_UPDATES_LISTENER = "subscribe_for_updates"
    val KEY_OSMAND_INIT_LISTENER = "on_osmand_init"
    val KEY_GPX_BITMAP_LISTENER = "on_bitmap_created"
    val KEY_NAV_INFO_LISTENER = "on_nav_info_update"
    val KEY_NAV_VOICE_INFO_LISTENER = "on_nav_voice_info_update"
    val KEY_CONTEXT_BTN_LISTENER = "on_ctx_btn_click"
    val KEY_OSMAND_LOGCAT_LISTENER = "osmand_logcat_listener"
    val DEMO_INTENT_URI = "osmand_api_demo://main_activity"
    private val APP_MODE_CAR = "car"
    private val APP_MODE_PEDESTRIAN = "pedestrian"
    private val APP_MODE_BICYCLE = "bicycle"
    private val APP_MODE_BOAT = "boat"
    private val APP_MODE_AIRCRAFT = "aircraft"
    private val APP_MODE_BUS = "bus"
    private val APP_MODE_TRAIN = "train"
    private val SPEED_CONST_KILOMETERS_PER_HOUR = "KILOMETERS_PER_HOUR"
    private val SPEED_CONST_MILES_PER_HOUR = "MILES_PER_HOUR"
    private val SPEED_CONST_METERS_PER_SECOND = "METERS_PER_SECOND"
    private val SPEED_CONST_MINUTES_PER_MILE = "MINUTES_PER_MILE"
    private val SPEED_CONST_MINUTES_PER_KILOMETER = "MINUTES_PER_KILOMETER"
    private val SPEED_CONST_NAUTICALMILES_PER_HOUR = "NAUTICALMILES_PER_HOUR"
    private val METRIC_CONST_KILOMETERS_AND_METERS = "KILOMETERS_AND_METERS"
    private val METRIC_CONST_MILES_AND_FEET = "MILES_AND_FEET"
    private val METRIC_CONST_MILES_AND_METERS = "MILES_AND_METERS"
    private val METRIC_CONST_MILES_AND_YARDS = "MILES_AND_YARDS"
    private val METRIC_CONST_NAUTICAL_MILES = "NAUTICAL_MILES"
    private val OSMAND_SHARED_PREFERENCES_NAME = "AlpDroid osmData"

    /**
     * private val appModesAll = null
     * private val appModesNone = emptyList<String>()
     * private val appModesPedestrian = listOf(APP_MODE_PEDESTRIAN)
     * private val appModesPedestrianBicycle = listOf(APP_MODE_PEDESTRIAN, APP_MODE_BICYCLE)
     * private val appModesExceptAirBoatDefault = listOf(APP_MODE_CAR, APP_MODE_BICYCLE, APP_MODE_PEDESTRIAN)
    </String> */


    private val textView: TextView? = null
    private val number = 0
    private val frame: CanFrame? = null
    private val buff: String? = null
    private val message = "{\"bus\":0,\"id\":05ED,\"data\":[00,00,00,00,00,11,22,33]}".toByteArray()

    lateinit var application:AlpdroidApplication

    private var mAidlHelper: OsmAndAidlHelper? = null
    private var  callbackKeys:Long=0

    companion object {

        const val EXTRA_INITIAL_TAB = "initial_tab"
        const val TAB_NOW_PLAYING = 0
        const val TAB_ENGINE = 1

        var locationPermissionGranted = false

    }


    private lateinit var mSectionsPagerAdapter: SectionsPagerAdapter

    /** The [ViewPager] that will host the section contents.  */
    private lateinit var mViewPager: ViewPager2
    var lastLatitude = 0.0
    var lastLongitude = 0.0
    private val delay = 5000

    @SuppressLint("NoLoggedException")
    override fun onCreate(savedInstanceState: Bundle?) {

        MLog.d(TAG, "Activity OnCreate")

        super.onCreate(savedInstanceState)

        application=getApplication() as AlpdroidApplication

        AlpdroidApplication.setContext(application)

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
        mViewPager!!.adapter=mSectionsPagerAdapter

        val tabLayout = findViewById<TabLayout>(R.id.tabs)
        TabLayoutMediator(tabLayout, mViewPager!!) { tab, position ->
            tab.text = mSectionsPagerAdapter!!.getPageTitle(position+1)
        }.attach()

        // Initial tab may have been specified in the intent.
        val initialTab = intent.getIntExtra(EXTRA_INITIAL_TAB, TAB_ENGINE)
        mViewPager!!.setCurrentItem(initialTab)

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

        //important! set your user agent to prevent getting banned from the osm servers

        //5.6 and newer
//        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);

  /*      mOsmAndHelper = OsmAndHelper(this, REQUEST_OSMAND_API, this)

        scheduleCoroutineAtFixedRate(GlobalScope,10.seconds, Duration.ZERO)
        {
            mOsmAndHelper.getInfo()
            Log.d(TAG, "MainActivity : getInfo() ")
        }
*/
        Log.d("MainActivity : OnCreate ", TAG)
        MLog.d(TAG, "Activity OnCreate : fin")
    }

    fun resultCodeStr(resultCode: Int): String {
        when (resultCode) {
            RESULT_OK -> return "OK"
            RESULT_CANCELED -> return "Canceled"
            RESULT_FIRST_USER -> return "First user"
        }
        return "" + resultCode
    }


    /**
     * Schedules [action] to be executed on [scope] every [period] with a [initialDelay]
     */
    fun scheduleCoroutineAtFixedRate(scope: CoroutineScope, period: Duration, initialDelay: Duration = Duration.ZERO, action: RunnableCoroutine) {
        scope.launch {
            delay(initialDelay)

            val mutex = Mutex()

            while (true) {
                launch {
                    mutex.withLock {
                        action.run()
                    }
                }
                delay(period)
            }
        }
    }

    fun interface RunnableCoroutine {
        suspend fun run()
    }

    public override fun onRestart() {

        MLog.d(TAG, "Activity onRestart : Begin")
        application=getApplication() as AlpdroidApplication

        AlpdroidApplication.setContext(application)

        application.startVehicleServices()
        application.alpdroidData = VehicleServices()
        application.startListenerService()


        super.onRestart()
        Log.d("MainActivity : OnRestart ", TAG)
        Toast.makeText(this, "on Restart", Toast.LENGTH_SHORT)
        MLog.d(TAG, "Activity onRestart : End")

    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == REQUEST_OSMAND_API) {
           Log.d("OSMAND Helper", "callback for onnavigation update")
            if (data != null) {
                val extras = data.extras
                if (extras != null && extras.size() > 0) {
                 //   if (application.isBound) application.alpdroidServices.alpine2Cluster.fromOsmData(extras)
                    for (key in extras.keySet()) {

                        Log.d("key to read : ", key)
                        Log.d("value read : ", extras[key].toString())
                    }
                }
            }
        }

        if (resultCode == RESULT_OK)
        {
            if (requestCode==0)
            {
                Log.d("OSMAND Helper", "callback for other case update")
            }

        }

        Log.d("OSMAND Helper", "passage par callback mais ni resultcode ni requestcode")
        super.onActivityResult(requestCode, resultCode, data)
    }



    public override fun onResume() {
        val oldPolicy: ThreadPolicy
        MLog.d(TAG, "Activity onResume : Begin")
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
        MLog.d(TAG, "Activity onResume : End")
    }

    public override fun onPause() {
        val oldPolicy: ThreadPolicy
        MLog.d(TAG, "Activity onPause : Begin")
        super.onPause()

        //  application.startVehicleServices();
        //  application.alpdroidData = new VehicleServices();
        oldPolicy = StrictMode.allowThreadDiskReads()
        try {
            // Do reads here
            //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        } finally {
            StrictMode.setThreadPolicy(oldPolicy)
        }
        MLog.d(TAG, "Activity onPause : End")
    }

    public override fun onDestroy() {
        MLog.d(TAG, "Activity onDestroy : Begin")
        super.onDestroy()

    }

    fun onNavInfoUpdate(directionInfo: ADirectionInfo) {
        Log.d(TAG,"ça passe ici")
        application.alpdroidServices.alpine2Cluster.distanceToturn =
            directionInfo.distanceTo
        application.alpdroidServices.alpine2Cluster.nextTurnTypee =
            directionInfo.turnType
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
            SettingsDisplay()
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
                4 -> return getString(R.string.settings)
            }
            return null
        }
    }




}