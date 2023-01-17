package com.alpdroid.huGen10.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
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
import kotlin.time.ExperimentalTime


@OptIn(ExperimentalTime::class)
class MainActivity : FragmentActivity() {

    private val TAG = "MainActivity"

 /*   private val textView: TextView? = null
    private val number = 0
    private val frame: CanFrame? = null
    private val buff: String? = null

    private val message = "{\"bus\":0,\"id\":05ED,\"data\":[00,00,00,00,00,11,22,33]}".toByteArray()
*/

    lateinit var application:AlpdroidApplication

    companion object {

        const val EXTRA_INITIAL_TAB = "initial_tab"
        const val TAB_NOW_PLAYING = 0
        const val TAB_ENGINE = 1

        var locationPermissionGranted = false

    }


    private lateinit var mSectionsPagerAdapter: SectionsPagerAdapter

    /** The [ViewPager] that will host the section contents.  */
    private lateinit var mViewPager: ViewPager2

    @SuppressLint("NoLoggedException")
    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)

        application=getApplication() as AlpdroidApplication

        AlpdroidApplication.setContext(application)

        application.startVehicleServices()
        application.alpdroidData = VehicleServices()
        application.startListenerService()

        val oldPolicy: ThreadPolicy = StrictMode.getThreadPolicy()
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

        //important! set your user agent to prevent getting banned from the osm servers

        //5.6 and newer
        //  Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);

    }


    public override fun onRestart() {

        application=getApplication() as AlpdroidApplication

        AlpdroidApplication.setContext(application)

        application.startVehicleServices()
        application.alpdroidData = VehicleServices()
        application.startListenerService()


        super.onRestart()


    }


    public override fun onResume() {
        val oldPolicy: ThreadPolicy

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

    }

    public override fun onPause() {
        val oldPolicy: ThreadPolicy

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

    }

    public override fun onDestroy() {

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
    //        ComputerDisplay()
    //        SettingsDisplay()
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
    //            3 -> return getString(R.string.computer_display)
  //              4 -> return getString(R.string.settings)
            }
            return null
        }
    }




}