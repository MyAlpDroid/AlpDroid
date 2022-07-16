package com.alpdroid.huGen10.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView


import com.github.anastr.speedviewlib.*

import com.alpdroid.huGen10.ui.MainActivity.application
import com.alpdroid.huGen10.ui.MainActivity.alpineServices
import com.alpdroid.huGen10.R
import com.alpdroid.huGen10.databinding.AcDisplayBinding

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class EngineDisplay : UIFragment(250) {

    private  var fragmentBlankBinding: AcDisplayBinding?=null

    lateinit var press_FL : TextView
    lateinit var press_RL: TextView
    lateinit var press_FR: TextView
    lateinit var press_RR: TextView
    lateinit var temp_FL: TextView
    lateinit var temp_FR: TextView
    lateinit var temp_RL: TextView
    lateinit var temp_RR: TextView
    lateinit var yaw_Rate: TextView
    lateinit var fuel_inst: TextView
    lateinit var fuel_level: TextView
    lateinit var gear_active: ImageView
    lateinit var gear_next: ImageView
    lateinit var speed : TextView
    lateinit var angle_steering : ImageSpeedometer
    lateinit var oil_temp : ImageSpeedometer
    lateinit var cool_temp : ImageSpeedometer
    lateinit var intake_temp : ImageSpeedometer
    lateinit var gear_temp : ImageSpeedometer
    lateinit var otherJauge3: ImageSpeedometer
    lateinit var speedthrottle : ProgressiveGauge
    lateinit var brakethrottle : ProgressiveGauge
    lateinit var textCtl : TextView


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = AcDisplayBinding.inflate(inflater, container, false)
        fragmentBlankBinding = binding
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = AcDisplayBinding.bind(view)
        fragmentBlankBinding = binding

        press_FL = fragmentBlankBinding!!.textPressFL
        press_RL = fragmentBlankBinding!!.textPressRL
        press_FR = fragmentBlankBinding!!.textPressFR
        press_RR = fragmentBlankBinding!!.textPressRR

        temp_FL = fragmentBlankBinding!!.textTempFL
        temp_FR = fragmentBlankBinding!!.textTempFR
        temp_RL = fragmentBlankBinding!!.textTempRL
        temp_RR = fragmentBlankBinding!!.textTempRR


        angle_steering = fragmentBlankBinding!!.angularWheel

        speed = fragmentBlankBinding!!.textSpeed

        oil_temp = fragmentBlankBinding!!.OILJauge
        cool_temp = fragmentBlankBinding!!.CooLJauge
        intake_temp = fragmentBlankBinding!!.IntakeJauge
        gear_temp = fragmentBlankBinding!!.GearJauge
        yaw_Rate = fragmentBlankBinding!!.textYawRate
        fuel_inst = fragmentBlankBinding!!.textFuelInst
        fuel_level = fragmentBlankBinding!!.textFueLevel

        gear_active = fragmentBlankBinding!!.gearActive
        gear_next = fragmentBlankBinding!!.gearNext

        speedthrottle = fragmentBlankBinding!!.throttlePress
        brakethrottle = fragmentBlankBinding!!.brakePress

        otherJauge3 = fragmentBlankBinding!!.OilPressure


        if (application.isBound)
        {
            timerTask = {
                activity?.runOnUiThread {
                    press_FL.text = String.format(
                        " %d mBar",
                        (alpineServices.get_FrontLeftWheelPressure_V2() * 30)
                    )
                    temp_FL.text = String.format(
                        " %d °C",
                        ((alpineServices.get_FrontLeftBrakeTemperature() * 5) - 50)
                    )
                    press_RL.text = String.format(
                        " %d mBar",
                        (alpineServices.get_RearLeftWheelPressure_V2() * 30)
                    )
                    temp_RL.text = String.format(
                        " %d °C",
                        ((alpineServices.get_RearLeftBrakeTemperature() * 5) - 50)
                    )
                    press_FR.text = String.format(
                        " %d mBar",
                        (alpineServices.get_FrontRightWheelPressure_V2() * 30)
                    )
                    temp_FR.text = String.format(
                        "  %d °C",
                        ((alpineServices.get_FrontRightBrakeTemperature() * 5) - 50)
                    )
                    press_RR.text = String.format(
                        " %d mBar",
                        (alpineServices.get_RearRightWheelPressure_V2() * 30)
                    )
                    temp_RR.text = String.format(
                        "  %d°C",
                        ((alpineServices.get_RearRightBrakeTemperature() * 5) - 50)
                    )

                    val steeringAngle:Float = ((alpineServices.get_SteeringWheelAngle()/10)-3276.7).toFloat()
                    if (steeringAngle>0)
                        angle_steering.speedTo(steeringAngle+180)
                    else
                        angle_steering.speedTo(steeringAngle+90)
                    oil_temp.speedTo((alpineServices.get_OilTemperature() - 40).toFloat())
                    cool_temp.speedTo((alpineServices.get_EngineCoolantTemp() - 40).toFloat())
                    intake_temp.speedTo((alpineServices.get_IntakeAirTemperature() - 40).toFloat())
                    gear_temp.speedTo((alpineServices.get_RST_ATClutchTemperature() + 60).toFloat())

                   speed.text = String.format(" %d KM/H", (alpineServices.get_Disp_Speed_MM()/100))

                    otherJauge3.speedTo((alpineServices.get_EngineOilPressure()).toFloat())

                    yaw_Rate.text = String.format(" %.2f km", (alpineServices.get_DistanceTotalizer_MM()/100).toFloat())
                    fuel_level.text =
                        String.format(" %2d l", (alpineServices.get_FuelLevelDisplayed()))
                    fuel_inst.text = String.format(
                        " %.2f l/s",
                        ((alpineServices.get_FuelConsumption() * 8/100000).toFloat() )
                    )

                    brakethrottle.speedTo((alpineServices.get_BrakingPressure()).toFloat()*2)
                    speedthrottle.speedTo((alpineServices.get_RawSensor() / 8).toFloat())

                    var StringGenerated: String

                    when (alpineServices.get_CurrentGear()) {
                        0 -> StringGenerated = "1"
                        1 -> StringGenerated = "2"
                        2 -> StringGenerated = "3"
                        3 -> StringGenerated = "4"
                        4 -> StringGenerated = "5"
                        5 -> StringGenerated = "6"
                        6 -> StringGenerated = "7"
                        else -> {
                            StringGenerated = "n"
                        }
                    }

                    val id =
                        resources.getIdentifier(
                            "gear_$StringGenerated",
                            "drawable",
                            context?.packageName
                        )
                    gear_active.setImageResource(id)


                    when (alpineServices.get_RST_ATPreSelectedRange()) {
                        0 -> StringGenerated = "1"
                        1 -> StringGenerated = "2"
                        2 -> StringGenerated = "3"
                        3 -> StringGenerated = "4"
                        4 -> StringGenerated = "5"
                        5 -> StringGenerated = "6"
                        6 -> StringGenerated = "7"
                        else -> {
                            StringGenerated = "n"
                        }
                    }
                    val id1 =
                        resources.getIdentifier(
                            "shift_$StringGenerated",
                            "drawable",
                            context?.packageName
                        )
                    gear_next.setImageResource(id1)
                }
            }
        }
    }
}