package com.alpdroid.huGen10.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.alpdroid.huGen10.GPSTracker
import com.alpdroid.huGen10.R
import com.alpdroid.huGen10.databinding.ConfortDisplayBinding
import com.alpdroid.huGen10.ui.MainActivity.alpineServices
import java.util.*


@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class ConfortDisplay : UIFragment(250) {

    private  var fragmentBlankBinding: ConfortDisplayBinding?=null

    lateinit var battstate:ImageView
    lateinit var batttext:TextView
    var battvalue:Float = 0.0f
    lateinit var enginestate:ImageView
    var enginevalue:Int =0
    var tankvalue:Float = 0.0f
    lateinit var tanklevel:ImageView
    lateinit var tanktext:TextView
    lateinit var washerlevel:ImageView
    var washerLevel:Int=0
    lateinit var calendar: CalendarView
    lateinit var externaltemp:TextView

    // GPSTracker class
    var gps: GPSTracker? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = ConfortDisplayBinding.inflate(inflater, container, false)
        fragmentBlankBinding = binding

        try {
            if (this.context?.let {
                    ContextCompat.checkSelfPermission(
                        it,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                } != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this.context as Activity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                    101
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        this.context?.let { getLocation(it) }

        return binding.root
    }

    fun getLocation(context: Context) {
        gps = GPSTracker(context)
        if (gps!!.canGetLocation()) {
            val latitude: Double = gps!!.getLatitude()
            val longitude: Double = gps!!.getLongitude()
        } else {
            gps!!.showSettingsAlert()
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        val binding = ConfortDisplayBinding.bind(view)
        fragmentBlankBinding = binding

        externaltemp=fragmentBlankBinding!!.externalTemp

        battstate=fragmentBlankBinding!!.batterieState
        batttext=fragmentBlankBinding!!.batterieValue

        calendar=fragmentBlankBinding!!.calendarView

        tanklevel=fragmentBlankBinding!!.gastankLevel
        tanktext=fragmentBlankBinding!!.tankValue

        enginestate=fragmentBlankBinding!!.engineState

        washerlevel=fragmentBlankBinding!!.washerLevel

        calendar.date = Calendar.getInstance().timeInMillis

        timerTask = {
            activity?.runOnUiThread {

                externaltemp.text=String.format(
                    " %d °C",
                    alpineServices.get_MM_ExternalTemp()-40)

             //   battvalue= (alpineServices.get_BatteryVoltage().toFloat()/16)
                  battvalue = gps?.getLongitude()!!.toFloat()
             //   tankvalue= alpineServices.get_FuelLevelDisplayed()
                  tankvalue = gps?.latitude!!.toFloat()

                battstate.setImageResource(R.drawable.batterie_ok)
                batttext.text=String.format(
                    "%.4f °",
                    battvalue)

                if (battvalue<9.5)
                    battstate.setImageResource(R.drawable.batterie_ko)
                else if (battvalue<13.5)
                    battstate.setImageResource(R.drawable.batterie_norm)

                tanklevel.setImageResource(R.drawable.gastank_levelfull)
                tanktext.text=String.format(
                    " %.4f °",
                   tankvalue)

                if (tankvalue<5)
                    tanklevel.setImageResource(R.drawable.gastank_levellow)
                else if (tankvalue<15)
                    tanklevel.setImageResource(R.drawable.gastank_levelmed)

                enginestate.setImageResource(R.drawable.engine_ok)
                enginevalue= alpineServices.get_GlobalVehicleWarningState()
                if (enginevalue!=0)
                    enginestate.setImageResource(R.drawable.engine_check)

                washerlevel.setImageResource(R.drawable.washerlevel_norm)

                if (alpineServices.get_WasherLevelWarningState())
                    washerlevel.setImageResource(R.drawable.washerlevel_low)


            }
        }
   }
}