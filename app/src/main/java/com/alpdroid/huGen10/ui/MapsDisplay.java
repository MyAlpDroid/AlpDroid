package com.alpdroid.huGen10.ui;


import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.alpdroid.huGen10.R;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;


public class MapsDisplay extends UIFragment implements LocationListener
{

        private MyLocationNewOverlay mLocationOverlay;
        private CompassOverlay mCompassOverlay;
        private ScaleBarOverlay mScaleBarOverlay;
        private RotationGestureOverlay mRotationGestureOverlay;
        protected ImageButton btCenterMap;
        protected ImageButton btFollowMe;
        private LocationManager lm;
        private Location currentLocation = null;

        // ===========================================================
        // Fields
        // ===========================================================

        protected MapView mMapView;

        public MapsDisplay(long uiRefreshTime) {
            super(uiRefreshTime);
        }

        public MapView getmMapView() {
            return mMapView;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.maps_display, null);
            mMapView = v.findViewById(R.id.mapview);
            return v;

        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            //super.onViewCreated(view, savedInstanceState);

            final Context context = this.getActivity();
            final DisplayMetrics dm = context.getResources().getDisplayMetrics();

            this.mCompassOverlay = new CompassOverlay(context, new InternalCompassOrientationProvider(context),
                    mMapView);
            this.mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(context),
                    mMapView);

            mScaleBarOverlay = new ScaleBarOverlay(mMapView);
            mScaleBarOverlay.setCentred(true);
            mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10);

            mRotationGestureOverlay = new RotationGestureOverlay(mMapView);
            mRotationGestureOverlay.setEnabled(true);

            mMapView.getController().setZoom(15);
            mMapView.setTilesScaledToDpi(true);
            mMapView.setMultiTouchControls(true);
            mMapView.setFlingEnabled(true);
            mMapView.getOverlays().add(this.mLocationOverlay);
            mMapView.getOverlays().add(this.mCompassOverlay);
            mMapView.getOverlays().add(this.mScaleBarOverlay);

            mLocationOverlay.enableMyLocation();
            mLocationOverlay.enableFollowLocation();
            mLocationOverlay.setOptionsMenuEnabled(true);
            mCompassOverlay.enableCompass();

            btCenterMap = view.findViewById(R.id.ic_center_map);

            btCenterMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("Follow me", "centerMap clicked ");
                    if (currentLocation != null) {
                        GeoPoint myPosition = new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
                        mMapView.getController().animateTo(myPosition);
                    }
                }
            });

            btFollowMe = view.findViewById(R.id.ic_follow_me);

            btFollowMe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("bouton Follow me", "btFollowMe clicked ");
                    if (!mLocationOverlay.isFollowLocationEnabled()) {
                        mLocationOverlay.enableFollowLocation();
                        btFollowMe.setImageResource(R.drawable.osm_ic_follow_me_on);
                    } else {
                        mLocationOverlay.disableFollowLocation();
                        btFollowMe.setImageResource(R.drawable.osm_ic_follow_me);
                    }
                }
            });

        }

        @Override
        public void onPause() {
            super.onPause();
            try {
                lm.removeUpdates(this);
            } catch (Exception ex) {
            }

            mCompassOverlay.disableCompass();
            mLocationOverlay.disableFollowLocation();
            mLocationOverlay.disableMyLocation();
            mScaleBarOverlay.enableScaleBar();
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onResume() {
            super.onResume();
            lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            try {
                //this fails on AVD 19s, even with the appcompat check, says no provided named gps is available
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0l, 0f, this);
            } catch (Exception ex) {
            }

            try {
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0l, 0f, this);
            } catch (Exception ex) {
            }

            mLocationOverlay.enableFollowLocation();
            mLocationOverlay.enableMyLocation();
            mScaleBarOverlay.disableScaleBar();
        }

        public String getSampleTitle() {
            return "Follow Me";
        }

        @Override
        public void onLocationChanged(Location location) {
            currentLocation = location;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            lm = null;
            currentLocation = null;

            mLocationOverlay = null;
            mCompassOverlay = null;
            mScaleBarOverlay = null;
            mRotationGestureOverlay = null;
            btCenterMap = null;
            btFollowMe = null;
        }

}