package com.alpdroid.huGen10.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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
    var tankvalue:Int = 0
    lateinit var tanklevel:ImageView
    lateinit var tanktext:TextView
    lateinit var washerlevel:ImageView
    var washerLevel:Int=0
    lateinit var calendar: CalendarView
    lateinit var externaltemp:TextView


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = ConfortDisplayBinding.inflate(inflater, container, false)
        fragmentBlankBinding = binding
        return binding.root
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

                battvalue= (alpineServices.get_BatteryVoltage()/16).toFloat()
                tankvalue= alpineServices.get_FuelLevelDisplayed()

                battstate.setImageResource(R.drawable.batterie_ok)
                batttext.text=String.format(
                    " %.2f V",
                    battvalue)

                if (battvalue<9.5)
                    battstate.setImageResource(R.drawable.batterie_ko)
                else if (battvalue<13.5)
                    battstate.setImageResource(R.drawable.batterie_norm)

                tanklevel.setImageResource(R.drawable.gastank_levelfull)
                tanktext.text=String.format(
                    " %2d l",
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