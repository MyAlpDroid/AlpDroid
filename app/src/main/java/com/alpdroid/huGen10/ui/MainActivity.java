package com.alpdroid.huGen10.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import com.alpdroid.huGen10.ApiActionType;
import com.alpdroid.huGen10.CanFrame;
import com.alpdroid.huGen10.Location;
import com.alpdroid.huGen10.OsmAndHelper;
import com.alpdroid.huGen10.R;
import com.alpdroid.huGen10.VehicleServices;
import com.google.android.material.tabs.TabLayout;
import com.google.common.collect.ImmutableList;

import org.osmdroid.config.Configuration;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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

    public static AlpdroidApplication application;
    public static VehicleServices alpineServices;

    public static boolean locationPermissionGranted;
//    public static Context mContext= application.getContext();

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

    private OsmAndHelper mOsmAndHelper= null;
  //  private OsmAndAidlHelper mAidlHelper = null;
    double lastLatitude =0;
    double lastLongitude =0;
    
    private int delay = 5000;

    void execApiAction(ApiActionType apiActionType, Boolean delayed, Location location) {
        Location mLocation = Optional.ofNullable(location).isPresent() ? Optional.ofNullable(location).get() : null;
        Boolean isDelayed = Optional.ofNullable(delayed).isPresent() ? Optional.ofNullable(delayed).get() : true;

        if (mLocation != null) {
             lastLatitude = mLocation.getLat();
             lastLongitude = mLocation.getLon();
        }

        if (isDelayed) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> execApiActionImpl(apiActionType, mLocation), delay);
        } else {
            execApiActionImpl(apiActionType,mLocation);
        }
    }

    void execApiActionImpl(ApiActionType apiActionType, Location location) {
   //     private aidlHelper = mAidlHelper;
        OsmAndHelper osmandHelper = mOsmAndHelper;

        if (osmandHelper != null) {
            switch (apiActionType) {
                case UNDEFINED:
                    break;
                case AIDL_ADD_MAP_MARKER:
                    break;
                case AIDL_UPDATE_MAP_MARKER:
                    break;
                case AIDL_REMOVE_MAP_MARKER:
                    break;
                case AIDL_ADD_FIRST_MAP_WIDGET:
                    break;
                case AIDL_ADD_SECOND_MAP_WIDGET:
                    break;
                case AIDL_UPDATE_FIRST_MAP_WIDGET:
                    break;
                case AIDL_UPDATE_SECOND_MAP_WIDGET:
                    break;
                case AIDL_REMOVE_FIRST_MAP_WIDGET:
                    break;
                case AIDL_REMOVE_SECOND_MAP_WIDGET:
                    break;
                case AIDL_ADD_MAP_POINT:
                    break;
                case AIDL_UPDATE_MAP_POINT:
                    break;
                case AIDL_REMOVE_MAP_POINT:
                    break;
                case AIDL_ADD_MAP_LAYER:
                    break;
                case AIDL_UPDATE_MAP_LAYER:
                    break;
                case AIDL_REMOVE_MAP_LAYER:
                    break;
                case AIDL_IMPORT_GPX:
                    break;
                case AIDL_SHOW_GPX:
                    break;
                case AIDL_HIDE_GPX:
                    break;
                case AIDL_GET_ACTIVE_GPX_FILES:
                    break;
                case AIDL_SET_MAP_LOCATION:
                    break;
                case AIDL_REFRESH_MAP:
                    break;
                case AIDL_ADD_FAVORITE_GROUP:
                    break;
                case AIDL_UPDATE_FAVORITE_GROUP:
                    break;
                case AIDL_REMOVE_FAVORITE_GROUP:
                    break;
                case AIDL_ADD_FAVORITE:
                    break;
                case AIDL_UPDATE_FAVORITE:
                    break;
                case AIDL_REMOVE_FAVORITE:
                    break;
                case AIDL_START_GPX_REC:
                    break;
                case AIDL_STOP_GPX_REC:
                    break;
                case AIDL_TAKE_PHOTO:
                    break;
                case AIDL_START_VIDEO_REC:
                    break;
                case AIDL_START_AUDIO_REC:
                    break;
                case AIDL_STOP_REC:
                    break;
                case AIDL_NAVIGATE:
                    break;
                case AIDL_NAVIGATE_GPX:
                    break;
                case AIDL_REMOVE_GPX:
                    break;
                case AIDL_SHOW_MAP_POINT:
                    break;
                case AIDL_SET_NAV_DRAWER_ITEMS:
                    break;
                case AIDL_PAUSE_NAVIGATION:
                    break;
                case AIDL_RESUME_NAVIGATION:
                    break;
                case AIDL_STOP_NAVIGATION:
                    break;
                case AIDL_MUTE_NAVIGATION:
                    break;
                case AIDL_UNMUTE_NAVIGATION:
                    break;
                case AIDL_SEARCH:
                    break;
                case AIDL_NAVIGATE_SEARCH:
                    break;
                case AIDL_REGISTER_FOR_UPDATES:
                    break;
                case AIDL_UNREGISTER_FORM_UPDATES:
                    break;
                case AIDL_HIDE_DRAWER_PROFILE:
                    break;
                case AIDL_SET_ENABLED_UI_IDS:
                    break;
                case AIDL_SET_DISABLED_UI_IDS:
                    break;
                case AIDL_SET_ENABLED_MENU_PATTERNS:
                    break;
                case AIDL_SET_DISABLED_MENU_PATTERNS:
                    break;
                case AIDL_REG_WIDGET_VISIBILITY:
                    break;
                case AIDL_REG_WIDGET_AVAILABILITY:
                    break;
                case AIDL_CUSTOMIZE_OSMAND_SETTINGS:
                    break;
                case AIDL_GET_IMPORTED_GPX_FILES:
                    break;
                case AIDL_GET_SQLITEDB_FILES:
                    break;
                case AIDL_GET_ACTIVE_SQLITEDB_FILES:
                    break;
                case AIDL_SHOW_SQLITEDB_FILE:
                    break;
                case AIDL_HIDE_SQLITEDB_FILE:
                    break;
                case AIDL_SET_NAV_DRAWER_LOGO:
                    break;
                case AIDL_SET_NAV_DRAWER_FOOTER:
                    break;
                case AIDL_RESTORE_OSMAND:
                    break;
                case AIDL_CHANGE_PLUGIN_STATE:
                    break;
                case AIDL_REGISTER_FOR_OSMAND_INITIALIZATION:
                    break;
                case AIDL_GET_BITMAP_FOR_GPX:
                    break;
                case AIDL_COPY_FILE_TO_OSMAND:
                    break;
                case AIDL_REGISTER_FOR_NAV_UPDATES:
                    break;
                case AIDL_UNREGISTER_FOR_NAV_UPDATES:
                    break;
                case AIDL_GET_AVOID_ROADS:
                    break;
                case AIDL_ADD_AVOID_ROAD:
                    break;
                case AIDL_REMOVE_AVOID_ROAD:
                    break;
                case AIDL_ADD_CONTEXT_MENU_BUTTONS:
                    break;
                case AIDL_REMOVE_CONTEXT_MENU_BUTTONS:
                    break;
                case AIDL_UPDATE_CONTEXT_MENU_BUTTONS:
                    break;
                case AIDL_ARE_OSMAND_SETTINGS_CUSTOMIZED:
                    break;
                case AIDL_SET_CUSTOMIZATION:
                    break;
                case AIDL_SET_UI_MARGINS:
                    break;
                case AIDL_REGISTER_FOR_VOICE_ROUTE_MESSAGES:
                    break;
                case AIDL_UNREGISTER_FROM_VOICE_ROUTE_MESSAGES:
                    break;
                case AIDL_REMOVE_ALL_ACTIVE_MAP_MARKERS:
                    break;
                case AIDL_IMPORT_PROFILE:
                    break;
                case AIDL_EXPORT_PROFILE:
                    break;
                case AIDL_IS_FRAGMENT_OPEN:
                    break;
                case AIDL_IS_MENU_OPEN:
                    break;
                case AIDL_EXIT_APP:
                    break;
                case AIDL_GET_TEXT:
                    break;
                case AIDL_GET_PREFERENCE:
                    break;
                case AIDL_SET_PREFERENCE:
                    break;
                case AIDL_REGISTER_FOR_LISTEN_LOGS:
                    break;
                case AIDL_UNREGISTER_FROM_LISTEN_LOGS:
                    break;
                case INTENT_ADD_FAVORITE:
                    break;
                case INTENT_ADD_MAP_MARKER:
                    break;
                case INTENT_SHOW_LOCATION:
                    break;
                case INTENT_TAKE_PHOTO:
                    break;
                case INTENT_START_VIDEO_REC:
                    break;
                case INTENT_START_AUDIO_REC:
                    break;
                case INTENT_NAVIGATE:
                    break;
                case INTENT_NAVIGATE_SEARCH:
                    break;
                case INTENT_PAUSE_NAVIGATION : {
                    osmandHelper.pauseNavigation();
                }
                case INTENT_RESUME_NAVIGATION: {
                    osmandHelper.resumeNavigation();
                }
                case INTENT_STOP_NAVIGATION : {
                    osmandHelper.stopNavigation();
                }
                case INTENT_MUTE_NAVIGATION : {
                    osmandHelper.muteNavigation();
                }
                case INTENT_UNMUTE_NAVIGATION : {
                    osmandHelper.umuteNavigation();
                }
                break;
                default: break;
            }
            // location depended types
            if (location != null) {
                switch (apiActionType) {
                    case INTENT_ADD_FAVORITE: {
                        osmandHelper.addFavorite(location.getLat(), location.getLon(), location.getName(),
                                location.getName() + " city", "Cities", "red", true);
                    }
                    case INTENT_ADD_MAP_MARKER: {
                        osmandHelper.addMapMarker(location.getLat(), location.getLon(), location.getName());
                    }
                    case INTENT_SHOW_LOCATION: {
                        osmandHelper.showLocation(location.getLat(), location.getLon());
                    }
                    case INTENT_TAKE_PHOTO: {
                        osmandHelper.takePhoto(location.getLat(), location.getLon());
                    }
                    case INTENT_START_VIDEO_REC: {
                        osmandHelper.recordVideo(location.getLat(), location.getLon());
                    }
                    case INTENT_START_AUDIO_REC: {
                        osmandHelper.recordAudio(location.getLat(), location.getLon());
                    }
                    case INTENT_NAVIGATE: {
                        osmandHelper.navigate(location.getName() + " start",
                                location.getLatStart(), location.getLonStart(),
                                location.getName() + " finish", location.getLat(), location.getLon(),
                                "bicycle", true, true);
                    }
                    case INTENT_NAVIGATE_SEARCH: {
                        AlertDialog.Builder alert = new AlertDialog.Builder(this);
                        EditText editText = new EditText(this);
                        alert.setTitle("Enter Search Query");
                        alert.setView(editText);
                        alert.setPositiveButton("Navigate",
                                    null);
                        alert.setNegativeButton("Cancel", null);
                        alert.show();
                    }
                    break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + apiActionType);
                }
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mOsmAndHelper = new OsmAndHelper(this, REQUEST_OSMAND_API, this);

        setContentView(R.layout.activity_main);

        application = (AlpdroidApplication) getApplication();
        application.startListenerService();
        application.startVehicleServices();

        Log.d("Main", "MainActivity started");

        if (application.isBound()) {
            alpineServices = application.getAlpdroidService();
            Log.d("Main", "MainActivity Service Bound");
        }
        else
        {
            Log.d("Main", "MainActivity not bound");

        }



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
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
                locationPermissionGranted=true;
                Log.d("access permission granted","ok");
            }
            locationPermissionGranted=true;
        } catch (Exception e){
            locationPermissionGranted=true;
            e.printStackTrace();

        }

        Context ctx = getApplicationContext();
        //important! set your user agent to prevent getting banned from the osm servers

        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        mOsmAndHelper.getInfo();

        Log.d("osmHelper : OnActivity result","ok");



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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OSMAND_API) {
            StringBuilder sb = new StringBuilder();
            sb.append("ResultCode = <b>").append(resultCodeStr(resultCode)).append("</b>");
            if (data != null) {
                Bundle extras = data.getExtras();
                if (extras != null && extras.size() > 0) {
                    Set<String> bundleKeySet = extras.keySet(); // string key set
                    for(String key : bundleKeySet){ // traverse and print pairs
                        Object value = extras.get(key);
                        if (sb.length()>0) {
                            sb.append("<br>");
                        }
                        sb.append(key).append(" = <b>").append(value).append("</b>");
                    }
                }
            }
            showOsmandInfoDialog(sb.toString());
        }
        if (resultCode == RESULT_OK)

            // si cas intermédiaire
            super.onActivityResult(requestCode, resultCode, data);
        else
            super.onActivityResult(requestCode, resultCode, data);
    }

    private void showOsmandInfoDialog(String infoText) {
        Bundle args = new Bundle();
        args.putString(OsmAndInfoDialog.INFO_KEY, infoText);
        OsmAndInfoDialog infoDialog = new OsmAndInfoDialog();
        infoDialog.setArguments(args);
        getSupportFragmentManager().beginTransaction().add(infoDialog, null).commitAllowingStateLoss();
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
        Log.d("Main", "MainActivity Resume");
        application.resume();  // Start listening notifications from UsbService
        if (application.isBound()) {
            alpineServices = application.getAlpdroidService();
            Log.d("Main", "MainActivity Service Bound after Resume");
        }
        else
        {
            Log.d("Main", "MainActivity not bound after Resume");

        }
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
//        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("Main", "MainActivity Pause");
        application.pause();
        if (application.isBound()) {
            alpineServices = application.getAlpdroidService();
            Log.d("Main", "MainActivity Service Bound after Pause");
        }
        else
        {
            Log.d("Main", "MainActivity not bound after Pause");

        }
    }


    @Override
    public void onStop() {

        super.onStop();
        application.close();
    }

    @Override
    public void osmandMissing() {

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
                   return getString(R.string.comfort_display);

                case 3:
                    return getString(R.string.computer_display);
                case 4 :
                    return getString(R.string.maps_display);
            }
            return null;
        }
    }


}