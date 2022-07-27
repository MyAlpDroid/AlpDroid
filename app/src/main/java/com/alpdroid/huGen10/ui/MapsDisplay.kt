package com.alpdroid.huGen10.ui


import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alpdroid.huGen10.databinding.MapsDisplayBinding
import com.alpdroid.huGen10.ui.MainActivity.application
import com.alpdroid.huGen10.ui.MainActivity.locationPermissionGranted
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng




@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class MapsDisplay : UIFragment(250), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener
{
    private  var fragmentBlankBinding: MapsDisplayBinding?=null

    private var map: GoogleMap? = null
    private var cameraPosition: CameraPosition? = null

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private val defaultLocation = LatLng(-33.8523341, 151.2106085)

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private var lastKnownLocation: Location? = null
    private var likelyPlaceNames: Array<String?> = arrayOfNulls(0)
    private var likelyPlaceAddresses: Array<String?> = arrayOfNulls(0)
    private var likelyPlaceAttributions: Array<List<*>?> = arrayOfNulls(0)
    private var likelyPlaceLatLngs: Array<LatLng?> = arrayOfNulls(0)


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        // Initialize view
        val view: View = inflater.inflate(com.alpdroid.huGen10.R.layout.maps_display, container, false)


        // Initialize map fragment
        val supportMapFragment:SupportMapFragment = childFragmentManager.findFragmentById(com.alpdroid.huGen10.R.id.map) as SupportMapFragment

        // Async map
        supportMapFragment.getMapAsync(this)

        return view
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {

        map = googleMap

        if (locationPermissionGranted)
            map!!.isMyLocationEnabled = true
        Log.d("Location permissions : ", locationPermissionGranted.toString())
        map!!.setOnMyLocationButtonClickListener(this)
        map!!.setOnMyLocationClickListener(this)

    }

    override fun onMyLocationClick(location: Location) {
        Log.d( "Current location:", location.toString())
    }

    override fun onMyLocationButtonClick(): Boolean {
        Log.d( "MyLocation button clicked", "click")

        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }


    override fun onDestroyView() {
        // Consider not storing the binding instance in a field, if not needed.
        fragmentBlankBinding = null
        super.onDestroyView()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val binding = MapsDisplayBinding.bind(view)
        fragmentBlankBinding = binding



if (application.isBound)
        timerTask = {
            activity?.runOnUiThread {


                // TODO

            }
        }
    }


}