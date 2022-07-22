package com.alpdroid.huGen10.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.alpdroid.huGen10.AlpdroidApplication;
import com.alpdroid.huGen10.CanFrame;
import com.alpdroid.huGen10.GPSTracker;
import com.alpdroid.huGen10.R;
import com.alpdroid.huGen10.VehicleServices;
import com.google.android.material.tabs.TabLayout;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private final int number=0;
    private CanFrame frame;
    private String buff;

    public static final String EXTRA_INITIAL_TAB = "initial_tab";
    public static final int TAB_NOW_PLAYING = 0;
    public static final int TAB_ENGINE = 1;

    public static AlpdroidApplication application;
    public static VehicleServices alpineServices;
    // GPSTracker class
    public static GPSTracker gps;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        application = (AlpdroidApplication) getApplication();
        application.startListenerService();
  //      application.startVehicleServices();

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

        gps= new GPSTracker(application.getContext());

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
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("Main", "MainActivity Pause");
       application.pause();
    }


    @Override
    public void onStop() {

        application.close();
        super.onStop();
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the
     * sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        List<Fragment> fragments =
                ImmutableList.of(new NowPlayingFragment(),new EngineDisplay(), new ConfortDisplay() , new ComputerDisplay());

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
            }
            return null;
        }
    }


}