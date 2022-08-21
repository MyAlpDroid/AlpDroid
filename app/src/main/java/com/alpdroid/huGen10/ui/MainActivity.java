package com.alpdroid.huGen10.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.alpdroid.huGen10.AlpdroidApplication;
import com.alpdroid.huGen10.BuildConfig;
import com.alpdroid.huGen10.CanFrame;
import com.alpdroid.huGen10.OsmAndHelper;
import com.alpdroid.huGen10.R;
import com.alpdroid.huGen10.VehicleServices;
import com.google.android.material.tabs.TabLayout;
import com.google.common.collect.ImmutableList;

import org.osmdroid.config.Configuration;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import m.co.rh.id.alogger.ILogger;

public class MainActivity extends AppCompatActivity implements OsmAndHelper.OnOsmandMissingListener {

        private final String TAG = "MainActivity";
        final int REQUEST_OSMAND_API = 1001;
        final int REQUEST_NAVIGATE_GPX_RAW_DATA = 1002;
        final int REQUEST_SHOW_GPX_RAW_DATA = 1003;
        final int REQUEST_NAVIGATE_GPX_URI = 1004;
        final int REQUEST_SHOW_GPX_URI = 1005;
        final int REQUEST_SHOW_GPX_RAW_DATA_AIDL = 1006;
        final int REQUEST_SHOW_GPX_URI_AIDL = 1007;
        final int REQUEST_NAVIGATE_GPX_RAW_DATA_AIDL = 1008;
        final int REQUEST_NAVIGATE_GPX_URI_AIDL = 1009;
        final int REQUEST_GET_GPX_BITMAP_URI_AIDL = 1010;
        final int REQUEST_COPY_FILE = 1011;
        final int REQUEST_IMPORT_FILE = 1012;
        final String AUTHORITY = "net.osmand.osmandapidemo.fileprovider";
        final String GPX_FILE_NAME = "test.gpx";
        final String SQLDB_FILE_NAME = "test.sqlitedb";

        final String MAP_LAYER_ID = "layer_1";

        final String KEY_UPDATES_LISTENER = "subscribe_for_updates";
        final String KEY_OSMAND_INIT_LISTENER = "on_osmand_init";
        final String KEY_GPX_BITMAP_LISTENER = "on_bitmap_created";
        final String KEY_NAV_INFO_LISTENER = "on_nav_info_update";
        final String KEY_NAV_VOICE_INFO_LISTENER = "on_nav_voice_info_update";
        final String KEY_CONTEXT_BTN_LISTENER = "on_ctx_btn_click";
        final String KEY_OSMAND_LOGCAT_LISTENER = "osmand_logcat_listener";

        final String DEMO_INTENT_URI = "osmand_api_demo://main_activity";

        private final String APP_MODE_CAR = "car";
        private final String APP_MODE_PEDESTRIAN = "pedestrian";
        private final String APP_MODE_BICYCLE = "bicycle";
        private final String APP_MODE_BOAT = "boat";
        private final String APP_MODE_AIRCRAFT = "aircraft";
        private final String APP_MODE_BUS = "bus";
        private final String APP_MODE_TRAIN = "train";

        private final String SPEED_CONST_KILOMETERS_PER_HOUR = "KILOMETERS_PER_HOUR";
        private final String SPEED_CONST_MILES_PER_HOUR = "MILES_PER_HOUR";
        private final String SPEED_CONST_METERS_PER_SECOND = "METERS_PER_SECOND";
        private final String SPEED_CONST_MINUTES_PER_MILE = "MINUTES_PER_MILE";
        private final String SPEED_CONST_MINUTES_PER_KILOMETER = "MINUTES_PER_KILOMETER";
        private final String SPEED_CONST_NAUTICALMILES_PER_HOUR = "NAUTICALMILES_PER_HOUR";

        private final String METRIC_CONST_KILOMETERS_AND_METERS = "KILOMETERS_AND_METERS";
        private final String METRIC_CONST_MILES_AND_FEET = "MILES_AND_FEET";
        private final String METRIC_CONST_MILES_AND_METERS = "MILES_AND_METERS";
        private final String METRIC_CONST_MILES_AND_YARDS = "MILES_AND_YARDS";
        private final String METRIC_CONST_NAUTICAL_MILES = "NAUTICAL_MILES";

        private final String OSMAND_SHARED_PREFERENCES_NAME = "osmand-api-demo";
/**
        private val appModesAll = null
        private val appModesNone = emptyList<String>()
        private val appModesPedestrian = listOf(APP_MODE_PEDESTRIAN)
        private val appModesPedestrianBicycle = listOf(APP_MODE_PEDESTRIAN, APP_MODE_BICYCLE)
        private val appModesExceptAirBoatDefault = listOf(APP_MODE_CAR, APP_MODE_BICYCLE, APP_MODE_PEDESTRIAN)
 */


    private TextView textView;
    private final int number=0;
    private CanFrame frame;
    private String buff;

