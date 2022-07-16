package com.alpdroid.huGen10.ui

import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.alpdroid.huGen10.databinding.MpgDisplayBinding
import com.alpdroid.huGen10.ui.MainActivity.alpineServices
import com.alpdroid.huGen10.ui.MainActivity.application


@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class ComputerDisplay : UIFragment(250) {

    private  var fragmentBlankBinding: MpgDisplayBinding?=null
    lateinit var mpg_text: TextView
    lateinit var avg_mpg_text: TextView
    lateinit var tank_mpg: TextView
    lateinit var fuel_usage: TextView
    lateinit var fuel_consumed_curr: TextView
    lateinit var ac_header : TextView
    lateinit var canframeText: TextView
    lateinit var canframeText2: TextView


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        val binding = MpgDisplayBinding.inflate(inflater, container, false)
        fragmentBlankBinding = binding
        return binding.root
    }
    override fun onDestroyView() {
        // Consider not storing the binding instance in a field, if not needed.
        fragmentBlankBinding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        val binding = MpgDisplayBinding.bind(view)
        fragmentBlankBinding = binding

        canframeText = fragmentBlankBinding!!.canFrameView
        canframeText2 =fragmentBlankBinding!!.canFrameView2

        canframeText.setMovementMethod(ScrollingMovementMethod())
        canframeText2.setMovementMethod(ScrollingMovementMethod())

        ac_header = fragmentBlankBinding!!.acHeader

        val rx_metric = fragmentBlankBinding!!.bytesRx
        val tx_metric = fragmentBlankBinding!!.bytesTx

        Log.d("MPG Display", "MPG init")

if (application.isBound)
        timerTask = {
            activity?.runOnUiThread {

                if (alpineServices?.alpine2Cluster!!.clusterStarted)
                    ac_header.setText("Cluster Is Working")
                else ac_header.setText("Cluster Disconnected")

                if (alpineServices?.isVehicleEnabled() == true)
                    rx_metric.setText("Arduino Is Working")
                else rx_metric.setText("Arduino Disconnected")

                if (alpineServices?.isBad == false)
                    tx_metric.setText(".....Ok")
                else tx_metric.setText("......Bad")


                val keyItem: MutableSet<Int> = alpineServices.mapFrame.keys

                for (key in keyItem) {
                    if (alpineServices.getFrame(key)?.bus!=1) {
                        canframeText.append(alpineServices.getFrame(key).toString())
                        canframeText.append(System.getProperty("line.separator"))
                    }
                    else
                    {
                        canframeText2.append(alpineServices.getFrame(key).toString())
                        canframeText2.append(System.getProperty("line.separator"))
                    }
                }


                /*           val postfix = if(CarData.isMetric) {"l/100km"} else {"mpg"}

                when {
                  CarData.currSpd == 0.0 -> {
                        // Idle so 0 MPG!
                        mpg_text.setTextColor(Color.WHITE)
                        mpg_text.text = if (CarData.isMetric) { "Current: Inf "+postfix } else { "Current: 0.0 "+postfix }
                    }
                    CarData.fuelCurrent == 0 -> {
                        // 0 Fuel used, (Infinite MPG!)
                        mpg_text.setTextColor(Color.GREEN)
                        mpg_text.text = if (CarData.isMetric) { "Current: 0.0 "+postfix } else { "Current: Inf "+postfix }

                    }*/
                /* Using fuel and cruising
                    else -> {
                        // calculate how much fuel used in 1km based on current consumption
  //                      var consump_curr = CarData.currSpd / (3600.0 * (CarData.fuelCurrent / 1000000.0))
   //                     var comump_mpg = consump_curr*2.824809363
  //                      consump_curr *= if (CarData.isMetric) {
                            0.425144 // L/100km
                        } else {
                            2.824809363 // MPG UK
                        }
                        when {
                            // Set colour of text based on MPG
      //                      comump_mpg >= 40 -> mpg_text.setTextColor(Color.WHITE)
     //                       comump_mpg >= 20 -> mpg_text.setTextColor(Color.parseColor("#FF8C00"))
                            else -> mpg_text.setTextColor(Color.RED)
                        }
                        // Display current MPG
     //                   mpg_text.text = String.format("Current: %3.1f%s", min(999.9, consump_curr), postfix)
                    }
                }
            //    How far have we travelled since engine on?
               tank_mpg.text = if(CarData.isMetric) {
                    String.format("Trip: %2.2f km, %2.2f L", CarData.tripDistance, CarData.tripFuelConso / 100.0)
                } else {
                    String.format("Trip: %2.2f miles, %2.2f L", CarData.tripDistance * 5.0 / 8.0, CarData.tripFuelConso / 100.0)
                }
                // If trip has gone a distance and used some fuel, calculate average MPG
                // based on running totals of distance and fuel usage
                if (CarData.tripDistance != 0.0 && CarData.tripFuelConso != 0.0) {
                    avg_mpg_text.text = String.format("Average consumption: %2.1f %s",
                            (CarData.tripDistance / (CarData.tripFuelConso / 100.0)) * if(CarData.isMetric){ 0.425144 } else{ 2.824809363 }, postfix)
                } else {
                    avg_mpg_text.text = String.format("Average consumption: 0.0 %s", postfix)
                } */
            }
        }
    }


}