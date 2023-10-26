package com.alpdroid.huGen10.ui


import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.res.ResourcesCompat
import com.alpdroid.huGen10.AlpdroidApplication
import com.alpdroid.huGen10.R
import com.alpdroid.huGen10.VehicleServices
import com.alpdroid.huGen10.databinding.EngineDisplayBinding
import com.github.anastr.speedviewlib.ImageSpeedometer
import com.github.anastr.speedviewlib.ProgressiveGauge
import com.github.anastr.speedviewlib.components.indicators.ImageIndicator


@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class EngineDisplay : UIFragment(250)
{


    private lateinit var alpineServices : VehicleServices
    private lateinit var fragmentBlankBinding: EngineDisplayBinding
    

    private lateinit var press_FL : TextView
    private lateinit var press_RL: TextView
    private lateinit var press_FR: TextView
    private lateinit var press_RR: TextView

    private lateinit var temp_FL2: TextView
    private lateinit var temp_FR2: TextView
    private lateinit var temp_RL2: TextView
    private lateinit var temp_RR2: TextView

    lateinit var fuel_level: TextView
    lateinit var gear_active: ImageView
    lateinit var speed : TextView

    lateinit var oil_temp : ImageSpeedometer
    lateinit var cool_temp : ImageSpeedometer
    lateinit var intake_temp : ImageSpeedometer
    lateinit var gear_temp : ImageSpeedometer
    lateinit var otherJauge3: ImageSpeedometer
    lateinit var speedthrottle : ProgressiveGauge
    lateinit var brakethrottle : ProgressiveGauge


    var speed_100:Int = 0
    var fuelgauge:Int =0

    // offset b2r1S
    var offset_tyretemp: Int = 0
    var offset_tyrepress: Float = 0.0f
    var toggle_braketemp: Boolean = false
    var offset_tpms:Int = 0

    private var alert_set = false
    private var blink_switch = false
    private var alert_ack = false

    lateinit var tanklevel:ImageView


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


        var press_color:Int
        var temp_color:Int

        var tyre_calul4alert:Boolean

        press_FL = fragmentBlankBinding.textPressFL
        press_RL = fragmentBlankBinding.textPressRL
        press_FR = fragmentBlankBinding.textPressFR
        press_RR = fragmentBlankBinding.textPressRR



        temp_FL2 = fragmentBlankBinding.textTempFL2
        temp_FR2 = fragmentBlankBinding.textTempFR2
        temp_RL2 = fragmentBlankBinding.textTempRL2
        temp_RR2 = fragmentBlankBinding.textTempRR2



        speed = fragmentBlankBinding.textSpeed


        oil_temp = fragmentBlankBinding.OILJauge
        cool_temp = fragmentBlankBinding.CooLJauge
        intake_temp = fragmentBlankBinding.IntakeJauge
        gear_temp = fragmentBlankBinding.GearJauge

        fuel_level = fragmentBlankBinding.textFueLevel

        gear_active = fragmentBlankBinding.gearActive


        speedthrottle = fragmentBlankBinding.throttlePress
        brakethrottle = fragmentBlankBinding.brakePress

        otherJauge3 = fragmentBlankBinding.OilPressure

        tanklevel= fragmentBlankBinding.gastankLevel

        fragmentBlankBinding.imagetpms1.setImageResource(R.drawable.tpms_checks)
        fragmentBlankBinding.imagetpms2.setImageResource(R.drawable.tpms_checks_right)

        fragmentBlankBinding.imageView3.setImageResource(R.drawable.degre_c)
        fragmentBlankBinding.imageView11.setImageResource(R.drawable.unite_bar)

        fragmentBlankBinding.imageView5.setImageResource(R.drawable.degre_c)
        fragmentBlankBinding.imageView10.setImageResource(R.drawable.unite_bar)

        fragmentBlankBinding.imageView6.setImageResource(R.drawable.degre_c)
        fragmentBlankBinding.imageView12.setImageResource(R.drawable.unite_bar)

        fragmentBlankBinding.imageView7.setImageResource(R.drawable.degre_c)
        fragmentBlankBinding.imageView9.setImageResource(R.drawable.unite_bar)

        loadPreferences()

        val imageIndicatorBlue = getDrawable(AlpdroidApplication.app, R.drawable.image_indicator2)?.let {
            ImageIndicator(
                AlpdroidApplication.app,
                it
            )
        }

        if (imageIndicatorBlue != null) {
            oil_temp.indicator = imageIndicatorBlue
            intake_temp.indicator = imageIndicatorBlue
        }

        val imageIndicatorWhite = getDrawable(AlpdroidApplication.app,R.drawable.image_indicator_white)?.let {
            ImageIndicator(
                AlpdroidApplication.app,
                it
            )
        }

        if (imageIndicatorWhite != null) {
            cool_temp.indicator = imageIndicatorWhite
        }

        val imageIndicatorRed = getDrawable(AlpdroidApplication.app,R.drawable.image_indicator_red)?.let {
            ImageIndicator(
                AlpdroidApplication.app,
                it
            )
        }

        if (imageIndicatorRed != null) {
            gear_temp.indicator = imageIndicatorRed
            otherJauge3.indicator = imageIndicatorRed
        }


        val snackbar_oilalert = AlertDialog.Builder(this.context)
            .setTitle(R.string.engine_alert_title)
            .setMessage(R.string.engine_alert_text)
            .setPositiveButton(
                android.R.string.ok
            ) { dialogInterface: DialogInterface?, i: Int ->

                fragmentBlankBinding.engineLayout.setBackgroundColor(0)
                alert_ack=true
            }



        val snackbar_tyrealert = AlertDialog.Builder(this.context)
            .setTitle(R.string.engine_alert_title)
            .setMessage(R.string.tyre_alert_text)
            .setPositiveButton(
                android.R.string.ok
            ) { dialogInterface: DialogInterface?, i: Int ->

                fragmentBlankBinding.engineLayout.setBackgroundColor(0)
                alert_ack=true
                tyre_calul4alert=false

            }

            alert_ack=false
            alert_set=false


            timerTask = {
                activity?.runOnUiThread {
                  if (AlpdroidApplication.app.isBound)
                    {

                        alpineServices = AlpdroidApplication.app.alpdroidData

                        val flbrake_press:Int = alpineServices.get_FrontLeftWheelPressure_V2() * 30
                        val frbrake_press:Int = alpineServices.get_FrontRightWheelPressure_V2() * 30
                        val rlbrake_press:Int = alpineServices.get_RearLeftWheelPressure_V2() * 30
                        val rrbrake_press:Int = alpineServices.get_RearRightWheelPressure_V2() * 30

                        val tyretemp_fl2:Int =alpineServices.get_TyreTemperature1()
                        val tyretemp_fr2:Int =alpineServices.get_TyreTemperature2()
                        val tyretemp_rl2:Int =alpineServices.get_TyreTemperature3()
                        val tyretemp_rr2:Int =alpineServices.get_TyreTemperature4()

                        // temp
                        temp_FL2.text= String.format("%d", tyretemp_fl2+offset_tyretemp)
                        temp_FR2.text= String.format("%d",tyretemp_fr2+offset_tyretemp)
                        temp_RL2.text= String.format("%d",tyretemp_rl2+offset_tyretemp)
                        temp_RR2.text= String.format("%d",tyretemp_rr2+offset_tyretemp)

                        // Mode
                        val rst_vehicleMode=alpineServices.get_RST_VehicleMode()

                        var psi_limit_low_AVAR=1990
                        var psi_limit_mid_AV=2100
                        var psi_limit_mid_AR=2100
                        var psi_limit_high_AV=2250
                        var psi_limit_high_AR=2250

                        var tyretemp_limit_low=20
                        var tyretemp_limit_mid_step1=20
                        var tyretemp_limit_mid_step2=60
                        var tyretemp_limit_high=60

                        var oil_alert=125

                        tyre_calul4alert=false


                        if (rst_vehicleMode==3)
                        {
                             psi_limit_low_AVAR=1950
                             psi_limit_mid_AV=2050
                             psi_limit_mid_AR=2100
                             psi_limit_high_AV=2250
                             psi_limit_high_AR=2300

                             tyretemp_limit_low=40
                             tyretemp_limit_mid_step1=50
                             tyretemp_limit_mid_step2=80
                             tyretemp_limit_high=85

                             oil_alert=125
                        }

                        tyretemp_limit_low-=offset_tpms
                        tyretemp_limit_mid_step1-=offset_tpms
                        tyretemp_limit_mid_step2-=offset_tpms
                        tyretemp_limit_high-=offset_tpms

                        press_FL.text = String.format(
                            "%.2f",
                            offset_tyrepress +  (flbrake_press.toFloat()/1000)
                        )



                        if (rst_vehicleMode!=3)
                             {

                         // affectation des couleurs pressions & temp pour conduite normal / sport

                            press_color = when {
                                flbrake_press < psi_limit_low_AVAR -> R.color.rouge
                                flbrake_press < psi_limit_mid_AV -> R.color.vert
                                flbrake_press < psi_limit_high_AV -> R.color.orange
                                else -> R.color.rouge // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite
                            }


                            press_FL.setBackgroundColor(ResourcesCompat.getColor(getResources(), press_color, null))

                            press_color = when {
                                frbrake_press < psi_limit_low_AVAR -> R.color.rouge
                                frbrake_press < psi_limit_mid_AV -> R.color.vert
                                frbrake_press < psi_limit_high_AV -> R.color.orange
                                else -> R.color.rouge // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite
                            }


                            press_FR.setBackgroundColor(ResourcesCompat.getColor(getResources(), press_color, null))

                            press_color = when {
                                rlbrake_press < psi_limit_low_AVAR -> R.color.rouge
                                rlbrake_press < psi_limit_mid_AV -> R.color.vert
                                rlbrake_press < psi_limit_high_AV -> R.color.orange
                                else -> R.color.rouge // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite
                            }

                            press_RL.setBackgroundColor(ResourcesCompat.getColor(getResources(), press_color, null))


                            press_color = when {
                                rrbrake_press < psi_limit_low_AVAR -> R.color.rouge
                                rrbrake_press < psi_limit_mid_AV -> R.color.vert
                                rrbrake_press < psi_limit_high_AV -> R.color.orange
                                else -> R.color.rouge // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite
                            }


                            press_RR.setBackgroundColor(ResourcesCompat.getColor(getResources(), press_color, null))

                            temp_color = when {
                                tyretemp_fl2 < tyretemp_limit_low -> R.color.orange
                                tyretemp_fl2 < tyretemp_limit_mid_step2 -> R.color.vert
                                else -> R.color.rouge // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite

                            }


                            temp_FL2.setBackgroundColor(ResourcesCompat.getColor(getResources(), temp_color, null))

                            temp_color = when {
                                tyretemp_rl2 < tyretemp_limit_low -> R.color.orange
                                tyretemp_rl2 < tyretemp_limit_mid_step2 -> R.color.vert
                                else -> R.color.rouge // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite

                            }

                            temp_RL2.setBackgroundColor(ResourcesCompat.getColor(getResources(), temp_color, null))

                            temp_color = when {
                                tyretemp_fr2 < tyretemp_limit_low -> R.color.orange
                                tyretemp_fr2 < tyretemp_limit_mid_step2 -> R.color.vert
                                else -> R.color.rouge // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite

                            }

                            temp_FR2.setBackgroundColor(ResourcesCompat.getColor(getResources(), temp_color, null))

                            temp_color = when {
                                tyretemp_rr2 < tyretemp_limit_low -> R.color.orange
                                tyretemp_rr2 < tyretemp_limit_mid_step2 -> R.color.vert
                                else -> R.color.rouge // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite

                            }

                            temp_RR2.setBackgroundColor(ResourcesCompat.getColor(getResources(), temp_color, null))


                        }
                        else
                        {


                            // affectation des couleurs pressions & temp pour conduite track

                            press_color = when {
                                flbrake_press < psi_limit_low_AVAR -> R.color.rouge
                                flbrake_press < psi_limit_mid_AV -> R.color.orange
                                flbrake_press < psi_limit_high_AV -> R.color.vert
                                else -> R.color.rouge // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite
                            }

                            press_FL.setBackgroundColor(ResourcesCompat.getColor(getResources(), press_color, null))

                            if (flbrake_press > psi_limit_high_AV  )
                                tyre_calul4alert=true

                            press_color = when {
                                frbrake_press < psi_limit_low_AVAR -> R.color.rouge
                                frbrake_press < psi_limit_mid_AV -> R.color.orange
                                frbrake_press < psi_limit_high_AV -> R.color.vert
                                else -> R.color.rouge // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite
                            }

                            press_FR.setBackgroundColor(ResourcesCompat.getColor(getResources(), press_color, null))

                            if (frbrake_press > psi_limit_high_AV)
                                tyre_calul4alert=true

                            press_color = when {
                                rlbrake_press < psi_limit_low_AVAR -> R.color.rouge
                                rlbrake_press < psi_limit_mid_AR -> R.color.orange
                                rlbrake_press < psi_limit_high_AR -> R.color.vert
                                else -> R.color.rouge // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite
                            }

                            press_RL.setBackgroundColor(ResourcesCompat.getColor(getResources(), press_color, null))

                            if (rlbrake_press > psi_limit_high_AV)
                                tyre_calul4alert=true

                            press_color = when {
                                rrbrake_press < psi_limit_low_AVAR -> R.color.rouge
                                rrbrake_press < psi_limit_mid_AR -> R.color.orange
                                rrbrake_press < psi_limit_high_AR -> R.color.vert
                                else -> R.color.rouge // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite
                            }

                            press_RR.setBackgroundColor(ResourcesCompat.getColor(getResources(), press_color, null))

                            if (rrbrake_press > psi_limit_high_AV)
                                tyre_calul4alert=true

                            temp_color = when {
                                tyretemp_fl2 < tyretemp_limit_low -> R.color.rouge
                                tyretemp_fl2 < tyretemp_limit_mid_step1 -> R.color.orange
                                tyretemp_fl2 < tyretemp_limit_mid_step2 -> R.color.vert
                                tyretemp_fl2 < tyretemp_limit_high -> R.color.orange
                                else -> R.color.rouge // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite

                            }

                            temp_FL2.setBackgroundColor(ResourcesCompat.getColor(getResources(), temp_color, null))

                            if (tyretemp_fl2 > tyretemp_limit_high)
                                tyre_calul4alert=true

                            temp_color = when {
                                tyretemp_rl2 < tyretemp_limit_low -> R.color.rouge
                                tyretemp_rl2 < tyretemp_limit_mid_step1 -> R.color.orange
                                tyretemp_rl2 < tyretemp_limit_mid_step2 -> R.color.vert
                                tyretemp_rl2 < tyretemp_limit_high -> R.color.orange
                                else -> R.color.rouge // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite

                            }

                            if (tyretemp_rl2 > tyretemp_limit_high)
                                tyre_calul4alert=true

                            temp_RL2.setBackgroundColor(ResourcesCompat.getColor(getResources(), temp_color, null))

                            temp_color = when {
                                tyretemp_fr2 < tyretemp_limit_low -> R.color.rouge
                                tyretemp_fr2 < tyretemp_limit_mid_step1 -> R.color.orange
                                tyretemp_fr2 < tyretemp_limit_mid_step2 -> R.color.vert
                                tyretemp_fr2 < tyretemp_limit_high -> R.color.orange
                                else -> R.color.rouge // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite

                            }

                            if (tyretemp_fr2> tyretemp_limit_high)
                                tyre_calul4alert=true

                            temp_FR2.setBackgroundColor(ResourcesCompat.getColor(getResources(), temp_color, null))

                            temp_color = when {
                                tyretemp_rr2 < tyretemp_limit_low -> R.color.rouge
                                tyretemp_rr2 < tyretemp_limit_mid_step1 -> R.color.orange
                                tyretemp_rr2 < tyretemp_limit_mid_step2 -> R.color.vert
                                tyretemp_rr2 < tyretemp_limit_high -> R.color.orange
                                else -> R.color.rouge // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite

                            }

                            if (tyretemp_rr2> tyretemp_limit_high)
                                tyre_calul4alert=true

                            temp_RR2.setBackgroundColor(ResourcesCompat.getColor(getResources(), temp_color, null))

                        }

                        press_RL.text = String.format(
                            "%.2f",
                            offset_tyrepress + (rlbrake_press.toFloat()/1000)
                        )


                        press_FR.text = String.format(
                            "%.2f",
                            offset_tyrepress + (frbrake_press.toFloat()/1000)
                        )


                        press_RR.text = String.format(
                            "%.2f",
                            offset_tyrepress + (rrbrake_press.toFloat()/1000)
                        )


                        press_FR.text = String.format(
                            "%.2f",
                            offset_tyrepress +  (frbrake_press.toFloat()/1000)
                        )



                        if (!alert_ack && !alert_set && tyre_calul4alert) {
                            alert_set = true
                            snackbar_tyrealert.show()

                        }
                        else
                        {
                            if (!tyre_calul4alert)
                            {
                                alert_set=false
                                alert_ack=false
                            }
                        }

                        oil_temp.speedTo((alpineServices.get_OilTemperature() - 40).toFloat())

                        if ((alpineServices.get_OilTemperature() - 40)>oil_alert)
                        {
                           if (!alert_ack && !alert_set) {
                               alert_set = true
                               snackbar_oilalert.show()
                           }
                        }
                        else
                        {
                            if (alert_ack && !alert_set)
                            {alert_set=false
                            alert_ack=false
                            }
                        }


                        if (!alert_ack && alert_set)
                        {
                            if (blink_switch) {

                                fragmentBlankBinding.engineLayout.setBackgroundResource(R.drawable.background_alert)
                                blink_switch = false
                            }
                            else {
                                fragmentBlankBinding.engineLayout.setBackgroundColor(0)
                                blink_switch = true
                            }
                        }

                        cool_temp.speedTo((alpineServices.get_EngineCoolantTemp() - 40).toFloat())
                        intake_temp.speedTo((alpineServices.get_IntakeAirTemperature() - 40).toFloat())
                        gear_temp.speedTo((alpineServices.get_RST_ATClutchTemperature() + 60).toFloat())

                        speed_100=(alpineServices.get_Disp_Speed_MM()/100)
                        speed.text = String.format("%d Km/h", speed_100)

                        otherJauge3.speedTo((alpineServices.get_EngineOilPressure()).toFloat()/10)


                        fuelgauge=(alpineServices.get_FuelLevelDisplayed())
                        fuel_level.text =
                            String.format("%2d l", fuelgauge)


                        if (fuelgauge>18)
                            fuel_level.setBackgroundColor(ResourcesCompat.getColor(getResources(), android.R.color.transparent, null))
                        else if (fuelgauge>9)
                            fuel_level.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.orange, null))
                        else
                            fuel_level.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.rouge, null))


                        tanklevel.setImageResource(R.drawable.gastank_levelfull)


                        if (fuelgauge<5)
                            tanklevel.setImageResource(R.drawable.gastank_levellow)
                        else if (fuelgauge<15)
                            tanklevel.setImageResource(R.drawable.gastank_levelmed)

                        brakethrottle.speedTo((alpineServices.get_BrakingPressure()).toFloat()/2)

                        speedthrottle.speedTo(alpineServices.get_RawSensor().toFloat()/8)


                        val id =
                            resources.getIdentifier(
                                "shift_${(alpineServices.get_CurrentGear()+1)}",
                                "drawable",
                                context?.packageName
                            )
                        gear_active.setImageResource(id)


                      }
                }
                }
            }

    private fun loadPreferences() {
        val sharedPreferences: SharedPreferences =
            requireContext().getSharedPreferences("EnginePrefs", Context.MODE_PRIVATE)

        offset_tyretemp = sharedPreferences.getInt("offset_tyretemp", 0)
        offset_tyrepress = sharedPreferences.getFloat("offset_tyrepress", 0.0f)
        toggle_braketemp = sharedPreferences.getBoolean("toggle_braketemp", false)
        offset_tpms = sharedPreferences.getInt("offset_tpms", 0)
    }

}