    public static final String EXTRA_INITIAL_TAB = "initial_tab";
    public static final int TAB_NOW_PLAYING = 0;
    public static final int TAB_ENGINE = 1;

    @SuppressLint("StaticFieldLeak")
    public static AlpdroidApplication application;

    public static boolean locationPermissionGranted;

    private final byte[] message="{\"bus\":0,\"id\":05ED,\"data\":[00,00,00,00,00,11,22,33]}".getBytes();

    /**
     * The {@link PagerAdapter} that will provide fragments for each of the
     * sections. We use a {@link FragmentPagerAdapter} derivative, which will keep every loaded
     * fragment in memory. If this becomes too memory intensive, it may be best to switch to a {@link
     * FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /** The {@link ViewPager} that will host the section contents. */
    private ViewPager mViewPager;

    private final ScheduledExecutorService executor =
            Executors.newScheduledThreadPool(1);

    double lastLatitude =0;
    double lastLongitude =0;

    private int delay = 5000;

    public static ILogger logger;

    @SuppressLint("NoLoggedException")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        application = (AlpdroidApplication) getApplication();
        logger = application.getLogger();
        logger.d("%s : AlpDroid Ok, OnCreate MainActivity", TAG);
        super.onCreate(savedInstanceState);


         application.startVehicleServices();

         application.alpdroidData = new VehicleServices();

         application.startListenerService();

        setContentView(R.layout.activity_main);

     //   application.mOsmAndHelper = new OsmAndHelper(this, REQUEST_OSMAND_API, this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        // Initial tab may have been specified in the intent.
        int initialTab = getIntent().getIntExtra(EXTRA_INITIAL_TAB, TAB_ENGINE);
        mViewPager.setCurrentItem(initialTab);



        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED )
            {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
                locationPermissionGranted=true;
            }
            locationPermissionGranted=true;
        } catch (Exception e)
        {
            locationPermissionGranted=true;
           // e.printStackTrace();
        }

        //important! set your user agent to prevent getting banned from the osm servers

        //5.6 and newer
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        logger.d("MainActivity is launching : ", TAG);


    }

    String resultCodeStr(int resultCode) {
        switch (resultCode) {
            case Activity.RESULT_OK : return "OK";
            case Activity.RESULT_CANCELED : return "Canceled";
            case Activity.RESULT_FIRST_USER : return "First user";
            case OsmAndHelper.RESULT_CODE_ERROR_UNKNOWN : return "Unknown error";
            case OsmAndHelper.RESULT_CODE_ERROR_NOT_IMPLEMENTED : return "Feature is not implemented";
            case OsmAndHelper.RESULT_CODE_ERROR_GPX_NOT_FOUND : return "GPX not found";
            case OsmAndHelper.RESULT_CODE_ERROR_INVALID_PROFILE : return "Invalid profile";
            case OsmAndHelper.RESULT_CODE_ERROR_PLUGIN_INACTIVE : return "Plugin inactive";
            case OsmAndHelper.RESULT_CODE_ERROR_EMPTY_SEARCH_QUERY : return "Empty search query";
            case OsmAndHelper.RESULT_CODE_ERROR_SEARCH_LOCATION_UNDEFINED : return "Search location undefined";
            case OsmAndHelper.RESULT_CODE_ERROR_QUICK_ACTION_NOT_FOUND : return "Quick action not found";

        }
        return "" + resultCode;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OSMAND_API) {
            if (data != null) {
                Bundle extras = data.getExtras();
                if (extras != null && extras.size() > 0) {
               //   if (application.isBound()) application.alpdroidServices.fromOsmData(extras);

                }

            }
            //     if (resultCode == RESULT_OK)
            // si cas intermédiaire
            //          super.onActivityResult(requestCode, resultCode, data);
            //     else
           // super.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings_item) {
            Intent intent = new Intent(getBaseContext(), SettingsActivity.class);
            startActivityForResult(intent, 1);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onResume() {
        super.onResume();

            logger.d(TAG, "Application Service Bound after Resume, need to rebind");

            application.startVehicleServices();


        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

    }

    @Override
    public void onPause() {
        super.onPause();
        logger.d(TAG, "MainActivity Pause");

            logger.d(TAG, "MainActivity need to be bound after Pause");
            application.startVehicleServices();

        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));


    }



    @Override
    public void onDestroy()
    {
        application.close();
        super.onDestroy();
    }

    @Override
    public void osmandMissing() {
        logger.d("OsmAND missiong : ", "Wrong version detected ?");
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the
     * sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        List<Fragment> fragments =
                ImmutableList.of(new NowPlayingFragment(), new EngineDisplay(), new ConfortDisplay(), new ComputerDisplay(), new MapsDisplay(500));


        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.tab_now_playing);
                case 1:
                    return getString(R.string.engine_display);

                case 2:
                   return getString(R.string.confort_display);

                case 3:
                    return getString(R.string.computer_display);
                case 4 :
                    return getString(R.string.maps_display);
            }
            return null;
        }
    }


}