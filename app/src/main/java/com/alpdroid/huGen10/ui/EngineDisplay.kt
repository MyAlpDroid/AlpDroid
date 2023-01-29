package com.alpdroid.huGen10.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.alpdroid.huGen10.AlpdroidApplication
import com.alpdroid.huGen10.R
import com.alpdroid.huGen10.VehicleServices
import com.alpdroid.huGen10.databinding.EngineDisplayBinding
import com.github.anastr.speedviewlib.ImageLinearGauge
import com.github.anastr.speedviewlib.ImageSpeedometer
import com.github.anastr.speedviewlib.ProgressiveGauge


@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class EngineDisplay : UIFragment(250)
{

    lateinit var alpineServices : VehicleServices
    private  var fragmentBlankBinding: EngineDisplayBinding?=null
    
    private var currentDegree:Float = 90.0f
    private var steeringAngle:Float = 90.0f
    lateinit var press_FL : TextView
    lateinit var press_RL: TextView
    lateinit var press_FR: TextView
    lateinit var press_RR: TextView
    lateinit var temp_FL: TextView
    lateinit var temp_FR: TextView
    lateinit var temp_RL: TextView
    lateinit var temp_RR: TextView
    lateinit var temp_FL2: TextView
    lateinit var temp_FR2: TextView
    lateinit var temp_RL2: TextView
    lateinit var temp_RR2: TextView
    lateinit var oddo_Rate: TextView
    lateinit var fuel_inst: TextView
    lateinit var fuel_level: TextView
    lateinit var gear_active: ImageView
    lateinit var gear_next: ImageView
    lateinit var speed : TextView
    lateinit var rpm_gauge : ImageLinearGauge
    lateinit var angle_steering : ImageView
    lateinit var oil_temp : ImageSpeedometer
    lateinit var cool_temp : ImageSpeedometer
    lateinit var intake_temp : ImageSpeedometer
    lateinit var gear_temp : ImageSpeedometer
    lateinit var otherJauge3: ImageSpeedometer
    lateinit var speedthrottle : ProgressiveGauge
    lateinit var brakethrottle : ProgressiveGauge
    lateinit var textCtl : TextView

    var fuel_instant:Float = 0.0f
    var speed_100:Int = 0
    var fuelgauge:Int =0




    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = EngineDisplayBinding.inflate(inflater, container, false)
        fragmentBlankBinding = binding
        return binding.root
    }

    override fun onDestroyView() {
        // Consider not storing the binding instance in a field, if not needed.
        //fragmentBlankBinding = null
        super.onDestroyView()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        press_FL = fragmentBlankBinding!!.textPressFL
        press_RL = fragmentBlankBinding!!.textPressRL
        press_FR = fragmentBlankBinding!!.textPressFR
        press_RR = fragmentBlankBinding!!.textPressRR

        temp_FL = fragmentBlankBinding!!.textTempFL
        temp_FR = fragmentBlankBinding!!.textTempFR
        temp_RL = fragmentBlankBinding!!.textTempRL
        temp_RR = fragmentBlankBinding!!.textTempRR

        temp_FL2 = fragmentBlankBinding!!.textTempFL2
        temp_FR2 = fragmentBlankBinding!!.textTempFR2
        temp_RL2 = fragmentBlankBinding!!.textTempRL2
        temp_RR2 = fragmentBlankBinding!!.textTempRR2

        angle_steering = fragmentBlankBinding!!.angularWheel

        speed = fragmentBlankBinding!!.textSpeed

        rpm_gauge = fragmentBlankBinding!!.rpmgauge

        oil_temp = fragmentBlankBinding!!.OILJauge
        cool_temp = fragmentBlankBinding!!.CooLJauge
        intake_temp = fragmentBlankBinding!!.IntakeJauge
        gear_temp = fragmentBlankBinding!!.GearJauge
        oddo_Rate = fragmentBlankBinding!!.textOddoRate
        fuel_inst = fragmentBlankBinding!!.textFuelInst
        fuel_level = fragmentBlankBinding!!.textFueLevel

        gear_active = fragmentBlankBinding!!.gearActive
        gear_next = fragmentBlankBinding!!.gearNext

        speedthrottle = fragmentBlankBinding!!.throttlePress
        brakethrottle = fragmentBlankBinding!!.brakePress

        otherJauge3 = fragmentBlankBinding!!.OilPressure


            timerTask = {
                activity?.runOnUiThread {
                  if (AlpdroidApplication.app.isBound)
                    {

                        alpineServices = AlpdroidApplication.app.alpdroidData

                        val flbrake_temp:Int = (alpineServices.get_FrontLeftBrakeTemperature() * 5) - 50
                        val frbrake_temp:Int = (alpineServices.get_FrontRightBrakeTemperature() * 5) - 50
                        val rlbrake_temp:Int = (alpineServices.get_RearLeftBrakeTemperature() * 5) - 50
                        val rrbrake_temp:Int = (alpineServices.get_RearRightBrakeTemperature() * 5) - 50
                        val flbrake_press:Int = alpineServices.get_FrontLeftWheelPressure_V2() * 30
                        val frbrake_press:Int = alpineServices.get_FrontRightWheelPressure_V2() * 30
                        val rlbrake_press:Int = alpineServices.get_RearLeftWheelPressure_V2() * 30
                        val rrbrake_press:Int = alpineServices.get_RearRightWheelPressure_V2() * 30

                        // temp
                        temp_FL2.text= String.format("%d °C",(alpineServices.get_TyreTemperature() - 30))

                        temp_FR2.text= String.format("%d °C",(alpineServices.get_FrontRightWheelTemperature() - 30))
                        temp_RL2.text= String.format("%d °C",(alpineServices.get_RearLeftWheelTemperature() - 30) )
                        temp_RR2.text= String.format("%d °C",(alpineServices.get_RearRightWheelTemperature() - 30))


                        press_FL.text = String.format(
                            "%d mBar",
                            flbrake_press
                        )

                        if (flbrake_press<2300)
                            press_FL.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.vert, null))
                        else if (flbrake_press<2500)
                            press_FL.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.orange, null))
                        else
                            press_FL.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.rouge, null))

                        temp_FL.text = String.format(
                            "%d °C",
                            flbrake_temp
                        )

                        if (flbrake_temp<120)
                            temp_FL.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.vert, null))
                        else if (flbrake_temp<250)
                            temp_FL.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.orange, null))
                        else
                            temp_FL.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.rouge, null))


                        press_RL.text = String.format(
                            "%d mBar",
                            rlbrake_press
                        )

                        if (rlbrake_press<2300)
                            press_RL.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.vert, null))
                        else if (rlbrake_press<2500)
                            press_RL.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.orange, null))
                        else
                            press_RL.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.rouge, null))


                        temp_RL.text = String.format(
                            "%d °C",
                            rlbrake_temp
                        )

                        if (rlbrake_temp<120)
                            temp_RL.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.vert, null))
                        else if (rlbrake_temp<250)
                            temp_RL.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.orange, null))
                        else
                            temp_RL.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.rouge, null))


                        press_FR.text = String.format(
                            "%d mBar",
                            frbrake_press
                        )

                        if (frbrake_press<2300)
                            press_FR.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.vert, null))
                        else if (frbrake_press<2500)
                            press_FR.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.orange, null))
                        else
                            press_FR.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.rouge, null))

                        temp_FR.text = String.format(
                            "%d °C",
                            frbrake_temp
                        )

                        if (frbrake_temp<120)
                            temp_FR.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.vert, null))
                        else if (frbrake_temp<250)
                            temp_FR.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.orange, null))
                        else
                            temp_FR.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.rouge, null))

                        press_RR.text = String.format(
                            "%d mBar",
                            rrbrake_press
                        )

                        if (rrbrake_press<2300)
                            press_RR.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.vert, null))
                        else if (rrbrake_press<2500)
                            press_RR.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.orange, null))
                        else
                            press_RR.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.rouge, null))


                        temp_RR.text = String.format(
                            "%d °C",
                            rrbrake_temp
                        )

                        if (rrbrake_temp<120)
                            temp_RR.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.vert, null))
                        else if (rrbrake_temp<250)
                            temp_RR.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.orange, null))
                        else
                            temp_RR.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.rouge, null))


                        rpm_gauge.speedTo(alpineServices.get_EngineRPM_MMI().toFloat()/8)

                        var steeringAngle:Float = -((alpineServices.get_SteeringWheelAngle()/10)-3276.7).toFloat()

                        angle_steering.rotation=steeringAngle

                        oil_temp.speedTo((alpineServices.get_OilTemperature() - 40).toFloat())
                        cool_temp.speedTo((alpineServices.get_EngineCoolantTemp() - 40).toFloat())
                        intake_temp.speedTo((alpineServices.get_IntakeAirTemperature() - 40).toFloat())
                        gear_temp.speedTo((alpineServices.get_RST_ATClutchTemperature() + 60).toFloat())

                        speed_100=(alpineServices.get_Disp_Speed_MM()/100)
                        speed.text = String.format(" %d Km/h", speed_100)

                        otherJauge3.speedTo((alpineServices.get_EngineOilPressure()).toFloat()/10)

                        oddo_Rate.text = String.format(" %.2f km", (alpineServices.get_DistanceTotalizer_MM()).toFloat()/100)


                        fuelgauge=(alpineServices.get_FuelLevelDisplayed())
                        fuel_level.text =
                            String.format(" %2d l", fuelgauge)


                        if (fuelgauge>18)
                            fuel_level.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.vert, null))
                        else if (fuelgauge>9)
                            fuel_level.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.orange, null))
                        else
                            fuel_level.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.rouge, null))

                        fuel_instant = alpineServices.get_FuelConsumption().toFloat()*3600*speed_100/(100*1000000)

                        fuel_inst.text = String.format(
                            " %.2f l/100",
                            fuel_instant
                        )

                        brakethrottle.speedTo((alpineServices.get_BrakingPressure()).toFloat()/2)
                        speedthrottle.speedTo(alpineServices.get_RawSensor().toFloat()/8)


                        val id =
                            resources.getIdentifier(
                                "shift_${(alpineServices.get_CurrentGear()+1)}",
                                "drawable",
                                context?.packageName
                            )
                        gear_active.setImageResource(id)

                        val id1 =
                            resources.getIdentifier(
                                "shift_${(alpineServices.get_RST_ATPreSelectedRange())}",
                                "drawable",
                                context?.packageName
                            )
                        gear_next.setImageResource(id1)
                      }
                }
                }
            }


}